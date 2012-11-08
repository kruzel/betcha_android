package com.betcha.model.cache;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.util.Log;

import com.betcha.BetchaApp;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.server.api.RestClient;

public class SyncTask extends AsyncTask<Void, Void, HttpStatus> {
	private static SyncTask syncThread;
	private static IModelListener modelListener;
	
	public SyncTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void setListener(IModelListener modelListener) {
		SyncTask.modelListener = modelListener;
	}
	
	@TargetApi(11)
	public static void run(IModelListener modelListener) {
		Log.i("SyncTask.run()", "called");	
		
		if (syncThread != null
				&& (syncThread.getStatus() == Status.RUNNING || syncThread
						.getStatus() == Status.PENDING))
			return;

		syncThread = new SyncTask();
		SyncTask.setListener(modelListener);
		syncThread.executeOnExecutor(THREAD_POOL_EXECUTOR, (Void[])null);
		
		Log.i("SyncTask.run()", "calling exectue");	

	}

	@Override
	protected HttpStatus doInBackground(Void... params) {
		Log.i("SyncTask.doInBackground()", "started");	
		
		if(!RestClient.isOnline()) {
			ModelCache.enableConnectivityReciever();
			Log.i("SyncTask.doInBackground()", "service unavailable");	
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
		
		if(BetchaApp.getInstance().getCurUser()==null) {
			Log.i("SyncTask.doInBackground()", "user not defined");	
			return HttpStatus.UNAUTHORIZED;
		}
		
		if(!BetchaApp.getInstance().getCurUser().isServerCreated()) {
			if(BetchaApp.getInstance().getCurUser().onRestCreate()>0) {
				BetchaApp.getInstance().getCurUser().setServerCreated(true);
				BetchaApp.getInstance().getCurUser().setServerUpdated(true);
				BetchaApp.getInstance().getCurUser().onLocalUpdate();
			} else {
				Log.i("SyncTask.doInBackground()", "user creation failed");	
				return HttpStatus.UNAUTHORIZED;
			}
		}
		
		if(RestClient.GetToken()==null || RestClient.GetToken().length()==0) {
			if(BetchaApp.getInstance().getCurUser().restCreateToken()==0) {
				Log.i("SyncTask.doInBackground()", "missing token");	
				return HttpStatus.UNAUTHORIZED;
			}
		}
		
		// get all updates from server (bets and their predictions and chat_messages
		// TODO - use the show all update for current user
		Bet.getAllUpdatesForCurUser(BetchaApp.getInstance().getLastSyncTime());
		
		Log.i("SyncTask.doInBackground()", "done getAllUpdatesForCurUser");	

		// push all bets that not yet pushed to server
		
		//TODO change to one call with all updates in one json
		List<Bet> bets = null;
		try {
			bets = Bet.getModelDao().queryForEq("server_updated", false);
		} catch (SQLException e1) {
		}
		
		if (bets == null || bets.size() == 0) {
			return HttpStatus.OK;
		}

		for (Bet bet : bets) {
			if (!bet.isServerCreated()) {
//				if(bet.onRestCreate()>0) {
//					bet.setServerCreated(true);
//					bet.setServerUpdated(true);
//					try {
//						Bet.getModelDao().update(bet);
//					} catch (SQLException e) {
//						e.printStackTrace();
//					}
//				} 
				//TODO handle create of existing bet properly
				bet.onRestCreate();
				bet.setServerCreated(true);
				bet.setServerUpdated(true);
				try {
					Bet.getModelDao().update(bet);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				continue;
			}
			
			if (!bet.isServerUpdated()) {
				bet.onRestSync();
				bet.setServerUpdated(true);
				try {
					Bet.getModelDao().update(bet);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			List<Prediction> predictions = null;
			try {
				predictions = Prediction.getModelDao().queryForEq(
						"server_updated", false);
				if (predictions == null || predictions.size() == 0) {
					continue;
				}
			} catch (SQLException e1) {
				continue;
			}

			for (Prediction prediction : predictions) {
				if (!prediction.isServerUpdated()) {
					if(!prediction.isServerCreated()) {
						prediction.onRestCreate();
						prediction.setServerCreated(true);
						prediction.setServerUpdated(true);
						try {
							Prediction.getModelDao().update(prediction);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						prediction.onRestUpdate(); 
						prediction.setServerUpdated(true);
						try {
							Prediction.getModelDao().update(prediction);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} 
				}
			}
		}

		Log.i("SyncTask.doInBackground()", "done");	
		
		return HttpStatus.OK;
	}

	@Override
	protected void onPostExecute(HttpStatus errorCode) {
		Log.i("SyncTask.onPostExecute()", "errorCode " + errorCode);	
		
		if(errorCode==HttpStatus.OK) {
			BetchaApp.getInstance().setLastSyncTime(new DateTime());
		}
		
		if (modelListener != null)
			modelListener.onGetComplete(SyncTask.class, errorCode);

		super.onPostExecute(errorCode);
	}

}