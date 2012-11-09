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
	private int drawable_id;
	
	private static Map<String, Reward> rewardsList;
	
	public static void init(Context context) {
		//TODO load categories from model coming from server
		
		rewardsList = new HashMap<String, Reward>();
		Reward reward = null;
				
		reward = new Reward();
		reward.setId("Drink");
		reward.setGroup("Drink");
		reward.setName("Drink");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_beer));
		reward.setDrawable_id(R.drawable.stake_beer);
		rewardsList.put(reward.getId(),reward);
		
		reward = new Reward();
		reward.setId("Lunch");
		reward.setGroup("Food");
		reward.setName("Lunch");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
		reward.setDrawable_id(R.drawable.stake_meal);
		rewardsList.put(reward.getId(),reward);
		
		reward = new Reward();
		reward.setId("Movie");
		reward.setGroup("Fun");
		reward.setName("Movie");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
		reward.setDrawable_id(R.drawable.stake_movie);
		rewardsList.put(reward.getId(),reward);
		
		reward = new Reward();
		reward.setId("Twist");
		reward.setGroup("Twist");
		reward.setName("Twist");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
		reward.setDrawable_id(R.drawable.stake_twist);
		rewardsList.put(reward.getId(),reward);
		
		reward = new Reward();
		reward.setId("Groupon");
		reward.setGroup("Coupons");
		reward.setName("Groupon");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
		reward.setDrawable_id(R.drawable.stake_groupon);
		rewardsList.put(reward.getId(),reward);
		
		reward = new Reward();
		reward.setId("Coins");
		reward.setGroup("Virtual Currency");
		reward.setName("Coins");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
		reward.setDrawable_id(R.drawable.stake_coins);
		rewardsList.put(reward.getId(),reward);
		
		reward = new Reward();
		reward.setId("FB brag");
		reward.setGroup("Brag");
		reward.setName("FB brag");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
		reward.setDrawable_id(R.drawable.stake_facebook);
		rewardsList.put(reward.getId(),reward);
		
		reward = new Reward();
		reward.setId("Tweet");
		reward.setGroup("Brag");
		reward.setName("Tweet");
		reward.setDescription("....");
		//reward.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.stake_meal));
		reward.setDrawable_id(R.drawable.stake_tweeter);
		rewardsList.put(reward.getId(),reward);
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
	
	public static Reward get(String id) {
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
	public int getDrawable_id() {
		return drawable_id;
	}
	public void setDrawable_id(int resource_id) {
		this.drawable_id = resource_id;
	}
	
	public static String[] getIds() {
		String[] strArray = new String[rewardsList.size()];
		int i = 0;
		for (Reward reward : rewardsList.values()) {
			strArray[i] = reward.getId();
			i++;
		}
		return strArray;
	}
	
	public static String[] getNames() {
		String[] strArray = new String[rewardsList.size()];
		int i = 0;
		for (Reward reward : rewardsList.values()) {
			strArray[i] = reward.getName();
			i++;
		}
		return strArray;
	}

	public static int[] getDrawables() {
		int[] array = new int[rewardsList.size()];
		int i = 0;
		for (Reward reward : rewardsList.values()) {
			array[i] = reward.getDrawable_id();
			i++;
		}
		return array;
	}
}
