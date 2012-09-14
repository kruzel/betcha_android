package com.betcha.model.cache;

import org.json.JSONObject;


public interface IModel {
	//return the serverId, if failed -1
	abstract public int onRestCreate();
	
	//return the number of updated rows, if failed 0
	abstract public int onRestUpdate();
	
	abstract public int onRestGet();
		
	abstract public int onRestGetWithDependents();
	
	abstract public int onRestGetAllForCurUser();
	
	//return the number of updated rows, if failed 0
	abstract public int onRestDelete();
	
	//return the number of updated rows, if failed 0
	abstract public int onRestSyncToServer();
		
	abstract public void setServer_id(int serverId);
	
	abstract public int getServer_id();
	
	//get missing and create and update client changes on server 
	abstract public void setServerUpdated(Boolean synced);
	
	abstract public Boolean setJson(JSONObject json);
}
