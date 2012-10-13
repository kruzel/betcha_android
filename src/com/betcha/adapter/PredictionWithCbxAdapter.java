package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.Prediction;

public class PredictionWithCbxAdapter extends ArrayAdapter<Prediction> {
	private BetchaApp app;
	
	public PredictionWithCbxAdapter(Context context, int textViewResourceId,
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
		
		TextView tvUserName = (TextView) v.findViewById(R.id.tv_prediction_user_name);
		EditText tvPrediction = (EditText) v.findViewById(R.id.tv_prediction_text);
		CheckBox cbWinner = (CheckBox) v.findViewById(R.id.cb_prediction_win);
		ImageView ivProfPic = (ImageView) v.findViewById(R.id.iv_participant_pic);
		
		tvPrediction.setTag(prediction);
		
		if(app.getMe().getId().equals(prediction.getBet().getOwner().getId())) {
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
		if(app.getMe().getId().equals(prediction.getUser().getId())) {
				
			tvPrediction.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(!hasFocus) {
						EditText et = (EditText) v;
						Prediction p = (Prediction) v.getTag();
						p.setPrediction(et.getText().toString());
						p.update();
						et.clearFocus();
					}
					
				}
			});
			
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
