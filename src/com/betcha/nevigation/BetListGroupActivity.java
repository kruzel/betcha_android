package com.betcha.nevigation;

import java.util.ArrayList;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.betcha.activity.BetsListActivity;

public class BetListGroupActivity extends ActivityGroup {
	
    // Keep this in a static variable to make it accessible for all the nesten activities,
	//lets them manipulate the view
	public static BetListGroupActivity group;
 
    // Need to keep track of the history if you want the back-button to
	// work properly, don't use this if your activities requires a lot of memory.
	private ArrayList<View> history;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      this.history = new ArrayList<View>();
	      group = this;
 
              // Start the root activity withing the group and get its view
	      View view = getLocalActivityManager().startActivity("BetsListActivity", new
	    	      							Intent(this,BetsListActivity.class)
	    	      							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
	    	                                .getDecorView();
 
              // Replace the view of this ActivityGroup
	      replaceView(view);
	   }
 
	public void replaceView(View v) {
                // Adds the old one to history
		history.add(v);
                // Changes this Groups View to the new View.
		setContentView(v);
	}
 
	public void back() {
		if(history.size() > 1) {
			history.remove(history.size()-1);
			setContentView(history.get(history.size()-1));
			try {
				BetsListActivity bl = (BetsListActivity) getLocalActivityManager().getActivity("BetsListActivity"); 
				if(bl!=null)
					bl.populate();
			} catch (ClassCastException e) {
				
			}
		}else {
			finish();
		}
	}
	
	public void setToFirst() {
		while(history.size() > 1) {
			history.remove(history.size()-1);
			setContentView(history.get(history.size()-1));
		}
		
		try {
			BetsListActivity bl = (BetsListActivity) getLocalActivityManager().getActivity("BetsListActivity"); 
			if(bl!=null)
				bl.populate();
		} catch (ClassCastException e) {
			
		}
	}
 
   @Override
    public void onBackPressed() {
	   BetListGroupActivity.group.back();
	   
        return;
    }

	@Override
	protected void onResume() {
		
		super.onResume();
	}

}
