package com.betcha.model.cache;

public interface IModelListener {
	public abstract void onCreateComplete(Boolean success);
	
	public abstract void onUpdateComplete(Boolean success);
	
	public abstract void onDeleteComplete(Boolean success);
	
	public abstract void onSyncComplete(Boolean success);

}
