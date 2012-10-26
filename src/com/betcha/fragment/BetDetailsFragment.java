package com.betcha.fragment;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.betcha.BetchaApp;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.adapter.PredictionWithCbxAdapter;

public class BetDetailsFragment extends SherlockFragment {
	private BetchaApp app;
	
	private ListView lvPredictions;
	private FrameLayout frmPredictionContainer;
	private PredictionWithCbxAdapter predictionAdapter;
	
	private ImageView ivProfPic;
	private TextView tvBetDate;
	private TextView tvBetOwner;
	
	// internal frame 
	TextView tvBetSubject;
	TextView tvBetReward;
	
	public void refresh() {
		predictionAdapter = null;
		populate();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = BetchaApp.getInstance();
                
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.bet_details_fragment, container);
		
		frmPredictionContainer = (FrameLayout) view.findViewById(R.id.frm_bet_predictions_container);
		lvPredictions = (ListView) view.findViewById(R.id.lv_bet_predictions);
		
		ivProfPic = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
		tvBetDate = (TextView) view.findViewById(R.id.tv_bet_date);
		tvBetOwner = (TextView) view.findViewById(R.id.tv_bet_owner);
		
		// internal frame 
		tvBetSubject = (TextView) view.findViewById(R.id.tv_bet_topic);
		tvBetReward = (TextView) view.findViewById(R.id.tv_bet_reward);
		
		FontUtils.setTextViewTypeface(tvBetReward, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvBetDate, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvBetOwner, CustomFont.HELVETICA_CONDENSED);
        FontUtils.setTextViewTypeface(tvBetSubject, CustomFont.HELVETICA_CONDENSED_BOLD);
				        
		return view;
	}
	
	@Override
	public void onResume() {
		populate();
		super.onResume();
	}
	
	protected void populate() {
		//bet owner and other details (outer frame)
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM HH:mm");
		app.getCurBet().getOwner().setProfilePhoto(ivProfPic);
		tvBetOwner.setText(app.getCurBet().getOwner().getName());
		
		if(app.getCurBet().getDueDate().isAfterNow()) {
			if(app.getCurBet().getDueDate().minusHours(24).isBeforeNow()) {
				if(app.getCurBet().getDueDate().minusMinutes(60).isBeforeNow()) {
					//less then 1 hr
					tvBetDate.setText(Integer.toString(Minutes.minutesBetween(DateTime.now(), app.getCurBet().getDueDate()).getMinutes()) + " m");
				} else {
					//less then 24 hours left
					tvBetDate.setText(Integer.toString(Hours.hoursBetween(DateTime.now(), app.getCurBet().getDueDate()).getHours()) + " hr");
				}
			} else {
				//more then 24 hours left
				tvBetDate.setText(Integer.toString(Days.daysBetween(DateTime.now(), app.getCurBet().getDueDate()).getDays()) + " d");
	 		}
		} else {
			tvBetDate.setText("--");
		}
		
		tvBetSubject.setText(app.getCurBet().getSubject());
		tvBetReward.setText(app.getCurBet().getReward());
		
		LayoutParams layoutParams = frmPredictionContainer.getLayoutParams();
		layoutParams.height = app.getCurBet().getPredictionsCount() > 2 ? 100 * app.getCurBet().getPredictionsCount() : 200;
		frmPredictionContainer.setLayoutParams(layoutParams);
		
		if(predictionAdapter==null) {
			predictionAdapter = new PredictionWithCbxAdapter(getActivity(), R.layout.bet_prediction_list_item, app.getCurBet().getPredictions());
			lvPredictions.setAdapter(predictionAdapter);
		} else {
			predictionAdapter.notifyDataSetChanged();
		}
	}

}
