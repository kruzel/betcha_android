package com.betcha;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.betcha.model.ActivityEvent;
import com.betcha.model.Bet;
import com.betcha.model.Contact;
import com.betcha.model.Friend;
import com.betcha.model.Prediction;
import com.betcha.model.User;
import com.betcha.model.cache.DatabaseHelper;
import com.betcha.model.cache.IModelListener;
import com.betcha.model.server.api.RestClient;
import com.google.android.gcm.GCMRegistrar;
import com.j256.ormlite.android.apptools.OpenHelperManager;

@ReportsCrashes(formKey = "dFNHeE4wcDNfYWFCQWdnazVkdHdLSGc6MQ")
public class BetchaApp extends Application implements IModelListener {

	/** preferences file **/
	private static SharedPreferences prefs;

	/**
	 * You'll need this in your class to cache the helper in the class.
	 */
	private DatabaseHelper databaseHelper = null;

	private User me;
	private String invite_bet_id = "-1";
	
	private DateTime lastSyncTime;
	
	private List<User> friends;
	private Object friendsLock = new Object();

	public static int THEME = R.style.Theme_Sherlock;
	public static BetchaApp app;
	
	public Bet curBet;
	
	public Bet getCurBet() {
		return curBet;
	}

	public void setCurBet(Bet curBet) {
		this.curBet = curBet;
	}

	@Override
	public void onCreate() {
		//Debug.startMethodTracing("dropabet");
		
		ACRA.init(this);

		app = this;

		prefs = getSharedPreferences(getString(R.string.prefs_name),
				Context.MODE_PRIVATE);
		
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this,
					DatabaseHelper.class);
			
			if(databaseHelper==null || !databaseHelper.initModel(this)) {
				Log.e(getClass().getSimpleName(),
						".initDB() - failed creating helper");
			} else {
				initUserParams();
			}
		}
				
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		releaseDbHelpers();
		super.onTerminate();
	}

	public static BetchaApp getInstance() {
		return app;
	}

	/**
	 * You'll need this in your class to get the helper from the manager once
	 * per class.
	 */
	public Boolean createDbHelpers() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(this,
					DatabaseHelper.class);
			databaseHelper.initModel(this);
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
		if (prefs == null)
			return;

		String myUserId = prefs.getString("my_user_id", "-1");

		if (!myUserId.equals("-1")) { // load my User from DB
			try {
				List<User> users = User.getModelDao()
						.queryForEq("id", myUserId);
				if (users != null && users.size() > 0)
					me = users.get(0);

				String authToken = prefs.getString("auth_token", null);
				RestClient.SetToken(authToken);
				
				String strLastSyncTime = prefs.getString("last_synch_time", null);
				if(strLastSyncTime!=null) {
					DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
					lastSyncTime = formatter.parseDateTime(strLastSyncTime);
				}
				
				registerToPushNotifications();
				
				Friend friend = new Friend(me);
				friend.setListener(this);
				friend.getAllForCurUser(); //check for new friends
				
				//init friends list
				loadFriends();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public User getCurUser() {
		return me;
	}

	public void setMe(User me) {
		this.me = me;
		Editor editor = prefs.edit();
		if(me==null)
			editor.remove("my_user_id");
		else
			editor.putString("my_user_id", me.getId());
		editor.commit();
	}

	public DateTime getLastSyncTime() {
		return lastSyncTime;
	}

	public void setLastSyncTime(DateTime lastSyncTime) {
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		this.lastSyncTime = lastSyncTime;
		Editor editor = prefs.edit();
		editor.putString("last_synch_time", formatter.print(lastSyncTime));
		editor.commit();
	}

	public static void setToken(String token) {
		RestClient.SetToken(token);
		Editor editor = prefs.edit();
		editor.putString("auth_token", token);
		editor.commit();
	}

	public void setBetId(String invite_bet_id) {
		this.invite_bet_id = invite_bet_id;
	}

	public String getBetId() {
		return invite_bet_id;
	}

	public List<User> getFriends() {
		synchronized (friendsLock) {
			if(friends==null)
				loadFriends();
				
			return friends;
		}
	}
		
	public void loadFriends() {
		Thread t = new Thread(new Runnable() {
					
			@Override
			public void run() {		
				synchronized(friendsLock) {
					try {
						// TODO add distinct email
						if (getCurUser() != null)
							friends = User.getModelDao().queryBuilder()
									.orderBy("name", true).where()
									.ne("id", getCurUser().getId()).query();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (Throwable e) {
						e.printStackTrace();
					}
			
					if (friends == null) {
						friends = new ArrayList<User>();
					}
					
					List<Contact> fbFriends = null;
					try {
						// TODO add distinct email
						if (getCurUser() != null)
							fbFriends = Contact.getModelDao().queryForAll();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (Throwable e) {
						e.printStackTrace();
					}
					
					if(fbFriends!=null && fbFriends.size()>0) {
						for (Contact contact : fbFriends) {
							User user = new User();
							user.setContact(contact);
							friends.add(user);
						}
					}
			
					// invite users only from pre-loaded friend list should be
					// loaded ad registration)
					ContentResolver cr = getContentResolver();
					Cursor emailCur = cr.query(
							ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							new String[] { Contacts._ID, Contacts.PHOTO_ID,
									ContactsContract.Data.DISPLAY_NAME,
									ContactsContract.CommonDataKinds.Email.DATA },
							null, null, "lower("
									+ ContactsContract.Data.DISPLAY_NAME + ") ASC");
					while (emailCur.moveToNext()) {
						// This would allow you get several email addresses
						// if the email addresses were stored in an array
						String email = emailCur.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						String name = emailCur.getString(emailCur
								.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
						
						int ind = name.indexOf('@');
						if(ind!=-1)
							name = name.substring(0, ind);
			
						long contact_id = emailCur.getLong(emailCur
								.getColumnIndex(ContactsContract.Contacts._ID));
						long photo_id = emailCur.getLong(emailCur
								.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
			
						// verify this user is not a known user and already included
						List<User> foundUsers = null;
						try {
							foundUsers = User.getModelDao().queryForEq("email",
									email);
						} catch (SQLException e) {
							e.printStackTrace();
						}
			
						if ((name != null || email != null)
								&& (foundUsers == null || foundUsers.size() == 0)) {
							User tmpUser = new User();
							tmpUser.setName(name);
							tmpUser.setEmail(email);
							tmpUser.setProvider("email");
							tmpUser.setContact_id(contact_id);
							tmpUser.setContact_photo_id(photo_id);
			
							friends.add(tmpUser);
						}
					}
					emailCur.close();
				}
			}
		});
		t.start();
		
	}

	public void registerToPushNotifications() {

		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					GCMRegistrar.checkDevice(BetchaApp.this);
					GCMRegistrar.checkManifest(BetchaApp.this);
					final String regId = GCMRegistrar.getRegistrationId(BetchaApp.this);
					if (regId.equals("")) {
						GCMRegistrar.register(BetchaApp.this, "1053196289883"); // google api
																		// project
																		// number
					} else {
						Log.v("BetchaApp.onCreate()", "Already registered");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	@Override
	public void onCreateComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdateComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetComplete(Class clazz, HttpStatus errorCode) {
		if (clazz.getSimpleName().contentEquals("Friend") && errorCode==HttpStatus.OK) {
			// new friedns found
			loadFriends();
		}
	}

	@Override
	public void onDeleteComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub

	}

}
