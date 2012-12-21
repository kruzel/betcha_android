package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import android.util.Log;

import com.betcha.BetchaApp;
import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.BetRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A bet object we are creating and persisting to the database.
 */

@DatabaseTable(tableName = "bets")
public class Bet extends ModelCache<Bet, String> {

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user; // owner
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private TopicCategory category;
	@DatabaseField
	private String topicCustom;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private Topic topic;
	@DatabaseField
	private String reward; // benefit
	@DatabaseField
	private String reward_id; // benefit
	@DatabaseField(defaultValue = "1")
	private Integer reward_amount; 
	@DatabaseField
	private DateTime date;
	@DatabaseField
	private DateTime dueDate;
	@DatabaseField
	private String state; // open/due/closed
	@ForeignCollectionField(eager = true)
	private ForeignCollection<Prediction>  predictions;
	@ForeignCollectionField(eager = false)
	private ForeignCollection<ChatMessage>  chatMessages;
	
	private Prediction ownerPrediction;
	private List<User> participants;
	
	// non persistent
	public static final String STATE_OPEN = "open";
	public static final String STATE_DUE = "due";
	public static final String STATE_CLOSED = "closed";

	// private static GetUserBetsTask getUserBetsTask;
	private static BetRestClient betClient;
	private static Dao<Bet, String> dao;
	
