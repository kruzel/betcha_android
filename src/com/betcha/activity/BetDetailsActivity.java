package com.betcha.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
	private Bet bet;
	private BetDetailsFragment betDetailsFragment;
	private ProgressDialog dialog;
	
	private BetChatMessagesFragment betChatFragment;
	
	private Button btnInvite;
	private Button btnClose;
	private Button btnDelete;
	
	ListView lvFriends;
	FriendAdapter friendAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bet_details_activity);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

		app = (BetchaApp) getApplication();
		
		betDetailsFragment =  (BetDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.bet_details_fragment);
		betChatFragment = (BetChatMessagesFragment) getSupportFragmentManager().findFragmentById(R.id.chat_fragment);
		
		btnInvite = (Button) findViewById(R.id.button_invite);
		btnClose = (Button) findViewById(R.id.button_close);
		btnDelete = (Button) findViewById(R.id.button_delete);
		
		if(app.getFriends()==null) {
			Toast.makeText(this, "No friends found", Toast.LENGTH_LONG);
		} else {
			btnInvite.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
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
					    	
					    	bet.setParticipants(participants);
					    	bet.update();
					    	
					    	betDetailsFragment.refresh();
					    	
					    	dialog.dismiss();
					    	
						}
					});
			        
			        dialog.show();
				}
			});
		}
		
		btnClose.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bet.setState(Bet.STATE_CLOSED);
				bet.update();
				finish();
			}
		});

		btnDelete.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				bet.delete();
				finish();
				
			}
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		String betId = intent.getStringExtra("betId");
		Boolean isNewBet = intent.getBooleanExtra("is_new_bet", false);

		if (betId==null || betId.equals("-1"))
			return;

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
		
		if(app.getMe().getId().equals(bet.getOwner().getId())) {
			btnInvite.setVisibility(View.VISIBLE);
			btnClose.setVisibility(View.VISIBLE);
			btnDelete.setVisibility(View.VISIBLE);
			
			View fragmentView = (View) findViewById(R.id.chat_fragment);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fragmentView.getLayoutParams();
			params.addRule(RelativeLayout.BELOW, R.id.buttons_frame);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			
		}

		if (isNewBet) {
			dialog = ProgressDialog.show(BetDetailsActivity.this,
					"", getString(R.string.msg_bet_loading), true);
		}
		
		betDetailsFragment.init(bet, app.getMe());
		betChatFragment.init(bet, app.getMe());
	}

	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.menu_refresh:
        	if (dialog != null && dialog.isShowing()) {
    			dialog.dismiss();
    			dialog = null;
    		}
        	dialog = ProgressDialog.show(BetDetailsActivity.this,
					"", getString(R.string.msg_bet_loading), true);
        	bet.setListener(this);
        	bet.get();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.bet_details_activity, menu);
	    return true;
	}

	// Prediction list OnClick
	public void onClick(View v) {

		Prediction.update(bet.getPredictions(),bet.getId());

		bet.setState(Bet.STATE_CLOSED);
		bet.update();
		
	}

	protected void getFromServer() {
		if(bet==null)
			return;
		
		bet.setListener(this);
		bet.get();
		
		app.setBetId("-1"); //avoid going here on next resume
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
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		
		if(success) {
			betDetailsFragment.refresh();
	    	betChatFragment.refresh();
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
