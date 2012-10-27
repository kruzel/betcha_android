package com.betcha.activity;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.fragment.CreatePredictionFragment;
import com.betcha.model.User;
import com.betcha.model.cache.IModelListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class LoginActivity extends SherlockFragmentActivity implements
		IModelListener {

	private BetchaApp app;
	private User tmpMe;

	private Facebook facebook = new Facebook("299434100154215");

	private Dialog dialog = null;
	private HttpStatus lastErrorCode = HttpStatus.OK;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		app = (BetchaApp) getApplication();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		User me = app.getCurUser();

		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (app.getCurUser() != null)
				finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onSubmit(View v) {
	    /*
		Intent intent = new Intent(this, LoginEmailActivity.class);
		startActivity(intent);
		*/
	    SherlockDialogFragment f = CreatePredictionFragment.newInstance("", new String[] { "Yes", "No", "Maybe", "Definitely", "Tomorrow", "Today", "Me", "You" });
	    f.show(getSupportFragmentManager(), "dialog");
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
									onCreateComplete(app.getCurUser().getClass(),HttpStatus.UNAUTHORIZED);
								
								JSONObject json = null;
								try {
									json = new JSONObject(resp);											
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
								if(json==null)
									onCreateComplete(app.getCurUser().getClass(), HttpStatus.UNAUTHORIZED);

								tmpMe = app.getCurUser();
								
								if (tmpMe == null || tmpMe.getId() == null) {
									tmpMe = new User();
								}
								
								tmpMe.setProvider("facebook");
								tmpMe.setUid(json.optString("id"));
								tmpMe.setAccess_token(token);
								tmpMe.setListener(LoginActivity.this);

								int res = 0;
								if (tmpMe.getId() == null) {
									res = tmpMe.create();
								} else {
									res = tmpMe.update();
								}

								if (res == 0) {
									onCreateComplete(app.getCurUser().getClass(),
											HttpStatus.UNPROCESSABLE_ENTITY);
								}
							}
						});
						t.start();

						return;
					}

					@Override
					public void onFacebookError(FacebookError error) {
						onCreateComplete(User.class, HttpStatus.UNAUTHORIZED);
					}

					@Override
					public void onError(DialogError e) {
						onCreateComplete(User.class, HttpStatus.SERVICE_UNAVAILABLE);
					}

					@Override
					public void onCancel() {
					}
				});

	}

	@Override
	public void onCreateComplete(Class clazz, HttpStatus errorCode) {

		if (clazz.getSimpleName().contentEquals("User")) {
			String msg = "";
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();

			switch (errorCode) {
			case OK:
			case CREATED:
				app.setMe(tmpMe);
				app.registerToPushNotifications();
				app.loadFriends();

				msg = getString(R.string.error_registration_succeeded);
				break;
			case SERVICE_UNAVAILABLE:
				app.setMe(null);
				msg = getString(R.string.error_connectivity_error);
				break;
			case INTERNAL_SERVER_ERROR:
				app.setMe(null);
				msg = getString(R.string.error_server_error);
				break;
			case UNAUTHORIZED:
				app.setMe(null);
				msg = getString(R.string.error_authorization_error);
				break;
			case UNPROCESSABLE_ENTITY:
				app.setMe(null);
				msg = getString(R.string.error_internal_error);
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

					if (lastErrorCode == HttpStatus.OK)
						finish();
				}
			});
			t.start();

		}
	}

	@Override
	public void onUpdateComplete(Class clazz, HttpStatus errorCode) {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();

		if (errorCode == HttpStatus.OK)
			finish();
	}

	@Override
	public void onDeleteComplete(Class clazz, HttpStatus errorCode) {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}

	@Override
	public void onSyncComplete(Class clazz, HttpStatus errorCode) {
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}

	@Override
	public void onGetComplete(Class clazz, HttpStatus errorCode) {

		if (dialog != null && dialog.isShowing())
			dialog.dismiss();
	}
}