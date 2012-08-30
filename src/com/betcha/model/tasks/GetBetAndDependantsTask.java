package com.betcha.model.tasks;

import java.sql.SQLException;
import java.util.List;

import android.os.AsyncTask;

import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;

public class GetBetAndDependantsTask extends AsyncTask<Void, Void, Boolean> {
	private Bet bet = null;
	private User owner = null;
	private int bet_server_id;
	private IGetBetAndDependantCB cb;

	public void setValues(int bet_server_id, IGetBetAndDependantCB cb) {
		this.bet_server_id = bet_server_id;
		this.cb = cb;
	}
	
	public void run() {
		
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("server_id", bet_server_id);
			if(bets.size()>0) {
				bet = bets.get(0);
				List<User> owners = User.getModelDao().queryForEq("id", bet.getOwner().getId());
				if(owners.size()>0) {
					owner = owners.get(0);
					bet.setOwner(owner);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
				
		if(getStatus()!=Status.RUNNING) {
			execute();
		}
	}	

	@Override
	protected Boolean doInBackground(Void... params) {
		bet = Bet.getAndCreateBet(bet.getServer_id());
		if(bet==null)
			return false;
		
		if(Prediction.getAndCreatePredictions(bet)==null)
			return false;
			       					
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		
		cb.OnGetBetCompleted(result, bet);
			
		super.onPostExecute(result);
	}
	
}
