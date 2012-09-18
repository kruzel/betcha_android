package com.betcha.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.FriendAdapter;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;

public class CreateBetActivity extends SherlockActivity implements OnClickListener {
	private BetchaApp app;
	
	private Bet bet;
	private TextView betSubject;
	private TextView betReward;
	private EditText betDueDate;
	private EditText betDueTime;
	private EditText betMyBet;
	
	SlidingDrawer sliding;
	private FriendAdapter friendAdapter;
    private ListView lvFriends;  
    
    private Button submitButton;
	
	DatePickerDialog dateDialog;
	TimePickerDialog timeDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createbet);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        app = (BetchaApp) getApplication();
                
        betSubject = (TextView) findViewById(R.id.bet_subject);
        betReward = (TextView) findViewById(R.id.bet_on);
        betDueDate = (EditText) findViewById(R.id.bet_due_date);
        betDueTime = (EditText) findViewById(R.id.bet_due_time);
        betMyBet = (EditText) findViewById(R.id.bet_my_bet);
        
        betDueDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus){
					OnDateClick(v);		
				}
			}
		});

        betDueTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {	
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus){
					OnTimeClick(v);		
				}
			}
		});
        
        submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this);
        
        app.initFriendList(); //in case not init yet
        
    }
    
    private void initFriendListAdapter() {
    	if(app.getFriends()!=null) {
    		for (User friend : app.getFriends()) {
				friend.setIsInvitedToBet(false);
			}
        	sliding=(SlidingDrawer) findViewById(R.id.drawer);
	        lvFriends = (ListView) sliding.findViewById(R.id.invites_list);
	        friendAdapter = new FriendAdapter(this, R.layout.invite_list_item, app.getFriends());
	        lvFriends.setAdapter(friendAdapter);
	        lvFriends.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        
        }
    }

	@Override
	protected void onResume() {
		DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");
		
		//initialize values	    	
		betDueDate.setText(fmtDate.print(new DateTime()));
	    betDueTime.setText(fmtTime.print(new DateTime()));
	    betSubject.setText("");
        betReward.setText("");
        betMyBet.setText("");
				
        initFriendListAdapter();
        
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
	
	public void onCreateBet(View v) {
		boolean errorFound = false;
		
		//validations
		if(betSubject.length() == 0) {
			betSubject.setError(getString(R.string.error_missing_bet_subject));
			errorFound = true;
		}
        if(betReward.length() == 0) {
        	betReward.setError(getString(R.string.error_missing_bet_reward));
        	errorFound = true;
        }
        if(betMyBet.length() == 0) {
        	betMyBet.setError(getString(R.string.error_missing_bet));
        	errorFound = true;
        }
         
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        DateTime betDueDateAndTime = null;
    	      
        if(errorFound == true) {
        	sliding.close();
        } else {
        	betDueDateAndTime = DateTime.parse(betDueDate.getText().toString() + " " + betDueTime.getText().toString(), fmt);
        	       
        	//create a new bet and userBet
        	bet = new Bet();
			bet.setDueDate(new DateTime()); //set to current date and time
        	bet.setSubject(betSubject.getText().toString());
        	bet.setReward(betReward.getText().toString());
        	bet.setDate(new DateTime());
        	bet.setDueDate(betDueDateAndTime);
        	bet.setState(Bet.STATE_OPEN);
        	
        	//set myself as first user
        	User me = app.getMe();
        	if(me==null) {
        		Toast.makeText(this, R.string.error_please_register, Toast.LENGTH_LONG).show();
        		return;
        	}
        	
        	bet.setOwner(me);
        	
        	Prediction prediction = new Prediction(bet);
        	prediction.setUser(me);
        	prediction.setPrediction(betMyBet.getText().toString());
        	prediction.setMyAck(getString(R.string.pending));
    		       	
        	bet.setOwnerPrediction(prediction);
        	
        	//set selected friends and send invite
        	List<User> participants = new ArrayList<User>();
        	for (User friend : app.getFriends()) {
				if(friend.getIsInvitedToBet()) {
					participants.add(friend);
				}
			}
        	
        	//create predictions place holders and send invites (after be created)
        	bet.setParticipants(participants);
        	
        	Toast.makeText(this, R.string.publishing_bet, Toast.LENGTH_LONG).show();
        	
        	try {
				bet.create();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	//prediction is create after bet is created as part of bet creation
	        	
        	finish();
        }
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		return super.onCreateDialog(id);
	}
	
	public void OnDateClick(View v) {
		final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
		dateDialog = new DatePickerDialog(this, new OnDateSetListener() {
			
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				betDueDate.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth));
			}
		}, year, month, day);
		dateDialog.show();
	}
	
	//DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");
	//betDueTime.setText(fmtTime.print(new DateTime()));
	public void OnTimeClick(View v) {
		final Calendar c = Calendar.getInstance();
        int hourBefore = c.get(Calendar.HOUR_OF_DAY);
        int minBefore = c.get(Calendar.MINUTE);
        //int is24HourView = c.get(Calendar.AM);
		timeDialog = new TimePickerDialog(CreateBetActivity.this, new TimePickerDialog.OnTimeSetListener() {
			
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				betDueTime.setText(new StringBuilder().append(pad(hourOfDay))
						.append(":").append(pad(minute)));
			}
		}, hourBefore, minBefore, true);
		timeDialog.show();
	}
	
	private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}

	public void onClick(View v) {
		onCreateBet(v);
		sliding.close();
	}
	
}