/**
 * 
 */
package com.betcha.model.cache;

import java.sql.SQLException;

import android.os.AsyncTask;

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
	protected Boolean synced = false;
	@DatabaseField
	protected RestMethod last_rest_call; //use this to know what server operation need to be completed
	
	private RestTask restTask;
	IModelListener listener;
	
	protected Boolean authenticateCreate() {
		return true;
	}
	
	protected Boolean authenticateUpdate() {
		return true;
	}
	
	protected Boolean authenticateDelete() {
		return true;
	}
	
	protected Boolean authenticateRefresh() {
		return true;
	}
	
	// TODO schedule the synch task for every period (configurable) 
	
	public ModelCache()  {
		super();
		initDao();
	}
	
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
		// create on local model
		int res = createLocal();
		
		setSynced(false);
		if(authenticateCreate() && RestClient.GetToken()==null)
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.CREATE;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.execute(RestMethod.CREATE);
		
		return res;
	}
	
	public int createLocal() throws SQLException {
		return super.create();
	}
	
	public int createOrUpdateLocal() throws SQLException {
		CreateOrUpdateStatus status = getDao().createOrUpdate((T) this);
		return status.getNumLinesChanged();
		//return super.create();
	}

	@Override
	public int delete() throws SQLException {
		// delete from local model
		int res =  deleteLocal();
		
		setSynced(false);
		if(authenticateDelete() && RestClient.GetToken()==null)
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.DELETE;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.execute(RestMethod.DELETE);
		
		return res;
	}
	
	public int deleteLocal() throws SQLException {
		return super.delete();
	}

	@Override
	public int update() throws SQLException {
		// update local model
		int res =  updateLocal();
		
		setSynced(false);
		if(authenticateUpdate() && RestClient.GetToken()==null)
			return res;
		
		// run task to update server
		last_rest_call = RestMethod.UPDATE;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.execute(RestMethod.UPDATE);
		
		return res;
	}
	
	public int updateLocal() throws SQLException {
		return super.update();
	}
	
	@Override
	public int refresh() throws SQLException {	
		setSynced(false);
		if(authenticateCreate() && RestClient.GetToken()==null)
			return -1;
		
		// run task to update server
		last_rest_call = RestMethod.SYNC;
		restTask = new RestTask();
		restTask.setModel(this);
		restTask.setModelListener(listener);
		restTask.execute(RestMethod.SYNC);
		
		return getServer_id();
	}

	public void setServer_id(int serverId) {
		this.server_id = serverId;
		setSynced(true);
		
	}
	
	public int getServer_id() {
		return server_id;
	}

	public void setSynced(Boolean synced) {
		this.synced = synced;
	}

	/**
	 * background task that does all the rest calls
	 * @author ofer
	 *
	 */
	public static class RestTask extends  AsyncTask<RestMethod, Void, Boolean> {
		public enum RestMethod { CREATE, UPDATE, DELETE, SYNC }
		RestMethod currMethod;
		
		private IModel model;
		private IModelListener modelListener;

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
			switch (currMethod) {
			case CREATE:
				if (model.onRestCreate()>0) {
					model.setSynced(true);
					return true;
				}
				break;
			case UPDATE:
				if(model.onRestUpdate()>0) {
					model.setSynced(true);
					return true;
				}
				break;
			case DELETE:
				if(model.onRestDelete()>0) {
					model.setSynced(true);
					return true;
				}
				break;
			case SYNC:
				if(model.onRestSync()>0) {
					model.setSynced(true);
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
					modelListener.onCreateComplete(result);
					break;
				case UPDATE:
					modelListener.onUpdateComplete(result);
					break;
				case DELETE:
					modelListener.onDeleteComplete(result);
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
		
//	public static Dao<?,?> getModelDao() throws SQLException {
//		Log.e("ModelCache.getModelDao()","must be implemented by derived class!");
//		return null;
//	}
	
}
