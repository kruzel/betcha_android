package com.betcha.activity;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.UserBetAdapter;
import com.betcha.model.Bet;
import com.betcha.model.User;
import com.betcha.model.UserBet;
import com.betcha.model.tasks.IGetUserBetCB;
import com.betcha.model.tasks.IGetUserBetsCB;
import com.betcha.nevigation.BetListGroupActivity;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class BetDetailsActivity extends Activity implements OnClickListener, IGetUserBetsCB, IGetUserBetCB {
	private BetchaApp app;
	private UserBetAdapter userBetAdapter;
	private List<UserBet> usersBets;
		
	private Bet bet;
	private UserBet myUserBet;
	private TextView tvState;
	private TextView tvDate;
	private TextView tvOwner;
	private TextView tvSubject;
	
	private PullToRefreshListView lvUsersBets;
	private Button btnPublishResult;
	
	private View footerView;
	private EditText etMyBet;
	
	private ProgressDialog dialog;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bet_details);
		
	    app = (BetchaApp) getApplication();
	    
	    lvUsersBets = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_betdetails_list);
	
        LayoutInflater inflater=this.getLayoutInflater();
        View header=inflater.inflate(R.layout.bet_details_header, null);
        lvUsersBets.addHeaderView(header);		
        
        tvState = (TextView) findViewById(R.id.tv_bet_header_state);
    	tvDate = (TextView) findViewById(R.id.tv_bet_header_date);
    	tvOwner = (TextView) findViewById(R.id.tv_bet_header_owner);
    	tvSubject = (TextView) findViewById(R.id.tv_bet_header_subject);
    	
    	footerView = inflater.inflate(R.layout.add_bet_footer, null);
    	    			
    	lvUsersBets.setOnRefreshListener(new OnRefreshListener() {

    	    public void onRefresh() {
    	        getFromServer();
    	    }
    	});
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
    	Intent intent = getIntent();
		Integer betId = intent.getIntExtra("betId",-1);
		Boolean isNewBet = intent.getBooleanExtra("is_new_bet", false);

		if(betId==-1)
			return;
		
		try {
			bet = app.getHelper().getBetDao().queryForId(betId);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		if(isNewBet) {
			dialog = ProgressDialog.show(BetDetailsActivity.this.getParent(), "", 
	                getString(R.string.msg_bet_loading), true);
		}
		
	   	populateList();
    	
 		// Creating a button - only if bet owner is the current logged in user
 		if(bet.getOwner().getId() == app.getMe().getId()) {
 			if (btnPublishResult==null) {
 				lvUsersBets.removeFooterView(footerView);
 				
	 			btnPublishResult = new Button(BetDetailsActivity.this);
	 	    	btnPublishResult.setText(getString(R.string.publish_results));
	 	    	btnPublishResult.setOnClickListener(BetDetailsActivity.this);
	 	    	 
	 	    	// Adding button to listview at footer
	 	    	lvUsersBets.addFooterView(btnPublishResult); 
 			}
 		} else {
 			if(btnPublishResult!=null) {
 				lvUsersBets.removeFooterView(btnPublishResult);
 				btnPublishResult = null;
 			}
 			
 			lvUsersBets.addFooterView(footerView); 
 			etMyBet = (EditText) findViewById(R.id.editTextAddBet);
 			etMyBet.clearFocus();

 		}
    	
    	getFromServer();
		  	
    	DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yy HH:mm");
    	
    	tvState.setText(bet.getState());
    	tvDate.setText(fmt.print(bet.getDate()));
    	tvOwner.setText(bet.getOwner().getName());
    	tvSubject.setText(bet.getSubject());
    	
 
	}

	protected void populateList() {
		//TODO change to foreign collection
 		try {
 			usersBets = app.getHelper().getUserBetDao().queryForEq("bet_id",bet.getId());	
 		} catch (SQLException e) {
 			Log.e(getClass().getSimpleName(), ".onCreate() - failed getting bet list");
 			e.printStackTrace();
 		}
 		
 		List<UserBet> myUserBets = null;
 		myUserBet = null;
 		try {
 			Map<String, Object> myBetKey = new HashMap<String, Object>();
 			myBetKey.put("bet_id",bet.getId());
 			myBetKey.put("user_id", app.getMe().getId());
 			
 			myUserBets = app.getHelper().getUserBetDao().queryForFieldValues(myBetKey);	
 		} catch (SQLException e) {
 			Log.e(getClass().getSimpleName(), ".onCreate() - no user bet for current user for this bet");
 			e.printStackTrace();
 		}
 		
 		if(myUserBets.size()>0) {
 			myUserBet = myUserBets.get(0);
 		}
        
 		userBetAdapter = new UserBetAdapter(this, R.layout.bets_list_item, usersBets);
 		
 		lvUsersBets.setAdapter(userBetAdapter);
	}
		   
	@Override
	public void onBackPressed() {
		BetListGroupActivity.group.back();
	}

	//UserBet list OnClick
	public void onClick(View v) {

		for(int i=2;i<lvUsersBets.getCount()-1; i++) {
			View vListItem = lvUsersBets.getChildAt(i);
			CheckBox cb = (CheckBox) vListItem.findViewById(R.id.cb_user_bet_win);
			if(cb!=null)
				usersBets.get(i-2).setResult(cb.isChecked());
		}
		
		app.createUpdateUsersBetsTask();
		app.getUpdateUsersBetsTask().setValues(usersBets);
		app.getUpdateUsersBetsTask().run();
		
		bet.setState(Bet.STATE_CLOSED);
		app.createUpdateBetTask();
		app.getUpdateBetTask().setValues(bet);
		app.getUpdateBetTask().run();
		tvState.setText(bet.getState());
	}
	
	protected void getFromServer() {
		if(bet.getServer_id() == -1) { //TODO this should not happen
			if(dialog!=null && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
			return;
		}
			
		
		app.createGetUserBetTask().setValues(bet.getServer_id(), this);
		if(!app.getGetUserBetTask().run()){
			if(dialog!=null && dialog.isShowing()) {
				dialog.dismiss();
				dialog = null;
			}
		}
	}

	public void onAddBet(View v) { //by this user invited to bet
		boolean errorFound = false;
		
		etMyBet.clearFocus();
		
		//validations
        if(etMyBet.length() == 0) {
        	etMyBet.setError(getString(R.string.error_missing_bet));
        	errorFound = true;
        }
         
        if(errorFound == false) {
       	               	
        	//set myself as first user
        	User me = app.getMe();
        	if(me==null) {
        		Toast.makeText(this, R.string.error_please_register, Toast.LENGTH_LONG).show();
        		return;
        	}
        	
        	if(myUserBet == null) {
        		//if current user have no bet yet then create current user UserBet for the existing bet
        		myUserBet = new UserBet();
        		myUserBet.setBet(bet);
            	myUserBet.setUser(me);
            	
            	myUserBet.setDate(new DateTime()); //current betting time
            	myUserBet.setMyBet(etMyBet.getText().toString());
            	myUserBet.setMyAck(getString(R.string.pending));
        		       	
            	Toast.makeText(this, R.string.publishing_bet, Toast.LENGTH_LONG);
            	
            	app.createCreateUserBetTask().setValues(myUserBet, this);
            	app.getCreateUserBetTask().run();
        	} else {
        		//just update current UserBet
	        	myUserBet.setDate(new DateTime()); //current betting time
	        	myUserBet.setMyBet(etMyBet.getText().toString());
	        	myUserBet.setMyAck(getString(R.string.pending));
	    		       	
	        	Toast.makeText(this, R.string.publishing_bet, Toast.LENGTH_LONG);
	        	
	        	app.createUpdateUserBetTask().setValues(myUserBet);
	        	app.getUpdateUserBetTask().run();
        	}
        	
        	populateList();	
        }
	}

	public void OnGetUserBetCompleted(Boolean success, UserBet userbet) {
		populateList();	
	}
	
	public void OnGetUserBetsCompleted(Boolean success, List<UserBet> usersbets) {
		if(usersBets!=null && usersBets.size()>0) {
			this.usersBets = usersbets;
		}
		populateList();
		if(dialog!=null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		lvUsersBets.onRefreshComplete();
	}

	
}
