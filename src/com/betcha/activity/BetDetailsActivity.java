package com.betcha.activity;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.PredictionAdapter;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;
import com.betcha.model.cache.IModelListener;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class BetDetailsActivity extends SherlockActivity implements OnClickListener, IModelListener {
	private BetchaApp app;
	private PredictionAdapter predictionAdapter;
	private List<Prediction> predictions;

	private Bet bet;
	private Prediction myPrediction;
	private TextView tvState;
	private TextView tvDate;
	private TextView tvOwner;
	private TextView tvSubject;

	private PullToRefreshListView lvPredictions;
	private Button btnPublishResult;

	private View footerView;
	private EditText etMyBet;

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bet_details);
		
		ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

		app = (BetchaApp) getApplication();

		lvPredictions = (PullToRefreshListView) findViewById(R.id.pull_to_refresh_betdetails_list);

		LayoutInflater inflater = this.getLayoutInflater();
		View header = inflater.inflate(R.layout.bet_details_header, null);
		lvPredictions.addHeaderView(header);

		tvState = (TextView) findViewById(R.id.tv_bet_header_state);
		tvDate = (TextView) findViewById(R.id.tv_bet_header_date);
		tvOwner = (TextView) findViewById(R.id.tv_bet_header_owner);
		tvSubject = (TextView) findViewById(R.id.tv_bet_header_subject);

		footerView = inflater.inflate(R.layout.add_bet_footer, null);
		lvPredictions.addFooterView(footerView);
		etMyBet = (EditText) findViewById(R.id.editTextAddBet);
		etMyBet.clearFocus();

		lvPredictions.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				getFromServer();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		String betId = intent.getStringExtra("betId");
		Boolean isNewBet = intent.getBooleanExtra("is_new_bet", false);

		if (betId == "-1")
			return;

		try {
			
			List<Bet> bets = Bet.getModelDao().queryForEq("id",betId);
			if(bets!=null && bets.size()>0)
				bet = bets.get(0);
		
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		if(bet==null)
			return;

		if (isNewBet) {
			dialog = ProgressDialog.show(BetDetailsActivity.this.getParent(),
					"", getString(R.string.msg_bet_loading), true);
		}

		populateList();

		// Creating a button - only if bet owner is the current logged in user
		if (bet.getOwner().getId().equals(app.getMe().getId())) {
			if (btnPublishResult == null) {
				btnPublishResult = new Button(BetDetailsActivity.this);
				btnPublishResult.setText(getString(R.string.publish_results));
				btnPublishResult.setOnClickListener(BetDetailsActivity.this);

				// Adding button to listview at footer
				lvPredictions.addFooterView(btnPublishResult);
			}
		} else {
			if (btnPublishResult != null) {
				lvPredictions.removeFooterView(btnPublishResult);
				btnPublishResult = null;
			}

		}

		//getFromServer();

		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yy HH:mm");

		tvState.setText(bet.getState());
		tvDate.setText(fmt.print(bet.getDate()));
		tvOwner.setText(bet.getOwner().getName());
		tvSubject.setText(bet.getSubject());

	}

	protected void populateList() {
		// TODO update bet details
		
		// update predictions
		try {
			predictions = Prediction.getModelDao().queryForEq("bet_id",
					bet.getId());
		} catch (SQLException e) {
			Log.e(getClass().getSimpleName(),
					".onCreate() - failed getting bet list");
			e.printStackTrace();
		}

		List<Prediction> myPredictions = null;
		myPrediction = null;
		try {
			Map<String, Object> myBetKey = new HashMap<String, Object>();
			myBetKey.put("bet_id", bet.getId());
			myBetKey.put("user_id", app.getMe().getId());

			myPredictions = Prediction.getModelDao().queryForFieldValues(
					myBetKey);
		} catch (SQLException e) {
			Log.e(getClass().getSimpleName(),
					".onCreate() - no user bet for current user for this bet");
			e.printStackTrace();
		}

		if (myPredictions.size() > 0) {
			myPrediction = myPredictions.get(0);
		}

		predictionAdapter = new PredictionAdapter(this, R.layout.bets_list_item,
				predictions);

		lvPredictions.setAdapter(predictionAdapter);
	}

	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.menu_refresh:
        	lvPredictions.setRefreshing();
        	getFromServer();
            return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.bet_details_activity, menu);
	    return true;
	}

	// Prediction list OnClick
	public void onClick(View v) {

		for (int i = 2; i < lvPredictions.getChildCount() - 1; i++) {
			View vListItem = lvPredictions.getChildAt(i);
			CheckBox cb = (CheckBox) vListItem
					.findViewById(R.id.cb_user_bet_win);
			if (cb != null)
				predictions.get(i - 2).setResult(cb.isChecked());
		}

		Prediction.update(predictions,bet.getId());

		bet.setState(Bet.STATE_CLOSED);
		bet.update();
		tvState.setText(bet.getState());
	}

	protected void getFromServer() {
		if(bet==null)
			return;
		
		bet.setListener(this);
		bet.get();
		
		app.setBetId("-1"); //avoid going here on next resume
	}

	public void onAddBet(View v) { // by this user invited to bet
		boolean errorFound = false;

		etMyBet.clearFocus();

		// validations
		if (etMyBet.length() == 0) {
			etMyBet.setError(getString(R.string.error_missing_bet));
			errorFound = true;
		}

		if (errorFound == false) {

			// set myself as first user
			User me = app.getMe();
			if (me == null) {
				Toast.makeText(this, R.string.error_please_register,
						Toast.LENGTH_LONG).show();
				return;
			}

			if (myPrediction == null) {
				// if current user have no bet yet then create current user
				// UserBet for the existing bet
				myPrediction = new Prediction(bet);
				myPrediction.setUser(me);
				myPrediction.setPrediction(etMyBet.getText().toString());
				myPrediction.setMyAck(getString(R.string.pending));

				Toast.makeText(this, R.string.publishing_bet, Toast.LENGTH_LONG).show();

				myPrediction.create();
			} else {
				// just update current UserBet
				myPrediction.setPrediction(etMyBet.getText().toString());
				myPrediction.setMyAck(getString(R.string.pending));

				Toast.makeText(this, R.string.publishing_bet, Toast.LENGTH_LONG).show();
				lvPredictions.invalidate();
				myPrediction.update();
			}

			populateList();
		}
	}

	@Override
	public void onCreateComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetComplete(Class clazz, Boolean success) {
		lvPredictions.onRefreshComplete();
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		
		populateList();
		
		lvPredictions.onRefreshComplete();
	}

	@Override
	public void onGetWithDependentsComplete(Class clazz, Boolean success) {
		lvPredictions.onRefreshComplete();
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
		
		populateList();
		
		lvPredictions.onRefreshComplete();
	}

	@Override
	public void onDeleteComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

}
