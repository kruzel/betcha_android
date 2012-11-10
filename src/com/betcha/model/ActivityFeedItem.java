package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Hours;
import org.joda.time.Seconds;

import android.util.Log;

import com.betcha.BetchaApp;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public class ActivityFeedItem {
	public enum Type { BET_CREATE, BET_UPDATE, PREDICTION_CREATE, PREDICTION_UPDATE, CHAT_CREATE, OTHER };
	
	private Object obj;
	private Type type;
		
	public Object getObj() {
		return obj;
	}

	public Type getType() {
		return type;
	}

	private static List<ActivityFeedItem> activities = new ArrayList<ActivityFeedItem>();
	
	public static List<ActivityFeedItem> getActivities() {
		activities.clear();
		
		List<Bet> bets = null;
		List<Prediction> predictions = null;
		List<ChatMessage> chatMessages = null;
		
		//TODO limit to latest 50 activities
		try {
			QueryBuilder<Bet, String> betsQueryBuilder = Bet.getModelDao().queryBuilder();
			QueryBuilder<Prediction,String> predictionQueryBuilder = Prediction.getModelDao().queryBuilder();
			QueryBuilder<ChatMessage,String> chatMessageQueryBuilder = ChatMessage.getModelDao().queryBuilder();
			
			PreparedQuery<Bet> betsPreparedQuery = null;
			betsQueryBuilder = Bet.getModelDao().queryBuilder();
			betsQueryBuilder.orderBy("created_at", false);
			betsPreparedQuery = betsQueryBuilder.prepare();
			bets = Bet.getModelDao().query(betsPreparedQuery);
			
			PreparedQuery<Prediction> predictionsPreparedQuery = null;
			predictionQueryBuilder = Prediction.getModelDao().queryBuilder();
			predictionQueryBuilder.orderBy("updated_at", false);
			predictionsPreparedQuery = predictionQueryBuilder.prepare();
			predictions = Prediction.getModelDao().query(predictionsPreparedQuery);
			
			PreparedQuery<ChatMessage> chatMessagesPreparedQuery = null;
			chatMessageQueryBuilder = ChatMessage.getModelDao().queryBuilder();
			chatMessageQueryBuilder.orderBy("updated_at", false);
			chatMessagesPreparedQuery = chatMessageQueryBuilder.prepare();
			chatMessages = ChatMessage.getModelDao().query(chatMessagesPreparedQuery);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		
		if(bets==null)
			return activities;
		
		Iterator<Bet> betItr = bets.iterator();
		Iterator<Prediction> predictionItr = predictions.iterator();
		Iterator<ChatMessage> chatMessageItr = chatMessages.iterator();
		
		Bet bet = null;
		Prediction prediction = null;
		ChatMessage chatMessage = null;
		ActivityFeedItem item = null;
		
		while(betItr.hasNext() || predictionItr.hasNext() || chatMessageItr.hasNext()) {
			//Log.i("getActivities()", "bet: " + bet.getCreated_at().toString() + " | prediction: " + prediction.getUpdated_at().toString() + " | chat: " + chatMessage.getUpdated_at().toString() );
			
			if(betItr.hasNext()) { 
				bet = betItr.next();
				if((prediction==null && chatMessage==null) ||
					 (!predictionItr.hasNext() && !chatMessageItr.hasNext()) || 
					 (prediction!=null && !bet.getCreated_at().isBefore(prediction.getUpdated_at()) && !chatMessageItr.hasNext()) ||
					 (chatMessage!=null && !bet.getCreated_at().isBefore(chatMessage.getUpdated_at()) && !predictionItr.hasNext()) ||
					 (chatMessage!=null && prediction!=null && !bet.getCreated_at().isBefore(prediction.getUpdated_at()) && !bet.getCreated_at().isBefore(chatMessage.getUpdated_at()))) {
					
					item = new ActivityFeedItem();
					item.obj = bet;
					item.type = Type.BET_CREATE;
				 }
			} else if(predictionItr.hasNext()) {
				prediction = predictionItr.next();
				if ((bet==null && chatMessage==null) ||
				 (!betItr.hasNext() && !chatMessageItr.hasNext()) || 
				 (bet!=null && !prediction.getUpdated_at().isBefore(bet.getCreated_at()) && !chatMessageItr.hasNext()) ||
				 (chatMessage!=null && !prediction.getUpdated_at().isBefore(chatMessage.getUpdated_at()) && !betItr.hasNext()) ||
				 (chatMessage!=null && bet!=null && !prediction.getUpdated_at().isBefore(bet.getCreated_at()) && !prediction.getUpdated_at().isBefore(chatMessage.getUpdated_at()))) {
				
					item = new ActivityFeedItem();
					item.obj = prediction;
					if(Seconds.secondsBetween(prediction.getCreated_at(), prediction.getUpdated_at()).isLessThan(Seconds.seconds(10)) || prediction.getPrediction().length()==0) {
						item.type = Type.PREDICTION_CREATE;
					} else {
						item.type = Type.PREDICTION_UPDATE;
					}
					
				}
			} else if(chatMessageItr.hasNext()){
				chatMessage = chatMessageItr.next();
				item = new ActivityFeedItem();
				item.obj = chatMessage;
				item.type = Type.CHAT_CREATE;
			} 
			
			if(item.type!=Type.PREDICTION_CREATE)
				activities.add(item);
		}	
		
		return activities;
	}
	
	public void remove(String objId) {
		
	}
	
	public String getText() {
		Bet bet = null;
		Prediction prediction = null;
		ChatMessage chatMessage = null;
		User curUser = BetchaApp.getInstance().getCurUser();
		
		switch (type) {
			case BET_CREATE:
				bet = (Bet) obj;
				if(bet.getOwner().getId().equals(curUser.getId()))
					return "You have created a bet \"" + bet.getSubject() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
				else
					return bet.getOwner().getName() + " has invited you to bet \"" + bet.getSubject() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
			case BET_UPDATE:
				bet = (Bet) obj;
				if(bet.getOwner().getId().equals(curUser.getId()))
					return "You have updated the bet to \"" + bet.getSubject() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
				else
					return bet.getOwner().getName() + " has updated the bet to \"" + bet.getSubject() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
			case PREDICTION_CREATE:
				prediction = (Prediction) obj;
				if(prediction.getBet().getOwner().getId().equals(curUser.getId()))
					return "You have added " + prediction.getUser().getName() + " as a new participant";
				else
					return prediction.getBet().getOwner().getName() + " has added " + prediction.getUser().getName() + " as a new participant";
			case PREDICTION_UPDATE:
				prediction = (Prediction) obj;
				if(prediction.getUser().getId().equals(curUser.getId()))
					return "You have updated your bet to \"" + prediction.getPrediction() + "\"";
				else
					return prediction.getUser().getName() + " has updated his bet to \"" + prediction.getPrediction() + "\"";
			case CHAT_CREATE:
				chatMessage = (ChatMessage) obj;
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
		switch (type) {
		case BET_CREATE:
		case BET_UPDATE:
			return (Bet) obj;
		case PREDICTION_CREATE:
		case PREDICTION_UPDATE:
			Prediction prediction = (Prediction) obj;
			return prediction.getBet();
		case CHAT_CREATE:
			ChatMessage chatMessage = (ChatMessage) obj;
			return chatMessage.getBet();
		case OTHER:
		default:
			return null;
	}
	}
}
