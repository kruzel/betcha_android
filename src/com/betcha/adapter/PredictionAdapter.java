package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.Prediction;

public class PredictionAdapter extends ArrayAdapter<Prediction> {
	private BetchaApp app;
	
	OnPredictionEditListener predictionEditListener;
	
	public interface OnPredictionEditListener {
		public abstract void OnPredictionEdit(Prediction prediction, TextView predictionView);
	}
	
	public OnPredictionEditListener getPredictionEditListener() {
		return predictionEditListener;
	}

	public void setPredictionEditListener(
			OnPredictionEditListener predictionEditListener) {
		this.predictionEditListener = predictionEditListener;
	}

	public PredictionAdapter(Context context, int textViewResourceId,
			List<Prediction> objects) {
		super(context, textViewResourceId, objects);	
		app = (BetchaApp) context.getApplicationContext();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.bet_prediction_list_item), null);
		}
		
		Prediction prediction = getItem(position);
		
		TextView tvUserName = (TextView) v.findViewById(R.id.tv_participant_name);
		TextView tvPrediction = (TextView) v.findViewById(R.id.tv_participant_prediction);
		CheckBox cbWinner = (CheckBox) v.findViewById(R.id.cb_prediction_win);
		ImageView ivProfPic = (ImageView) v.findViewById(R.id.iv_participant_pic);
		
		FontUtils.setTextViewTypeface(tvUserName, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvPrediction, CustomFont.HELVETICA_CONDENSED);
		
		tvPrediction.setTag(prediction);
		
		if(app.getCurUser().getId().equals(prediction.getBet().getOwner().getId())) {
			cbWinner.setClickable(true);
		} else {
			cbWinner.setClickable(false);
		}
		
		if(prediction.getUser()!=null) { //should not happen
			prediction.getUser().setProfilePhoto(ivProfPic);
			if(prediction.getUser().getName()==null)
				tvUserName.setText(prediction.getUser().getEmail().substring(0, prediction.getUser().getEmail().indexOf('@')));
			else
				tvUserName.setText(prediction.getUser().getName());
		}
		
		tvPrediction.setText(prediction.getPrediction()==null ? "" : prediction.getPrediction() );
		
		if(app.getCurUser().getId().equals(prediction.getUser().getId())) {
													
			tvPrediction.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(predictionEditListener!=null) {
						TextView predictionView = (TextView) v;
						Prediction p = (Prediction) predictionView.getTag();
						predictionEditListener.OnPredictionEdit(p, predictionView);
					}
				}
			});
			
			if(tvPrediction.getText().length()==0 && predictionEditListener!=null) {
				predictionEditListener.OnPredictionEdit(prediction, tvPrediction);
			}
			
		} else {
			tvPrediction.setEnabled(false);
		}
		
		Boolean res = prediction.getResult();
		if(res!=null)
			cbWinner.setChecked(res); //true = win
		else 
			cbWinner.setChecked(false);
		
		cbWinner.setTag(prediction);
		
		cbWinner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Prediction prediction = (Prediction) v.getTag();
				if(prediction.getResult()==null) {
					prediction.setResult(true);
				} else {
					prediction.setResult(!prediction.getResult());
				}
			}
		});
		
		return v;		
	}
		
}
