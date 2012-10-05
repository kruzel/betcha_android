package com.betcha.adapter;

import java.util.Collection;
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
import android.view.ViewGroup.LayoutParams;
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
	public void addAll(Bet... items) {
		for (Bet bet : items) {
			add(bet);
		}
	}

	@Override
	public void addAll(Collection<? extends Bet> collection) {
		for (Bet bet : collection) {
			add(bet);
		}
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
		TextView betDueDate = (TextView) v.findViewById(R.id.tv_bet_date);
		TextView tvBetOwner = (TextView) v.findViewById(R.id.tv_bet_owner);
		
		bet.getOwner().setProfilePhoto(ivProfPic);
		
		if(bet.getDueDate().plusHours(24).isAfterNow()) {
			//less then 24 hours left
			DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");
			betDueDate.setText(fmtTime.print(bet.getDueDate()));
		} else {
			//more then 24 hours left
			DateTimeFormatter fmtDate = DateTimeFormat.forPattern("MM-dd");
			betDueDate.setText(fmtDate.print(bet.getDueDate()));
		} 
		
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
			PredictionAdapter predictionAdapter = new PredictionAdapter(getContext(), R.layout.bet_prediction_short_item, predictions);
			lvPredictions.setAdapter(predictionAdapter);
		}
		
		//TODO dynamic height of list view
//		LayoutParams layoutParams = lvPredictions.getLayoutParams();
//		layoutParams.height = layoutParams.height * predictions.size();
//		lvPredictions.setLayoutParams(layoutParams);
		
		return v;
	}

}
