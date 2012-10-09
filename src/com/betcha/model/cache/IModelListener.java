package com.betcha.model.cache;

public interface IModelListener {
	public enum ErrorCode {
	    OK, ERR_INTERNAL, ERR_CONNECTIVITY, ERR_NOT_REGISTERED, ERR_UNAUTHOTISED, ERR_SERVER_ERROR  
	}
	
	public abstract void onCreateComplete(Class clazz, ErrorCode errorCode);
	
	public abstract void onUpdateComplete(Class clazz, ErrorCode errorCode);
	
	public abstract void onGetComplete(Class clazz, ErrorCode errorCode);
		
	public abstract void onDeleteComplete(Class clazz, ErrorCode errorCode);
	
	public abstract void onSyncComplete(Class clazz, ErrorCode errorCode);

}
