package com.betcha.fragment;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.BetchaApp;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.adapter.PredictionAdapter;
import com.betcha.adapter.PredictionAdapter.OnPredictionEditListener;
import com.betcha.fragment.ChangePredictionDialogFragment.OnPredictionSelectedListener;
import com.betcha.model.TopicCategory;
import com.betcha.model.Prediction;
import com.betcha.model.PredictionOption;
import com.betcha.model.Stake;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class BetDetailsFragment extends SherlockFragment implements OnPredictionEditListener, OnPredictionSelectedListener {
	private BetchaApp app;
	
	private Prediction predictionEdit;
	private TextView predictionEditView;
	ChangePredictionDialogFragment predictionDialog;
	
	private ListView lvPredictions;
	private FrameLayout frmPredictionContainer;
	private PredictionAdapter predictionAdapter;
	
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	
	private ImageView ivProfPic;
	private TextView tvBetDate;
	private TextView tvBetOwner;
	private ImageView ivBetCategory;
	
	// internal frame 
	private TextView tvBetSubject;
	private TextView tvBetReward;
	private ImageView ivBetRewardImage;
	
	public void refresh() {
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
		ivBetCategory = (ImageView) view.findViewById(R.id.iv_bet_category);
		
		// internal frame 
		tvBetSubject = (TextView) view.findViewById(R.id.tv_bet_topic);
		tvBetReward = (TextView) view.findViewById(R.id.tv_bet_reward);
		ivBetRewardImage = (ImageView) view.findViewById(R.id.iv_bet_reward);
		
		FontUtils.setTextViewTypeface(tvBetReward, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvBetDate, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvBetOwner, CustomFont.HELVETICA_CONDENSED);
        FontUtils.setTextViewTypeface(tvBetSubject, CustomFont.HELVETICA_CONDENSED_BOLD);
        
        if(imageLoader==null) {
			imageLoader = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
				        
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
		String name = app.getCurBet().getOwner().getName();
		int spacePos = name.indexOf(" ");
		if(spacePos==-1)
			spacePos=name.length();
		tvBetOwner.setText(name.substring(0, spacePos));
		
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
			tvBetDate.setText("Due");
		}
		
		tvBetSubject.setText(app.getCurBet().getTopicCustom());
		if(app.getCurBet().getStake().getName().equals("Coins"))
			tvBetReward.setText("" + app.getCurBet().getRewardAmount() + " " + app.getCurBet().getStake().getName());
		else
			tvBetReward.setText(app.getCurBet().getStake().getName());
		
		Stake r = Stake.get(app.getCurBet().getStake().getName());
		if(r!=null) {
			imageLoader.displayImage(app.getCurBet().getStake().getImage_url() , ivBetRewardImage,defaultOptions);
		}
		
		if(app.getCurBet().getCategory()!=null) {
			imageLoader.displayImage(app.getCurBet().getCategory().getImageUrl() , ivBetCategory,defaultOptions);
		} else {
			imageLoader.cancelDisplayTask(ivBetCategory);
			ivBetCategory.setImageResource(android.R.color.transparent);
		}
		
		LayoutParams layoutParams = frmPredictionContainer.getLayoutParams();
		layoutParams.height = app.getCurBet().getPredictionsCount() > 2 ? 88 * app.getCurBet().getPredictionsCount() : 88*2;
		frmPredictionContainer.setLayoutParams(layoutParams);
		
		predictionAdapter = new PredictionAdapter(getActivity(), R.layout.bet_prediction_list_item, app.getCurBet().getPredictions());
		lvPredictions.setAdapter(predictionAdapter);
		predictionAdapter.setPredictionEditListener(this);
	}

	@Override
	public void OnPredictionEdit(Prediction prediction, TextView predictionView) {
		if(predictionDialog==null) {
			predictionEdit = prediction;
			predictionEditView = predictionView;
						
			String suggestionId = null;
			if(prediction.getPredictionSuggestion()!=null)
					suggestionId = prediction.getPredictionSuggestion().getId();
			
			String topicId = null;
			if(prediction.getBet().getTopic()!=null) {
				topicId = prediction.getBet().getTopic().getId();
			}
			
			FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			predictionDialog = ChangePredictionDialogFragment.newInstance(topicId , suggestionId, prediction.getPrediction());
			predictionDialog.setListener(this);
			predictionDialog.show(ft, "dialog");
		}
		
	}

	@Override
	public void onPredictionSelected(String suggestionId, String prediction) {
		if(predictionEdit!=null) {
//			try {
//				Prediction.getModelDao().refresh(predictionEdit);
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
			predictionEdit.setPrediction(prediction);
			predictionEdit.setPredictionSuggestion(PredictionOption.get(suggestionId));
			predictionEditView.setText(prediction);
			if(predictionDialog!=null) {
				predictionDialog.dismiss();
				predictionDialog = null;
			}
			
			predictionEdit.update();
		}
		
	}

}
