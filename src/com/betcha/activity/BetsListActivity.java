package com.betcha.activity;

import java.sql.SQLException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.BetAdapter;
import com.betcha.model.Bet;
import com.betcha.model.cache.IModelListener;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;


/**
 * @author ofer
 *
 */
public class BetsListActivity extends SherlockActivity implements IModelListener {
	private BetchaApp app;
	private BetAdapter betAdapter;
	private List<Bet> bets;
	private Bet newBet = null;
	
	private PullToRefreshListView lvBets;
	private ProgressDialog dialog;
	private Boolean isFirstBetsLoad = true;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bets_list);
                
        app = (BetchaApp) getApplication();

        lvBets = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_bets_list);
        
        lvBets.setOnRefreshListener(new OnRefreshListener() {

    	    public void onRefresh() {
    	    	Bet.syncWithServer(BetsListActivity.this);
    	    }
    	});
        
        lvBets.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Bet bet_item = (Bet) lvBets.getItemAtPosition(position);
				openDetailedActivity(bet_item, false);
			}
		});
        
        if(app.getMe()==null || app.getMe().getServer_id()==-1) {
        	Intent intent = new Intent();
        	intent.setClass(this, SettingsActivity.class);
        	startActivity(intent);
	    } 
    }
    
	protected void onResume() {
		
		if(app.getMe()!=null) {
		
			if(app.getMe().getServer_id()!=-1 && isFirstBetsLoad) {
				isFirstBetsLoad = false;
	        	lvBets.setRefreshing();
	        	Bet.syncWithServer(this);
	        }
			
			populate();
			
			if(app.getBetId()!=-1) {
				Bet bet = null;
				try {
					bet = Bet.getModelDao().queryForId(app.getBetId());
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if(bet!=null)
					openDetailedActivity(bet, false);
				
				app.setBetId(-1); //avoid going here on next resume
				
				if(bets==null || bets.size()==0) {
					Intent intent = new Intent(this,CreateBetActivity.class);
					startActivity(intent);
				}
			} 
		}
				
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.bet_list_activity, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
	        case R.id.menu_create_bet:
	             intent = new Intent(this, CreateBetActivity.class);
	            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_settings:
	            intent = new Intent(this, SettingsActivity.class);
	            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_refresh:
	        	lvBets.setRefreshing();
	        	Bet.syncWithServer(this);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void openDetailedActivity( Bet bet, Boolean isNewBet) {
		//populate();
		
		Intent i = new Intent(this, BetDetailsActivity.class);
        i.putExtra("betId", bet.getId());
        i.putExtra("is_new_bet", isNewBet);
        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        startActivity(i);
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


	@Override
	public void onCreateComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetComplete(Class clazz, Boolean success) {
		if(dialog!=null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		
		lvBets.onRefreshComplete();
		
		if(success ) {
			populate();
						
			if(app.getBetId()!=-1) {
				app.setBetId(-1); //avoid going here on next resume
				openDetailedActivity(newBet, true);
			}
		} else {
			Toast.makeText(this, R.string.error_bet_not_found, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onGetWithDependentsComplete(Class clazz, Boolean success) {
		if(dialog!=null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		
		lvBets.onRefreshComplete();
		
		if(success ) {
			populate();
						
			if(app.getBetId()!=-1) {
				app.setBetId(-1); //avoid going here on next resume
				openDetailedActivity(newBet, true);
			}
		} else {
			Toast.makeText(this, R.string.error_bet_not_found, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDeleteComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}
	
}
