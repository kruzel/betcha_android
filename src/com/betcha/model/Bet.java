package com.betcha.model;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

import android.util.Log;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.BetRestClient;
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
	
//	private static GetUserBetsTask getUserBetsTask;
	private static BetRestClient betClient;
	private static Dao<Bet,Integer> dao;
	
	private List<User> participants;
	private Prediction ownerPrediction;
	
	public void setBet(Bet bet) {
		this.id = bet.getId();
		this.user = bet.getOwner();
		this.subject = bet.getSubject();
		this.reward = bet.getReward();
		this.date = bet.getDate();
		this.dueDate = bet.getDueDate();
		this.state = bet.getState();
	}

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
		this.ownerPrediction.setBet(this);
	}

	/**
	 * static methods that must be implemented by derived class
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Bet,Integer> getModelDao() throws SQLException  {
		if(dao==null){
			dao = getDbHelper().getDao(Bet.class);;
		}
		return dao;
	}
	
	@Override
	public void initDao() {
		try {
			setDao(getModelDao());
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
		
		if(participants!=null) {
			for (User participant : participants) {
				if(participant.getServer_id()==-1) {
					if(participant.createUserAccount()==0) 
						continue;
				}
				
				Prediction prediction = new Prediction(this);
				prediction.setSendInvite(true);
				prediction.setUser(participant);
				prediction.setBet(this);
				try {
					res = res + prediction.create();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
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
		Bet bet = getAndCreateBet(getServer_id());
	
		//TODO save client side changes to server
		
		if(bet!=null)
			return 1;
		else
			return 0;
	}
	
	@Override
	public int onRestGet() {
		Bet bet = getAndCreateBet(getServer_id());
		
		if(bet!=null) {
			setBet(bet);
			return 1;
		}
		else
			return 0;
	}

	@Override
	public int onRestGetWithDependents() {
		Bet bet = null;
		User owner = null;
		
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("server_id", getServer_id());
			if(bets.size()>0) {
				bet = bets.get(0);
				List<User> owners = User.getModelDao().queryForEq("id", bet.getOwner().getId());
				if(owners.size()>0) {
					owner = owners.get(0);
					bet.setOwner(owner);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		bet = Bet.getAndCreateBet(bet.getServer_id());
		if(bet==null)
			return 0;
		
		setBet(bet);
		
		if(Prediction.getAndCreatePredictions(bet)==null)
			return 0;
		
		return 0;
	}
	
	public static Bet getAndCreateBet(int server_id) {	
		
		Bet tmpBet = null;
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("server_id", server_id);
			if(bets==null)
				return null;
			tmpBet = bets.get(0);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}
		
		if(tmpBet==null) {
			tmpBet = new Bet();
		}
		
		BetRestClient betClient = new BetRestClient();
		JSONObject jsonBet = null;
		try {
			jsonBet = betClient.show(server_id);
		} catch (RestClientException e) {
			e.printStackTrace();
			return null;
		}
		if(jsonBet==null)
			return null;
		
		if(tmpBet.setJson(jsonBet)) {
			try {
				tmpBet.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return tmpBet;
	}
	
	public Boolean setJson(JSONObject jsonBet) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		if(jsonBet==null)
			return null;
		
		// TODO move the get part outside to the caller 
		User owner = null;
		try {
			owner = User.getAndCreateUser(jsonBet.getInt("user_id"));
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if(owner==null)
			return false;
		
		try {
			setServer_id(jsonBet.getInt("id"));
			setOwner(owner);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			setReward(jsonBet.getString("reward"));
		} catch (JSONException e1) {	
		}
		
		try {
			setSubject(jsonBet.getString("subject"));
		} catch (JSONException e1) {
		}
		
		try {
			setDate(formatter.parseDateTime(jsonBet.getString("created_at")));
		} catch (JSONException e1) {
		}
		
		try {
			setDueDate(formatter.parseDateTime(jsonBet.getString("due_date")));
		} catch (JSONException e1) {
		}
		
		try {
			setState(jsonBet.getString("state"));
		} catch (JSONException e1) {
		}
			       					
		return true;
	}

}
