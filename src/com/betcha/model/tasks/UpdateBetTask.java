package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientBet;
import com.betcha.api.model.RESTBet;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.Bet;

public class UpdateBetTask extends AsyncTask<Void, Void, Boolean> {
	
	private Bet bet;
	private Context context;
	private DatabaseHelper dbHelper;	

	public UpdateBetTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(Bet bet) {
		this.bet = bet;
	}
	
	public void run() {
		try {
			bet.setDao(dbHelper.getBetDao());
			bet.update();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		execute();
	}

	@Override
	protected Boolean doInBackground(Void... args) {
    	
		RESTClientBet betClient = new RESTClientBet(context);
		Map<String,String> paramsBet = new HashMap<String,String>();
		paramsBet.put("state", bet.getState());
		
		RESTBet restBet = null;
		try {
			betClient.update(paramsBet,bet.getServer_id());
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
