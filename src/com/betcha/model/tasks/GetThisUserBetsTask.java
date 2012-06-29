package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientBet;
import com.betcha.api.model.RESTBet;
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
		
		RESTClientBet restClient = new RESTClientBet(context);
		List<RESTBet> restbets = restClient.show_for_user_id(user.getId());
		
		for (RESTBet restBet : restbets) {
			Bet bet = new Bet();
			try {
				bet.setOwner(dbHelper.getUserDao().queryForId(restBet.getUser_id()));
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			bet.setReward(restBet.getReward());
			bet.setServer_id(restBet.getId());
			bet.setState(restBet.getState());
			bet.setSubject(restBet.getSubject());
			bet.setUuid(restBet.getUuid());
			
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
