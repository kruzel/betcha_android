package com.betcha.activity;

import java.sql.SQLException;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.User;

public class SettingsActivity extends Activity {
	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
	          "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
	          "\\@" +
	          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
	          "(" +
	          "\\." +
	          "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
	          ")+"
	      );
	
	private BetchaApp app;
	EditText etEmail;
	EditText etPass;
	EditText etName;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        app = (BetchaApp) getApplication();
        
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etName = (EditText) findViewById(R.id.editTextName);
        etPass = (EditText) findViewById(R.id.editTextPass);
        
    }

	@Override
	protected void onResume() {
		User me = app.getMe();
		Button btEmailReg = (Button) findViewById(R.id.buttonEmailReg);
		
		if(me!=null) {
			btEmailReg.setText(getString(R.string.update));
			
	        String myEmail = app.getMe().getEmail();
	        
	        if(myEmail!= null && myEmail.length()!=0) {
		        etEmail.setText(myEmail);
	        }
	        
	        String myPass = app.getMe().getPassword();
	        if(myPass!=null && myPass.length()!=0) {
	        	etPass.setText(myPass);
	        }
	        
	        String myName = app.getMe().getName();
	        if(myName!=null && myName.length()!=0) {
		        etName.setText(myName);
	        }
        } else {
        	btEmailReg.setText(getString(R.string.register));
        }
		
		super.onResume();		
	}
	
	public void onSubmit(View v) {
		boolean errorFound = false;
		
		//validations
		if(etEmail.getText().length() == 0) {
			etEmail.setError(getString(R.string.error_bad_email));
			errorFound = true;
    	}
    	
    	try {
    		if(!EMAIL_ADDRESS_PATTERN.matcher(etEmail.getText().toString()).matches()) {
    			etEmail.setError(getString(R.string.error_bad_email));
    			errorFound = true;
    		}
        }
        catch( NullPointerException exception ) {
        	errorFound = true;
        }
    	
    	if(etPass.getText().length() < 6) {
    		etEmail.setError(getString(R.string.error_pass_short));
			errorFound = true;
    	}
    	
    	if(etName.getText().length()==0) {
    		etName.setText(etEmail.getText().toString());
    	}
		
        if(errorFound == false) {
        	User me = app.getMe();
        	
        	if(me==null) {
        		me = new User();
        		        
        		me.setProvider("email");
	        	me.setEmail(etEmail.getText().toString());
	        	me.setName(etName.getText().toString());
	        	me.setPassword(etPass.getText().toString());
    		
	        	try {
					me.create();
				} catch (SQLException e) {
					Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG);
					e.printStackTrace();
					return;
				}
	        	
    		} else {
    			
    			me.setEmail(etEmail.getText().toString());
	        	me.setName(etName.getText().toString());
	        	me.setPassword(etPass.getText().toString());
	        	
    			try {
					me.update();
				} catch (SQLException e) {
					Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG);
					e.printStackTrace();
					return;
				}
    		}
    		
    		app.setMe(me);
    		
    		TabActivity act = (TabActivity) getParent();
	        if(act==null)
	        	return;
	        
		    TabHost tabHost = act.getTabHost();  // The activity TabHost
		    if(tabHost != null){
		    	tabHost.getTabWidget().setEnabled(true);
	        	tabHost.setCurrentTab(0);
	        }
        	
        }
	}
	
	public void onSkip(View v) {
		User me = new User();
		me.setProvider("email");
		
		try {
			me.create();
		} catch (SQLException e) {
			Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG);
			e.printStackTrace();
			return;
		}
		
		app.setMe(me);
		
		TabActivity act = (TabActivity) getParent();
        if(act==null)
        	return;
        
	    TabHost tabHost = act.getTabHost();  // The activity TabHost
	    if(tabHost != null){
	    	tabHost.getTabWidget().setEnabled(true);
        	tabHost.setCurrentTab(0);
        }
	}
	
	public void OnFBConnect(View v) {
		//TODO run FB connect flow
		
	}
	
	//TODO on registration - load contacts to friends list
}