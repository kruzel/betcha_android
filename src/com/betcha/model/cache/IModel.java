package com.betcha.model.cache;


public interface IModel {
	//return the serverId, if failed -1
	abstract public int onRestCreate();
	
	//return the number of updated rows, if failed 0
	abstract public int onRestUpdate();
	
	abstract public int onRestGet();
		
	abstract public int onRestGetWithDependents();
	
	//return the number of updated rows, if failed 0
	abstract public int onRestDelete();
	
	//return the number of updated rows, if failed 0
	abstract public int onRestSync();
		
	abstract public void setServer_id(int serverId);
	
	abstract public int getServer_id();
	
	//get missing and create and update client changes on server 
	abstract public void setSynced(Boolean synced);
}
