package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;

import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.server.api.BetRestClient;

public class GetUserBetsTask extends AsyncTask<Void, Void, Boolean> {
	private IGetThisUserBetsCB cb;
	
	List<Bet> bets;

	public void setValues(IGetThisUserBetsCB cb) {
		this.cb = cb;
	}
	
	public Boolean run() {
				
		//get the rest from the server
		if(getStatus()!=Status.RUNNING)
			execute();
		
		return true;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		
		bets = new ArrayList<Bet>();
				
		BetRestClient restClient = new BetRestClient();
		JSONArray jsonBets = null;
		try {
			jsonBets = restClient.show_for_user(); //for logged in user
		} catch (RestClientException e) {
			e.printStackTrace();
			return false;
		} 
		
		for (int i = 0; i < jsonBets.length(); i++) {
			JSONObject jsonBet;
		
			try {
				jsonBet = jsonBets.getJSONObject(i);
			} catch (JSONException e3) {
				e3.printStackTrace();
				continue;
			}
			
			Bet tmpBet = null;
			try {
				List<Bet> tmpBets = Bet.getModelDao().queryForEq("server_id", jsonBet.getInt("id"));
				if(tmpBets!=null && tmpBets.size()>0) { 
					tmpBet = tmpBets.get(0);
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if(tmpBet==null) {
				tmpBet = new Bet();
			}
							
			if(!tmpBet.setJson(jsonBet))
				continue;
			
			try {
				tmpBet.createOrUpdateLocal();
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			
			if(Prediction.getAndCreatePredictions(tmpBet)==null)
				continue;
			
			bets.add(tmpBet);
			
		}
	       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		if(cb!=null)
			cb.OnGetUserBetsCompleted(result, bets);
		
		super.onPostExecute(result);
	}
	
}
