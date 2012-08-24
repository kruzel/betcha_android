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
import com.betcha.model.server.api.PredictionRestClient;
import com.betcha.model.server.api.UserRestClient;

public class GetBetPredictionsTask extends AsyncTask<Void, Void, Boolean> {
	private int betServerId;
	private IGetPredictionsCB cb;
	Bet bet;
	
	List<Prediction> predictions;


	public void setValues(int betServerId, IGetPredictionsCB cb) {
		this.betServerId = betServerId;
		this.cb = cb;
	}
	
	public Boolean run() {
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("server_id",betServerId);
			if(bets==null || bets.size()==0)
				return false;
			bet = bets.get(0);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		
		try {
			predictions = Prediction.getModelDao().queryForEq("bet_id", bet.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
				
		//get the rest from the server
		execute();
		return true;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		PredictionRestClient userBetClient = new PredictionRestClient(bet.getServer_id());
		JSONArray jsonPredictions = null;
		try {
			jsonPredictions = userBetClient.showPredictionsForBet(betServerId);
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
				
				user = new User();
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
					user.create();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {		
				Prediction prediction = null;
				List<Prediction> tmpPredictions;
				try {
					tmpPredictions = Prediction.getModelDao().queryForEq("server_id", jsonPrediction.getInt("id"));
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				} catch (JSONException e) {
					e.printStackTrace();
					continue;
				}
				
				if(tmpPredictions==null || tmpPredictions.size()==0) {
					prediction = new Prediction(bet);
					prediction.setDate(formatter.parseDateTime(jsonPrediction.getString("created_at")));
					prediction.setMyAck(jsonPrediction.getString("user_ack"));
					prediction.setPrediction(jsonPrediction.getString("prediction"));
					prediction.setResult(jsonPrediction.getBoolean("result"));
					prediction.setServer_id(jsonPrediction.getInt("id"));
					prediction.setUser(user);
					prediction.create();
					predictions.add(prediction);
				}				
				
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       	
		}
							
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		if(cb!=null)
			cb.OnGetPredictionsCompleted(result, predictions);
		
		super.onPostExecute(result);
	}
	
}
