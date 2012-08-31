package com.betcha.activity;

import java.sql.SQLException;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.User;
import com.betcha.model.cache.IModelListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class SettingsActivity extends Activity implements IModelListener {
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
	
	private Facebook facebook = new Facebook("299434100154215");
	
	private EditText etEmail;
	private EditText etPass;
	private EditText etName;
	
	private Dialog dialog = null;
	
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }

	@Override
	protected void onResume() {
		User me = app.getMe();
		Button btEmailReg = (Button) findViewById(R.id.buttonEmailReg);
		
		if(me!=null) {
						
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
        } 
		
		if(me!=null && me.getServer_id()!=-1) {
			btEmailReg.setText(getString(R.string.update));
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
        	int res = 0;
        	
        	dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
                    "Registering. Please wait...", true);
        	        	
        	if(me==null || me.getServer_id()==-1) {
        		if(me==null) {
        			me = new User();
        		}
        		        
        		me.setProvider("email");
	        	me.setEmail(etEmail.getText().toString());
	        	me.setName(etName.getText().toString());
	        	me.setPassword(etPass.getText().toString());
	        	
	        	me.setListener(this);
    		
	        	try {
					res = me.create();
				} catch (SQLException e) {
					Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG);
					e.printStackTrace();
					onCreateComplete(me.getClass(),false);
					return;
				}
	        	
    		} else {
    			
    			me.setEmail(etEmail.getText().toString());
	        	me.setName(etName.getText().toString());
	        	me.setPassword(etPass.getText().toString());
	        	
    			try {
					res = me.update();
				} catch (SQLException e) {
					Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG);
					e.printStackTrace();
					onCreateComplete(me.getClass(),false);
					return;
				}
    		}
    		
        	app.setMe(me);
        }
	}
	
	public void onSkip(View v) {
		User me = new User();
		me.setProvider("email");
		
		int res = 0;
		try {
			res = me.create();
		} catch (SQLException e) {
			Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG);
			e.printStackTrace();
			onCreateComplete(me.getClass(),false);
			return;
		}
		
		if(res>1) {
			app.setMe(me);
			onCreateComplete(me.getClass(),true);
		} else {
			onCreateComplete(me.getClass(),false);
		}
			
	}
	
	public void OnFBConnect(View v) {
		facebook.authorize(this, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
            	//TODO on registration - load contacts to friends list
            	String token = facebook.getAccessToken();
            	
            	//TODO implement FB connect registration on dropabet server
            	onCreateComplete(app.getMe().getClass(),false);
            	return;
            }

            @Override
            public void onFacebookError(FacebookError error) {}

            @Override
            public void onError(DialogError e) {}

            @Override
            public void onCancel() {}
        });
		
	}

	@Override
	public void onCreateComplete(Class clazz, Boolean success) {
		
		if(clazz.getName().contentEquals("com.betcha.model.User")) {
			if(success) {
				TabActivity act = (TabActivity) getParent();
		        if(act==null)
		        	return;
		        
			    TabHost tabHost = act.getTabHost();  // The activity TabHost
			    if(tabHost != null){
			    	tabHost.getTabWidget().setEnabled(true);
		        	tabHost.setCurrentTab(0);
		        }
			} else {
				Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
			}
		}
		
		dialog.dismiss();
	}

	@Override
	public void onUpdateComplete(Class clazz, Boolean success) {
		dialog.dismiss();
	}

	@Override
	public void onDeleteComplete(Class clazz, Boolean success) {
		dialog.dismiss();
	}

	@Override
	public void onSyncComplete(Class clazz, Boolean success) {
		dialog.dismiss();
	}
	
	
}