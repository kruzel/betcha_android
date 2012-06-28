package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.betcha.R;
import com.betcha.api.RESTClientUserBet;
import com.betcha.api.model.RESTUserBet;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.UserBet;

public class CreateUserBetTask extends AsyncTask<Void, Void, Boolean> {
	
	private UserBet userBet;
	private Context context;
	private DatabaseHelper dbHelper;
	IGetUserBetCB cb;

	public CreateUserBetTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(UserBet userBet, IGetUserBetCB cb) {
		this.userBet = userBet;
		this.cb = cb;
	}
	
	public void run() {
		try {
			userBet.setDao(dbHelper.getUserBetDao());
			userBet.create();
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
		paramsUserBet.put("user_id", Integer.toString(userBet.getUser().getServer_id()));
		paramsUserBet.put("bet_id", Integer.toString(userBet.getBet().getServer_id()));
		paramsUserBet.put("user_result_bet", userBet.getMyBet());
		paramsUserBet.put("date", userBet.getDate().toString());
		//paramsUserBet.put("result",userBet.getResult().toString());
		//paramsUserBet.put("user_ack",userBet.getMyAck());
		
		RESTUserBet restUserBet = null;
		try {
			restUserBet = userBetClient.create(paramsUserBet);
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		}
		
		userBet.setServer_id(restUserBet.getId());
		try {
			userBet.update();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		GetUserBetTask getBetTask = new GetUserBetTask(context,dbHelper);
		getBetTask.setValues(userBet.getBet().getServer_id(), null);
		getBetTask.run();
	       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(result) {	    	
	    	Toast.makeText(context, R.string.msg_bet_accepted, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, R.string.msg_bet_rejected, Toast.LENGTH_LONG).show();
		}
		
		if(cb!=null){
			cb.OnGetUserBetCompleted(result, userBet);
		}
		
		super.onPostExecute(result);
	}
	
}
