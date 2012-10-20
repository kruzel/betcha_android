package com.betcha.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.activity.BetDetailsActivity;
import com.betcha.activity.CreateBetActivity;
import com.betcha.activity.LoginActivity;
import com.betcha.adapter.BetAdapter;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.cache.IModelListener;
import com.betcha.model.task.SyncTask;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class BetsListFragment extends SherlockFragment  implements IModelListener, OnItemClickListener {
	enum Filter { ALL_BETS, NEW_BETS, MY_BETS };
	Filter betsFiler = Filter.ALL_BETS;
	
	private BetchaApp app;
	private BetAdapter betAdapter;
	private List<Bet> bets;
	private Bet newBet = null;
	
	private PullToRefreshListView lvBets;
	private ProgressDialog dialog;
	private Boolean isFirstBetsLoad = true;
	
	private RadioGroup rgBetsFilterGrou;
	
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
		
		View view = (ViewGroup) inflater.inflate(R.layout.bets_list_fragment, container);
		  
		lvBets = (PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_bets_list);
        
        lvBets.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				lvBets.setRefreshing();
				SyncTask.run(BetsListFragment.this);
			}
		});  
        
        lvBets.setOnItemClickListener(this);
        
        rgBetsFilterGrou = (RadioGroup) view.findViewById(R.id.bet_list_filter_group);
        
        rgBetsFilterGrou.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.all_bets_filter:
					betsFiler = Filter.ALL_BETS;
					break;
				case R.id.new_bet_filter:
					betsFiler = Filter.NEW_BETS;
					break;
				case R.id.my_bet_filter:
					betsFiler = Filter.MY_BETS;
					break;
				default:
					betsFiler = Filter.ALL_BETS;	
				}
				populate();
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
	        	SyncTask.run(this);
	        }
			
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
	        	lvBets.setRefreshing();
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
		// query for all of the bets objects in the database
 		try {
 			QueryBuilder<Bet, String> betsQueryBuilder = Bet.getModelDao().queryBuilder();
// 			QueryBuilder<Prediction, Integer> predictionQueryBuilder = Prediction.getModelDao().queryBuilder();
 			PreparedQuery<Bet> preparedQuery = null;
 			switch (betsFiler) {				
			case NEW_BETS:			//un-met invitations
				betsQueryBuilder.where().ne("user_id", app.getMe().getId());
//				predictionQueryBuilder.where().eq("user_id",app.getMe().getId());
//				betsQueryBuilder.join(predictionQueryBuilder);
				betsQueryBuilder.orderBy("dueDate", false);
	 			preparedQuery = betsQueryBuilder.prepare();
				List<Bet> tmpBets = Bet.getModelDao().query(preparedQuery);
				bets = new ArrayList<Bet>();
				for (Bet bet : tmpBets) {
					for (Prediction prediction : bet.getPredictions()) {
						if(prediction.getUser()==app.getMe() && prediction.equals("")) {
							bets.add(bet);
						}
					}
				}
				break;
			case MY_BETS:	//bets I created
				betsQueryBuilder.where().eq("user_id", app.getMe().getId());
				betsQueryBuilder.orderBy("dueDate", false);
	 			preparedQuery = betsQueryBuilder.prepare();
				bets = Bet.getModelDao().query(preparedQuery);
				break;
			case ALL_BETS:
			default:
				betsQueryBuilder = Bet.getModelDao().queryBuilder();
				betsQueryBuilder.orderBy("dueDate", false);
	 			preparedQuery = betsQueryBuilder.prepare();
				bets = Bet.getModelDao().query(preparedQuery);
			}
 			
 			
 			
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
	public void onCreateComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetComplete(Class clazz, HttpStatus errorCode) {
		
		lvBets.onRefreshComplete();
		
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
		Intent i = new Intent(getActivity(), BetDetailsActivity.class);
        String betId = bets.get(position).getId();
        i.putExtra("bet_id", betId);
        getActivity().startActivity(i);
	}
}
