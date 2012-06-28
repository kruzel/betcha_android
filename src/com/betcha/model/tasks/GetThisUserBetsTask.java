package com.betcha.model.tasks;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.das.DatabaseHelper;
import com.betcha.model.Bet;
import com.betcha.model.User;
import com.betcha.model.UserBet;

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
		List<UserBet> usersBets;
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		//TODO fetch all UserBet for this user (server side api based on  User's server_id)
		
		//TODO fetch depended bets
	       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		if(cb!=null)
			cb.OnGetUserBetsCompleted(result, bets);
		
		super.onPostExecute(result);
	}
	
}
