package com.betcha.model.tasks;

import java.util.List;

import android.os.AsyncTask;

import com.betcha.model.User;
import com.betcha.model.server.api.PredictionRestClient;

public class PredictionsSendInvitesTask extends AsyncTask<Void, Void, Boolean> {
	
	private List<User> users;
	private int bet_server_id;

	public PredictionsSendInvitesTask(int bet_server_id) {
		super();
		this.bet_server_id = bet_server_id;
	}

	public void setValues(List<User> users) {
		this.users = users;
	}
		
	public int getBet_server_id() {
		return bet_server_id;
	}

	public void setBet_server_id(int bet_server_id) {
		this.bet_server_id = bet_server_id;
	}

	public void run() {
		
		if(getStatus()!=Status.RUNNING)
			execute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
    	PredictionRestClient restClient = new PredictionRestClient(getBet_server_id());
    	restClient.sendInvites(users);
    	
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
//		if(result) {	    	
//	    	Toast.makeText(context, R.string.msg_bet_accepted, Toast.LENGTH_LONG);
//		} else {
//			Toast.makeText(context, R.string.msg_bet_rejected, Toast.LENGTH_LONG);
//		}
		
		super.onPostExecute(result);
	}
	
}
