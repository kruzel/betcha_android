package com.betcha.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.betcha.R;

public class Reward {
	private String id;
	private String group;
	private String Name;
	private String description;
	private Bitmap image;
	
	private static Map<String, Reward> rewardsList;
	
	private static void init(Context context) {
		//TODO load categories from model coming from server
		
		rewardsList = new HashMap<String, Reward>();
		
		Reward reward1 = new Reward();
		reward1.setId("1");
		reward1.setGroup("Custom");
		reward1.setName("Custom");
		reward1.setDescription("Any reward you like");
		reward1.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		rewardsList.put(reward1.getId(), reward1);
		
		Reward reward2 = new Reward();
		reward2.setId("2");
		reward2.setGroup("Drink");
		reward2.setName("Beer");
		reward2.setDescription("....");
		reward2.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		rewardsList.put(reward2.getId(),reward2);
		
		Reward reward3 = new Reward();
		reward3.setId("3");
		reward3.setGroup("Sweet");
		reward3.setName("Ice Cream");
		reward3.setDescription("....");
		reward3.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
		rewardsList.put(reward3.getId(),reward3);	
	}
	
	public static List<Reward> getRewards(Context context, String rewardGroup) {
		
		if(rewardsList==null) {
			init(context);
		}
		
		List<Reward> tmpRewardsList = new ArrayList<Reward>();
		
		if(rewardGroup==null)
			tmpRewardsList.addAll(rewardsList.values());
		else {
			for (Reward reward : rewardsList.values()) {
				if(reward.getGroup().equals(rewardGroup))
					tmpRewardsList.add(reward);
			}
		}
		
		return tmpRewardsList;
	}
	
	public static Reward getReward(String id) {
		return rewardsList.get(id);
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
	public void setGroup(String RewardGroup) {
		this.group = RewardGroup;
	}
	public String getName() {
		return Name;
	}
	public void setName(String Reward) {
		this.Name = Reward;
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
