package com.betcha.activity;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.User;
import com.betcha.model.cache.IModelListener;
import com.betcha.model.cache.SyncTask;

public class LoginEmailActivity extends SherlockActivity implements
		IModelListener {
	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	private BetchaApp app;
	private User tmpMe;

	private EditText etEmail;
	private EditText etPass;
	private EditText etName;

	private Dialog dialog = null;
	private HttpStatus lastErrorCode = HttpStatus.OK;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_email_activity);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		app = (BetchaApp) getApplication();

		etEmail = (EditText) findViewById(R.id.editTextEmail);
		etPass = (EditText) findViewById(R.id.editTextPass);
		etName = (EditText) findViewById(R.id.editTextName);
		
		TextView tvEmail = (TextView) findViewById(R.id.textView1);
		TextView tvPass = (TextView) findViewById(R.id.textView2);
		TextView tvName = (TextView) findViewById(R.id.textViewName);
		
		FontUtils.setTextViewTypeface(etEmail, R.id.textView1, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(etPass, R.id.textView2, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(etName, R.id.textViewName, CustomFont.HELVETICA_CONDENSED);
		
		FontUtils.setTextViewTypeface(tvEmail, R.id.editTextEmail, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvName, R.id.editTextPass, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvPass, R.id.editTextName, CustomFont.HELVETICA_CONDENSED);
	}

	@Override
	protected void onResume() {
		User me = app.getCurUser();
		Button btEmailReg = (Button) findViewById(R.id.buttonEmailReg);

		if (me != null) {

			String myEmail = app.getCurUser().getEmail();

			if (myEmail != null && myEmail.length() != 0) {
				etEmail.setText(myEmail);
			}

			String myPass = app.getCurUser().getPassword();
			if (myPass != null && myPass.length() != 0) {
				etPass.setText(myPass);
			}

			String myName = app.getCurUser().getName();
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
			finish();
			return true;
		default:
			
		}
		
		return super.onOptionsItemSelected(item);
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
			tmpMe = app.getCurUser();
			int res = 0;

			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			dialog = ProgressDialog.show(this,
					getResources().getString(R.string.register),
					"Registering. Please wait...", true);

			if (tmpMe == null || tmpMe.getId() == null) {
				tmpMe = new User();

				tmpMe.setProvider("email");
				tmpMe.setEmail(etEmail.getText().toString());
				tmpMe.setName(etName.getText().toString());
				tmpMe.setPassword(etPass.getText().toString());

				tmpMe.setListener(this);

				tmpMe.setShouldCreateToken(true);
				res = tmpMe.create();

			} else {
				tmpMe.setProvider("email");
				tmpMe.setEmail(etEmail.getText().toString());
				tmpMe.setName(etName.getText().toString());
				tmpMe.setPassword(etPass.getText().toString());

				tmpMe.setListener(this);
				res = tmpMe.update();
			}

			if (res == 0) {
				onCreateComplete(tmpMe.getClass(), HttpStatus.UNPROCESSABLE_ENTITY);
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

	@Override
	public void onCreateComplete(Class clazz, HttpStatus errorCode) {

		tmpMe.setListener(null);
		
		if (clazz.getSimpleName().contentEquals("User")) {
			String msg = "";

			switch (errorCode) {
			case OK:
			case CREATED:
				app.setMe(tmpMe);
				if(tmpMe.getEmail()!=null)
					etEmail.setText(tmpMe.getEmail());
				if(tmpMe.getName()!=null)
					etName.setText(tmpMe.getName());
				
				app.registerToPushNotifications();
				app.loadFriends(); //TODO move into SyncTask
				SyncTask.run(this);

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

			if (lastErrorCode != HttpStatus.OK && lastErrorCode != HttpStatus.CREATED) {
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
					}
				});
				t.start();
			}
		} 
	}

	@Override
	public void onUpdateComplete(Class clazz, HttpStatus errorCode) {
		tmpMe.setListener(null);
		
		if (dialog != null && dialog.isShowing())
			dialog.dismiss();

		if (errorCode == HttpStatus.OK) {
			Intent intent = new Intent(getApplicationContext(), BetsListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
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
		if(clazz.getSimpleName().contentEquals("SyncTask")) {
			
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

					if (lastErrorCode == HttpStatus.OK || lastErrorCode == HttpStatus.CREATED) {
							Intent intent = new Intent(getApplicationContext(), BetsListActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
					}
				}
			});
			t.start();
		} else {
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
		}
	}
}