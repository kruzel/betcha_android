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
		
		String betUUID=null;
		Intent launchIntent = getIntent();
		Uri data = launchIntent.getData();
		if(data!=null) {
			List<String> params = data.getPathSegments();
			app.setBetUUID(params.get(0)); // "bet uuid"
		}

	    Resources res = getResources(); // Resource object to get Drawables
	    tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, CreateBetActivity.class);
	    intent.putExtra("betUUID", betUUID);

	    // Initialize a TabSpec for each tab and add it to the TabHost
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
	    
	    if(app.getMe()==null) {
	    	tabHost.getTabWidget().setEnabled(false);
	    	tabHost.setCurrentTab(2);
	    } else if(app.getBetUUID() != null) {
	    	tabHost.setCurrentTab(1);
	    } else {
	    	tabHost.setCurrentTab(0);
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
