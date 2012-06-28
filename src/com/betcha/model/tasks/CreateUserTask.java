package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;

import com.betcha.api.RESTClientUser;
import com.betcha.api.model.RESTUser;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.User;

public class CreateUserTask extends AsyncTask<Void, Void, Boolean> {
	
	private User user;
	private Context context;
	private DatabaseHelper dbHelper;	

	public CreateUserTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(User user) {
		this.user = user;
	}
	
	public void run() {
		
		try {
			user.setDao(dbHelper.getUserDao());
			user.create();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		execute();
	}

	@Override
	protected Boolean doInBackground(Void... args) {
    	
		RESTClientUser userClient = new RESTClientUser(context);
		Map<String,String> paramsUser = new HashMap<String,String>();
		paramsUser.put("name", user.getName());
		paramsUser.put("email", user.getEmail());
				
		RESTUser restUser = null;
		try {
			restUser = userClient.create(paramsUser);
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		}
		user.setServer_id(restUser.getId());
		try {
			user.update();
		} catch (SQLException e) {
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
