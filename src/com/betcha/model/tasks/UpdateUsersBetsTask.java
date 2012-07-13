package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientUserBet;
import com.betcha.api.model.RESTUserBetUpdate;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.UserBet;

public class UpdateUsersBetsTask extends AsyncTask<Void, Void, Boolean> {
	
	private List<UserBet> usersBets;
	private Context context;
	private DatabaseHelper dbHelper;	

	public UpdateUsersBetsTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(List<UserBet> usersBets) {
		this.usersBets = usersBets;
	}
	
	public void run() {
		
		for (UserBet userBet : usersBets) {
			try {
				userBet.setDao(dbHelper.getUserBetDao());
				userBet.update();
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
		
		execute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
    	
		List<RESTUserBetUpdate> paramList = new ArrayList<RESTUserBetUpdate>();
		
		RESTClientUserBet userBetClient = new RESTClientUserBet(context);
		
		for (int i=0; i<usersBets.size(); i++) {
			RESTUserBetUpdate entry = new RESTUserBetUpdate();
			entry.setId(usersBets.get(i).getServer_id());
			entry.setUser_result_bet(usersBets.get(i).getMyBet());
			paramList.add(entry);
		}
		
		try {
			userBetClient.update(paramList);
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
