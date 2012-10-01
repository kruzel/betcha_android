package com.betcha.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	
	private static Map<String, Category> categoriesList;
	
	private static void init(Context context) {
		//TODO load categories from model coming from server
		
		categoriesList = new HashMap<String, Category>();
		
		Category cat1 = new Category();
		cat1.setId("1");
		cat1.setGroup("Custom");
		cat1.setName("Custom");
		cat1.setDescription("Any bet you like");
		cat1.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		categoriesList.put(cat1.getId(), cat1);
		
		Category cat2 = new Category();
		cat2.setId("2");
		cat2.setGroup("Sport");
		cat2.setName("Footbal");
		cat2.setDescription("....");
		cat2.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		categoriesList.put(cat2.getId(),cat2);
		
		Category cat3 = new Category();
		cat3.setId("3");
		cat3.setGroup("Sport");
		cat3.setName("Soccer");
		cat3.setDescription("....");
		cat3.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		categoriesList.put(cat3.getId(),cat3);	
	}
	
	public static List<Category> getCategories(Context context, String categoryGroup) {
		
		if(categoriesList==null) {
			init(context);
		}
		
		List<Category> tmpCategoriesList = new ArrayList<Category>();
				
		for (Map.Entry<String, Category> categoryPair : categoriesList.entrySet()) {
			if(categoryPair.getValue().getGroup().equals(categoryGroup))
				tmpCategoriesList.add(categoryPair.getValue());
		}
		
		return tmpCategoriesList;
	}
	
	public static Category getCategory(String id) {
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

}
