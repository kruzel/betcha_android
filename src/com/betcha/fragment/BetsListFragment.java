package com.betcha.fragment;

import java.sql.SQLException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.activity.BetDetailsActivity;
import com.betcha.activity.CreateBetActivity;
import com.betcha.activity.SettingsActivity;
import com.betcha.adapter.BetAdapter;
import com.betcha.model.Bet;
import com.betcha.model.cache.IModelListener;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class BetsListFragment extends SherlockFragment  implements IModelListener {
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
                
        app = BetchaApp.getInstance();
        setHasOptionsMenu(true);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.bets_list_fragment, container);
		  
		lvBets = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_bets_list);
        
        lvBets.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				lvBets.setRefreshing();
				Bet.syncAllWithServer(BetsListFragment.this);
			}
		});        
        		
		return view;
	}
	
	@Override
	public void onResume() {
		if(app.getMe()!=null && app.getMe().getId()!=null) {
			if(isFirstBetsLoad) {
				isFirstBetsLoad = false;
	        	lvBets.setRefreshing();
	        	Bet.syncAllWithServer(this);
	        }
			
			populate();
        } 
		
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.bet_list_activity, menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		//Debug.stopMethodTracing();
		
		Intent intent;
		switch (item.getItemId()) {
	        case R.id.menu_create_bet:
	             //intent = new Intent(getActivity(), CreateBetActivity.class);
	        	intent = new Intent(getActivity(), CreateBetActivity.class);
	            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_settings:
	            intent = new Intent(getActivity(), SettingsActivity.class);
	            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_refresh:
	        	lvBets.setRefreshing();
	        	Bet.syncAllWithServer(this);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void openDetailedActivity( Bet bet, Boolean isNewBet) {
		//populate();
		
		Intent i = new Intent(getActivity(), BetDetailsActivity.class);
        i.putExtra("bet_id", bet.getId());
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
        
 		if(bets!=null && bets.size()>0) {
 			if(betAdapter==null){
	 			betAdapter = new BetAdapter(getActivity(), R.layout.bets_list_item, bets);
		        lvBets.setAdapter(betAdapter);    
 			} else {
	 			betAdapter.clear();
	 			betAdapter.addAll(bets);
	 			betAdapter.notifyDataSetChanged();
 			}
 		} else {
 			if(betAdapter!=null) {
 				betAdapter.clear();
 				betAdapter.notifyDataSetChanged();
 			}
 		}
 			
	}


	@Override
	public void onCreateComplete(Class clazz, ErrorCode errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateComplete(Class clazz, ErrorCode errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetComplete(Class clazz, ErrorCode errorCode) {
		
		lvBets.onRefreshComplete();
		
		if(errorCode==ErrorCode.OK ) {
			populate();
						
			if(app.getBetId()!="-1") {
				app.setBetId("-1"); //avoid going here on next resume
				openDetailedActivity(newBet, true);
			}
		} else {
			Toast.makeText(getActivity(), R.string.error_bet_not_found, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDeleteComplete(Class clazz, ErrorCode errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncComplete(Class clazz, ErrorCode errorCode) {
		// TODO Auto-generated method stub
		
	}
}
