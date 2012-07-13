package com.betcha;

import java.sql.SQLException;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.betcha.api.RESTClient;
import com.betcha.das.DatabaseHelper;
import com.betcha.model.User;
import com.betcha.model.tasks.CreateBetTask;
import com.betcha.model.tasks.CreateUserBetTask;
import com.betcha.model.tasks.CreateUserTask;
import com.betcha.model.tasks.GetBetAndOwnerTask;
import com.betcha.model.tasks.GetUserBetTask;
import com.betcha.model.tasks.GetThisUserBetsTask;
import com.betcha.model.tasks.UpdateBetTask;
import com.betcha.model.tasks.UpdateUserBetTask;
import com.betcha.model.tasks.UpdateUserTask;
import com.betcha.model.tasks.UpdateUsersBetsTask;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


public class BetchaApp extends Application {
	
	/** preferences file **/
	private SharedPreferences prefs;
	
	/**
	 * You'll need this in your class to cache the helper in the class.
	 */
	private DatabaseHelper databaseHelper = null;
	private Dao<User, Integer> userDao = null;
	private User me;
	
	private CreateBetTask createBetTask;
	private GetBetAndOwnerTask getBetAndOwnerTask;
	private CreateUserBetTask createUserBetTask;
	private GetUserBetTask getUserBetTask;
	private GetThisUserBetsTask getThisUserBetsTask;
	private CreateUserTask createUserTask;
	private UpdateUserTask updateUsertask;
	private UpdateUsersBetsTask updateUsersBetsTask;
	private UpdateUserBetTask updateUserBetTask;
	private UpdateBetTask updateBetTask;
	
	String betUUID;
	
	@Override
	public void onCreate() {
				
		prefs = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
		
		initMe();
				
		super.onCreate();
	}
	
	@Override
	public void onTerminate() {
		/*
		 * You'll need this in your class to release the helper when done.
		 */
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
		super.onTerminate();
	}
	
	/**
	 * You'll need this in your class to get the helper from the manager once per class.
	 */
	public DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return databaseHelper;
	}

	private void initMe() {
		// get our dao		
		try {
			userDao = getHelper().getUserDao();
		} catch (SQLException e) {
			Log.e(getClass().getSimpleName(), ".initDB() - failed getting Dao");
			e.printStackTrace();
			return;
		}
			
		Integer myUserId = prefs.getInt("my_user_id", -1);
		
		if(myUserId!=-1) { //load my User from DB
			try {
				me = userDao.queryForId(myUserId);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String authToken = prefs.getString("auth_token", null);
		RESTClient.SetToken(authToken);
	}
	
	public SharedPreferences getPrefs() {
		return prefs;
	}

	public User getMe() {
		return me;
	}

	public void setMe(User me) {
		this.me = me;	
		Editor editor = prefs.edit();
		editor.putInt("my_user_id", me.getId());
		editor.commit();
	}
	
	public void setToken(String token) {
		Editor editor = prefs.edit();
		editor.putString("auth_token", token);
		editor.commit();
	}

	public CreateBetTask getCreateBetTask() {
		return createBetTask;
	}
	
	public CreateBetTask createCreateBetTask() {
		this.createBetTask = new CreateBetTask(this,getHelper());
		return createBetTask;
	}
	
	public GetBetAndOwnerTask getGetBetAndOwnerTaks() {
		return getBetAndOwnerTask;
	}

	public GetBetAndOwnerTask createGetBetAndOwnerTaks() {
		this.getBetAndOwnerTask = new GetBetAndOwnerTask(this, getHelper());
		return getBetAndOwnerTask;
	}
	
	public CreateUserBetTask getCreateUserBetTask() {
		return createUserBetTask;
	}

	public CreateUserBetTask createCreateUserBetTask() {
		this.createUserBetTask = new CreateUserBetTask(this, getHelper());
		return createUserBetTask;
	}

	public String getBetUUID() {
		return betUUID;
	}

	public void setBetUUID(String betUUID) {
		this.betUUID = betUUID;
	}
	
	public GetUserBetTask getGetUserBetTask() {
		return getUserBetTask;
	}

	public GetUserBetTask createGetUserBetTask() {
		this.getUserBetTask = new GetUserBetTask(this,getHelper());
		return this.getUserBetTask;
	}
	
	public GetThisUserBetsTask getGetThisUserBetsTask() {
		return getThisUserBetsTask;
	}

	public GetThisUserBetsTask createGetThisUserBetsTask() {
		this.getThisUserBetsTask = new GetThisUserBetsTask(this,getHelper());
		return this.getThisUserBetsTask;
	}

	public CreateUserTask getCreateUserTask() {
		return createUserTask;
	}

	public CreateUserTask createCreateUserTask() {
		this.createUserTask = new CreateUserTask(this, getHelper());
		return this.createUserTask;
	}

	public UpdateUserTask getUpdateUsertask() {
		return updateUsertask;
	}

	public UpdateUserTask createUpdateUsertask() {
		this.updateUsertask = new UpdateUserTask(this, getHelper());
		return this.updateUsertask;
	}

	public UpdateUsersBetsTask getUpdateUsersBetsTask() {
		return updateUsersBetsTask;
	}

	public UpdateUsersBetsTask createUpdateUsersBetsTask() {
		this.updateUsersBetsTask = new UpdateUsersBetsTask(this,getHelper());
		return this.updateUsersBetsTask;
	}

	public UpdateBetTask getUpdateBetTask() {
		return updateBetTask;
	}

	public UpdateBetTask createUpdateBetTask() {
		this.updateBetTask = new UpdateBetTask(this,getHelper());
		return this.updateBetTask;
	}

	public UpdateUserBetTask getUpdateUserBetTask() {
		return updateUserBetTask;
	}

	public UpdateUserBetTask createUpdateUserBetTask() {
		this.updateUserBetTask = new UpdateUserBetTask(this,getHelper());
		return this.updateUserBetTask;
	}


}
