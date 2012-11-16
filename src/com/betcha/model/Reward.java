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
	
	public static List<Reward> getRewards(Context context, String rewardGroup) {
		
		if(rewardsList==null) {
			return null;
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
	
	public void create() {
		if(rewardsList==null)
			rewardsList = new HashMap<String, Reward>();
		
		rewardsList.put(getId(), this);
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
