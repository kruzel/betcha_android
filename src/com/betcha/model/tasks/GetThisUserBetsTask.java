package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientBet;
import com.betcha.api.RESTClientUser;
import com.betcha.api.model.RESTBet;
import com.betcha.api.model.RESTUser;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.Bet;
import com.betcha.model.User;

public class GetThisUserBetsTask extends AsyncTask<Void, Void, Boolean> {
	private User user;
	private Context context;
	private DatabaseHelper dbHelper;	
	private IGetThisUserBetsCB cb;
	
	List<Bet> bets;

	public GetThisUserBetsTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(User user, IGetThisUserBetsCB cb) {
		this.user = user;
		this.cb = cb;
	}
	
	public Boolean run() {
	
		//get the rest from the server
		execute();
		return true;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		
		bets = new ArrayList<Bet>();
		User betOwner = null;
		
		RESTClientBet restClient = new RESTClientBet(context);
		List<RESTBet> restbets = null;
		try {
			restbets = restClient.show_for_user_id(user.getServer_id());
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		} 
		
		for (RESTBet restBet : restbets) {						
			Bet bet = new Bet();
			betOwner = null;
			try {
				List<User> users = dbHelper.getUserDao().queryForEq("server_id",restBet.getUser_id());
				if(user!=null && users.size()>0) {
					betOwner = users.get(0);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} 
			if(betOwner==null) {
				RESTClientUser restUserClient = new RESTClientUser(context);
				RESTUser restUser = null;
				try {
					restUser = restUserClient.show(restBet.getUser_id());
				} catch (RestClientException e) {
					e.printStackTrace();
					return false;
				} 
				betOwner = new User();
				betOwner.setEmail(restUser.getEmail());
				betOwner.setName(restUser.getName());
				betOwner.setServer_id(restUser.getId());
				
				try {
					betOwner.setDao(dbHelper.getUserDao());
					betOwner.create();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
			
			bet.setReward(restBet.getReward());
			bet.setServer_id(restBet.getId());
			bet.setState(restBet.getState());
			bet.setSubject(restBet.getSubject());
			bet.setUuid(restBet.getUuid());
			bet.setOwner(betOwner);
			
			Bet tmpBet = null;
			try {
				List<Bet> tmpBets = dbHelper.getBetDao().queryForEq("server_id", restBet.getUser_id());
				if(tmpBets!=null && tmpBets.size()>0) { 
					tmpBet = tmpBets.get(0);
				}
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if(tmpBet==null) {
				try {
					bet.setDao(dbHelper.getBetDao());
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
