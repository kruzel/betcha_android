package com.betcha.fragment;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.R;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;
import com.betcha.model.User;

public class BetDetailsFragment extends SherlockFragment {
	private Bet bet;
	User curUser;
	
	private LinearLayout lvPredictions;
	private PredictionViewHolder[] rlPredictionItems;
	private LayoutInflater inflater;
	EditText tvCurUserPrediction;
		
	private ImageView ivProfPic;
	private TextView tvBetDate;
	private TextView tvBetOwner;
	
	// internal frame 
	TextView tvBetSubject;
	TextView tvBetReward;

	public void init(Bet bet, User curUser) {
		this.bet = bet;
		this.curUser = curUser;
		populate();
	}
	
	public void refresh() {
		//predictionAdapter = null;
		populate();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		this.inflater = inflater;
				
		View view = (ViewGroup) inflater.inflate(R.layout.bets_list_item, container);
		ImageView imgNav = (ImageView) view.findViewById(R.id.image_view_nav);
		imgNav.setVisibility(View.INVISIBLE);
		
		lvPredictions = (LinearLayout) view.findViewById(R.id.lv_bet_predictions);
		
		ivProfPic = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
		tvBetDate = (TextView) view.findViewById(R.id.tv_bet_date);
		tvBetOwner = (TextView) view.findViewById(R.id.tv_bet_owner);
		
		// internal frame 
		tvBetSubject = (TextView) view.findViewById(R.id.tv_bet_topic);
		tvBetReward = (TextView) view.findViewById(R.id.tv_bet_reward);
				
		return view;
	}
	
	private void inflatePredictions(LayoutInflater inflater, View view) {
		List<Prediction> predictions = bet.getPredictions();
		if(predictions==null || predictions.size()==0)
			return;
		
		int predictionSize = predictions.size();
				
		rlPredictionItems = new PredictionViewHolder[predictionSize];
	    
	    for(int i = 0; i<predictionSize ; i++ ) {
	    	rlPredictionItems[i] = new PredictionViewHolder();
	    	rlPredictionItems[i].layout = (RelativeLayout) inflater.inflate(R.layout.bet_prediction_list_item, lvPredictions, false);
	    	rlPredictionItems[i].ivParticipantProfPic = (ImageView) rlPredictionItems[i].layout.findViewById(R.id.iv_participant_pic);
	    	rlPredictionItems[i].tvParticipantName = (TextView) rlPredictionItems[i].layout.findViewById(R.id.tv_prediction_user_name);
	    	rlPredictionItems[i].tvParticipantPrediction = (EditText) rlPredictionItems[i].layout.findViewById(R.id.tv_prediction_text);
	    	rlPredictionItems[i].cbParticipantWinner = (CheckBox) rlPredictionItems[i].layout.findViewById(R.id.cb_prediction_win);
	    	rlPredictionItems[i].btnOK = (Button) rlPredictionItems[i].layout.findViewById(R.id.buttonEdit);
	    	lvPredictions.addView(rlPredictionItems[i].layout);
	    }
	    
	    LayoutParams itemLayoutParams = rlPredictionItems[0].layout.getLayoutParams();
	    LayoutParams layoutParams = lvPredictions.getLayoutParams();
	    layoutParams.height = itemLayoutParams.height * predictionSize;
	    lvPredictions.setLayoutParams(layoutParams);
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
	}

	protected void populate() {		
		//bet owner and other details (outer frame)
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM HH:mm");
		bet.getOwner().setProfilePhoto(ivProfPic);
		tvBetDate.setText(fmt.print(bet.getDate()));
		tvBetOwner.setText(bet.getOwner().getName());
		
		tvBetSubject.setText(bet.getSubject());
		tvBetReward.setText(bet.getReward());
		
		List<Prediction> predictions = bet.getPredictions();
		if(predictions==null || predictions.size()==0)
			return;
		
		int predictionSize = predictions.size();
		
		if(rlPredictionItems==null)
			inflatePredictions(inflater,lvPredictions);
		
		for(int i = 0; i<predictionSize ; i++ ) {
			Prediction prediction = predictions.get(i);
			prediction.getUser().setProfilePhoto(rlPredictionItems[i].ivParticipantProfPic);
	    	rlPredictionItems[i].tvParticipantName.setText(prediction.getUser().getName());
	    	rlPredictionItems[i].tvParticipantPrediction.setText(prediction.getPrediction());
	    	
	    	if(curUser.getId().equals(prediction.getBet().getOwner().getId())) {
	    		rlPredictionItems[i].cbParticipantWinner.setClickable(true);
			} else {
				rlPredictionItems[i].cbParticipantWinner.setClickable(false);
			}
	    	
	    	if(curUser.getId().equals(prediction.getUser().getId())) {
	    		tvCurUserPrediction=rlPredictionItems[i].tvParticipantPrediction;
	    		rlPredictionItems[i].tvParticipantPrediction.setEnabled(true);
	    		rlPredictionItems[i].tvParticipantPrediction.setTag(prediction);
	    		rlPredictionItems[i].tvParticipantPrediction.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(!hasFocus) {
							EditText et = (EditText) v;
							Prediction p = (Prediction) v.getTag();
							p.setPrediction(et.getText().toString());
							p.update();
						}
						
					}
				});
	    		
	    		rlPredictionItems[i].btnOK.setTag(prediction);
		    	rlPredictionItems[i].btnOK.setVisibility(View.VISIBLE);
		    	rlPredictionItems[i].btnOK.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//Prediction p = (Prediction) v.getTag();
						
						tvCurUserPrediction.clearFocus();
					}
				});
	    	} else {
	    		rlPredictionItems[i].tvParticipantPrediction.setEnabled(false);
	    		rlPredictionItems[i].btnOK.setVisibility(View.INVISIBLE);
			}
	    	
	    	if(prediction.getResult()!=null)
	    		rlPredictionItems[i].cbParticipantWinner.setSelected(prediction.getResult());
	    	else
	    		rlPredictionItems[i].cbParticipantWinner.setSelected(false);
	    	
	    	rlPredictionItems[i].cbParticipantWinner.setTag(prediction);
			
	    	rlPredictionItems[i].cbParticipantWinner.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Prediction prediction = (Prediction) v.getTag();
					if(prediction.getResult()==null) {
						prediction.setResult(true);
					} else {
						prediction.setResult(!prediction.getResult());
					}
					prediction.update();
				}
			});
	    }
	}

	private class PredictionViewHolder {
		RelativeLayout layout;
		ImageView ivParticipantProfPic;
		TextView tvParticipantName;
		EditText tvParticipantPrediction;
		CheckBox cbParticipantWinner;
		Button btnOK;
	}
}
