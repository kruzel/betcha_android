package com.betcha.model.cache;

import org.springframework.http.HttpStatus;

public interface IModelListener {
		
	public abstract void onCreateComplete(Class clazz, HttpStatus errorCode);
	
	public abstract void onUpdateComplete(Class clazz, HttpStatus errorCode);
	
	public abstract void onGetComplete(Class clazz, HttpStatus errorCode);
		
	public abstract void onDeleteComplete(Class clazz, HttpStatus errorCode);
	
	public abstract void onSyncComplete(Class clazz, HttpStatus errorCode);

}
