package com.betcha.activity;

import java.util.regex.Pattern;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.Friend;
import com.betcha.model.User;
import com.betcha.model.cache.IModelListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class SettingsActivity extends SherlockActivity implements IModelListener {
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
	
	private Friend friend;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
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
		
		if(me!=null && me.getId()!=null) {
			btEmailReg.setText(getString(R.string.update));
		} else {
        	btEmailReg.setText(getString(R.string.register));
        }
		
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
        	
        	if(dialog!=null && dialog.isShowing())
    			dialog.dismiss();
        	dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
                    "Registering. Please wait...", true);
        	        	
        	if(me==null || me.getId()==null) {
        		if(me==null) {
        			me = new User();
        		}
        		        
        		me.setProvider("email");
	        	me.setEmail(etEmail.getText().toString());
	        	me.setName(etName.getText().toString());
	        	me.setPassword(etPass.getText().toString());
	        	
	        	//me.setListener(this);
    		
	        	res = me.create();
	        	
    		} else {
    			
    			me.setEmail(etEmail.getText().toString());
	        	me.setName(etName.getText().toString());
	        	me.setPassword(etPass.getText().toString());
	        	
	        	//me.setListener(this);
    			res = me.update();
    		}
    		
        	app.setMe(me);
        	
        	if(res==0) {
        		Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
        	}
        	
        	onCreateComplete(me.getClass(),res!=0);
        }
	}
	
	public void onSkip(View v) {
		User me = new User();
		me.setProvider("email");
		
		if(dialog!=null && dialog.isShowing())
			dialog.dismiss();
		dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
                "Registering. Please wait...", true);
		
		int res = 0;
		res = me.create();
		
		if(res>1) {
			app.setMe(me);
			onCreateComplete(me.getClass(),true);
		} else {
			Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
			onCreateComplete(me.getClass(),false);
		}
			
	}
	
	public void OnFBConnect(View v) {
		if(dialog!=null && dialog.isShowing())
			dialog.dismiss();
		dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
                "Registering. Please wait...", true);
		
		facebook.authorize(this, new String [] { "email", "read_friendlists", "friends_about_me", "publish_stream", "user_online_presence", "friends_online_presence", "read_stream", "xmpp_login"}, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
            	//TODO on registration - load contacts to friends list
            	String token = facebook.getAccessToken();
            	
            	User me = new User();
        		me.setProvider("facebook");
        		me.setAccess_token(token);
        		me.setListener(SettingsActivity.this);
        		
        		int res = me.create();
        		
        		if(res>0)
        			app.setMe(me);
        		else {
        			Toast.makeText(SettingsActivity.this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
					onCreateComplete(app.getMe().getClass(),false);
        		}
        		
        		etEmail.setText(app.getMe().getEmail());
        		etName.setText(app.getMe().getName());
        		
            	return;
            }

            @Override
            public void onFacebookError(FacebookError error) {
            	onCreateComplete(app.getMe().getClass(),false);
            }

            @Override
            public void onError(DialogError e) {
            	onCreateComplete(app.getMe().getClass(),false);
            }

            @Override
            public void onCancel() {}
        });
		
	}

	@Override
	public void onCreateComplete(Class clazz, Boolean success) {
		
		if(clazz.getSimpleName().contentEquals("User")) {
			if(dialog!=null && dialog.isShowing())
				dialog.dismiss();
			
			if(success) {
				app.registerToPushNotifications();
				app.loadFriends();
				
				finish();
        		
			} else {
				
				dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
						getString(R.string.error_registration_existing_user_failed), true);
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						
						if(dialog!=null && dialog.isShowing())
							dialog.dismiss();
					}
				});
				t.start();
				return;
			}
		}
	}

	@Override
	public void onUpdateComplete(Class clazz, Boolean success) {
		if(dialog!=null && dialog.isShowing())
			dialog.dismiss();
		
		if(success)
			finish();
	}

	@Override
	public void onDeleteComplete(Class clazz, Boolean success) {
		if(dialog!=null && dialog.isShowing())
			dialog.dismiss();
	}

	@Override
	public void onSyncComplete(Class clazz, Boolean success) {
		if(dialog!=null && dialog.isShowing())
			dialog.dismiss();
	}

	@Override
	public void onGetComplete(Class clazz, Boolean success) {
		
		if(dialog!=null && dialog.isShowing())
			dialog.dismiss();
	}

	@Override
	public void onGetWithDependentsComplete(Class clazz, Boolean success) {
		if(dialog!=null && dialog.isShowing())
			dialog.dismiss();
	}
	
	
}