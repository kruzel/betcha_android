package com.betcha.activity;

import java.sql.SQLException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.BetAdapter;
import com.betcha.model.Bet;
import com.betcha.model.tasks.IGetBetAndOwnerCB;
import com.betcha.model.tasks.IGetThisUserBetsCB;
import com.betcha.nevigation.BetListGroupActivity;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

/**
 * @author ofer
 *
 */
public class BetsListActivity extends Activity implements IGetThisUserBetsCB, IGetBetAndOwnerCB {
	private BetchaApp app;
	private BetAdapter betAdapter;
	private List<Bet> bets;
	
	private PullToRefreshListView lvBets;
	
	private ProgressDialog dialog;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bets_list);
                
        app = (BetchaApp) getApplication();

        lvBets = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_bets_list);
        
        lvBets.setOnRefreshListener(new OnRefreshListener() {

    	    public void onRefresh() {
    	    	Bet.refreshForUser(app.getMe(), BetsListActivity.this);
    	    }
    	});
        
        lvBets.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Bet bet_item = (Bet) lvBets.getItemAtPosition(position);
				openDetailedActivity(bet_item, false);
			}
		});
    }
    
	protected void onResume() {
		if(app.getBetId()!=-1) {
			dialog = ProgressDialog.show(BetsListActivity.this.getParent(), "", 
	                getString(R.string.msg_bet_loading), true);
			
			Bet.fetchBetAndOwner(app.getBetId(), this);		
			
			app.setBetId(-1); //avoid going here on next resume
			
		} 
		
		populate();
		        
		super.onResume();
	}
	
	public void openDetailedActivity( Bet bet, Boolean isNewBet) {
		//populate();
		
		Intent i = new Intent(this, BetDetailsActivity.class);
        i.putExtra("betId", bet.getId());
        i.putExtra("is_new_bet", isNewBet);
 
        // Create the view using FirstGroup's LocalActivityManager
        View view = BetListGroupActivity.group.getLocalActivityManager()
        .startActivity("BetDetailsActivity", i
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        .getDecorView();
 
        // Again, replace the view
        BetListGroupActivity.group.replaceView(view);
	}

	public void populate() {
		// query for all of the bets objects in the database
 		try {
 			bets = Bet.getModelDao().queryForAll();
 		} catch (SQLException e) {
 			Log.e(getClass().getSimpleName(), ".onCreate() - failed getting bet list");
 			e.printStackTrace();
 		}
        
        betAdapter = new BetAdapter(this, R.layout.bets_list_item, bets);
        lvBets.setAdapter(betAdapter);
	}

	public void OnGetBetCompleted(Boolean success, Bet bet) {
		if(dialog!=null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		
		if(success && bet!=null) {
			openDetailedActivity(bet,true);
		} else {
			Toast.makeText(this, R.string.error_bet_not_found, Toast.LENGTH_LONG).show();
		}
		
	}

	public void OnGetUserBetsCompleted(Boolean success, List<Bet> bets) {
		populate();
		lvBets.onRefreshComplete();
	}
	
}
