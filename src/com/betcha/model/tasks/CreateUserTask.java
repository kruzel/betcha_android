package com.betcha.model.tasks;

import java.sql.SQLException;

import org.springframework.web.client.RestClientException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.betcha.R;
import com.betcha.api.RESTClientToken;
import com.betcha.api.model.RESTToken;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.User;

public class CreateUserTask extends AsyncTask<Void, Void, String> {
	
	private User user;
	private Context context;
	private DatabaseHelper dbHelper;	
	private ICreateUserCB cb;

	public CreateUserTask(Context context, DatabaseHelper dbHelper) {
		super();
		this.context = context;
		this.dbHelper = dbHelper;
	}

	public void setValues(User user, ICreateUserCB cb) {
		this.user = user;
		this.cb = cb;
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
	protected String doInBackground(Void... args) {
    	
		RESTClientToken tokenClient = new RESTClientToken(context);
				
		RESTToken restToken = null;
		try {
			restToken = tokenClient.create(user.getEmail(), user.getPass(), user.getName());
		} catch (RestClientException e) {
			e.printStackTrace();
			return null;
		}
		
		user.setServer_id(restToken.getId());
		
		try {
			user.update();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	       					
		return restToken.getToken();
	}

	@Override
	protected void onPostExecute(String token) {
		if(token==null) {	    	
	    	Toast.makeText(context, R.string.error_registration_failed, Toast.LENGTH_LONG);
		}
		
		if(cb!=null) {
			cb.OnRegistrationComplete(token);
		}
		
		super.onPostExecute(token);
	}
	
}
