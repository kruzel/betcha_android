/**
 * 
 */
package com.betcha.model.cache;

import java.sql.SQLException;

import org.joda.time.DateTime;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.betcha.model.cache.ModelCache.RestTask.RestMethod;
import com.betcha.model.server.api.RestClient;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;

/**
 * @author ofer
 *
 * @param <T>
 * @param <ID>
 */

public abstract class ModelCache<T,ID> extends BaseDaoEnabled<T,ID> implements IModel {
	@DatabaseField
	protected int server_id = -1;
	@DatabaseField
	protected Boolean serverUpdated = false;
	@DatabaseField
	protected RestMethod last_rest_call; //use this to know what server operation need to be completed
	@DatabaseField
	protected DateTime updated_at;
	
	private RestTask restTask;
	protected IModelListener listener;
	
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
	
	// TODO schedule the synch task for every period (configurable) 
	
	abstract public void initDao();

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
		
	/** BaseDaoEnabled overides
	 * @see com.j256.ormlite.misc.BaseDaoEnabled#create()
	 */
	@Override
	public int create() throws SQLException {
		initDao();
		
		// create on local model
		int res = createLocal();
		
		setServerUpdated(false);
		if(authenticateCreate() && RestClient.GetToken()==null)
			return res;
		
		if(last_rest_call == RestMethod.CREATE && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.CREATE;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		restTask.execute(RestMethod.CREATE);
		
		return res;
	}
	
	public int createLocal() throws SQLException {
		initDao();
		return super.create();
	}
	
	public int createOrUpdateLocal() throws SQLException {
		initDao();
		CreateOrUpdateStatus status = getDao().createOrUpdate((T) this);
		return status.getNumLinesChanged();
		//return super.create();
	}

	@Override
	public int delete() throws SQLException {
		initDao();
		
		// delete from local model
		int res =  deleteLocal();
		
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
		restTask.execute(RestMethod.DELETE);
		
		return res;
	}
	
	public int deleteLocal() throws SQLException {
		initDao();
		return super.delete();
	}

	@Override
	public int update() throws SQLException {
		initDao();
		// update local model
		int res =  updateLocal();
		
		setServerUpdated(false);
		if(authenticateUpdate() && RestClient.GetToken()==null)
			return res;
		
		Log.i("ModelCache.update()", getClass().getSimpleName());
		
		// run task to update server
		last_rest_call = RestMethod.UPDATE;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		restTask.execute(RestMethod.UPDATE);
		
		return res;
	}
	
	public int updateLocal() throws SQLException {
		initDao();
		return super.update();
	}
	
	public int get(int server_id) throws SQLException {	
		initDao();
		setServerUpdated(false);
		setServer_id(server_id);
		if(authenticateGet() && RestClient.GetToken()==null)
			return -1;
		
		if(last_rest_call == RestMethod.GET && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return 1;
		
		// run task to update server
		last_rest_call = RestMethod.GET;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		restTask.execute(RestMethod.GET);
		
		return 1;
	}
	
	public int getWithDependents(int server_id) throws SQLException {	
		initDao();
		setServerUpdated(false);
		setServer_id(server_id);
		if(authenticateGet() && RestClient.GetToken()==null)
			return -1;
		
		if(last_rest_call == RestMethod.GET_WITH_DEP && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return 1;
		
		// run task to update server
		last_rest_call = RestMethod.GET_WITH_DEP;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		restTask.execute(RestMethod.GET_WITH_DEP);
		
		return 1;
	}
	
	public int getAllForCurUser() {
		initDao();
		setServerUpdated(false);
		
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
		restTask.execute(RestMethod.GET_FOR_CUR_USER);
		
		return 1;
	}
		
	public int synch() throws SQLException {	
		initDao();
		setServerUpdated(false);
		if(authenticateCreate() && RestClient.GetToken()==null)
			return -1;
		
		if(last_rest_call == RestMethod.SYNC && restTask!=null && (restTask.getStatus()== Status.RUNNING || restTask.getStatus()== Status.PENDING))
			return 1;
		
		// run task to update server
		last_rest_call = RestMethod.SYNC;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.setModelClass(getClass());
		restTask.execute(RestMethod.SYNC);
		
		return 1;
	}

	public void setServer_id(int serverId) {
		this.server_id = serverId;
		setServerUpdated(true);
		
	}
	
	public int getServer_id() {
		return server_id;
	}

	public void setServerUpdated(Boolean serverUpdated) {
		this.serverUpdated = serverUpdated;
	}

	public Boolean isServerUpdated() {
		return serverUpdated;
	}



	/**
	 * background task that does all the rest calls
	 * @author ofer
	 *
	 */
	public static class RestTask extends  AsyncTask<RestMethod, Void, Boolean> {
		public enum RestMethod { CREATE, UPDATE, DELETE, SYNC, GET, GET_WITH_DEP, GET_FOR_CUR_USER }
		RestMethod currMethod;
		
		private IModel model;
		private IModelListener modelListener;
		private Class modelClass;

		public void setModelClass(Class clazz) {
			modelClass = clazz;
		}
		
		public void setModel(IModel model) {
			this.model = model;
		}

		public void setModelListener(IModelListener modelListener) {
			this.modelListener = modelListener;
		}

		protected Boolean doInBackground(RestMethod... params) {
			if (model==null){
				return false;
			}
			
			currMethod = params[0];
			
			if(!RestClient.isOnline()) {
				return false;
			}
					
			
			switch (currMethod) {
			case CREATE:
				if (model.onRestCreate()>0) {
					model.setServerUpdated(true);
					return true;
				}
				break;
			case UPDATE:
				if(model.onRestUpdate()>0) {
					model.setServerUpdated(true);
					return true;
				}
				break;
			case DELETE:
				if(model.onRestDelete()>0) {
					model.setServerUpdated(true);
					return true;
				}
				break;
			case GET:
				if(model.onRestGet()>0) {
					model.setServerUpdated(true);
					return true;
				}
				break;
			case GET_WITH_DEP:
				if(model.onRestGetWithDependents()>0) {
					model.setServerUpdated(true);
					return true;
				}
				break;
			case GET_FOR_CUR_USER:
				if(model.onRestGetAllForCurUser()>0) {
					model.setServerUpdated(true);
					return true;
				}
				break;
			case SYNC:
				if(model.onRestSyncToServer()>0) {
					model.setServerUpdated(true);
					return true;
				}
				break;
			default:
				break;
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
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
				case GET_WITH_DEP:
					modelListener.onGetWithDependentsComplete(modelClass,result);
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

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}	
	}
	
}
