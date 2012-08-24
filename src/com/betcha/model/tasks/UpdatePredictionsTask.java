package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;

import com.betcha.model.Prediction;
import com.betcha.model.server.api.PredictionRestClient;

public class UpdatePredictionsTask extends AsyncTask<Void, Void, Boolean> {
	
	private List<Prediction> predictions;
	private int bet_server_id;

	public UpdatePredictionsTask(int bet_server_id) {
		super();
		this.bet_server_id = bet_server_id;
	}

	public void setValues(List<Prediction> predictions) {
		this.predictions = predictions;
	}
		
	public int getBet_server_id() {
		return bet_server_id;
	}

	public void setBet_server_id(int bet_server_id) {
		this.bet_server_id = bet_server_id;
	}

	public void run() {
		
		for (Prediction prediction : predictions) {
			try {
				prediction.update();
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
		
		execute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
    	
		List<Prediction> paramList = new ArrayList<Prediction>();
		
		PredictionRestClient predictionClient = new PredictionRestClient(bet_server_id);
		
		for (Prediction prediction : predictions) {
			paramList.add(prediction);
		}
		
		try {
			predictionClient.update(paramList);
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		}
	       					
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
