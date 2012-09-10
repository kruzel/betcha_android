package com.betcha.activity;

import java.io.Serializable;
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
import com.betcha.model.Bet;
import com.betcha.model.Friend;
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
	
	private Friend friend;
	
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
					Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
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
					Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
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
		
		dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
                "Registering. Please wait...", true);
		
		int res = 0;
		try {
			res = me.create();
		} catch (SQLException e) {
			Toast.makeText(this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
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
		dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
                "Registering. Please wait...", true);
		
		facebook.authorize(this, new String [] { "email", "read_friendlists", "friends_about_me", "publish_stream", "user_online_presence", "friends_online_presence", "read_stream", "xmpp_login"}, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
            	//TODO on registration - load contacts to friends list
            	String token = facebook.getAccessToken();
            	
            	//TODO implement FB connect registration on dropabet server
            	User me = new User();
        		me.setProvider("facebook");
        		me.setAccess_token(token);
        		me.setListener(SettingsActivity.this);
        		
        		int res = 0;
        		try {
					res = me.create();
				} catch (SQLException e) {
					Toast.makeText(SettingsActivity.this, R.string.error_registration_failed, Toast.LENGTH_LONG).show();
					onCreateComplete(app.getMe().getClass(),false);
					e.printStackTrace();
				}
        		
        		if(res>0)
        			app.setMe(me);
        		
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
			dialog.dismiss();
			if(success) {
				app.registerToPushNotifications();
				
				TabActivity act = (TabActivity) getParent();
		        if(act==null)
		        	return;
		        
			    TabHost tabHost = act.getTabHost();  // The activity TabHost
			    if(tabHost != null){
			    	tabHost.getTabWidget().setEnabled(true);
		        	tabHost.setCurrentTab(0);
		        }
        		
			} else {
				
				dialog = ProgressDialog.show(this, getResources().getString(R.string.register), 
						getString(R.string.error_registration_existing_user_failed), true);
				Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						synchronized (this) {
							  try {
								wait(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
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

	@Override
	public void onGetComplete(Class clazz, Boolean success) {
		app.initFriendList();
		dialog.dismiss();
	}

	@Override
	public void onGetWithDependentsComplete(Class clazz, Boolean success) {
		dialog.dismiss();
	}
	
	
}