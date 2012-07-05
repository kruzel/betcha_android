package com.betcha.activity;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.InviteAdapter;
import com.betcha.model.Bet;
import com.betcha.model.User;
import com.betcha.model.UserBet;

public class CreateBetActivity extends Activity implements OnClickListener {
	private BetchaApp app;
	private TabHost tabHost;
	
	private Bet bet;
	private TextView betSubject;
	private TextView betReward;
	private EditText betDueDate;
	private EditText betDueTime;
	private EditText betMyBet;
	
	SlidingDrawer sliding;
	private InviteAdapter inviteAdapter;
    private ListView invitesList;  
    private List<User> inviteUsers;
    private Button submitButton;
	
	DatePickerDialog dateDialog;
	TimePickerDialog timeDialog;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createbet);
        
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
        
                
        TabActivity act = (TabActivity) getParent();
        if(act==null)
        	return;
        
	    tabHost = act.getTabHost();  // The activity TabHost
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
        
        submitButton.setText(R.string.invite_friends);
        
        //inviteUsers = fetch all users from DB and contacts list, later from FB
        try {
			inviteUsers = app.getHelper().getUserDao().queryBuilder().orderBy("name", true).query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        if(inviteUsers!=null) {
        	sliding=(SlidingDrawer) findViewById(R.id.drawer);
	        invitesList = (ListView) sliding.findViewById(R.id.invites_list);
	        inviteAdapter = new InviteAdapter(this, R.layout.invite_list_item, inviteUsers);
	        invitesList.setAdapter(inviteAdapter);
        }
				
		super.onResume();		
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
        	
        	UserBet userBet = new UserBet();
        	userBet.setBet(bet);
        	userBet.setUser(me);
        	userBet.setDate(new DateTime()); //current betting time
        	userBet.setMyBet(betMyBet.getText().toString());
        	userBet.setMyAck(getString(R.string.pending));
    		       	
        	Toast.makeText(this, R.string.publishing_bet, Toast.LENGTH_LONG).show();
        	
        	app.createCreateBetTask().setValues(bet, userBet, me);
        	app.getCreateBetTask().run();
        	
        	//TODO - set selected friends and send invite
			String subject = "Betcha";
			String emailtext = me.getName() + " is inviting you to bet on " + bet.getSubject() + ", losers buy winners a " + bet.getReward();
			emailtext += "\n\nLink to bet: http://betcha.com/" + bet.getUuid();
			emailtext += "\n\nLink to app on Google Play ...";
			emailtext += "\n\nLink to app on AppStore ...";
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	        emailIntent.setType("text/html");
	        String [] receipiants = new String[inviteUsers.size()];
	        int i=0;
	        for (User user : inviteUsers) {
	        	receipiants[i++] = user.getEmail();
			}
	        
	        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receipiants);
	        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
	        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailtext);
	        Intent chooserIntent = Intent.createChooser(emailIntent, "Send mail...");
	        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(chooserIntent);
	        
        	// When clicked, move back to records tab
            if(tabHost != null){
            	tabHost.setCurrentTab(1);
            }
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
	}
}