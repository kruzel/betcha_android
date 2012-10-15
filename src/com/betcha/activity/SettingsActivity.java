package com.betcha.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

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

public class SettingsActivity extends SherlockActivity implements
		IModelListener {
	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	private BetchaApp app;
	private User tmpMe;

	private Facebook facebook = new Facebook("299434100154215");

	private EditText etEmail;
	private EditText etPass;
	private EditText etName;

	private Dialog dialog = null;
	private ErrorCode lastErrorCode = ErrorCode.OK;

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

		if (me != null) {

			String myEmail = app.getMe().getEmail();

			if (myEmail != null && myEmail.length() != 0) {
				etEmail.setText(myEmail);
			}

			String myPass = app.getMe().getPassword();
			if (myPass != null && myPass.length() != 0) {
				etPass.setText(myPass);
			}

			String myName = app.getMe().getName();
			if (myName != null && myName.length() != 0) {
				etName.setText(myName);
			}
		}

		if (me != null && me.getId() != null) {
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
			if (app.getMe() != null)
				finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onSubmit(View v) {
		boolean errorFound = false;

		// validations
		if (etEmail.getText().length() == 0) {
			etEmail.setError(getString(R.string.error_bad_email));
			errorFound = true;
		}

		try {
			if (!EMAIL_ADDRESS_PATTERN.matcher(etEmail.getText().toString())
					.matches()) {
				etEmail.setError(getString(R.string.error_bad_email));
				errorFound = true;
			}
		} catch (NullPointerException exception) {
			errorFound = true;
		}

		if (etPass.getText().length() < 6) {
			etEmail.setError(getString(R.string.error_pass_short));
			errorFound = true;
		}

		if (etName.getText().length() == 0) {
			etName.setText(etEmail.getText().toString());
		}

		if (errorFound == false) {
			tmpMe = app.getMe();
			int res = 0;

			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			dialog = ProgressDialog.show(this,
					getResources().getString(R.string.register),
					"Registering. Please wait...", true);

			if (tmpMe == null || tmpMe.getId() == null) {
				if (tmpMe == null) {
					tmpMe = new User();
				}

				tmpMe.setProvider("email");
				tmpMe.setEmail(etEmail.getText().toString());
				tmpMe.setName(etName.getText().toString());
				tmpMe.setPassword(etPass.getText().toString());

				tmpMe.setListener(this);

				res = tmpMe.create();

			} else {

				tmpMe.setEmail(etEmail.getText().toString());
				tmpMe.setName(etName.getText().toString());
				tmpMe.setPassword(etPass.getText().toString());

				tmpMe.setListener(this);
				res = tmpMe.update();
			}

			// app.setMe(me);

			if (res == 0) {
				onCreateComplete(tmpMe.getClass(), ErrorCode.ERR_INTERNAL);
			}
		}
	}

	public void onResetPassword(View v) {
		// validations
		if (etEmail.getText().length() == 0) {
			etEmail.setError(getString(R.string.error_bad_email));
			return;
		}
					
		tmpMe = new User();
		tmpMe.setProvider("email");
		tmpMe.setEmail(etEmail.getText().toString());

		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
		dialog = ProgressDialog.show(this,
				getResources().getString(R.string.register),
				"Sending password email. Please wait...", true);

		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				tmpMe.resetPassword();
				
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
			}
		});
		t.start();
	}

	public void OnFBConnect(View v) {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
		dialog = ProgressDialog.show(this,
				getResources().getString(R.string.register),
				"Registering. Please wait...", true);

		facebook.authorize(this, new String[] { "email", "read_friendlists",
				"friends_about_me", "publish_stream", "user_online_presence",
				"friends_online_presence", "read_stream", "xmpp_login" },
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
						
						Thread t = new Thread(new Runnable() {
							
							@Override
							public void run() {
								// TODO on registration - load contacts to friends list
								String token = facebook.getAccessToken();
								
								String resp = null;
								try {
									resp = facebook.request("me");
								} catch (MalformedURLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								if(resp==null)
									onCreateComplete(app.getMe().getClass(),ErrorCode.ERR_UNAUTHOTISED);
								
								JSONObject json = null;
								try {
									json = new JSONObject(resp);											
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
								if(json==null)
									onCreateComplete(app.getMe().getClass(), ErrorCode.ERR_UNAUTHOTISED);

								tmpMe = new User();
								tmpMe.setProvider("facebook");
								tmpMe.setUid(json.optString("id"));
								tmpMe.setAccess_token(token);
								tmpMe.setListener(SettingsActivity.this);

								int res = tmpMe.create();

								if (res == 0) {
									onCreateComplete(app.getMe().getClass(),
											ErrorCode.ERR_INTERNAL);
								}
							}
						});
						t.start();

						return;
					}

					@Override
					public void onFacebookError(FacebookError error) {
						onCreateComplete(User.class, ErrorCode.ERR_FACEBOOK_ERROR);
					}

					@Override
					public void onError(DialogError e) {
						onCreateComplete(User.class, ErrorCode.ERR_FACEBOOK_ERROR);
					}

					@Override
					public void onCancel() {
					}
				});

	}

	@Override
	public void onCreateComplete(Class clazz, ErrorCode errorCode) {

		if (clazz.getSimpleName().contentEquals("User")) {
			String msg = "";
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();

			switch (errorCode) {
			case OK:
				app.setMe(tmpMe);
				if(tmpMe.getEmail()!=null)
					etEmail.setText(tmpMe.getEmail());
				if(tmpMe.getName()!=null)
					etName.setText(tmpMe.getName());
				
				app.registerToPushNotifications();
				app.loadFriends();

				msg = getString(R.string.error_registration_succeeded);
				break;
			case ERR_CONNECTIVITY:
				app.setMe(null);
				msg = getString(R.string.error_connectivity_error);
				break;
			case ERR_SERVER_ERROR:
				app.setMe(null);
				msg = getString(R.string.error_server_error);
				break;
			case ERR_UNAUTHOTISED:
				app.setMe(null);
				msg = getString(R.string.error_authorization_error);
				break;
			case ERR_INTERNAL:
				app.setMe(null);
				msg = getString(R.string.error_internal_error);
				break;
			case ERR_FACEBOOK_ERROR:
				app.setMe(null);
				msg = getString(R.string.error_facebook_error);
				break;
			default:
				app.setMe(null);
				msg = getString(R.string.error_internal_error);
				break;
			}

			lastErrorCode = errorCode;

			dialog = ProgressDialog.show(this,
					getResources().getString(R.string.register), msg, true);
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					if (dialog != null && dialog.isShowing())
						dialog.dismiss();

					if (lastErrorCode == ErrorCode.OK)
						finish();
				}
			});
			t.start();

		}
	}

	@Override
	public void onUpdateComplete(Class clazz, ErrorCode errorCode) {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();

		if (errorCode == ErrorCode.OK)
			finish();
	}

	@Override
	public void onDeleteComplete(Class clazz, ErrorCode errorCode) {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}

	@Override
	public void onSyncComplete(Class clazz, ErrorCode errorCode) {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}

	@Override
	public void onGetComplete(Class clazz, ErrorCode errorCode) {

		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
}