package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;

import com.betcha.model.Bet;
import com.betcha.model.User;
import com.betcha.model.server.api.BetRestClient;
import com.betcha.model.server.api.UserRestClient;

public class GetBetAndOwnerTask extends AsyncTask<Void, Void, Boolean> {
	private Bet bet = null;
	private User owner = null;
	private int bet_server_id;
	private IGetBetAndOwnerCB cb;

	public void setValues(int bet_server_id, IGetBetAndOwnerCB cb) {
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
				
		if(bet!=null && owner!=null)
			cb.OnGetBetCompleted(true, bet);
		else
			execute();
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
			
			owner = new User();
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
				owner.create();
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		try {
			bet = new Bet();
			bet.setServer_id(jsonBet.getInt("id"));
			bet.setOwner(owner);
			
			bet.create();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
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
			bet.create();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
			       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		cb.OnGetBetCompleted(result, bet);
			
		super.onPostExecute(result);
	}
	
}
