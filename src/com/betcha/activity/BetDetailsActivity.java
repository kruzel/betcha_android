package com.betcha.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.FriendAdapter;
import com.betcha.fragment.BetChatMessagesFragment;
import com.betcha.fragment.BetDetailsFragment;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;
import com.betcha.model.cache.IModelListener;

public class BetDetailsActivity extends SherlockFragmentActivity implements OnClickListener, IModelListener {
	private BetchaApp app;
	
	private BetDetailsFragment betDetailsFragment;
	private ProgressDialog dialog;
	
	private BetChatMessagesFragment betChatFragment;
	
	ListView lvFriends;
	FriendAdapter friendAdapter;
	
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bet_details_activity);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

		app = (BetchaApp) getApplication();
		
		betDetailsFragment =  (BetDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.bet_details_fragment);
		betChatFragment = (BetChatMessagesFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment);
		
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				betDetailsFragment.refresh();
				betChatFragment.refresh();
			}
		};
		
	}

	@Override
	protected void onResume() {

		super.onResume();
		
			Intent intent = getIntent();
			String betId = intent.getStringExtra("bet_id");
			Boolean isNewBet = intent.getBooleanExtra("is_new_bet", false);
	
			if (betId==null || betId.equals("-1"))
				return;
			
//			if(app.getCurBet()==null || (app.getCurBet()==null && !app.getCurBet().getId().equals(betId))) {
//				
//			}
			
			Bet bet = null;
			try {
				
				List<Bet> bets = Bet.getModelDao().queryForEq("id",betId);
				if(bets!=null && bets.size()>0)
					bet = bets.get(0);
			
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			
			if(bet==null)
				return;
			
			app.setCurBet(bet);
	
		if (isNewBet) {
			dialog = ProgressDialog.show(BetDetailsActivity.this,
					"", getString(R.string.msg_bet_loading), true);
		}
		
		final String BET_DETAILS_ACTION = "com.betcha.BetDetailsActivityReceiver";
	    IntentFilter intentFilter = new IntentFilter(BET_DETAILS_ACTION);
		registerReceiver(receiver,intentFilter);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if("Delete".equals(item.getTitle())) {
			deleteBet();
		} else if("Invite".equals(item.getTitle())) {
			inviteFriends();
		}else if("Close".equals(item.getTitle())) {
			closeBet();
		} else { 
		
			switch (item.getItemId()) {
	        case android.R.id.home:
	        	//app.getCurBet().update();
	            finish();
	            return true;
	        case R.id.menu_refresh:
	        	if (dialog != null && dialog.isShowing()) {
	    			dialog.dismiss();
	    			dialog = null;
	    		}
	        	dialog = ProgressDialog.show(BetDetailsActivity.this,
						"", getString(R.string.msg_bet_loading), true);
	        	app.getCurBet().setListener(this);
	        	app.getCurBet().get();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.bet_details_activity, menu);
	    
	    if(app.getCurBet()!=null && app.getCurUser().getId().equals(app.getCurBet().getOwner().getId())) {
	    				
			menu.add("Invite")
	        .setIcon(R.drawable.ic_menu_invite)
	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			
		    menu.add("Delete")
	        .setIcon(R.drawable.ic_menu_delete)
	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			
		    menu.add("Close")
	        .setIcon(R.drawable.stake_coins)
	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	    }
	    
	    return true;
	}

	// Prediction list OnClick
	public void onClick(View v) {

		Prediction.update(app.getCurBet().getPredictions(),app.getCurBet().getId());

		app.getCurBet().setState(Bet.STATE_CLOSED);
		app.getCurBet().update();
		
	}

	protected void getFromServer() {
		if(app.getCurBet()==null)
			return;
		
		app.getCurBet().setListener(this);
		app.getCurBet().get();
		
		app.setBetId("-1"); //avoid going here on next resume
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
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		
		if(errorCode == HttpStatus.OK) {
			betDetailsFragment.refresh();
	    	betChatFragment.refresh();
		}
	}

	@Override
	public void onDeleteComplete(Class clazz, HttpStatus errorCode) {
		// TODO Auto-generated method stub
		
	}

	private void inviteFriends() {
		if(app.getFriends()==null) {
			Toast.makeText(this, "No friends found", Toast.LENGTH_LONG);
		} else {
			// Create and show the dialog.
	        final Dialog dialog = new Dialog(BetDetailsActivity.this);
			dialog.setContentView(R.layout.friends_picker);
			dialog.setTitle(getResources().getString(R.string.select_friends));
							
			lvFriends = (ListView) dialog.findViewById(R.id.friends_list);
			friendAdapter = new FriendAdapter(BetDetailsActivity.this, R.layout.invite_list_item, app.getFriends());
	        lvFriends.setAdapter(friendAdapter);
	        lvFriends.setTextFilterEnabled(true);
	        
	        for (User friend : app.getFriends()) {
				if(friend.getIsInvitedToBet()) {
					friend.setIsInvitedToBet(false);
				}
			}
	        
	        EditText et = (EditText) dialog.findViewById(R.id.editTextSearch);
	        et.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					friendAdapter.getFilter().filter(s);
				}
			});
	        
	        Button dialogButton = (Button) dialog.findViewById(R.id.buttonOK);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					List<User> participants = new ArrayList<User>();
			    	for (User friend : app.getFriends()) {
						if(friend.getIsInvitedToBet()) {
							participants.add(friend);
						}
					}
			    	
			    	app.getCurBet().addPredictions(participants);
			    						    	
			    	betDetailsFragment.refresh();
			    	
			    	dialog.dismiss();
			    	
				}
			});
	        
	        dialog.show();
		}
	}
	
	private void closeBet() {
		app.getCurBet().setState(Bet.STATE_CLOSED);
		app.getCurBet().update();
	}
	
	private void deleteBet() {
		app.getCurBet().delete();
		app.setCurBet(null);
		finish();
	}
}
