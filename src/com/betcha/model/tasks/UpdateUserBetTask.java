package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientUserBet;
import com.betcha.api.RESTUserBetUpdate;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.UserBet;

public class UpdateUserBetTask extends AsyncTask<Void, Void, Boolean> {
	
	private UserBet userBet;
	private Context context;
	private DatabaseHelper dbHelper;	

	public UpdateUserBetTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(UserBet usersBet) {
		this.userBet = usersBet;
	}
	
	public void run() {
		
		try {
			userBet.setDao(dbHelper.getUserBetDao());
			userBet.update();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		execute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
    	
		RESTClientUserBet userBetClient = new RESTClientUserBet(context);	
		Map<String,String> paramsUserBet = new HashMap<String,String>();
		paramsUserBet.put("user_result_bet", userBet.getMyBet());
		
		try {
			userBetClient.update(paramsUserBet,userBet.getServer_id());
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		}
	       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
//		if(result) {	    	
//	    	Toast.makeText(context, R.string.msg_bet_accepted, Toast.LENGTH_LONG);
//		} else {
//			Toast.makeText(context, R.string.msg_bet_rejected, Toast.LENGTH_LONG);
//		}
		
		super.onPostExecute(result);
	}
	
}
