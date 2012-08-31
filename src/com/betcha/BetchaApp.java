package com.betcha;

import java.sql.SQLException;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;
import com.betcha.model.cache.DatabaseHelper;
import com.betcha.model.server.api.BetRestClient;
import com.betcha.model.server.api.PredictionRestClient;
import com.betcha.model.server.api.RestClient;
import com.betcha.model.server.api.TokenRestClient;
import com.betcha.model.server.api.UserRestClient;
import com.j256.ormlite.android.apptools.OpenHelperManager;

@ReportsCrashes(formKey = "dFNHeE4wcDNfYWFCQWdnazVkdHdLSGc6MQ") 
public class BetchaApp extends Application {
	
	/** preferences file **/
	private static SharedPreferences prefs;
	
	/**
	 * You'll need this in your class to cache the helper in the class.
	 */
	private DatabaseHelper databaseHelper = null;

	private User me;
	private int invite_bet_id = -1;
			
	@Override
	public void onCreate() {
				
		ACRA.init(this);

		prefs = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);
		
		if(!createDbHelpers()) {
			Log.e(getClass().getSimpleName(), ".initDB() - failed creating helper");
		} else {
			initUserParams();
		}
				
		super.onCreate();
	}
	
	@Override
	public void onTerminate() {
		releaseDbHelpers();
		super.onTerminate();
	}
	
	/**
	 * You'll need this in your class to get the helper from the manager once per class.
	 */
	public Boolean createDbHelpers() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
			User.setDbHelper(databaseHelper);
			Bet.setDbHelper(databaseHelper);
			Prediction.setDbHelper(databaseHelper);
			
			UserRestClient.setUrl(getString(R.string.betcha_api) + "/users");
			TokenRestClient.setUrl(getString(R.string.betcha_api) + "/tokens");
			BetRestClient.setUrl(getString(R.string.betcha_api) + "/bets");
			PredictionRestClient.setUrl(getString(R.string.betcha_api) + "/bets/{bet_id}/predictions");
		}
		
		return true;
	}
	
	public void releaseDbHelpers() {
		/*
		 * You'll need this in your class to release the helper when done.
		 */
		User.setDbHelper(null);
		Bet.setDbHelper(null);
		Prediction.setDbHelper(null);
		
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
		
	}

	private void initUserParams() {		
		Integer myUserId = prefs.getInt("my_user_id", -1);
		
		if(myUserId!=-1) { //load my User from DB
			try {
				me = User.getModelDao().queryForId(myUserId);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String authToken = prefs.getString("auth_token", null);
		RestClient.SetToken(authToken);
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
	
	public static void setToken(String token) {
		RestClient.SetToken(token);
		Editor editor = prefs.edit();
		editor.putString("auth_token", token);
		editor.commit();
	}

	public void setBetId(int invite_bet_id) {
		this.invite_bet_id = invite_bet_id;
	}

	public int getBetId() {
		return invite_bet_id;
	}

}
