/**
 * 
 */
package com.betcha.model.cache;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.betcha.BetchaApp;
import com.betcha.ConnectivityReceiver;
import com.betcha.model.ActivityEvent.Type;
import com.betcha.model.cache.ModelCache.RestTask.RestMethod;
import com.betcha.model.server.api.RestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.field.DatabaseField;

/**
 * @author ofer
 *
 * @param <T>
 * @param <ID>
 */

//TODO move BaseDaoEnabled<T,ID> to member so create,update, delete, get will not be exposed
// 							developers should use modelCreate, modelUpdate, modelDelete, modelGet only

public abstract class ModelCache<T,ID> { //extends BaseDaoEnabled<T,ID>
	@DatabaseField(id = true, canBeNull = false)
	protected String id;
	@DatabaseField(defaultValue="false")
	protected Boolean server_created;
	@DatabaseField(defaultValue="false")
	protected Boolean server_updated;
	@DatabaseField
	protected RestMethod last_rest_call; //use this to know what server operation need to be completed
	@DatabaseField
	protected DateTime updated_at;
	@DatabaseField
	private DateTime created_at;
	
	private RestTask restTask;
	protected IModelListener listener;
	protected static Context context;
	
	public abstract HttpStatus getLastRestErrorCode();
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String genId() {
		UUID uuid = UUID.randomUUID();
		id = uuid.toString();
		return id;
	}

	public static void setContext(Context context) {
		ModelCache.context = context;
	}
	
	public static Context setContext() {
		return context;
	}

	protected Boolean authenticateCreate() {
		return true;
	}
	
	protected Boolean authenticateUpdate() {
		return true;
	}
	
	protected Boolean authenticateDelete() {
		return true;
	}
	
	protected Boolean authenticateSynch() {
		return true;
	}
	
	protected Boolean authenticateGet() {
		return true;
	}

	/**
	 * You'll need this in your class to cache the helper in the class.
	 */
	private static DatabaseHelper databaseHelper = null;
	
	public static DatabaseHelper getDbHelper() {
		return databaseHelper;
	}
	
	public static void setDbHelper(DatabaseHelper dbHelper) {
		databaseHelper = dbHelper;
	}

	public void setListener(IModelListener listener) {
		this.listener = listener;
	}
		
	public IModelListener getListener() {
		return listener;
	}
	
	public Boolean isTaskInProgress() {
		if(last_rest_call == RestMethod.CREATE && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return true;
		else
			return false;
	}
	
	public int create() {
		int res = onLocalCreate();
				
		setServerUpdated(false);
		if(authenticateCreate() && RestClient.GetToken()==null)
			return res;
		
		if(last_rest_call == RestMethod.CREATE && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.CREATE;
		
		onCreateActivityEvent();
		
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		restTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, RestMethod.CREATE);
		//restTask.execute(RestMethod.CREATE);
		
		return res;
	}

	public int update() {		
		// update local model
		int res =  onLocalUpdate();
				
		setServerUpdated(false);
		if(authenticateUpdate() && RestClient.GetToken()==null)
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.UPDATE;
		
		onCreateActivityEvent();
		
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		//restTask.execute(RestMethod.UPDATE);
		restTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,RestMethod.UPDATE);
		
		return res;
	}

	public int get() {
		int res = 0;
		
		if(authenticateGet() && RestClient.GetToken()==null)
			return -1;
		
		if(last_rest_call == RestMethod.GET && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.GET;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		//restTask.execute(RestMethod.GET);
		restTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,RestMethod.GET);
		
		return res;
	}

	public int delete() {
		int res =  onLocalDelete();
		
		setServerUpdated(false);
		if(authenticateDelete() && RestClient.GetToken()==null)
			return res;
		
		if(last_rest_call == RestMethod.DELETE && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.DELETE;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		//restTask.execute(RestMethod.DELETE);
		restTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,RestMethod.DELETE);
		
		return res;
	}
		
