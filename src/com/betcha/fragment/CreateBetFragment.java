package com.betcha.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.R;
import com.betcha.adapter.FriendAdapter;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;

public class CreateBetFragment extends SherlockFragment {
	private Bet bet;
	private User loggedInUser;
	private List<User> friendsList;
	
	private TextView betOwner;
	private EditText betSubject;
	private EditText betReward;
	private TextView betDueDateSummary;
	private EditText betPrediction;
	private EditText betDueDate;
	private EditText betDueTime;
	private Button buttonIvite;
	private Button buttonPublish;
	
	DatePickerDialog dateDialog;
	TimePickerDialog timeDialog;
	
	ListView lvFriends;
	FriendAdapter friendAdapter;
		
	private OnBetDetailsEnteredListener listener;
	
	public interface OnBetDetailsEnteredListener {
		public void OnBetDetailsEntered();
	}

	public void init(Bet bet, User loggedInUser, List<User> friendsList) {
		this.bet = bet;
		this.loggedInUser = loggedInUser;
		this.friendsList = friendsList;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
            listener = (OnBetDetailsEnteredListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnBetDetailsEnteredListener");
        }
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.create_bet_fragment, container,false);
		
		betOwner = (TextView) view.findViewById(R.id.tv_bet_owner);    
		betSubject = (EditText) view.findViewById(R.id.et_bet_subject);        
        betReward = (EditText) view.findViewById(R.id.et_bet_reward);
        betDueDateSummary = (TextView) view.findViewById(R.id.et_bet_date);
        betPrediction = (EditText) view.findViewById(R.id.et_bet_prediction);
        buttonIvite = (Button) view.findViewById(R.id.buttonInvite);
        buttonPublish = (Button) view.findViewById(R.id.buttonPublish);
        betDueDate = (EditText) view.findViewById(R.id.bet_due_date);
        betDueTime = (EditText) view.findViewById(R.id.bet_due_time);
        
        //initialize values	    	
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");
		
		betDueDate.setText(fmtDate.print(new DateTime()));
		betDueTime.setText(fmtTime.print(new DateTime()));

		betOwner.setText(loggedInUser.getName());
        
        betSubject.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(betSubject.length() == 0) {
					betSubject.setError(getString(R.string.error_missing_bet_subject));
				} else {
					bet.setSubject(betSubject.getText().toString());
					betReward.setVisibility( View.VISIBLE);
				}
				return false;
			}
		});
        
        
        betReward.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(betReward.length() == 0) {
		        	betReward.setError(getString(R.string.error_missing_bet_reward));
		        } else {
		        	bet.setReward(betReward.getText().toString());
		        	betPrediction.setVisibility( View.VISIBLE);
		        }
				return false;
			}
		});
       
        betPrediction.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(betPrediction.length() == 0) {
		        	betPrediction.setError(getString(R.string.error_missing_bet));
		        } else {
		        	Prediction prediction = new Prediction(bet);
		        	prediction.setUser(loggedInUser);
		        	prediction.setPrediction(betPrediction.getText().toString());
		        	prediction.setMyAck(getString(R.string.pending));
		    		prediction.genId();       			      	        			
		        	bet.setOwnerPrediction(prediction);

		        	betDueDate.setVisibility(View.VISIBLE);
		        	betDueTime.setVisibility(View.VISIBLE);
		        	buttonIvite.setVisibility( View.VISIBLE);
				}
				return false;
			}
		});
        
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
                
        buttonIvite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
		        DateTime betDueDateAndTime = DateTime.parse(betDueDate.getText().toString() + " " + betDueTime.getText().toString(), fmt);
		        bet.setDueDate(betDueDateAndTime);
		        		
		        FragmentTransaction ft = getFragmentManager().beginTransaction();
		        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		        if (prev != null) {
		            ft.remove(prev);
		        }
		        ft.addToBackStack(null);

		        // Create and show the dialog.
		        final Dialog dialog = new Dialog(getActivity());
				dialog.setContentView(R.layout.friends_picker);
				dialog.setTitle(getResources().getString(R.string.select_friends));
								
				lvFriends = (ListView) dialog.findViewById(R.id.friends_list);
				friendAdapter = new FriendAdapter(getActivity(), R.layout.invite_list_item, CreateBetFragment.this.friendsList);
		        lvFriends.setAdapter(friendAdapter);
		        
		        for (User friend : friendsList) {
					if(friend.getIsInvitedToBet()) {
						friend.setIsInvitedToBet(false);
					}
				}
		        
		        Button dialogButton = (Button) dialog.findViewById(R.id.buttonOK);
				// if button is clicked, close the custom dialog
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						List<User> participants = new ArrayList<User>();
				    	for (User friend : friendsList) {
							if(friend.getIsInvitedToBet()) {
								participants.add(friend);
							}
						}
				    	
				    	bet.addParticipants(participants);
						
						bet.setState(Bet.STATE_OPEN);    	
				    	bet.setOwner(loggedInUser);
				    	
				    	dialog.dismiss();
				    	
				    	listener.OnBetDetailsEntered();
					}
				});
		        
		        dialog.show();
		        
			}
		});
        
        
		return view;
	}
	
	@Override
	public void onResume() {
		populate();
		super.onResume();
	}
	
	protected void populate() {
		if(bet.getSubject()!=null)
			betSubject.setText(bet.getSubject());
		if(bet.getReward()!=null)
			betReward.setText(bet.getReward());
		if(bet.getDueDate()!=null) {
			if(bet.getDueDate().plusHours(24).isAfterNow()) {
				DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");
				betDueDateSummary.setText(fmtTime.print(bet.getDueDate()));
			} else {
				//less then 24 hours left
				DateTimeFormatter fmtDate = DateTimeFormat.forPattern("MM-dd");
				betDueDateSummary.setText(fmtDate.print(bet.getDueDate()));
			} 
		}
		if(bet.getOwnerPrediction()!=null && bet.getOwnerPrediction().getPrediction()!=null)
			betPrediction.setText(bet.getOwnerPrediction().getPrediction());
	}
	
	public void OnDateClick(View v) {
		final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
		dateDialog = new DatePickerDialog(getActivity(), new OnDateSetListener() {
			
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
		timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			
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
}
