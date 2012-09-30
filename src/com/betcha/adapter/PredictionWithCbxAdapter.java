package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.Prediction;

public class PredictionWithCbxAdapter extends ArrayAdapter<Prediction> {
	private BetchaApp app;
	private List<Prediction> items;
	
	public PredictionWithCbxAdapter(Context context, int textViewResourceId,
			List<Prediction> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;		
		app = (BetchaApp) context.getApplicationContext();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.prediction_list_item), null);
		}
		
		Prediction prediction = items.get(position);
		
		TextView tvBetUser = (TextView) v.findViewById(R.id.tv_user_bet_user);
		TextView tvBetBet = (TextView) v.findViewById(R.id.tv_user_bet_bet);
		CheckBox cbResult = (CheckBox) v.findViewById(R.id.cb_user_bet_win);
		ImageView ivProfPic = (ImageView) v.findViewById(R.id.iv_participant_pic);
		
		if(app.getMe().getId().endsWith(prediction.getBet().getOwner().getId())) {
			cbResult.setClickable(true);
		} else {
			cbResult.setClickable(false);
		}
		
		prediction.getUser().setProfilePhoto(ivProfPic);
		tvBetUser.setText(prediction.getUser().getName());
		tvBetBet.setText(prediction.getPrediction()==null ? "" : prediction.getPrediction() );
		Boolean res = prediction.getResult();
		if(res!=null)
			cbResult.setChecked(res); //true = win
		else 
			cbResult.setChecked(false);
		
		return v;		
	}
	
}