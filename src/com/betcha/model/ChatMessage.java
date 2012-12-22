package com.betcha.model;

import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.ChatMessageRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "chat_messages")
public class ChatMessage extends ModelCache<ChatMessage, String> {

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Bet bet;
	@DatabaseField
	private String message;
	
	private static Dao<ChatMessage,String> dao;
	private static ChatMessageRestClient chatMessagesRestClient;
	
	public ChatMessageRestClient getChatMessageRestClient() {
		if(chatMessagesRestClient==null)
			//nested url = bets/:bet_id/predictions
			chatMessagesRestClient = new ChatMessageRestClient(bet.getId());
			
		return chatMessagesRestClient;
	}
	
	public HttpStatus getLastRestErrorCode() {
		return getChatMessageRestClient().getLastRestErrorCode();
	}
	
	public static ChatMessage get(String id) {
		ChatMessage chatMessage = null;
		try {
			chatMessage = getModelDao().queryForId(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return chatMessage;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Bet getBet() {
		return bet;
	}

	public void setBet(Bet bet) {
		this.bet = bet;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * static methods that must be implemented by derived class
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<ChatMessage,String> getModelDao() throws SQLException  {
		if(dao==null){
			dao = getDbHelper().getDao(ChatMessage.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
	
	@Override
	protected Dao<ChatMessage, String> getDao() throws SQLException {
		if(dao==null){
			dao = getDbHelper().getDao(ChatMessage.class);
			dao.setObjectCache(true);
		}
		return dao;
	}
		
	@Override
	public int onRestCreate() {
		int res = 0;
		
		JSONObject json = getChatMessageRestClient().create(this);
		
		if(json==null)
			return 0;
		
//		JSONArray jsonActivityEvents = null;
//		try {
//			jsonActivityEvents = json.getJSONArray("activity_events");
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		
//		if(jsonActivityEvents!=null) {
//			for (int j = 0; j < jsonActivityEvents.length(); j++) {
//				JSONObject jsonEvent;
//
//				try {
//					jsonEvent = jsonActivityEvents.getJSONObject(j);
//				} catch (JSONException e3) {
//					continue;
//				}
//				
//				if(jsonEvent!=null) {
//					ActivityFeedItem item = new ActivityFeedItem();
//					item.setJson(jsonEvent);
//					item.setServerCreated(true);
//					item.setServerUpdated(true);
//					item.onLocalCreate();
//				}
//			}
//		}
		
		return res;
	}

	@Override
	public int onRestUpdate() {
		getChatMessageRestClient().update(this, getId());
		return 1;
	}

	@Override
	public int onRestGet() {
		JSONObject jsonChatMessage = null;
		try {
			jsonChatMessage = getChatMessageRestClient().show(getId());
		} catch (RestClientException e) {
			e.printStackTrace();
			return 0;
		}
		if(jsonChatMessage==null)
			return 0;
		
		JSONArray jsonArray = null;
		try {
			jsonArray = jsonChatMessage.getJSONArray("chat_messages");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
		
		if(jsonArray==null)
			return 0;

		JSONObject jsonContent = null;
		try {
			jsonContent = jsonArray.getJSONObject(0);
		} catch (JSONException e1) {
		}
		
		if(jsonContent==null)
			return 0;
	
		setJson(jsonContent);
		
		try {
			createOrUpdateLocal();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		return 1;
	}

	@Override
	public int onRestDelete() {
		getChatMessageRestClient().delete(getId());
		return 1;
	}
	
	public Boolean setJson(JSONObject json) {
		super.setJson(json);
		
		message = json.optString("message");
		
		return true;
	}
	
	public JSONObject toJson() {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
//			jsonContent.put("type",chatMessage...ChatMessageRestClient..());
//			jsonContent.put("notification_sent",chatMessage.....());
			jsonContent.put("id",getId());
			jsonContent.put("message",getMessage());
			jsonContent.put("bet_id",getBet().getId());
			jsonContent.put("user_id",getUser().getId());
		
			try {
				jsonParent.put("chat_message", jsonContent);
			} catch (RestClientException e) {
				e.printStackTrace();
				return null;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		return jsonParent;
	}

}
