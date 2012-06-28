package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientUser;
import com.betcha.api.RESTClientUserBet;
import com.betcha.api.model.RESTUser;
import com.betcha.api.model.RESTUserBet;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.Bet;
import com.betcha.model.User;
import com.betcha.model.UserBet;

public class GetUserBetTask extends AsyncTask<Void, Void, Boolean> {
	private int betServerId;
	private Context context;
	private DatabaseHelper dbHelper;	
	private IGetUserBetsCB cb;
	Bet bet;
	
	List<UserBet> usersBets;

	public GetUserBetTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(int betServerId, IGetUserBetsCB cb) {
		this.betServerId = betServerId;
		this.cb = cb;
	}
	
	public Boolean run() {
		try {
			List<Bet> bets = dbHelper.getBetDao().queryForEq("server_id",betServerId);
			if(bets==null || bets.size()==0)
				return false;
			bet = bets.get(0);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		
		try {
			usersBets = dbHelper.getUserBetDao().queryForEq("bet_id", bet.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
				
		//get the rest from the server
		execute();
		return true;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		RESTClientUserBet userBetClient = new RESTClientUserBet(context);
		List<RESTUserBet> restUserBet = userBetClient.showBetId(betServerId);
		if(restUserBet==null || restUserBet.size()==0)
			return false;
		
		User user = null;
		for (RESTUserBet restUserBetIt : restUserBet) {
			try {
					user = dbHelper.getUserDao().queryForId(restUserBetIt.getUser_id());
					if(user==null) {
						RESTClientUser userClient = new RESTClientUser(context);
						RESTUser restUser = userClient.show(restUserBetIt.getUser_id());
						if(restUser==null)
							continue;
						
						user = new User();
						user.setEmail(restUser.getEmail());
						user.setName(restUser.getName());
						user.setServer_id(restUser.getId());
						
						user.setDao(dbHelper.getUserDao());
						user.create();
					}
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			try {		
					UserBet userBet = null;
					List<UserBet> tmpUsersBets;
					try {
						tmpUsersBets = dbHelper.getUserBetDao().queryForEq("server_id", restUserBetIt.getId());
					} catch (SQLException e) {
						e.printStackTrace();
						continue;
					}
					
					if(tmpUsersBets==null || tmpUsersBets.size()==0) {
						userBet = new UserBet();
						userBet.setBet(bet);
						userBet.setDate(formatter.parseDateTime(restUserBetIt.getDate()));
						userBet.setMyAck(restUserBetIt.getUser_ack());
						userBet.setMyBet(restUserBetIt.getUser_result_bet());
						userBet.setResult(restUserBetIt.getResult());
						userBet.setServer_id(restUserBetIt.getId());
						userBet.setUser(user);
						userBet.setServer_id(restUserBetIt.getId());
						userBet.setDao(dbHelper.getUserBetDao());
						userBet.create();
						usersBets.add(userBet);
					}				
					
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
		}
	       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		if(cb!=null)
			cb.OnGetUserBetsCompleted(result, usersBets);
		
		super.onPostExecute(result);
	}
	
}
