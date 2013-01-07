package com.betcha.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.betcha.R;
import com.betcha.fragment.ProfileDetailsFragment;

public class ProfileActivity extends SherlockFragmentActivity{
	
	public final static String UID = "com.betcha.ProfileActivity.UID";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the message from the intent
	    Intent intent = getIntent();
	    String uid = intent.getStringExtra(ProfileActivity.UID);
	    
		setContentView(R.layout.profile_activity);
		
		/*ProfileDetailsFragment pdFragment = new ProfileDetailsFragment();
		
		 
		// In case this activity was started with special instructions from an Intent,
        // pass the Intent's extras to the fragment as arguments
		pdFragment.setArguments(getIntent().getExtras());
		FrameLayout v = (FrameLayout)findViewById(R.id.profile_details_fragment_container);
		v.removeAllViews();
        // Add the fragment to the 'profile_details_fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.profile_details_fragment_container, pdFragment).commit();*/
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
        
	}
	
	
}
