package com.betcha.activity;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import utils.SoftKeyboardUtils;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.FriendAdapter;
import com.betcha.fragment.CreateCategoryFragment;
import com.betcha.fragment.CreateCategoryFragment.OnCategorySelectedListener;
import com.betcha.fragment.CreateDuedateFragment;
import com.betcha.fragment.CreateDuedateFragment.OnDuedateSelectedListener;
import com.betcha.fragment.CreateStakeFragment;
import com.betcha.fragment.CreateStakeFragment.OnStakeSelectedListener;
import com.betcha.fragment.CreateSubjectFragment;
import com.betcha.fragment.CreateSubjectFragment.OnSubjectSelectedListener;
import com.betcha.model.Bet;
import com.betcha.model.Category;
import com.betcha.model.Prediction;
import com.betcha.model.Reward;
import com.betcha.model.User;

public class CreateBetActivity extends SherlockFragmentActivity implements OnCategorySelectedListener, OnSubjectSelectedListener, OnStakeSelectedListener, OnDuedateSelectedListener {
	
	private BetchaApp app;
	
	private CreateStakeFragment createStakeFragment;
	private CreateCategoryFragment betCategoryFragment;
	private CreateSubjectFragment createSubjectFragment;
	private CreateDuedateFragment createDuedateFragment;
	
	private ListView lvFriends;
	private FriendAdapter friendAdapter;
	
	private Menu menu;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_bet_activity);
        
        app = (BetchaApp) getApplication();
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        app.setCurBet(new Bet());
        app.getCurBet().setOwner(app.getCurUser());
        
        Prediction prediction = new Prediction(app.getCurBet());
        prediction.setUser(app.getCurUser());
        prediction.setPrediction("");
        
        app.getCurBet().setOwnerPrediction(prediction);
        
        String[] sections = new String[] { "Custom", "Sport" };
        
        betCategoryFragment = CreateCategoryFragment.newInstance(sections);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.create_bet_fragment_container, betCategoryFragment);
		transaction.addToBackStack(null);
		transaction.commit();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;  
		
//		MenuInflater inflater = getSupportMenuInflater();
//		inflater.inflate(R.menu.create_bet_activity, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ("Send".equals(item.getTitle())) {
			app.getCurBet().create();

			Intent i = new Intent(this, BetDetailsActivity.class);
	        i.putExtra("bet_id", app.getCurBet().getId());
	        startActivity(i);

	        finish();	
	        
			return true;
		}
		
		
		switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onCategorySelected(Category category) {
		app.getCurBet().setCategoryId(category.getId());
				
		String[] subjects = { "which team win", "who get highest grade" };
		
		createSubjectFragment = CreateSubjectFragment.newInstance(subjects);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.create_bet_fragment_container, createSubjectFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onSubjectSelected(String subject) {
		app.getCurBet().setSubject(subject);
			
		createStakeFragment = CreateStakeFragment.newInstance(Reward.getIds(), Reward.getNames(), Reward.getDrawables(),app.getCurBet().getSubject());
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.create_bet_fragment_container, createStakeFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	@Override
	public void onStakeSelected(String stake_id, String stake) {
		app.getCurBet().setReward(stake);
		app.getCurBet().setReward_id(stake_id);
		
		createDuedateFragment = CreateDuedateFragment.newInstance(app.getCurBet().getSubject(), stake,stake_id);
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.create_bet_fragment_container, createDuedateFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onDuedateSelected(DateTime dateTime) {
        app.getCurBet().setDueDate(dateTime);
		        		
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.friends_picker);
		dialog.setTitle(getResources().getString(R.string.select_friends));
						
		lvFriends = (ListView) dialog.findViewById(R.id.friends_list);
		friendAdapter = new FriendAdapter(this, R.layout.invite_list_item, app.getFriends());
        lvFriends.setAdapter(friendAdapter);
        
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
        
        et.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					v.clearFocus();
					SoftKeyboardUtils.hideSoftwareKeyboard(v);
			        return true;
				}
				
				return false;
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
		    	
		    	app.getCurBet().addParticipants(participants);
				
		    	app.getCurBet().setState(Bet.STATE_OPEN);    	
		    	app.getCurBet().setOwner(app.getCurUser());
		    	
		    	dialog.dismiss();
		    	
		    	
		    	menu.add("Send")
		        .setIcon(R.drawable.ic_menu_forward)
		        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			}
		});
        
        dialog.show();
	}    
}
