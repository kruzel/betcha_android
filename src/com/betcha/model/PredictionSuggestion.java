package com.betcha.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

public class PredictionSuggestion {
	private String id;
	private Topic topic;
	private String name;
	private Bitmap image;
	
	private static Map<String, PredictionSuggestion> suggestionList;
	
	public void create() {
		if(suggestionList==null) {
			suggestionList = new HashMap<String, PredictionSuggestion>();
		}
		
		suggestionList.put(getId(), this);		
	}
	
	public static PredictionSuggestion get(String id) {
		return suggestionList.get(id);
	}
	
	public static List<PredictionSuggestion> getForTopic(Context context, String topicId) {
		
		if(suggestionList==null) {
			return null;
		}
		
		List<PredictionSuggestion> newSuggestionsList = new ArrayList<PredictionSuggestion>();
				
		for (Map.Entry<String, PredictionSuggestion> suggestionPair : suggestionList.entrySet()) {
			if(suggestionPair.getValue().getTopic().getId().equals(topicId))
				newSuggestionsList.add(suggestionPair.getValue());
		}
		
		return newSuggestionsList;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Topic getTopic() {
		return topic;
	}
	public void setTopic(Topic topic) {
		this.topic = topic;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	
	
}
