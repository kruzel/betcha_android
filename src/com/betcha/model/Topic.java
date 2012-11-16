package com.betcha.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import android.content.Context;

public class Topic {
	private String id;
	private Category category;
	private DateTime startTime;
	private DateTime endtTime;
	private String location;
	private String name;
	private String dscription;
	private Collection<PredictionSuggestion> suggestions;
	
	private static Map<String, Topic> topicsList;
	
	public void create() {
		if(topicsList==null) {
			topicsList = new HashMap<String, Topic>();
		}
		
		topicsList.put(getId(), this);		
	}
	
	public static List<Topic> getForCategory(Context context, String catpgoryId) {
		
		if(topicsList==null) {
			return null;
		}
		
		List<Topic> newTopicsList = new ArrayList<Topic>();
				
		for (Map.Entry<String, Topic> topicPair : topicsList.entrySet()) {
			if(topicPair.getValue().getCategory().getId().equals(catpgoryId))
				newTopicsList.add(topicPair.getValue());
		}
		
		return newTopicsList;
	}
	
	public static Topic get(String id) {
		return topicsList.get(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public DateTime getEndtTime() {
		return endtTime;
	}

	public void setEndtTime(DateTime endtTime) {
		this.endtTime = endtTime;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDscription() {
		return dscription;
	}

	public void setDscription(String dscription) {
		this.dscription = dscription;
	}

	public Collection<PredictionSuggestion> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(Collection<PredictionSuggestion> options) {
		this.suggestions = options;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
