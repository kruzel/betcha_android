package com.betcha.model.cache;

public interface IModelListener {
	public abstract void onCreateComplete(Class clazz, Boolean success);
	
	public abstract void onUpdateComplete(Class clazz, Boolean success);
	
	public abstract void onGetComplete(Class clazz, Boolean success);
	
	public abstract void onGetWithDependentsComplete(Class clazz, Boolean success);
	
	public abstract void onDeleteComplete(Class clazz, Boolean success);
	
	public abstract void onSyncComplete(Class clazz, Boolean success);

}
