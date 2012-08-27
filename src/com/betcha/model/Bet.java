package com.betcha.model;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.BetRestClient;
import com.betcha.model.tasks.GetBetAndDependantsTask;
import com.betcha.model.tasks.GetUserBetsTask;
import com.betcha.model.tasks.IGetBetAndDependantCB;
import com.betcha.model.tasks.IGetThisUserBetsCB;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A bet object we are creating and persisting to the database.
 */

@DatabaseTable(tableName = "bets")
public class Bet extends ModelCache<Bet,Integer>  {
	
	// id is generated by the database and set on the object automagically
	@DatabaseField(generatedId  = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user; //owner
	@DatabaseField
	private String subject;
	@DatabaseField
	private String reward; //benefit
	@DatabaseField
	private DateTime date;
	@DatabaseField
	private DateTime dueDate;
	@DatabaseField
	private String state; // open/due/closed
	
	//non persistent
	public static final String STATE_OPEN = "open";
	public static final String STATE_DUE = "due";
	public static final String STATE_CLOSED = "closed";
	
	private static GetUserBetsTask getUserBetsTask;
	private static GetBetAndDependantsTask getBetAndDependantTask;
	private static BetRestClient betClient;
	
	private List<User> participants;
	private Prediction ownerPrediction;
	
	public BetRestClient getBetClient() {
		if(betClient==null)
			betClient = new BetRestClient();
		
		return betClient;
	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public User getOwner() {
		return user;
	}

	public void setOwner(User owner) {
		this.user = owner;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String betSubject) {
		this.subject = betSubject;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String betReward) {
		this.reward = betReward;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public DateTime getDueDate() {
		return dueDate;
	}

	public void setDueDate(DateTime dueDate) {
		this.dueDate = dueDate;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	//non persistent
	public Prediction getOwnerPrediction() {
		return ownerPrediction;
	}

	public void setOwnerPrediction(Prediction ownerPrediction) {
		this.ownerPrediction = ownerPrediction;
	}

	/**
	 * static methods that must be implemented by derived class
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Bet,Integer> getModelDao() throws SQLException {
		return getDbHelper().getDao(Bet.class);
	}
	
	@Override
	public void initDao() {
		try {
			setDao((Dao<Bet, Integer>) getDbHelper().getDao(Bet.class));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** inherited ModelCache methods */
	
	public int onRestCreate() {
		int res = 0;
		JSONObject json = null;
		json = getBetClient().create(this);
		
		setServer_id(json.optInt("id", -1));
		try {
			res = updateLocal();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			if(ownerPrediction!=null)
				ownerPrediction.create();
		} catch (SQLException e) {
			Log.e("CreateBetActivity", "Failed creating prediction!");
			e.printStackTrace();
		}
		
		//create predictions place holders and send invites
		ownerPrediction.send_invites(participants);
		
		return res;
	}

	public int onRestUpdate() {
		getBetClient().update(this, getServer_id());
		return 1;
	}

	public int onRestDelete() {
		getBetClient().delete(getServer_id());
		return 1;
	}

	public int onRestSync() {
		JSONObject json = getBetClient().show(getServer_id());
	
		try {
			user = User.getModelDao().queryForId(json.getInt("user_id"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		try {
			subject = json.getString("subject");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			reward= json.getString("reward");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		try {
			date= DateTime.parse(json.getString("created_at"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			dueDate= DateTime.parse(json.getString("due_date"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			state= json.getString("state");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		int res = 0;
		try {
			res = updateLocal();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	/** bulk sync per user */
	static public void refreshForUser(User user, IGetThisUserBetsCB cb) {
		getUserBetsTask = new GetUserBetsTask();
		getUserBetsTask.setValues(user, cb);
		getUserBetsTask.run();
	}
	
	/** get a new bet via bet id on server, to be used for joining bets invites */
	static public void fetchBetAndOwner(int bet_server_id, IGetBetAndDependantCB cb) {
		getBetAndDependantTask = new GetBetAndDependantsTask();
		getBetAndDependantTask.setValues(bet_server_id, cb);
		getBetAndDependantTask.run();
	}
}
