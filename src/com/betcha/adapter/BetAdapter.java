package com.betcha.adapter;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.betcha.R;
import com.betcha.activity.BetDetailsActivity;
import com.betcha.model.Bet;
import com.betcha.model.Prediction;

public class BetAdapter extends ArrayAdapter<Bet> {
	
	private List<Bet> items;
	
	public BetAdapter(Context context, int textViewResourceId, List<Bet> bets) {
		super(context, textViewResourceId, bets);
		this.items = bets;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
	        v = inflater.inflate(R.layout.bets_list_item, parent, false);
	        v.setClickable(false);
		    v.setFocusable(false);
		    v.setBackgroundResource(android.R.drawable.menuitem_background);
		    v.setOnClickListener(new OnClickListener() {

		        @Override
		        public void onClick(View v) {
		            Intent i = new Intent(getContext(), BetDetailsActivity.class);
		            String betId = (String) v.getTag();
		            i.putExtra("betId", betId);
		            //i.putExtra("is_new_bet", isNewBet);
		            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
		            getContext().startActivity(i);
		        }

		    });
		}
		
		
		
		Bet bet = items.get(position);
		if(bet==null || bet.getOwner()==null)
			return v;
		
		v.setTag(bet.getId());
		
		//bet owner and other details (outer frame)
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM HH:mm");
		ImageView ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		TextView tvBetDate = (TextView) v.findViewById(R.id.tv_bet_date);
		TextView tvBetOwner = (TextView) v.findViewById(R.id.tv_bet_owner);
		
		bet.getOwner().setProfilePhoto(ivProfPic);
		tvBetDate.setText(fmt.print(bet.getDate()));
		tvBetOwner.setText(bet.getOwner().getName());
		
		// internal frame 
		TextView tvBetSubject = (TextView) v.findViewById(R.id.tv_bet_topic);
		TextView tvBetReward = (TextView) v.findViewById(R.id.tv_bet_reward);
		
		tvBetSubject.setText(bet.getSubject());
		tvBetReward.setText(bet.getReward());
		
		ViewGroup vg = (ViewGroup) v;
		
		ListView lvPredictions = (ListView) v.findViewById(R.id.lv_bet_predictions);
		List<Prediction> predictions = bet.getPredictions();
		if(predictions!=null) {
			PredictionShortAdapter predictionAdapter = new PredictionShortAdapter(getContext(), R.layout.bet_prediction_short_item, predictions);
			lvPredictions.setAdapter(predictionAdapter);
		}
		
		return v;
	}

}
