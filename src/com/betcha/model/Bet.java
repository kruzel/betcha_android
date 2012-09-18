package com.betcha.model;

import java.lang.Thread.State;
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
import android.util.Log;

import com.betcha.R;
import com.betcha.model.cache.IModelListener;
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
	private static Thread syncThread;
	
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
	
	/** inherited ModelCache methods */
	
	@Override
	public void initDao() {
		try {
			setDao(getModelDao());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int onRestCreate() {
		int res = 0;
		JSONObject json = null;
		json = getBetClient().create(this);
		if(json==null)
			return 0;
		
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
					User tmpParticipant = User.getAndCreateUserViaEmail(participant.getEmail());
					if(tmpParticipant==null) {
						if(participant.createUserAccount()==0) {
							continue;
						} else {
							participant = tmpParticipant;
						}
					} else {
						participant = tmpParticipant;
					}
				}
				
				Prediction prediction = new Prediction(this);
				prediction.setSendInvite(true);
				prediction.setUser(participant);
	        	prediction.setPrediction("");
	        	prediction.setMyAck(context.getString(R.string.pending));
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

	public int onRestSyncToServer() {
		int res = 0;
		if(!isServerUpdated()) {
			if(getServer_id()==-1) {
				res = onRestCreate();
			} else {
				res = onRestUpdate(); 
			}
		} 
		
		return res;
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
		
		Bet tmpBet = null;
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("server_id", getServer_id());
			if(bets==null || bets.size()==0)
				return 0;
			tmpBet = bets.get(0);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return 0;
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
			return 0;
		}
		if(jsonBet==null)
			return 0;
		
		if(tmpBet.setJson(jsonBet)) {
			try {
				tmpBet.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		JSONArray jsonPredictions = null;
		try {
			jsonPredictions = jsonBet.getJSONArray("predictions");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0; //must have at list one prediction
		}
		
		if(jsonPredictions==null)
			return 0;
		
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
				List<Prediction> tmpPredictions = Prediction.getModelDao().queryForEq("server_id", jsonPrediction.getInt("id"));
				if(tmpPredictions!=null && tmpPredictions.size()>0) { 
					tmpPrediction = tmpPredictions.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if(tmpPrediction==null) {
				tmpPrediction = new Prediction();
			}
							
			if(!tmpPrediction.setJson(jsonPrediction))
				continue;
			
			try {
				tmpPrediction.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
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
			if(lastUpdate==null)
				jsonBets = restClient.show_for_user(); //for logged in user
			else {
				jsonBets = restClient.show_updates_for_user(lastUpdate); //for logged in user
			}
				
		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		} 
		
		if(jsonBets==null)
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
				List<Bet> tmpBets = Bet.getModelDao().queryForEq("server_id", jsonBet.getInt("id"));
				if(tmpBets!=null && tmpBets.size()>0) { 
					tmpBet = tmpBets.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if(tmpBet==null) {
				tmpBet = new Bet();
			}
							
			if(!tmpBet.setJson(jsonBet))
				continue;
			
			try {
				tmpBet.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			
			JSONArray jsonPredictions = null;
			try {
				jsonPredictions = jsonBet.getJSONArray("predictions");
			} catch (JSONException e) {
				e.printStackTrace();
				continue; //must have at list one prediction
			}
			
			if(jsonPredictions==null)
				continue;
			
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
					List<Prediction> tmpPredictions = Prediction.getModelDao().queryForEq("server_id", jsonPrediction.getInt("id"));
					if(tmpPredictions!=null && tmpPredictions.size()>0) { 
						tmpPrediction = tmpPredictions.get(0);
					}
				} catch (SQLException e2) {
					e2.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if(tmpPrediction==null) {
					tmpPrediction = new Prediction();
				}
								
				if(!tmpPrediction.setJson(jsonPrediction))
					continue;
				
				tmpPrediction.setBet(tmpBet);
				
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
				continue; //must have at list one prediction
			}
			
			if(jsonChatMessages==null)
				continue;
			
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
					List<ChatMessage> tmpChatMessages = ChatMessage.getModelDao().queryForEq("server_id", jsonChatMessage.getInt("id"));
					if(tmpChatMessages!=null && tmpChatMessages.size()>0) { 
						tmpChatMessage = tmpChatMessages.get(0);
					}
				} catch (SQLException e2) {
					e2.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if(tmpChatMessage==null) {
					tmpChatMessage = new ChatMessage();
				}
								
				if(!tmpChatMessage.setJson(jsonChatMessage))
					continue;
				
				User user = null;
				try {
					user = User.getAndCreateUser(jsonChatMessage.getInt("user_id"));
				} catch (JSONException e2) {
					e2.printStackTrace();
					continue;
				}
				
				if(user==null)
					continue;
				
				tmpChatMessage.setUser(user);
				tmpChatMessage.setBet(tmpBet);
				
				try {
					tmpChatMessage.createOrUpdateLocal();
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
				
			}
			
			bets.add(tmpBet);
			
		}

		return bets.size(); 
	}
	
	public static Bet getAndCreateBet(int server_id) {	
		
		Bet tmpBet = null;
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("server_id", server_id);
			if(bets==null || bets.size()==0)
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
	
	public static void syncWithServer(IModelListener modelListener) {	
		if(syncThread!=null && syncThread.getState()==State.RUNNABLE)
			return;
		
		SyncTask task = new SyncTask();
		task.setListener(modelListener);
		task.execute();
		
	}
	
	public Boolean setJson(JSONObject jsonBet) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		if(jsonBet==null)
			return null;
		
		super.setJson(jsonBet);
		
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
			       					
		return true;
	}
	
	private static class SyncTask extends AsyncTask<Void, Void, Boolean> {
		private IModelListener modelListener;
				
		public void setListener(IModelListener modelListener) {
			this.modelListener = modelListener;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			//get all updates from server (bets and their predictions and chat_messages
			//TODO - use the show all update for current user
			Bet.getAllUpdatesForCurUser(Bet.getLastUpdateFromServer());
			
			// push all bets that not yet synced pushed to server
			List<Bet> bets = null;
			try {
				bets = Bet.getModelDao().queryForEq("server_updated", false);
				if(bets==null || bets.size()==0) {
					return true;
				}
			} catch (SQLException e1) {
				if(modelListener!=null)
					modelListener.onGetComplete(Bet.class, true);
				return true;
			}
			
			for (Bet bet : bets) {
				if(!bet.isServerUpdated()) {
					bet.onRestSyncToServer();
				}
				
				List<Prediction> predictions = null;
				try {
					predictions = Prediction.getModelDao().queryForEq("server_updated", false);
					if(predictions==null || predictions.size()==0) {
						continue;
					}
				} catch (SQLException e1) {
					continue;
				}
				
				for (Prediction prediction : predictions) {
					if(!prediction.isServerUpdated()) {
						prediction.onRestSyncToServer();
					}
				}
			}
			
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(modelListener!=null)
				modelListener.onGetComplete(Bet.class, result);
			super.onPostExecute(result);
		}
		
	};

}
