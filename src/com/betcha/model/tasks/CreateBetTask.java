package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.betcha.R;
import com.betcha.api.RESTClientBet;
import com.betcha.api.RESTClientUserBet;
import com.betcha.api.model.RESTBet;
import com.betcha.api.model.RESTUserBet;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.Bet;
import com.betcha.model.User;
import com.betcha.model.UserBet;

public class CreateBetTask extends AsyncTask<Void, Void, Boolean> {
	
	private Bet bet;
	private UserBet userBet;
	private User user;
	private Context context;
	private DatabaseHelper dbHelper;	

	public CreateBetTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(Bet bet, UserBet userBet, User user) {
		this.bet = bet;
		this.userBet = userBet;
		this.user = user;
	}
	
	public void run() {
		try {
			bet.setDao(dbHelper.getBetDao());
			bet.create();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
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
	protected Boolean doInBackground(Void... args) {
    	
		RESTClientBet betClient = new RESTClientBet(context);
		Map<String,String> paramsBet = new HashMap<String,String>();
		paramsBet.put("uuid", bet.getUuid());
		paramsBet.put("user_id", Integer.toString(bet.getOwner().getServer_id()));
		paramsBet.put("subject", bet.getSubject());
		paramsBet.put("reward", bet.getReward());
		paramsBet.put("date", bet.getDate().toString());
		paramsBet.put("due_date", bet.getDueDate().toString());
		paramsBet.put("state", bet.getState());
		
		RESTBet restBet = null;
		try {
			restBet = betClient.create(paramsBet);
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		}
		bet.setServer_id(restBet.getId());
		try {
			bet.update();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
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
	       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(result) {	    	
	    	Toast.makeText(context, R.string.msg_bet_accepted, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, R.string.msg_bet_rejected, Toast.LENGTH_LONG).show();
		}
		
		super.onPostExecute(result);
	}
	
}
