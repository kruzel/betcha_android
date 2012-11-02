package com.betcha.fragment;

import java.util.List;

import org.springframework.http.HttpStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.activity.BetDetailsActivity;
import com.betcha.activity.CreateBetActivity;
import com.betcha.activity.LoginActivity;
import com.betcha.adapter.ActivityFeedAdapter;
import com.betcha.model.ActivityFeedItem;
import com.betcha.model.Bet;
import com.betcha.model.cache.IModelListener;
import com.betcha.model.task.SyncTask;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class ActivityFeedFragment extends SherlockFragment  implements IModelListener, OnItemClickListener {
	enum Filter { ALL_BETS, NEW_BETS, MY_BETS };
	Filter betsFiler = Filter.ALL_BETS;
	
	private BetchaApp app;
	private ActivityFeedAdapter activityAdapter;
	private List<ActivityFeedItem> activities;
	private Bet newBet = null;
	
	private PullToRefreshListView lvActivities;
	private Boolean isFirstBetsLoad = true;
		
	private BroadcastReceiver receiver;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        app = BetchaApp.getInstance();
        setHasOptionsMenu(true);
        
        receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				populate();
			}
		};
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.activity_feed_fragment, container,false);
		  
		lvActivities = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_bets_list);
        
        lvActivities.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				lvActivities.setRefreshing();
				SyncTask.run(ActivityFeedFragment.this);
			}
		});  
        
        lvActivities.setOnItemClickListener(this);
                		
		return view;
	}
	
	@Override
	public void onResume() {
		activities = ActivityFeedItem.getActivities();
		
		if(app.getCurUser()!=null && app.getCurUser().getId()!=null) {
//			if(isFirstBetsLoad) {
//				isFirstBetsLoad = false;
//	        	lvActivities.setRefreshing();
//	        	SyncTask.run(this);
//	        }
			
			populate();
        } 
		
		final String BET_LIST_ACTION = "com.betcha.BetsListFragmentReceiver";
	    IntentFilter intentFilter = new IntentFilter(BET_LIST_ACTION);
		getActivity().registerReceiver(receiver,intentFilter);
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(receiver);
		super.onPause();
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
	            intent = new Intent(getActivity(), LoginActivity.class);
	            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        case R.id.menu_refresh:
	        	lvActivities.setRefreshing();
	        	SyncTask.run(this);
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
		activities = ActivityFeedItem.getActivities();
		
 		if(activities!=null && activities.size()>0) {
 			if(activityAdapter==null){
	 			activityAdapter = new ActivityFeedAdapter(getActivity(), R.layout.activity_feed_fragment, activities);
		        lvActivities.setAdapter(activityAdapter);    
 			} else {
	 			activityAdapter.clear();
	 			activityAdapter.addAll(activities);
	 			activityAdapter.notifyDataSetChanged();
 			}
 		} else {
 			if(activityAdapter!=null) {
 				activityAdapter.clear();
 				activityAdapter.notifyDataSetChanged();
 			}
 		}
 			
	}


	@Override
	public void onCreateComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetComplete(Class clazz, HttpStatus errorCode) {
		
		lvActivities.onRefreshComplete();
		
		if(errorCode==HttpStatus.OK ) {
			populate();
						
			if(app.getBetId()!="-1") {
				app.setBetId("-1"); //avoid going here on next resume
				openDetailedActivity(newBet, true);
			}
		} else {
			Toast.makeText(getActivity(), getString(R.string.error_bet_not_found) + ", error " + errorCode.value(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDeleteComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Bet bet = activities.get(position).getBet();
		if(bet==null)
			return;
			
		Intent i = new Intent(getActivity(), BetDetailsActivity.class);
        i.putExtra("bet_id", bet.getId());
        getActivity().startActivity(i);
	}
}
