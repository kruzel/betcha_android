package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientBet;
import com.betcha.api.RESTClientUser;
import com.betcha.api.model.RESTBet;
import com.betcha.api.model.RESTUser;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.Bet;
import com.betcha.model.User;

public class GetBetAndOwnerTask extends AsyncTask<Void, Void, Boolean> {
	private Bet bet = null;
	private User owner = null;
	private String uuid;
	private Context context;
	private DatabaseHelper dbHelper;	
	private IGetBetAndOwnerCB cb;

	public GetBetAndOwnerTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(String uuid, IGetBetAndOwnerCB cb) {
		this.uuid = uuid;
		this.cb = cb;
	}
	
	public void run() {
		
		try {
			List<Bet> bets = dbHelper.getBetDao().queryForEq("uuid", uuid);
			if(bets.size()>0) {
				bet = bets.get(0);
				List<User> owners = dbHelper.getUserDao().queryForEq("id", bet.getOwner().getId());
				if(owners.size()>0) {
					owner = owners.get(0);
					bet.setOwner(owner);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
				
		if(bet!=null && owner!=null)
			cb.OnGetBetCompleted(true, bet);
		else
			execute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		RESTClientBet betClient = new RESTClientBet(context);
		RESTBet restBet = betClient.showUUID(uuid);
		if(restBet==null)
			return false;
		
		try {
			List<User> listUser = null;
			listUser = dbHelper.getUserDao().queryForEq("server_id", restBet.getUser_id());
			if(listUser!=null && listUser.size()>0) {
				owner = listUser.get(0);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if(owner == null) { //then create it
			RESTClientUser userClient = new RESTClientUser(context);
			RESTUser restOwner = userClient.show(restBet.getUser_id());
			if(restOwner==null)
				return false;
			
			owner = new User();
			owner.setEmail(restOwner.getEmail());
			owner.setName(restOwner.getName());
			owner.setServer_id(restOwner.getId());
			
			try {
				owner.setDao(dbHelper.getUserDao());
				owner.create();
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		try {
			bet = new Bet();
			bet.setReward(restBet.getReward());
			bet.setSubject(restBet.getSubject());
			bet.setDate(formatter.parseDateTime(restBet.getDate()));
			bet.setDueDate(formatter.parseDateTime(restBet.getDueDate()));
			bet.setOwner(owner);
			bet.setServer_id(restBet.getId());
			bet.setState(restBet.getState());
			bet.setUuid(uuid);
			bet.setDao(dbHelper.getBetDao());
			bet.create();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
			       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		cb.OnGetBetCompleted(result, bet);
			
		super.onPostExecute(result);
	}
	
}
