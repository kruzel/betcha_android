package com.betcha.model.task;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;

import android.os.AsyncTask;

import com.betcha.BetchaApp;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.cache.IModelListener;
import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.RestClient;

public class SyncTask extends AsyncTask<Void, Void, HttpStatus> {
	private static SyncTask syncThread;
	private IModelListener modelListener;
	
	public SyncTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setListener(IModelListener modelListener) {
		this.modelListener = modelListener;
	}
	
	public static void run(IModelListener modelListener) {
		if (syncThread != null
				&& (syncThread.getStatus() == Status.RUNNING || syncThread
						.getStatus() == Status.PENDING))
			return;

		syncThread = new SyncTask();
		syncThread.setListener(modelListener);
		syncThread.execute();

	}

	@Override
	protected HttpStatus doInBackground(Void... params) {
		
		if(!RestClient.isOnline()) {
			ModelCache.enableConnectivityReciever();
			return HttpStatus.SERVICE_UNAVAILABLE;
		}
		
		if(BetchaApp.getInstance().getMe()==null)
			return HttpStatus.UNAUTHORIZED;
		
		if(!BetchaApp.getInstance().getMe().isServerCreated()) {
			if(BetchaApp.getInstance().getMe().onRestCreate()>0) {
				BetchaApp.getInstance().getMe().setServerCreated(true);
				BetchaApp.getInstance().getMe().setServerUpdated(true);
				BetchaApp.getInstance().getMe().onLocalUpdate();
			} else {
				return HttpStatus.UNAUTHORIZED;
			}
		}
		
		if(RestClient.GetToken()==null || RestClient.GetToken().length()==0) {
			if(BetchaApp.getInstance().getMe().restCreateToken()==0) 
				return HttpStatus.UNAUTHORIZED;
		}
		
		// get all updates from server (bets and their predictions and chat_messages
		// TODO - use the show all update for current user
		Bet.getAllUpdatesForCurUser(BetchaApp.getInstance().getLastSyncTime());

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
				if(bet.onRestCreate()>0) {
					bet.setServerCreated(true);
					bet.setServerUpdated(true);
					try {
						Bet.getModelDao().update(bet);
					} catch (SQLException e) {
						e.printStackTrace();
					}
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

		return HttpStatus.OK;
	}

	@Override
	protected void onPostExecute(HttpStatus errorCode) {
		if(errorCode==HttpStatus.OK) {
			BetchaApp.getInstance().setLastSyncTime(new DateTime());
		}
		
		if (modelListener != null)
			modelListener.onGetComplete(SyncTask.class, errorCode);

		super.onPostExecute(errorCode);
	}

}