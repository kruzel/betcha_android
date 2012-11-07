package com.betcha.model.cache;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.betcha.R;
import com.betcha.model.Bet;
import com.betcha.model.Category;
import com.betcha.model.ChatMessage;
import com.betcha.model.Friend;
import com.betcha.model.Prediction;
import com.betcha.model.Reward;
import com.betcha.model.User;
import com.betcha.model.server.api.BetRestClient;
import com.betcha.model.server.api.ChatMessageRestClient;
import com.betcha.model.server.api.FriendRestClient;
import com.betcha.model.server.api.PredictionRestClient;
import com.betcha.model.server.api.RestClient;
import com.betcha.model.server.api.TokenRestClient;
import com.betcha.model.server.api.UserRestClient;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "dropabet.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 25;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Bet.class);
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, Prediction.class);
			TableUtils.createTable(connectionSource, Friend.class);
			TableUtils.createTable(connectionSource, ChatMessage.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Bet.class, true);
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, Prediction.class, true);
			TableUtils.dropTable(connectionSource, Friend.class, true);
			TableUtils.dropTable(connectionSource, ChatMessage.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
	}
	
	/**
	 * You'll need this in your class to get the helper from the manager once
	 * per class.
	 */
	public Boolean initModel(Context context) {
		
		ModelCache.setContext(context);
		ModelCache.disableConnectivityReciever();

		User.setDbHelper(this);
		Bet.setDbHelper(this);
		Prediction.setDbHelper(this);
		Friend.setDbHelper(this);
		ChatMessage.setDbHelper(this);

		RestClient.setContext(context);
		UserRestClient.setUrl(context.getString(R.string.betcha_api) + "/users");
		TokenRestClient.setUrl(context.getString(R.string.betcha_api) + "/tokens");
		BetRestClient.setUrl(context.getString(R.string.betcha_api) + "/bets");
		PredictionRestClient.setUrl(context.getString(R.string.betcha_api)
				+ "/bets/{bet_id}/predictions");
		FriendRestClient.setUrl(context.getString(R.string.betcha_api)
				+ "/users/{user_id}/friends");
		ChatMessageRestClient.setUrl(context.getString(R.string.betcha_api)
				+ "/bets/{bet_id}/chat_messages");
		
		Reward.init(context);
		Category.init(context);

		return true;
	}
}
