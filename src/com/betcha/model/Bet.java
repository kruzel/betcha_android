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
import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;

import com.betcha.model.cache.IModelListener;
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
public class Bet extends ModelCache<Bet, Integer> {

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user; // owner
	@DatabaseField
	private String category;
	@DatabaseField
	private String subject;
	@DatabaseField
	private String reward; // benefit
	@DatabaseField
	private DateTime date;
	@DatabaseField
	private DateTime dueDate;
	@DatabaseField
	private String state; // open/due/closed
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Prediction ownerPrediction;
	@ForeignCollectionField(eager = false)
	private ForeignCollection<Prediction>  predictions;
	@ForeignCollectionField(eager = false)
	private ForeignCollection<ChatMessage>  chatMessages;
 
	private List<User> participants;
	

	// non persistent
	public static final String STATE_OPEN = "open";
	public static final String STATE_DUE = "due";
	public static final String STATE_CLOSED = "closed";

	// private static GetUserBetsTask getUserBetsTask;
	private static BetRestClient betClient;
	private static Dao<Bet, Integer> dao;
	private static SyncTask syncThread;

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
		if (betClient == null)
			betClient = new BetRestClient();

		return betClient;
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	// non persistent
	public Prediction getOwnerPrediction() {
		return ownerPrediction;
	}

	public void setOwnerPrediction(Prediction ownerPrediction) {
		this.ownerPrediction = ownerPrediction;
		this.ownerPrediction.setBet(this);
	}

	public List<Prediction> getPredictions() {
		List<Prediction> list = new ArrayList<Prediction>(predictions);
		return list;
	}
	
	public List<ChatMessage> getChatMessages() {
		List<ChatMessage> list = new ArrayList<ChatMessage>(chatMessages);
		return list;
	}

	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Bet, Integer> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Bet.class);
		}
		return dao;
	}

	@Override
	protected Dao<Bet, Integer> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Bet.class);
		}
		return dao;
	}

	public int onRestCreate() {
		int res = 0;
		
		if(getPredictions()!=null) {
			for (Prediction prediction : getPredictions()) {
				if(prediction.getUser().onRestGet()==0) {
					if(prediction.getUser().restCreateUserAccount()==0)
						continue;
				} 
			}
		}
		
		if (getBetClient().create(this) != null)
			res = 1;

		return res;
	}

	public int onRestUpdate() {
		getBetClient().update(this);
		return 1;
	}

	public int onRestDelete() {
		getBetClient().delete(getId());
		return 1;
	}

	public int onRestSync() {
		int res = 0;
		if (!isServerUpdated()) {
			getBetClient().updateOrCreate(this);
		}

		return res;
	}

	@Override
	public int onRestGet() {
		Bet tmpBet = null;
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("id", getId());
			if (bets == null || bets.size() == 0)
				return 0;
			tmpBet = bets.get(0);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return 0;
		}

		if (tmpBet == null) {
			tmpBet = new Bet();
		}

		BetRestClient betClient = new BetRestClient();
		JSONObject jsonBet = null;
		try {
			jsonBet = betClient.show(getId());
		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}
		if (jsonBet == null)
			return 0;

		if (tmpBet.setJson(jsonBet)) {
			try {
				tmpBet.createOrUpdateLocal();
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
		JSONArray jsonBets = null;
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

		for (int i = 0; i < jsonBets.length(); i++) {
			JSONObject jsonBet;

			try {
				jsonBet = jsonBets.getJSONObject(i);
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

	public static void syncAllWithServer(IModelListener modelListener) {
		if (syncThread != null
				&& (syncThread.getStatus() == Status.RUNNING || syncThread
						.getStatus() == Status.PENDING))
			return;

		syncThread = new SyncTask();
		syncThread.setListener(modelListener);
		syncThread.execute();

	}

	private static class SyncTask extends AsyncTask<Void, Void, Boolean> {
		private IModelListener modelListener;

		public void setListener(IModelListener modelListener) {
			this.modelListener = modelListener;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// get all updates from server (bets and their predictions and chat_messages
			// TODO - use the show all update for current user
			Bet.getAllUpdatesForCurUser(Bet.getLastUpdateFromServer());

			// push all bets that not yet pushed to server
			
			//TODO change to one call with all updates in one json
			List<Bet> bets = null;
			try {
				bets = Bet.getModelDao().queryForEq("server_updated", false);
				if (bets == null || bets.size() == 0) {
					return true;
				}
			} catch (SQLException e1) {
				return true;
			}

			for (Bet bet : bets) {
				if (!bet.isServerUpdated()) {
					bet.onRestSync();
					bet.setServerUpdated(true);
					try {
						bet.getDao().update(bet);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				List<Prediction> predictions = null;
				try {
					predictions = Prediction.getModelDao().queryForEq(
							"server_updated", false);
					if (predictions == null || predictions.size() == 0) {
						continue;
					}
				} catch (SQLException e1) {
					continue;
				}

				for (Prediction prediction : predictions) {
					if (!prediction.isServerUpdated()) {
						if(prediction.getId()==null) {
							prediction.onRestCreate();
							prediction.setServerCreated(true);
							prediction.setServerUpdated(true);
							try {
								prediction.getDao().update(prediction);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} else {
							prediction.onRestUpdate(); 
							prediction.setServerUpdated(true);
							try {
								prediction.getDao().update(prediction);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						} 
					}
				}
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (modelListener != null)
				modelListener.onGetComplete(Bet.class, result);
			super.onPostExecute(result);
		}

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
		
		ownerPrediction.onLocalCreate();
		
		for (User participant : participants) {
			if(participant.getId()==null) //new friend
				participant.onLocalCreate(); //locally
			
			Prediction prediction = new Prediction();
			prediction.setUser(participant);
			prediction.setBet(this);
			prediction.setPrediction("");
			prediction.setSendInvite(true);
			if(prediction.onLocalCreate()!=0) {
				res += 1;
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
	public int onLocalUpdate() {
		setUpdated_at(new DateTime());

		int res = 0;
		try {
			res = getDao().update(this);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		for (Prediction prediction : predictions) {
			res += prediction.onLocalUpdate();
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
			setSubject(jsonBet.getString("subject"));
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
			try {
				List<Prediction> tmpPredictions = Prediction.getModelDao()
						.queryForEq("id", jsonPrediction.getString("id"));
				if (tmpPredictions != null && tmpPredictions.size() > 0) {
					tmpPrediction = tmpPredictions.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (tmpPrediction == null) {
				tmpPrediction = new Prediction();
			}

			if (!tmpPrediction.setJson(jsonPrediction))
				continue;

			tmpPrediction.setBet(this);

			try {
				tmpPrediction.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

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
			try {
				List<ChatMessage> tmpChatMessages = ChatMessage.getModelDao()
						.queryForEq("id", jsonChatMessage.getString("id"));
				if (tmpChatMessages != null && tmpChatMessages.size() > 0) {
					tmpChatMessage = tmpChatMessages.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
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
			jsonBetContent.put("subject", getSubject());
			jsonBetContent.put("reward", getReward());
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

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		// no need to send add chat messages here, they are sent one by one

		return jsonRoot;
	}

}
