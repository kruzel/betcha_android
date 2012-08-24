package com.betcha.model.cache;

import com.betcha.model.User;

public interface IModel {
	//return the serverId, if failed -1
	abstract public int onRestCreate();
	
	////return the number of updated rows, if failed 0
	abstract public int onRestUpdate();
	
	////return the number of updated rows, if failed 0
	abstract public int onRestDelete();
	
	////return the number of updated rows, if failed 0
	abstract public int onRestSync();
		
	abstract public void setServer_id(int serverId);
	
	abstract public int getServer_id();
	
	abstract public void setSynced(Boolean synced);
}
