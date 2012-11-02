package com.betcha.activity;

import java.sql.SQLException;
import java.util.List;

import utils.Filter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.fragment.ActivityFeedFragment;
import com.betcha.fragment.BetsListFragment;
import com.betcha.model.Bet;


/**
 * @author ofer
 *
 */
public class BetsListActivity extends SherlockFragmentActivity {
	private BetchaApp app;
	private Boolean firstResume = true;
	
	private ActivityFeedFragment activityFeedFragment;
	private BetsListFragment betListFragment;
	private RadioGroup rgBetsFilterGrou;
	Filter betsFilter = Filter.ALL_BETS;
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bets_list_activity);
                
        app = (BetchaApp) getApplication();
        
        rgBetsFilterGrou = (RadioGroup) findViewById(R.id.bet_list_filter_group);
        
        rgBetsFilterGrou.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				
				switch (checkedId) {
				case R.id.new_bet_filter:
					betsFilter = Filter.NEW_BETS;
					betListFragment = new BetsListFragment();
					betListFragment.setBetsFilter(betsFilter);
					break;
				case R.id.my_bet_filter:
					betsFilter = Filter.MY_BETS;
					betListFragment = new BetsListFragment();
					betListFragment.setBetsFilter(betsFilter);
					transaction.replace(R.id.bets_list, betListFragment);
					transaction.commit();
					break;
				case R.id.all_bets_filter:
				default:
					betsFilter = Filter.ALL_BETS;
					//if(activityFeedFragment==null)
					activityFeedFragment = new ActivityFeedFragment();
					transaction.replace(R.id.bets_list, activityFeedFragment);
					transaction.commit();
				}
				
			}
		});
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        activityFeedFragment = new ActivityFeedFragment();
		transaction.replace(R.id.bets_list, activityFeedFragment);
		transaction.commit();
        
    }
    
	protected void onResume() {
		
		if(app.getCurUser()==null || app.getCurUser().getId()==null) {
        	Intent intent = new Intent();
        	intent.setClass(this, LoginActivity.class);
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
        i.putExtra("bet_id", bet.getId());
        i.putExtra("is_new_bet", isNewBet);
        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        startActivity(i);
	}
}
