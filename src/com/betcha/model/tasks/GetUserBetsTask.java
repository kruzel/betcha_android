package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;

import com.betcha.model.Bet;
import com.betcha.model.User;
import com.betcha.model.server.api.BetRestClient;
import com.betcha.model.server.api.UserRestClient;

public class GetUserBetsTask extends AsyncTask<Void, Void, Boolean> {
	private User user;
	private IGetThisUserBetsCB cb;
	
	List<Bet> bets;

	public void setValues(User user, IGetThisUserBetsCB cb) {
		this.user = user;
		this.cb = cb;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		
		bets = new ArrayList<Bet>();
		User betOwner = null;
		
		BetRestClient restClient = new BetRestClient();
		JSONArray jsonBets = null;
		try {
			jsonBets = restClient.show_for_user_id(user.getServer_id());
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		} 
		
		for (int i = 0; i < jsonBets.length(); i++) {
			JSONObject jsonBet;
			int user_server_id = -1;
			try {
				jsonBet = jsonBets.getJSONObject(i);
				user_server_id = jsonBet.getInt("user_id");
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}
							
			Bet bet = new Bet();
			betOwner = null;
			try {
				List<User> users = User.getModelDao().queryForEq("server_id",user_server_id);
				if(user!=null && users.size()>0) {
					betOwner = users.get(0);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} 
			if(betOwner==null) {
				UserRestClient restUserClient = new UserRestClient();
				JSONObject jsonUser = null;
				try {
					jsonUser = restUserClient.show(user_server_id);
				} catch (RestClientException e) {
					e.printStackTrace();
					return false;
				} 
				betOwner = new User();
				try {
					betOwner.setServer_id(jsonUser.getInt("id"));
				} catch (JSONException e1) {
					e1.printStackTrace();
					continue;
				}
				
				try {
					betOwner.setUid(jsonUser.getString("uid"));
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					betOwner.setEmail(jsonUser.getString("email"));
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					betOwner.setName(jsonUser.getString("full_name"));
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				try {
					betOwner.create();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
			
			try {
				bet.setReward(jsonBet.getString("reward"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				bet.setServer_id(jsonBet.getInt("id"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				bet.setState(jsonBet.getString("state"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				bet.setSubject(jsonBet.getString("subject"));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			bet.setOwner(betOwner);
			
			Bet tmpBet = null;
			try {
				List<Bet> tmpBets = Bet.getModelDao().queryForEq("server_id", jsonBet.getInt("id"));
				if(tmpBets!=null && tmpBets.size()>0) { 
					tmpBet = tmpBets.get(0);
				}
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
				continue; //this shouldn't happen
			}
			if(tmpBet==null) {
				try {
					bet.create();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			bets.add(bet);
		}
	       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		if(cb!=null)
			cb.OnGetUserBetsCompleted(result, bets);
		
		super.onPostExecute(result);
	}
	
}
