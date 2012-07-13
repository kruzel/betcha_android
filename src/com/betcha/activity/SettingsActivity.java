package com.betcha.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.User;
import com.betcha.model.tasks.CreateUserTask;
import com.betcha.model.tasks.ICreateUserCB;
import com.betcha.model.tasks.UpdateUserTask;

public class SettingsActivity extends Activity implements ICreateUserCB {
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
        
        User me = app.getMe();
        
        etEmail = (EditText) findViewById(R.id.editTextEmail);
        etName = (EditText) findViewById(R.id.editTextName);
        etPass = (EditText) findViewById(R.id.editTextPass);
        
        if(me!=null) {
	        String myEmail = app.getMe().getEmail();
	        
	        if(myEmail!= null && myEmail.length()!=0) {
		        etEmail.setText(myEmail);
	        }
	        
	        String myPass = app.getMe().getPass();
	        if(myPass!=null && myPass.length()!=0) {
	        	etPass.setText(myPass);
	        }
	        
	        String myName = app.getMe().getName();
	        if(myName!=null && myName.length()!=0) {
		        etName.setText(myName);
	        }
        }
    }

	@Override
	protected void onResume() {
		
		
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
        	}
        	
	        	me.setEmail(etEmail.getText().toString());
	        	me.setName(etName.getText().toString());
	        	me.setPass(etPass.getText().toString());
    		
    			app.createCreateUserTask();
            	app.getCreateUserTask().setValues(me, this);
            	app.getCreateUserTask().run();
//    		} else {
//    			me.setEmail(etEmail.getText().toString());
//	        	me.setName(etName.getText().toString());
//	        	me.setPass(etPass.getText().toString());
//	        	
//    			app.createUpdateUsertask();
//        		app.getUpdateUsertask().setValues(me);
//        		app.getUpdateUsertask().run();
//    		}
    		
    		app.setMe(me);
        	
        }
	}
	
	public void onSkip(View v) {
		User me = new User();
		
		//we create a user without details anyhow for the user id
		app.createCreateUserTask();
    	app.getCreateUserTask().setValues(me, this);
    	app.getCreateUserTask().run();
		
		app.setMe(me);
    	
    	TabActivity act = (TabActivity) getParent();
        if(act==null)
        	return;
	}
	
	public void OnFBConnect(View v) {
		//TODO
		
	}


	public void OnRegistrationComplete(String token) {
		app.setToken(token);
		
		if(token!=null) {
			TabActivity act = (TabActivity) getParent();
	        if(act==null)
	        	return;
	        
		    TabHost tabHost = act.getTabHost();  // The activity TabHost
		    if(tabHost != null){
	        	tabHost.setCurrentTab(0);
	        }
		}
		
	}
}