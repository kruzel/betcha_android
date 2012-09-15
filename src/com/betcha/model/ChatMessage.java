package com.betcha.model;

import java.sql.SQLException;

import org.joda.time.DateTime;
import org.json.JSONObject;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.ChatMessageRestClient;
import com.betcha.model.server.api.PredictionRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "chat_messages")
public class ChatMessage extends ModelCache<ChatMessage, Integer> {

	// id is generated by the database and set on the object automagically
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Bet bet;
	@DatabaseField
	private String message;
	
	private static Dao<ChatMessage,Integer> dao;
	private static ChatMessageRestClient chatMessagesRestClient;
	
	public ChatMessageRestClient getChatMessageRestClient() {
		if(chatMessagesRestClient==null)
			//nested url = bets/:bet_id/predictions
			chatMessagesRestClient = new ChatMessageRestClient(bet.getServer_id());
			
		return chatMessagesRestClient;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
	public static Dao<ChatMessage,Integer> getModelDao() throws SQLException  {
		if(dao==null){
			dao = getDbHelper().getDao(ChatMessage.class);
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
	
	public Boolean setJson(JSONObject json) {
		super.setJson(json);
		
		message = json.optString("message");
		
		return true;
	}
		
	@Override
	public int onRestCreate() {
		int res = 0;
		JSONObject json = null;
		
		json = getChatMessageRestClient().create(this);
		
		if(json!=null) {
			setServer_id(json.optInt("id", -1));
			try {
				res = updateLocal();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		return res;
	}

	@Override
	public int onRestUpdate() {
		getChatMessageRestClient().update(this, getServer_id());
		return 1;
	}

	@Override
	public int onRestGet() {
		getChatMessageRestClient().update(this, getServer_id());
		return 1;
	}

	@Override
	public int onRestDelete() {
		getChatMessageRestClient().delete(getServer_id());
		return 1;
	}

	@Override
	public int onRestGetWithDependents() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int onRestGetAllForCurUser() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int onRestSyncToServer() {
		// TODO Auto-generated method stub
		return 0;
	}

}
