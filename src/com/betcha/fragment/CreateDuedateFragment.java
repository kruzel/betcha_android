package com.betcha.fragment;

import java.util.Calendar;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;

public class CreateDuedateFragment extends SherlockFragment {
    
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_STAKE = "stake";
    private static final String ARG_DUEDATE = "duedate";
    
    private String mSubject;
    private String mStake;
    
    private TextView dateView;
    private TextView timeView;
    
    private DateTime mDateTime;
    
    DatePickerDialog dateDialog;
	TimePickerDialog timeDialog;
    
    private OnDuedateSelectedListener mListener;
    
    public static CreateDuedateFragment newInstance(String subject, String stake) {
        CreateDuedateFragment f = new CreateDuedateFragment();
        
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        args.putString(ARG_STAKE, stake);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnDuedateSelectedListener {
        void onDuedateSelected(DateTime dateTime);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnDuedateSelectedListener) {
            mListener = (OnDuedateSelectedListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        mSubject = getArguments().getString(ARG_SUBJECT);
        mStake = getArguments().getString(ARG_STAKE);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_due_date, container, false);
        
        ImageView profileView = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
        // TODO set user's image
        
        TextView subjectView = (TextView) view.findViewById(R.id.tv_bet_topic);
        FontUtils.setTextViewTypeface(subjectView, CustomFont.HELVETICA_CONDENSED_BOLD);
        subjectView.setText(mSubject);
        
        TextView stakeView = (TextView) view.findViewById(R.id.tv_bet_reward);
        FontUtils.setTextViewTypeface(stakeView, CustomFont.HELVETICA_CONDENSED);
        stakeView.setText(mStake);
        
        TextView dateHintView = (TextView) view.findViewById(R.id.tv_bet_date_hint);
        FontUtils.setTextViewTypeface(dateHintView, CustomFont.HELVETICA_CONDENSED);

        dateView = (TextView) view.findViewById(R.id.tv_bet_date_button);
        timeView = (TextView) view.findViewById(R.id.tv_bet_time_button);
        FontUtils.setTextViewTypeface(dateView, CustomFont.HELVETICA_CONDENSED_BOLD);
        FontUtils.setTextViewTypeface(timeView, CustomFont.HELVETICA_CONDENSED_BOLD);
        dateView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });
        timeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker();
            }
        });
        
        mDateTime = null;
        if (savedInstanceState != null) {
            mDateTime = (DateTime) savedInstanceState.getSerializable(ARG_DUEDATE);
        }
        if (mDateTime == null) {
            mDateTime = new DateTime();
        }
        
        updateDateTime(dateView, timeView);
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ARG_DUEDATE, mDateTime);
    }
    
    private void updateDateTime(TextView dateView, TextView timeView) {
        DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd");
        dateView.setText(fmtDate.print(new DateTime()));
        
        DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");
        timeView.setText(fmtTime.print(new DateTime()));
    }
    
    private void updateDateTime() {
        View view = getView();
        if (view != null) {
//            TextView dateView = (TextView) view.findViewById(R.id.tv_bet_date_button);
//            TextView timeView = (TextView) view.findViewById(R.id.tv_bet_time_button);
            updateDateTime(dateView, timeView);
        }
    }
    
    private void openDatePicker() {
    	final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
		dateDialog = new DatePickerDialog(getActivity(), new OnDateSetListener() {
			
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				dateView.setText(new StringBuilder()
				// Month is 0 based, just add 1
				.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth));
			}
		}, year, month, day);
		dateDialog.show();
    }
    
    private void openTimePicker() {
    	final Calendar c = Calendar.getInstance();
        int hourBefore = c.get(Calendar.HOUR_OF_DAY);
        int minBefore = c.get(Calendar.MINUTE);
        //int is24HourView = c.get(Calendar.AM);
		timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				timeView.setText(new StringBuilder().append(pad(hourOfDay))
						.append(":").append(pad(minute)));
			}
		}, hourBefore, minBefore, true);
		timeDialog.show();
    }
    
    private OnDateSetListener mDateSetListener = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            // TODO
            updateDateTime();
        }
    };
    
    private OnTimeSetListener mTimeSetListener = new OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // TODO
            updateDateTime();
        }
    };
    
    private void submit() {
        if (mListener != null) {
            mListener.onDuedateSelected(mDateTime);
        }
    }
    
    private static String pad(int c) {
		if (c >= 10)
		   return String.valueOf(c);
		else
		   return "0" + String.valueOf(c);
	}
    
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//inflater.inflate(R.menu.due_date_fragment, menu);
		menu.add("Invite")
        .setIcon(R.drawable.ic_menu_invite)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
	}
    
    

	@Override
	public boolean onOptionsItemSelected(
		com.actionbarsherlock.view.MenuItem item) {
	
		if ("Invite".equals(item.getTitle())) {
			submit();
            return true;
	    }
		
		return super.onOptionsItemSelected(item);
	}
    
}
