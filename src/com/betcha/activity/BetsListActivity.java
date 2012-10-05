package com.betcha.activity;

import java.sql.SQLException;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.Bet;


/**
 * @author ofer
 *
 */
public class BetsListActivity extends SherlockFragmentActivity {
	private BetchaApp app;
	private Boolean firstResume = true;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bets_list_activity);
                
        app = (BetchaApp) getApplication();
        
    }
    
	protected void onResume() {
		
		if(app.getMe()==null || app.getMe().getId()==null) {
        	Intent intent = new Intent();
        	intent.setClass(this, SettingsActivity.class);
        	startActivity(intent);
	    } else if(!app.getBetId().equals("-1")) {
				Bet bet = null;
				try {
					List<Bet> bets = Bet.getModelDao().queryForEq("id",app.getBetId());
					if(bets!=null && bets.size()>0)
						bet=bets.get(0);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if(bet!=null)
					openDetailedActivity(bet, false);
				
				app.setBetId("-1"); //avoid going here on next resume
		} else {
			long count=0;
			try {
				count = Bet.getModelDao().countOf();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if(count==0 && firstResume) {
				Intent intent = new Intent(this,CreateBetActivity.class);
				startActivity(intent);
				firstResume=false;
			}
		}
					
		super.onResume();
	}

	public void openDetailedActivity( Bet bet, Boolean isNewBet) {
		//populate();
		
		Intent i = new Intent(this, BetDetailsActivity.class);
        i.putExtra("betId", bet.getId());
        i.putExtra("is_new_bet", isNewBet);
        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        startActivity(i);
	}
}
