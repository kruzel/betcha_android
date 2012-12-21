package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import com.betcha.BetchaApp;
import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.ActivityEventRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class ActivityFeedItem  extends ModelCache<ActivityFeedItem, String> {
	public enum Type { BET_CREATE, BET_UPDATE, PREDICTION_CREATE, PREDICTION_UPDATE, CHAT_CREATE, OTHER };
	
	@DatabaseField
	private String object_id;
	@DatabaseField
	private int type;
	@DatabaseField
	private String description;
				
	public Object getObj() {
		Object obj = null;
		switch (getType()) {
		case BET_CREATE:
		case BET_UPDATE:
			obj = Bet.get(object_id);
			break;
		case PREDICTION_CREATE:
		case PREDICTION_UPDATE:
			obj = Prediction.get(object_id);
			break;
		case CHAT_CREATE:
			obj = ChatMessage.get(object_id);
		default:
			break;
		}
		
		return obj;
	}

	public void setObj(String objId) {
		this.object_id = objId;
	}

	public Type getType() {
		return Type.values()[type];
	}

	public void setType(Type type) {
		switch(type) {
		case BET_CREATE:
			this.type = 0;
			break;
		case BET_UPDATE:
			this.type = 1;
			break;
		case PREDICTION_CREATE:
			this.type = 2;
			break;
		case PREDICTION_UPDATE:
			this.type = 3;
			break;
		case CHAT_CREATE:
			this.type = 4;
			break;
		case OTHER:
			this.type = 5;
			break;
		}
		
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static List<ActivityFeedItem> getActivities() {
		List<ActivityFeedItem> activities = null;
		
		try {
			activities = ActivityFeedItem.getModelDao().queryBuilder()
					.orderBy("updated_at", false).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return activities;
	}
	
	public String getText() {
		Bet bet = null;
		Prediction prediction = null;
		ChatMessage chatMessage = null;
		User curUser = BetchaApp.getInstance().getCurUser();
		
		switch (getType()) {
			case BET_CREATE:
				bet = Bet.get(object_id);
				if(bet.getOwner().getId().equals(curUser.getId()))
					return "You have created a bet \"" + bet.getTopicCustom() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
				else
					return bet.getOwner().getName() + " has invited you to bet \"" + bet.getTopicCustom() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
			case BET_UPDATE:
				bet = Bet.get(object_id);
				if(bet.getOwner().getId().equals(curUser.getId()))
					return "You have updated the bet to \"" + bet.getTopicCustom() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
				else
					return bet.getOwner().getName() + " has updated the bet to \"" + bet.getTopicCustom() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
			case PREDICTION_CREATE:
				prediction = Prediction.get(object_id);
				if(prediction.getBet().getOwner().getId().equals(curUser.getId()))
					return "You have added " + prediction.getUser().getName() + " as a new participant";
				else
					return prediction.getBet().getOwner().getName() + " has added " + prediction.getUser().getName() + " as a new participant";
			case PREDICTION_UPDATE:
				prediction = Prediction.get(object_id);
				if(prediction.getUser().getId().equals(curUser.getId()))
					return "You have updated your bet to \"" + prediction.getPrediction() + "\"";
				else
					return prediction.getUser().getName() + " has updated his bet to \"" + prediction.getPrediction() + "\"";
			case CHAT_CREATE:
				chatMessage = ChatMessage.get(object_id);
				if(chatMessage.getUser().getId().equals(curUser.getId()))
					return "You have sent a chat message, \"" + chatMessage.getMessage() + "\"";
				else
					return chatMessage.getUser().getName() + " has sent a chat message, \"" + chatMessage.getMessage() + "\"";
			case OTHER:
			default:
				return "";
		}
	}
	
	public Bet getBet() {
		switch (getType()) {
		case BET_CREATE:
		case BET_UPDATE:
			return Bet.get(object_id);
		case PREDICTION_CREATE:
		case PREDICTION_UPDATE:
			Prediction prediction = Prediction.get(object_id);
			return prediction.getBet();
		case CHAT_CREATE:
			ChatMessage chatMessage = ChatMessage.get(object_id);
			return chatMessage.getBet();
		case OTHER:
		default:
			return null;
		}
	}
	
	/**
	 * static methods that must be implemented by derived class
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	
	private static Dao<ActivityFeedItem, String> dao;
	private ActivityEventRestClient restClient;
	
	public static Dao<ActivityFeedItem, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(ActivityFeedItem.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<ActivityFeedItem, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(ActivityFeedItem.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	public ActivityEventRestClient getRestClient() {
		if (restClient == null)
			restClient = new ActivityEventRestClient();

		return restClient;
	}

	@Override
	public HttpStatus getLastRestErrorCode() {
		return getRestClient().getLastRestErrorCode();
	}

	@Override
	protected int onRestCreate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int onRestUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int onRestGet() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int onRestDelete() {
		// TODO Auto-generated method stub
		return 0;
	}
		
	@Override
	public int onRestGetAllForCurUser() {
		return ActivityFeedItem.getAllUpdatesForCurUser(null);
	}

	public static int getAllUpdatesForCurUser(DateTime lastUpdate) {
		
		List<ActivityFeedItem> events = new ArrayList<ActivityFeedItem>();

		ActivityEventRestClient restClient = new ActivityEventRestClient();
		JSONObject jsonEvents = null;
		try {
			if(lastUpdate==null)
				jsonEvents = restClient.show_for_user();
			else
				jsonEvents = restClient.show_updates_for_user(lastUpdate); 

		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}

		if (jsonEvents == null)
			return 0;
		
		JSONArray jsonEventsArray = null;
		try {
			jsonEventsArray = jsonEvents.getJSONArray("activity_events");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jsonEventsArray == null)
			return 0;

		for (int i = 0; i < jsonEventsArray.length(); i++) {
			JSONObject jsonEvent;

			try {
				jsonEvent = jsonEventsArray.getJSONObject(i);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}

			ActivityFeedItem tmpEvent = null;
			try {
				List<ActivityFeedItem> tmpEvents = ActivityFeedItem.getModelDao().queryForEq("id",jsonEvent.getString("id"));
				if (tmpEvents != null && tmpEvents.size() > 0) {
					tmpEvent = tmpEvents.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (tmpEvent == null) {
				tmpEvent = new ActivityFeedItem();
			}

			if (!tmpEvent.setJson(jsonEvent))
				continue;

			tmpEvent.setServerCreated(true);
			tmpEvent.setServerUpdated(true);
			
			try {
				tmpEvent.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

			events.add(tmpEvent);

		}

		return events.size();
	}
	
	public Boolean setJson(JSONObject json) {
		
		if (json == null)
			return null;

		super.setJson(json);
		
		String objectId = null;
		try {
			objectId = json.getString("object_id");
		} catch (JSONException e1) {
		}
		
		String tmpType = null;
		try {
			tmpType = json.getString("event_type");
		} catch (JSONException e1) {
		}
		
		setObj(objectId);
		
		if (tmpType.equals("bet")) {
			setType(Type.BET_CREATE);	
		} else if(tmpType.equals("bet_update")) {
			setType(Type.BET_UPDATE);
		} else if(tmpType.equals("prediction")) {
			setType(Type.PREDICTION_CREATE);
		} else if(tmpType.equals("prediction_update")) {
			setType(Type.PREDICTION_UPDATE);
		} else if(tmpType.equals("chat")) {
			setType(Type.CHAT_CREATE);
		}
		
		try {
			setDescription(json.getString("description"));
		} catch (JSONException e1) {
		}
		
		return true;
	}
}
