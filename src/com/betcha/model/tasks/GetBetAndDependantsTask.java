package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;

import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;
import com.betcha.model.server.api.BetRestClient;
import com.betcha.model.server.api.PredictionRestClient;
import com.betcha.model.server.api.UserRestClient;

public class GetBetAndDependantsTask extends AsyncTask<Void, Void, Boolean> {
	private Bet bet = null;
	private User owner = null;
	private int bet_server_id;
	private IGetBetAndDependantCB cb;

	public void setValues(int bet_server_id, IGetBetAndDependantCB cb) {
		this.bet_server_id = bet_server_id;
		this.cb = cb;
	}
	
	public void run() {
		
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("server_id", bet_server_id);
			if(bets.size()>0) {
				bet = bets.get(0);
				List<User> owners = User.getModelDao().queryForEq("id", bet.getOwner().getId());
				if(owners.size()>0) {
					owner = owners.get(0);
					bet.setOwner(owner);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
				
		if(getStatus()!=Status.RUNNING) {
			execute();
		}
	}	

	@Override
	protected Boolean doInBackground(Void... params) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		BetRestClient betClient = new BetRestClient();
		JSONObject jsonBet = null;
		try {
			jsonBet = betClient.show(bet_server_id);
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		}
		if(jsonBet==null)
			return false;
		
		try {
			List<User> listUser = null;
			listUser = User.getModelDao().queryForEq("server_id", jsonBet.getInt("user_id"));
			if(listUser!=null && listUser.size()>0) {
				owner = listUser.get(0);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(owner == null) { //then create it
			owner = new User();
		}
		
		UserRestClient userClient = new UserRestClient();
		JSONObject jsonOwner = null;
		try {
			jsonOwner = userClient.show(jsonBet.getInt("user_id"));
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(jsonOwner==null)
			return false;
		
		
		try {
			owner.setEmail(jsonOwner.getString("email"));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			owner.setName(jsonOwner.getString("full_name"));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			owner.setServer_id(jsonOwner.getInt("id"));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			owner.createOrUpdateLocal();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			bet = new Bet();
			bet.setServer_id(jsonBet.getInt("id"));
			bet.setOwner(owner);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			bet.setReward(jsonBet.getString("reward"));
			bet.setSubject(jsonBet.getString("subject"));
			bet.setDate(formatter.parseDateTime(jsonBet.getString("created_at")));
			bet.setDueDate(formatter.parseDateTime(jsonBet.getString("due_date")));
			bet.setState(jsonBet.getString("state"));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			bet.createOrUpdateLocal();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		if(!getPredictions())
			return false;
			       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		cb.OnGetBetCompleted(result, bet);
			
		super.onPostExecute(result);
	}
	
	private Boolean getPredictions() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		PredictionRestClient userBetClient = new PredictionRestClient(bet.getServer_id());
		JSONArray jsonPredictions = null;
		try {
			jsonPredictions = userBetClient.showPredictionsForBet(bet.getServer_id());
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		}
		if(jsonPredictions==null)
			return false;
		
		User user = null;
		JSONObject jsonPrediction = null;
		for (int i = 0; i < jsonPredictions.length(); i++) {
			try {
				jsonPrediction = jsonPredictions.getJSONObject(i);
			} catch (JSONException e1) {
				e1.printStackTrace();
				continue;
			}
					
			List<User> tmpUsers = null;
			try {
				tmpUsers = User.getModelDao().queryForEq("server_id",jsonPrediction.getInt("user_id"));
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(tmpUsers!=null && tmpUsers.size()>0) {
				user = tmpUsers.get(0);
			}
			if(user==null) {
				user = new User();
			
				UserRestClient userClient = new UserRestClient();
				JSONObject jsonUser;
				try {
					jsonUser = userClient.show(jsonPrediction.getInt("user_id"));
				} catch (RestClientException e) {
					e.printStackTrace();
					continue;
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
				
				try {
					user.setServer_id(jsonUser.getInt("id"));
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
				
				try {
					user.setEmail(jsonUser.getString("email"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					user.setName(jsonUser.getString("full_name"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					user.setUid(jsonUser.getString("uuid"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					user.createOrUpdateLocal();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {		
				Prediction prediction = null;
				List<Prediction> tmpPredictions = null;
				try {
					tmpPredictions = Prediction.getModelDao().queryForEq("server_id", jsonPrediction.getInt("id"));
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if(tmpPredictions==null || tmpPredictions.size()==0) {
					prediction = new Prediction(bet);
				} else {
					prediction = tmpPredictions.get(0);
				}
				
				prediction.setDate(formatter.parseDateTime(jsonPrediction.optString("created_at")));
				prediction.setMyAck(jsonPrediction.optString("user_ack"));
				prediction.setPrediction(jsonPrediction.optString("prediction"));
				prediction.setResult(jsonPrediction.optBoolean("result"));
				prediction.setServer_id(jsonPrediction.optInt("id",-1));
				prediction.setUser(user);
				prediction.createOrUpdateLocal();				
				
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
       	
		}
		return true;
	}
	
}
