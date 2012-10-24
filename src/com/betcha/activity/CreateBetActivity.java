package com.betcha.activity;

import org.joda.time.DateTime;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
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

public class CreateBetActivity extends SherlockFragmentActivity implements OnCategorySelectedListener, OnSubjectSelectedListener, OnStakeSelectedListener, OnDuedateSelectedListener {
	
	private BetchaApp app;
	
	private CreateStakeFragment createStakeFragment;
	private CreateCategoryFragment betCategoryFragment;
	private CreateSubjectFragment createSubjectFragment;
	CreateDuedateFragment createDuedateFragment;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_bet_activity);
        
        app = (BetchaApp) getApplication();
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        app.setCurBet(new Bet());
        
        String[] sections = new String[] { "Custom", "Sport" };
        
        betCategoryFragment = CreateCategoryFragment.newInstance(sections);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.create_bet_fragment_container, betCategoryFragment);
		transaction.addToBackStack(null);
		transaction.commit();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
			
		createStakeFragment = CreateStakeFragment.newInstance(getResources(), R.array.stake_names, R.array.stake_icons,app.getCurBet().getSubject());
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.create_bet_fragment_container, createStakeFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	@Override
	public void onStakeSelected(String stake) {
		app.getCurBet().setReward(stake);
		
		createDuedateFragment = CreateDuedateFragment.newInstance(app.getCurBet().getSubject(), app.getCurBet().getReward());
		
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.create_bet_fragment_container, createDuedateFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onDuedateSelected(DateTime dateTime) {
        app.getCurBet().setDueDate(dateTime);
		
        //TODO open participants selection fragment
	}

	//TODO continue
    //app.getCurBet().create();
    //finish();
    
}
