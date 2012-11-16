package com.betcha.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.betcha.R;

public class Category {
	private String id;
	private String group;
	private String name;
	private String description;
	private Bitmap image;
	private Collection<Topic> topics;
	
	private static Map<String, Category> categoriesList;
	
	public static List<Category> getCategories(Context context, String categoryGroup) {
		
		if(categoriesList==null) {
			return null;
		}
		
		List<Category> tmpCategoriesList = new ArrayList<Category>();
				
		for (Map.Entry<String, Category> categoryPair : categoriesList.entrySet()) {
			if(categoryPair.getValue().getGroup().equals(categoryGroup))
				tmpCategoriesList.add(categoryPair.getValue());
		}
		
		return tmpCategoriesList;
	}
	
	public void create() {
		if(categoriesList==null) {
			categoriesList = new HashMap<String, Category>();
		}
		
		categoriesList.put(getId(), this);		
	}
	
	public static Category get(String id) {
		return categoriesList.get(id);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String categoryGroup) {
		this.group = categoryGroup;
	}
	public String getName() {
		return name;
	}
	public void setName(String category) {
		this.name = category;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public Collection<Topic> getTopics() {
		return topics;
	}
	public void setTopics(Collection<Topic> topics) {
		this.topics = topics;
	}
}
