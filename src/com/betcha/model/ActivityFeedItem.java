package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.Hours;
import org.joda.time.Seconds;

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
			betsQueryBuilder.orderBy("updated_at", false);
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
		
		if(betItr.hasNext())
			bet = betItr.next();
		if(predictionItr.hasNext())
			prediction = predictionItr.next();
		if(chatMessageItr.hasNext())
			chatMessage = chatMessageItr.next();
		
		ActivityFeedItem item = null;
		
		while(betItr.hasNext() || predictionItr.hasNext() || chatMessageItr.hasNext()) {
			
			if(betItr.hasNext() && 
				((prediction==null && chatMessage==null) ||
				 (!predictionItr.hasNext() && !chatMessageItr.hasNext()) || 
				 (prediction!=null && !bet.getUpdated_at().isBefore(prediction.getUpdated_at()) && !chatMessageItr.hasNext()) ||
				 (chatMessage!=null && !bet.getUpdated_at().isBefore(chatMessage.getUpdated_at()) && !predictionItr.hasNext()) ||
				 (!bet.getUpdated_at().isBefore(prediction.getUpdated_at()) && !bet.getUpdated_at().isBefore(chatMessage.getUpdated_at())))) {
				
				item = new ActivityFeedItem();
				item.obj = bet;
				if(Seconds.secondsBetween(bet.getCreated_at(), bet.getUpdated_at()).isLessThan(Seconds.seconds(10))) {
					item.type = Type.BET_CREATE;
				} else {
					//item.type = Type.BET_UPDATE;
					if(betItr.hasNext())
						bet = betItr.next();
					continue;
				}
				if(betItr.hasNext())
					bet = betItr.next();
			} else if(predictionItr.hasNext() &&
				((!betItr.hasNext() && !chatMessageItr.hasNext()) || 
				 (bet!=null && !prediction.getUpdated_at().isBefore(bet.getUpdated_at()) && !chatMessageItr.hasNext()) ||
				 (chatMessage!=null && !prediction.getUpdated_at().isBefore(chatMessage.getUpdated_at()) && !betItr.hasNext()) ||
				 (!prediction.getUpdated_at().isBefore(bet.getUpdated_at()) && !prediction.getUpdated_at().isBefore(chatMessage.getUpdated_at())))) {
				
				item = new ActivityFeedItem();
				item.obj = prediction;
				if(Seconds.secondsBetween(prediction.getCreated_at(), prediction.getUpdated_at()).isLessThan(Seconds.seconds(10)) || prediction.getPrediction().length()==0) {
					item.type = Type.PREDICTION_CREATE;
				} else {
					item.type = Type.PREDICTION_UPDATE;
				}
				if(predictionItr.hasNext())
					prediction = predictionItr.next();
			} else if(chatMessageItr.hasNext()){
				item = new ActivityFeedItem();
				item.obj = chatMessage;
				item.type = Type.CHAT_CREATE;
				if(chatMessageItr.hasNext())
					chatMessage = chatMessageItr.next();
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
		
		switch (type) {
			case BET_CREATE:
				bet = (Bet) obj;
				return bet.getOwner().getName() + " has invited you to bet \"" + bet.getSubject() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
			case BET_UPDATE:
				bet = (Bet) obj;
				return bet.getOwner().getName() + " has update the bet to \"" + bet.getSubject() + "\" winner wins a \"" + bet.getReward().getName() + "\"";
			case PREDICTION_CREATE:
				prediction = (Prediction) obj;
				return prediction.getBet().getOwner().getName() + " has added " + prediction.getUser().getName() + " as a new participant";
			case PREDICTION_UPDATE:
				prediction = (Prediction) obj;
				return prediction.getUser().getName() + " has updated his bet to \"" + prediction.getPrediction() + "\"";
			case CHAT_CREATE:
				chatMessage = (ChatMessage) obj;
				return chatMessage.getUser().getName() + " has send a chat message, \"" + chatMessage.getMessage() + "\"";
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