	public static Bet get(String id) {
		Bet bet = null;
		try {
			bet = getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return bet;
	}

	public void setBet(Bet bet) {
		this.id = bet.getId();
		this.user = bet.getOwner();
		this.topicCustom = bet.getTopicCustom();
		this.topic = bet.getTopic();
		this.reward = bet.getReward().getName(); //TODO replace with Reward object
		this.reward_id = bet.getReward_id();
		this.date = bet.getDate();
		this.dueDate = bet.getDueDate();
		this.state = bet.getState();
		this.category = bet.getCategory();
	}

	public BetRestClient getBetClient() {
		if (betClient == null)
			betClient = new BetRestClient();

		return betClient;
	}
	
	public HttpStatus getLastRestErrorCode() {
		return getBetClient().getLastRestErrorCode();
	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;
	}
	
	public void addParticipants(List<User> participants) {
		if(this.participants==null) {
			this.participants = new ArrayList<User>();
		}
		this.participants.addAll(participants);
	}

	public User getOwner() {
		return user;
	}

	public void setOwner(User owner) {
		this.user = owner;
	}

	public String getTopicCustom() {
		return topicCustom;
	}

	public void setTopicCustom(String betTopic) {
		this.topicCustom = betTopic;
	}
	
	public Topic getTopic() {
		return topic;
	}
	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	public Stake getReward() {
		Stake r = Stake.get(reward); //TODO replace with Reward object as member, currently the reward name is the id;
		
		if(r==null) {
			r = new Stake();
			r.setId("0");
			r.setName(reward);
		}
		
		return r;
	}

	public void setReward(String betReward) {
		this.reward = betReward; //TODO replace with Reward object as memeber, currently the reard name is the id
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

	public TopicCategory getCategory() {
		return category;
	}

	public void setCategory(TopicCategory category) {
		this.category = category;
	}

	public String getReward_id() {
		return reward_id;  //TODO replace with Reward object
	}

	public void setReward_id(String reward_id) {
		this.reward_id = reward_id; //TODO replace with Reward object
	}
	
	public Integer getRewardAmount() {
		return reward_amount;
	}

	public void setRewardAmount(Integer reward_amount) {
		this.reward_amount = reward_amount;
	}

	// non persistent
	public Prediction getOwnerPrediction() {
		if(ownerPrediction==null) {
			for (Prediction prediction : predictions) {
				if(prediction.getUser().getId().equals(getOwner().getId()))
					ownerPrediction = prediction;
			}
		}
		
		return ownerPrediction;
	}

	public void setOwnerPrediction(Prediction ownerPrediction) {
		this.ownerPrediction = ownerPrediction;
		this.ownerPrediction.setBet(this);
	}

	public List<Prediction> getPredictions() {
		if(predictions==null)
			return null;
		
		List<Prediction> list = new ArrayList<Prediction>(predictions);
		return list;
	}
	
	public int getPredictionsCount() {
		if(predictions==null)
			return 0;
		
		return predictions.size();
	}
	
	public List<ChatMessage> getChatMessages() {
		List<ChatMessage> list = new ArrayList<ChatMessage>(chatMessages);
		return list;
	}
	
	public int getChatMessagesCount() {
		return chatMessages.size();
	}

	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Bet, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Bet.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<Bet, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Bet.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	public int onRestCreate() {
		int res = 0;
		
		//create missing users
		if(getPredictions()!=null) {
			for (Prediction prediction : getPredictions()) {
				if(!prediction.getUser().isServerCreated()) {
					if(prediction.getUser().onRestGet()==0) {
						if(prediction.getUser().restCreateUserAccount()==0)
							continue;
					}
				} 
			}
		}
		
		if (getBetClient().create(this) != null) {
			res = 1;
			
			if(getPredictions()!=null) {
				for (Prediction prediction : getPredictions()) {
					prediction.setServerCreated(true);
					prediction.setServerUpdated(true);
					prediction.onLocalUpdate();
				}
			}
		}

		return res;
	}

	public int onRestUpdate() {
		getBetClient().update(this);
		
		if(getLastRestErrorCode()==HttpStatus.OK) {
			for (Prediction prediction : getPredictions()) {
				prediction.setServerUpdated(true);
				prediction.onLocalUpdate();
			}
		}
		
		return 1;
	}

	public int onRestDelete() {
		getBetClient().delete(getId());
		
		if(getLastRestErrorCode()==HttpStatus.OK) {
			for (Prediction prediction : getPredictions()) {
				prediction.setServerUpdated(true);
				prediction.onLocalUpdate();
			}
		}
		
		return 1;
	}

	public int onRestSync() {
		int res = 0;
		if (!isServerUpdated()) {
			onRestCreate();
		}

		return res;
	}

	@Override
	public int onRestGet() {
		BetRestClient betClient = new BetRestClient();
		JSONObject jsonBets = null;
		try {
			jsonBets = betClient.show(getId());
		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}
		if (jsonBets == null)
			return 0;
		
		JSONArray jsonBetsArray = null;
		try {
			jsonBetsArray = jsonBets.getJSONArray("bets");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
		
		if(jsonBetsArray==null)
			return 0;

		JSONObject jsonBetContent = null;
		try {
			jsonBetContent = jsonBetsArray.getJSONObject(0);
		} catch (JSONException e1) {
		}
		
		if(jsonBetContent==null)
			return 0;
		
		if (setJson(jsonBetContent)) {
			try {
				createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
		}

		return 1;
	}

	@Override
	public int onRestGetAllForCurUser() {
		return getAllUpdatesForCurUser(null);
	}

	public static int getAllUpdatesForCurUser(DateTime lastUpdate) {
		List<Bet> bets = new ArrayList<Bet>();

		BetRestClient restClient = new BetRestClient();
		JSONObject jsonBets = null;
		try {
			if (lastUpdate == null)
				jsonBets = restClient.show_for_user(); // for logged in user
			else {
				// for logged in user
				jsonBets = restClient.show_updates_for_user(lastUpdate); 
			}

		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}

		if (jsonBets == null)
			return 0;
		
		JSONArray jsonBetsArray = null;
		try {
			jsonBetsArray = jsonBets.getJSONArray("bets");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jsonBetsArray == null)
			return 0;

		for (int i = 0; i < jsonBetsArray.length(); i++) {
			JSONObject jsonBet;

			try {
				jsonBet = jsonBetsArray.getJSONObject(i);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}

			Bet tmpBet = null;
			try {
				List<Bet> tmpBets = Bet.getModelDao().queryForEq("id",jsonBet.getString("id"));
				if (tmpBets != null && tmpBets.size() > 0) {
					tmpBet = tmpBets.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (tmpBet == null) {
				tmpBet = new Bet();
			}

			if (!tmpBet.setJson(jsonBet))
				continue;

			tmpBet.setServerCreated(true);
			tmpBet.setServerUpdated(true);
			
			try {
				tmpBet.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

			bets.add(tmpBet);

		}

		return bets.size();
	}
	
	public int addPredictions(List<User> newParticipants) {
		int res = 0;
		for (User participant : newParticipants) {
			//avoid adding a user already participating in bet
			Boolean found = false;
			if(predictions!=null) {
				for (Prediction prediction : predictions) {
					if(prediction.getUser().getId().equals(participant.getId())) {
						found = true;
						break;
					}
				}
			}
			if(found)
				continue;
			
			if(participant.getId()==null) { //new friend
				//verify contact not exist already via mail
				List<User> foundUsers = null;
				try {
					foundUsers = participant.getDao().queryForEq("email", participant.getEmail());
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if(foundUsers.size()==0) {
					if(isServerCreated()) { 
						//bet already created on server, so need to add the specific usres individually
						participant.create();
					} else {
						participant.onLocalCreate(); //locally
					}
					//participants.add(participant); - temp variable for certain operations
					res += 1;
				} else { 
					participant = foundUsers.get(0);
				}
			}
			
			Prediction prediction = new Prediction();
			prediction.setUser(participant);
			prediction.setBet(this);
			prediction.setPrediction("");
			prediction.setSendInvite(true);
			if(isServerCreated()) { 
				//bet already created on server, so need to add the specific predictions individually
				prediction.create();
			} else {
				//will be created on the server together with the bet
				prediction.onLocalCreate();
			}
		}
		
		try {
			getDao().refresh(this);
			for (Prediction prediction : getPredictions()) {
				int n = prediction.getDao().refresh(prediction);
				if(prediction.getUser()!=null)
					Log.i("after refresh", "refresh()=" + n + " prediction.user: " + prediction.getUser().getName() + ", " + prediction.getUser().getEmail());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	// nested object creation
	@Override
	public int onLocalCreate() {
		genId();
		setCreated_at(new DateTime());
		setUpdated_at(new DateTime());

		int res = 0;
		try {
			res = getDao().create(this);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		if(getOwnerPrediction()!=null)
			getOwnerPrediction().onLocalCreate();
		
		int numNewFriends = 0;
		if(participants!=null && participants.size()>0) {
			numNewFriends = addPredictions(participants);
		
			if(numNewFriends>0) {
				BetchaApp.getInstance().loadFriends();
			}
		}
		
		return res;
	}

	@Override
	public int onLocalUpdate() {
		setUpdated_at(new DateTime());

		int res = 0;
		try {
			res = getDao().update(this);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		if(predictions!=null) {
			for (Prediction prediction : predictions) {
				res += prediction.onLocalUpdate();
			}
		}
		
		try {
			getDao().refresh(this);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public int onLocalDelete() {
		int res = 0;
		
		for (Prediction prediction : getPredictions()) {
			//destroy only local, server take care of destroying on its side
			prediction.onLocalDelete();
		}
		
		for (ChatMessage chatMsg : getChatMessages()) {
			//destroy only local, server take care of destroying on its side
			chatMsg.onLocalDelete();
		}
		
		try {
			res = getDao().delete(this);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		return res;
	}

	public Boolean setJson(JSONObject jsonBet) {
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

		if (jsonBet == null)
			return null;

		super.setJson(jsonBet);

		User owner = null;
		try {
			owner = User.getUserLocalOrRemoteInner(jsonBet.getString("user_id"));
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		if (owner == null)
			return false;

		setOwner(owner);

		try {
			setReward(jsonBet.getString("reward"));
		} catch (JSONException e1) {
		}

		try {
			setTopicCustom(jsonBet.getString("subject"));
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
		
		try {
			String topicId = jsonBet.getString("topic_id");
			if(topicId!=null){
				setTopic(Topic.get(topicId));
				setCategory(getTopic().getCategory());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray jsonPredictions = null;
		try {
			jsonPredictions = jsonBet.getJSONArray("predictions");
		} catch (JSONException e) {
			e.printStackTrace();
			return false; // must have at list one prediction
		}

		if (jsonPredictions == null)
			return false; // have to be at least one

		for (int j = 0; j < jsonPredictions.length(); j++) {
			JSONObject jsonPrediction;

			try {
				jsonPrediction = jsonPredictions.getJSONObject(j);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}
			
			Prediction tmpPrediction = null;
			if(predictions!=null && predictions.size()>0) {
				for (Prediction prediction : predictions) {
					String predictionId = jsonPrediction.optString("id");
					if(predictionId!=null && prediction.getId().equals(predictionId)) {
						tmpPrediction = prediction;
						continue;
					}
				}
			}

			if (tmpPrediction == null) {
				tmpPrediction = new Prediction();
			}

			if (!tmpPrediction.setJson(jsonPrediction))
				continue;

			tmpPrediction.setBet(this);
			
			tmpPrediction.setServerCreated(true);
			tmpPrediction.setServerUpdated(true);

			try {
				tmpPrediction.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			
			if(tmpPrediction.getUser().getId().equals(owner.getId())) {
				setOwnerPrediction(tmpPrediction);
			}

		}
		
		try {
			if(predictions!=null)
				predictions.refreshCollection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		JSONArray jsonChatMessages = null;
		try {
			jsonChatMessages = jsonBet.getJSONArray("chat_messages");
		} catch (JSONException e) {
			e.printStackTrace();
			return true; // must have at list one prediction
		}

		if (jsonChatMessages == null)
			return true;

		for (int j = 0; j < jsonChatMessages.length(); j++) {
			JSONObject jsonChatMessage;

			try {
				jsonChatMessage = jsonChatMessages.getJSONObject(j);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}

			ChatMessage tmpChatMessage = null;
			if(chatMessages!=null && predictions.size()>0) {
				for (ChatMessage chatMessage : chatMessages) {
					String chatMessageId = jsonChatMessage.optString("id");
					if(chatMessageId!=null && chatMessage.getId().equals(chatMessageId)) {
						tmpChatMessage = chatMessage;
						continue;
					}
				}
			}

			if (tmpChatMessage == null) {
				tmpChatMessage = new ChatMessage();
			}

			if (!tmpChatMessage.setJson(jsonChatMessage))
				continue;

			User user = null;
			try {
				user = User.getUserLocalOrRemoteInner(jsonChatMessage
						.getString("user_id"));
			} catch (JSONException e2) {
				e2.printStackTrace();
				continue;
			}

			if (user == null)
				continue;

			tmpChatMessage.setUser(user);
			tmpChatMessage.setBet(this);

			try {
				tmpChatMessage.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

		}
		
		try {
			if(chatMessages!=null)
				chatMessages.refreshCollection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return true;
	}

	public JSONObject toJson() {
		if(getOwner()==null)
			return null;
		
		JSONObject jsonRoot = new JSONObject();
		
		JSONObject jsonBetContent = new JSONObject();

		try {
			jsonBetContent.put("id", getId());
			jsonBetContent.put("user_id", getOwner().getId());
			jsonBetContent.put("subject", getTopicCustom());
			jsonBetContent.put("reward", getReward().getName());
			jsonBetContent.put("topic_id", getTopic().getId());
			if (getDueDate() != null)
				jsonBetContent.put("due_date", getDueDate().toString());
			if (getState() != null)
				jsonBetContent.put("state", getState());
			
			jsonRoot.put("bet", jsonBetContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}

		try {
			// for each prediction of bet, create it and its user if missing
			if(getPredictions()!=null) {
				JSONArray jsonPredictions = new JSONArray();
				for (Prediction prediction : getPredictions()) {
					JSONObject jsonPrediction = new JSONObject();
	
					jsonPrediction.put("id", prediction.getId());
					jsonPrediction.put("bet_id", prediction.getBet().getId());
					jsonPrediction.put("user_id", prediction.getUser().getId());
					jsonPrediction.put("prediction", prediction.getPrediction());
					if (prediction.getResult()!=null)
						jsonPrediction.put("result", prediction.getResult());
					jsonPredictions.put(jsonPrediction);
				}
	
				jsonRoot.put("predictions", jsonPredictions);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		// no need to send add chat messages here, they are sent one by one

		return jsonRoot;
	}

}
