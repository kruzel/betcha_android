package com.betcha.nevigation;

import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.activity.CreateBetActivity;
import com.betcha.activity.SettingsActivity;

public class BetUTabActivity extends TabActivity {
	
	private BetchaApp app;
	TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		app = (BetchaApp) getApplication();
		
		Intent launchIntent = getIntent();
		app.setBetId(launchIntent.getIntExtra("bet_id", -1));

	    Resources res = getResources(); // Resource object to get Drawables
	    tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, CreateBetActivity.class);
	    spec = tabHost.newTabSpec("bet").setIndicator("Bet",
	                      res.getDrawable(R.drawable.ic_tab_createbet))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, BetListGroupActivity.class);
	    spec = tabHost.newTabSpec("records").setIndicator("Records",
	                      res.getDrawable(R.drawable.ic_tab_records))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	        
	 // Do the same for the other tabs
	    intent = new Intent().setClass(this, SettingsActivity.class);
	    spec = tabHost.newTabSpec("settings").setIndicator("Settings",
	                      res.getDrawable(R.drawable.ic_tab_records))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    
	    if(app.getMe()==null || app.getMe().getServer_id()==-1) {
	    	tabHost.getTabWidget().setEnabled(false);
	    	tabHost.setCurrentTab(2);
	    } else if(app.getBetId() != -1) {
	    	tabHost.setCurrentTab(1);
	    } else {
	    	tabHost.setCurrentTab(1);
	    }
	    
	    tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			public void onTabChanged(String tabId) {
				if(tabHost.getCurrentTab() == 1 && BetListGroupActivity.group!=null){
					BetListGroupActivity.group.setToFirst();
				}
			}
		});
	}

}