	public int getAllForCurUser() {		
		if(authenticateGet() && RestClient.GetToken()==null)
			return -1;
		
		if(last_rest_call == RestMethod.GET_FOR_CUR_USER && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return 1;
		
		// run task to update server
		last_rest_call = RestMethod.GET_FOR_CUR_USER;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		//restTask.execute(RestMethod.GET_FOR_CUR_USER);
		restTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,RestMethod.GET_FOR_CUR_USER);
		
		return 1;
	}

	public int createOrUpdateLocal() throws SQLException {
		CreateOrUpdateStatus status = getDao().createOrUpdate((T) this);
		return status.getNumLinesChanged();
	}
			
	public void setServerCreated(Boolean server_created) {
		this.server_created = server_created;
	}
	
	public Boolean isServerCreated() {
		return server_created!=null && server_created;
	}

	public void setServerUpdated(Boolean serverUpdated) {
		this.server_updated = serverUpdated;
	}

	public Boolean isServerUpdated() {
		return server_updated!=null && server_updated;
	}
	
	public DateTime getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(DateTime updated_at) {
		this.updated_at = updated_at;
	}

	public DateTime getCreated_at() {
		return created_at;
	}

	public void setCreated_at(DateTime created_at) {
		this.created_at = created_at;
	}

	public static void enableConnectivityReciever() {
		ComponentName receiver = new ComponentName(context, ConnectivityReceiver.class);

		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		        PackageManager.DONT_KILL_APP);
	}
	
	public static void disableConnectivityReciever() {
		ComponentName receiver = new ComponentName(context, ConnectivityReceiver.class);

		PackageManager pm = context.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		        PackageManager.DONT_KILL_APP);
	}
	
	///////////// helper methods ///////////////////////////////
	
	protected abstract Dao<T,ID> getDao() throws SQLException;
	
	public Boolean setJson(JSONObject json) {
		
		try {
			setId(json.getString("id"));
		} catch (JSONException e) {
			e.printStackTrace();
			return false; //must have id on server
		}
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		try {
			setUpdated_at(formatter.parseDateTime(json.getString("updated_at")));
		} catch (JSONException e1) {
		}
		
		try {
			setCreated_at(formatter.parseDateTime(json.getString("created_at")));
		} catch (JSONException e1) {
		}
		
		return true;
	}
	
	public JSONObject toJson() {
		// add this class members
		return null;
	}
	
	//////////// REST API operations ////////////////////////////////
	//return the number of updated rows, if failed 0
	abstract protected int onRestCreate();
	
	//return the number of updated rows, if failed 0
	abstract protected int onRestUpdate();
	
	abstract protected int onRestGet();
	
	//return the number of updated rows, if failed 0
	abstract protected int onRestDelete();
	
	//return the number of updated rows, if failed 0
	public int onRestSync() {
		int res = 0;
		res = onRestGet();
		
		if(!isServerUpdated()) {
			if(getId()==null) {
				res =+ onRestCreate();
			} else {
				res =+ onRestUpdate(); 
			}
		} 
		
		return res;
	}
	
	public int onRestGetAllForCurUser() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	//////////////// local model operations ////////////////////
	
	public int onLocalCreate() {
		if(getId()==null)
			genId();
		setCreated_at(new DateTime());
		setUpdated_at(new DateTime());
		
		int res = 0;
		try {
			res  = createOrUpdateLocal();
		} catch (SQLException e1) {
			e1.printStackTrace();
			return 0;
		}
		return res;
	}

	public int onLocalUpdate() {
		setUpdated_at(new DateTime());
		int res = 0;
		try {
			res = getDao().update((T) this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public int onLocalDelete() {
		setUpdated_at(new DateTime());
		
		int res = 0;
		try {
			res = getDao().delete((T) this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
	public T onLocalGet() {
		T t = null;
	
		try {
			List<T> listT = null;
			listT = getDao().queryForEq("id", getId());
			if(listT!=null && listT.size()>0) {
				t = listT.get(0);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		return t;
	}
	
	public void onCreateActivityEvent() {
		
	}
	
	/**
	 * background task that does all the rest calls
	 * @author ofer
	 *
	 */
	public static class RestTask extends  AsyncTask<RestMethod, Void, HttpStatus> {
		public enum RestMethod { CREATE, UPDATE, DELETE, GET, GET_FOR_CUR_USER }
		RestMethod currMethod;
		
		private ModelCache model;
		private IModelListener modelListener;
		private Class modelClass;

		public void setModelClass(Class clazz) {
			modelClass = clazz;
		}
		
		public void setModel(ModelCache model) {
			this.model = model;
		}

		public void setModelListener(IModelListener modelListener) {
			this.modelListener = modelListener;
		}

		protected HttpStatus doInBackground(RestMethod... params) {
			if (model==null){
				return HttpStatus.BAD_REQUEST;
			}
			
			currMethod = params[0];
			
			if(!RestClient.isOnline()) {
				ModelCache.enableConnectivityReciever();
				return HttpStatus.SERVICE_UNAVAILABLE;
			}
			
			if(BetchaApp.getInstance().getCurUser()!=null && ( RestClient.GetToken()==null || RestClient.GetToken().length()==0)) {
				if(BetchaApp.getInstance().getCurUser().restCreateToken()==0) 
					return HttpStatus.UNAUTHORIZED;
			}
			
			HttpStatus code = model.getLastRestErrorCode();
						
			switch (currMethod) {
			case CREATE:
				model.onRestCreate();
				if (model.getLastRestErrorCode()==HttpStatus.CREATED) {
					model.setServerCreated(true);
					model.setServerUpdated(true);
					model.onLocalUpdate();
				} else {
					Log.i("ModelCache.doInBackground()", "create failed");
				}
				break;
			case UPDATE:
				if(model.isServerCreated()) {
					model.onRestUpdate();
					if(model.getLastRestErrorCode()==HttpStatus.OK) {
						model.setServerUpdated(true);
						model.onLocalUpdate();
					}
				} else {
					model.onRestCreate();
					if (model.getLastRestErrorCode()==HttpStatus.CREATED) {
						model.setServerCreated(true);
						model.setServerUpdated(true);
						model.onLocalUpdate();
					} else {
						Log.i("ModelCache.doInBackground()", "create during update failed");
						//work around - retry
						model.onRestUpdate();
						if(model.getLastRestErrorCode()==HttpStatus.OK) {
							model.setServerCreated(true);
							model.setServerUpdated(true);
							model.onLocalUpdate();
						}
					}
				}	
				break;
			case DELETE:
				if(model.isServerCreated()) {
					model.onRestDelete();
					if(model.getLastRestErrorCode()==HttpStatus.OK) {
						model.setServerUpdated(true);
						model.onLocalUpdate();
					}
				} 
				break;
			case GET:
				model.onRestGet();
				if(model.getLastRestErrorCode()==HttpStatus.OK) {
					model.setServerUpdated(true);
					model.setServerCreated(true);
					model.onLocalUpdate();
				}
				break;
			case GET_FOR_CUR_USER:
				model.onRestGetAllForCurUser();
				if(model.getLastRestErrorCode()==HttpStatus.OK) {
					model.setServerUpdated(true);
					model.setServerCreated(true);
					model.onLocalUpdate();
				}
				break;
			default:
				break;
			}
			return model.getLastRestErrorCode();
		}
		
		@Override
		protected void onPostExecute(HttpStatus result) {
			if(modelListener!=null) {
				switch (currMethod) {
				case CREATE:
					modelListener.onCreateComplete(modelClass,result);
					break;
				case UPDATE:
					modelListener.onUpdateComplete(modelClass,result);
					break;
				case DELETE:
					modelListener.onDeleteComplete(modelClass,result);
					break;
				case GET:
					modelListener.onGetComplete(modelClass,result);
					break;
				case GET_FOR_CUR_USER:
					modelListener.onGetComplete(modelClass,result);
					break;
				default:
					break;
				}
			}
			
			super.onPostExecute(result);
		}
	}
	
}
