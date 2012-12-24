package com.betcha.model.cache;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.betcha.R;
import com.betcha.model.ActivityEvent;
import com.betcha.model.Bet;
import com.betcha.model.ChatMessage;
import com.betcha.model.Friend;
import com.betcha.model.Location;
import com.betcha.model.Prediction;
import com.betcha.model.PredictionOption;
import com.betcha.model.Stake;
import com.betcha.model.Topic;
import com.betcha.model.TopicCategory;
import com.betcha.model.TopicResults;
import com.betcha.model.User;
import com.betcha.model.server.api.ActivityEventRestClient;
import com.betcha.model.server.api.BetRestClient;
import com.betcha.model.server.api.ChatMessageRestClient;
import com.betcha.model.server.api.FriendRestClient;
import com.betcha.model.server.api.LocationRestClient;
import com.betcha.model.server.api.PredictionOptionRestClinet;
import com.betcha.model.server.api.PredictionRestClient;
import com.betcha.model.server.api.RestClient;
import com.betcha.model.server.api.StakeRestClient;
import com.betcha.model.server.api.TokenRestClient;
import com.betcha.model.server.api.TopicCategoryRestClient;
import com.betcha.model.server.api.TopicRestClient;
import com.betcha.model.server.api.TopicResultRestClient;
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
			
			TableUtils.createTable(connectionSource, Location.class);
			TableUtils.createTable(connectionSource, ActivityEvent.class);
			TableUtils.createTable(connectionSource, Stake.class);
			TableUtils.createTable(connectionSource, TopicCategory.class);
			TableUtils.createTable(connectionSource, Topic.class);
			TableUtils.createTable(connectionSource, PredictionOption.class);
			TableUtils.createTable(connectionSource, TopicResults.class);
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
			TableUtils.dropTable(connectionSource, Location.class, true);
			TableUtils.dropTable(connectionSource, Location.class, true);
			TableUtils.dropTable(connectionSource, ActivityEvent.class, true);
			TableUtils.dropTable(connectionSource, Stake.class, true);
			TableUtils.dropTable(connectionSource, TopicCategory.class, true);
			TableUtils.dropTable(connectionSource, Topic.class, true);
			TableUtils.dropTable(connectionSource, PredictionOption.class, true);
			TableUtils.dropTable(connectionSource, TopicResults.class, true);
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
		ActivityEventRestClient.setUrl(context.getString(R.string.betcha_api)+"/activity_events");
		LocationRestClient.setUrl(context.getString(R.string.betcha_api)+"/locations");
		StakeRestClient.setUrl(context.getString(R.string.betcha_api)+"/stakes");
		TopicCategoryRestClient.setUrl(context.getString(R.string.betcha_api)+"/topic_categories");
		TopicRestClient.setUrl(context.getString(R.string.betcha_api)+"/topic_categories/{ctegory_id}/topics");
		PredictionOptionRestClinet.setUrl(context.getString(R.string.betcha_api)+"/topic_categories/{ctegory_id}/topics/{topic_id}/prediction_options");
		TopicResultRestClient.setUrl(context.getString(R.string.betcha_api)+"/topic_categories/{ctegory_id}/topics/{topic_id}/topic_results");
		
		//TODO for local tesing only.
		//initCategories(context);
		//initRewards(context);

		return true;
	}
	
