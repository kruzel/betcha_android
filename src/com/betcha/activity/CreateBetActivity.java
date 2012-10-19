package com.betcha.activity;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.fragment.CreateBetCategoryFragment;
import com.betcha.fragment.CreateBetCategoryFragment.OnBetCategorySelectionListener2;
import com.betcha.fragment.CreateBetFragment;
import com.betcha.fragment.CreateBetFragment.OnBetDetailsEnteredListener;
import com.betcha.model.Bet;
import com.betcha.model.Category;

public class CreateBetActivity extends SherlockFragmentActivity implements OnBetCategorySelectionListener2, OnBetDetailsEnteredListener {
	
	private BetchaApp app;
	
	private Bet newBet;
	
	private CreateBetFragment createBetFragment;
	private CreateBetCategoryFragment betCategoryFragment;
	private List<Category> customCategoriesList;
	private List<Category> featuredCategoriesList;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_bet_activity);
        
        app = (BetchaApp) getApplication();
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        newBet = new Bet();
        
        betCategoryFragment = new CreateBetCategoryFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.create_bet_fragment_container, betCategoryFragment);
		transaction.addToBackStack(null);
		transaction.commit();
    }
    
	@Override
	protected void onResume() {
		customCategoriesList = Category.getCategories(this,"Custom");
		featuredCategoriesList = Category.getCategories(this,"Sport");
				
		betCategoryFragment.init(newBet, customCategoriesList, featuredCategoriesList);
		super.onResume();
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
	public void OnBetCategorySelected() {
		if(app.getFriends()==null) {
			Toast.makeText(this, "No friends or contacts found", Toast.LENGTH_LONG);
		} else {
			createBetFragment = new CreateBetFragment();
			createBetFragment.init(newBet, app.getMe(),app.getFriends());
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.create_bet_fragment_container, createBetFragment);
			transaction.addToBackStack(null);
			transaction.commit();
		}
		
	}

	@Override
	public void OnBetDetailsEntered() {
		newBet.create();
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    ft.remove(createBetFragment);
	    ft.commit();
		
	    finish();
	}
    
}