//	public static void initCategories(Context context) {
//		Category category;
//		Topic topic;
//		PredictionSuggestion suggestion;
//		
//		Collection<Topic> topics;
//		Collection<PredictionSuggestion> suggestions;
//		
//		////////////////////////////////// category ////////////////////////////////////
//		
//		category = new Category();
//		category.setId("1");
//		category.setGroup("Custom");
//		category.setName("Custom");
//		category.setDescription("Any bet you like");
//		category.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.category_custom));
//		category.create();
//		
//		topics = new ArrayList<Topic>();
//		
//		//user defined topic
//		topic = new Topic(); 
//		topic.setId("0");
//		topic.setCategory(category);
//		topic.setName("");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("");
//		topic.create();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("0");
//		suggestion.setTopic(topic);
//		suggestion.setName("");
//		//suggestion.setImage(image);
//		suggestion.create();
//		
//		//------------------------ Topic --------------------------
//		
//		topic = new Topic(); 
//		topic.setId("1");
//		topic.setCategory(category);
//		topic.setName("Who win US elections");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("US");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("1");
//		suggestion.setTopic(topic);
//		suggestion.setName("Obama");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("2");
//		suggestion.setTopic(topic);
//		suggestion.setName("Romni");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		//------------------------ Topic --------------------------
//		
//		topic = new Topic(); 
//		topic.setId("2");
//		topic.setCategory(category);
//		topic.setName("Who win Israel elections");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("Israel");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("3");
//		suggestion.setTopic(topic);
//		suggestion.setName("Bibi");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("4");
//		suggestion.setTopic(topic);
//		suggestion.setName("Shely");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		category.setTopics(topics);
//		
//		//////////////////////////////////category ////////////////////////////////////
//		category = new Category();
//		category.setId("2");
//		category.setGroup("Sport");
//		category.setName("NFL");
//		category.setDescription("....");
//		category.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.category_nfl));
//		category.create();
//		
//		topics = new ArrayList<Topic>();
//		
//		//------------------------ Topic --------------------------
//		
//		topic = new Topic(); 
//		topic.setId("3");
//		topic.setCategory(category);
//		topic.setName("Detroit  Vs. Minnesota 11/11/12");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("Minnesota");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("5");
//		suggestion.setTopic(topic);
//		suggestion.setName("Tie");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("6");
//		suggestion.setTopic(topic);
//		suggestion.setName("Detroit");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nfl_detroit));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("7");
//		suggestion.setTopic(topic);
//		suggestion.setName("Minnesota");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nfl_minnesota));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		//------------------------ Topic --------------------------
//		
//		topic = new Topic(); 
//		topic.setId("4");
//		topic.setCategory(category);
//		topic.setName("NY Giants Vs. Cincinnati 11/11/12");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("	Cincinnati");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("8");
//		suggestion.setTopic(topic);
//		suggestion.setName("Tie");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("9");
//		suggestion.setTopic(topic);
//		suggestion.setName("NY Giants");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nfl_ny_giants));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("10");
//		suggestion.setTopic(topic);
//		suggestion.setName("Cincinnati");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nfl_cincinnati));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		category.setTopics(topics);
//		
//		//////////////////////////////////////////////////////////////////////////////////
//		
//		category = new Category();
//		category.setId("3");
//		category.setGroup("Sport");
//		category.setName("MLB");
//		category.setDescription("....");
//		category.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.category_mlb));
//		category.create();
//		
//		topics = new ArrayList<Topic>();
//		
//		//------------------------ Topic --------------------------
//
//		topic = new Topic(); 
//		topic.setId("5");
//		topic.setCategory(category);
//		topic.setName("Philadelphia Vs. Miami 11/11/12");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("Miami");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("11");
//		suggestion.setTopic(topic);
//		suggestion.setName("Tie");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("12");
//		suggestion.setTopic(topic);
//		suggestion.setName("Miami");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_mlb_miami));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("13");
//		suggestion.setTopic(topic);
//		suggestion.setName("Philadelphia");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_mlb_philadelphia));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		//------------------------ Topic --------------------------
//		
//		topic = new Topic(); 
//		topic.setId("6");
//		topic.setCategory(category);
//		topic.setName("Cleveland Vs. Baltimore  11/11/12");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("Cleveland");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("14");
//		suggestion.setTopic(topic);
//		suggestion.setName("Tie");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("15");
//		suggestion.setTopic(topic);
//		suggestion.setName("Cleveland");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_mlb_cleveland));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("16");
//		suggestion.setTopic(topic);
//		suggestion.setName("Baltimore");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_mlb_baltimore));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		category.setTopics(topics);
//		
//		//////////////////////////////////////////////////////////////////////////////////
//				
//		category = new Category();
//		category.setId("4");
//		category.setGroup("Sport");
//		category.setName("NBA");
//		category.setDescription("....");
//		category.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.category_nba));
//		category.create();
//		
//		topics = new ArrayList<Topic>();
//		
//		//------------------------ Topic --------------------------
//		
//		topic = new Topic(); 
//		topic.setId("7");
//		topic.setCategory(category);
//		topic.setName("Washington Vs. Charlotte  11/11/12");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("Charlotte");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("17");
//		suggestion.setTopic(topic);
//		suggestion.setName("Tie");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("18");
//		suggestion.setTopic(topic);
//		suggestion.setName("Washington");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nba_washington));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("19");
//		suggestion.setTopic(topic);
//		suggestion.setName("Charlotte");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nba_charlotte));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		//------------------------ Topic --------------------------
//		
//		topic = new Topic(); 
//		topic.setId("8");
//		topic.setCategory(category);
//		topic.setName("Toronto Vs. Indiana 11/11/12");
//		topic.setStartTime(new DateTime());
//		topic.setEndtTime(new DateTime());
//		topic.setLocation("Indiana");
//		topic.create();
//		topics.add(topic);
//		
//		suggestions = new ArrayList<PredictionSuggestion>();
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("20");
//		suggestion.setTopic(topic);
//		suggestion.setName("Tie");
//		//suggestion.setImage(image);
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("21");
//		suggestion.setTopic(topic);
//		suggestion.setName("Indiana");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nba_indiana));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		suggestion = new PredictionSuggestion();
//		suggestion.setId("22");
//		suggestion.setTopic(topic);
//		suggestion.setName("Toronto");
//		suggestion.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.team_nba_toronto));
//		suggestion.create();
//		suggestions.add(suggestion);
//		
//		topic.setSuggestions(suggestions);
//		
//		category.setTopics(topics);
//	
//	}
//	
//	public static void initRewards(Context context) {
//		Reward reward = null;
//				
//		reward = new Reward();
//		reward.setId("Drink");
//		reward.setGroup("Drink");
//		reward.setName("Drink");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_beer));
//		reward.setDrawable_id(R.drawable.stake_beer);
//		reward.create();
//		
//		reward = new Reward();
//		reward.setId("Lunch");
//		reward.setGroup("Food");
//		reward.setName("Lunch");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
//		reward.setDrawable_id(R.drawable.stake_meal);
//		reward.create();
//		
//		reward = new Reward();
//		reward.setId("Movie");
//		reward.setGroup("Fun");
//		reward.setName("Movie");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
//		reward.setDrawable_id(R.drawable.stake_movie);
//		reward.create();
//		
//		reward = new Reward();
//		reward.setId("Twist");
//		reward.setGroup("Twist");
//		reward.setName("Twist");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
//		reward.setDrawable_id(R.drawable.stake_twist);
//		reward.create();
//		
//		reward = new Reward();
//		reward.setId("Groupon");
//		reward.setGroup("Coupons");
//		reward.setName("Groupon");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
//		reward.setDrawable_id(R.drawable.stake_groupon);
//		reward.create();
//		
//		reward = new Reward();
//		reward.setId("Coins");
//		reward.setGroup("Virtual Currency");
//		reward.setName("Coins");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
//		reward.setDrawable_id(R.drawable.stake_coins);
//		reward.create();
//		
//		reward = new Reward();
//		reward.setId("FB brag");
//		reward.setGroup("Brag");
//		reward.setName("FB brag");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
//		reward.setDrawable_id(R.drawable.stake_facebook);
//		reward.create();
//		
//		reward = new Reward();
//		reward.setId("Tweet");
//		reward.setGroup("Brag");
//		reward.setName("Tweet");
//		reward.setDescription("....");
//		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
//		reward.setDrawable_id(R.drawable.stake_tweeter);
//		reward.create();
//	}
}
