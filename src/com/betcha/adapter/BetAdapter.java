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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        ViewHolder holder = null; // to reference the child views for later actions
        
        Bet bet = items.get(position);
		if(bet==null || bet.getOwner()==null)
			return v;
		
		List<Prediction> predictions = bet.getPredictions();
		if(predictions==null || predictions.size()==0)
			return v;
		
		int predictionSize = predictions.size();
		
		if (v == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
	        v = inflater.inflate(R.layout.bets_list_item, parent, false);
		    		    
		    holder = new ViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDueDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvBetOwner = (TextView) v.findViewById(R.id.tv_bet_owner);
		    
		    // internal frame 
		    holder.tvBetSubject = (TextView) v.findViewById(R.id.tv_bet_topic);
		    holder.tvBetReward = (TextView) v.findViewById(R.id.tv_bet_reward);
		    holder.ivNavArrow = (ImageView) v.findViewById(R.id.image_view_nav);
		    
		    holder.lvPredictions = (LinearLayout) v.findViewById(R.id.lv_bet_predictions);
		    		    
		    holder.rlPredictionItems = new PredictionHolder[predictionSize];
		    
		    for(int i = 0; i<predictionSize ; i++ ) {
		    	holder.rlPredictionItems[i] = new PredictionHolder();
		    	holder.rlPredictionItems[i].layout = (RelativeLayout) inflater.inflate(R.layout.bet_prediction_short_item, holder.lvPredictions, false);
		    	holder.rlPredictionItems[i].ivParticipantProfPic = (ImageView) holder.rlPredictionItems[i].layout.findViewById(R.id.iv_participant_pic);
		    	holder.rlPredictionItems[i].tvParticipantName = (TextView) holder.rlPredictionItems[i].layout.findViewById(R.id.tv_participant_name);
		    	holder.rlPredictionItems[i].tvParticipantPrediction = (TextView) holder.rlPredictionItems[i].layout.findViewById(R.id.tv_participant_prediction);
		    	holder.lvPredictions.addView(holder.rlPredictionItems[i].layout);
		    }
		    
		    LayoutParams itemLayoutParams = holder.rlPredictionItems[0].layout.getLayoutParams();
		    LayoutParams layoutParams = holder.lvPredictions.getLayoutParams();
		    layoutParams.height = itemLayoutParams.height * predictionSize;
		    holder.lvPredictions.setLayoutParams(layoutParams);
					    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            holder.ivNavArrow.setClickable(false);
            holder.ivNavArrow.setFocusable(false);
            holder.ivNavArrow.setBackgroundResource(android.R.drawable.menuitem_background);
            holder.ivNavArrow.setOnClickListener(new OnClickListener() {

		        @Override
		        public void onClick(View v) {
		            Intent i = new Intent(getContext(), BetDetailsActivity.class);
		            String betId = (String) v.getTag();
		            i.putExtra("betId", betId);
		            getContext().startActivity(i);
		        }

		    });
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (ViewHolder)v.getTag();
        }
			
		holder.ivNavArrow.setTag(bet.getId());
		
		bet.getOwner().setProfilePhoto(holder.ivProfPic);
		
		if(bet.getDueDate().plusHours(24).isAfterNow()) {
			//less then 24 hours left
			DateTimeFormatter fmtTime = DateTimeFormat.forPattern("HH:mm");
			holder.betDueDate.setText(fmtTime.print(bet.getDueDate()));
		} else {
			//more then 24 hours left
			DateTimeFormatter fmtDate = DateTimeFormat.forPattern("MM-dd");
			holder.betDueDate.setText(fmtDate.print(bet.getDueDate()));
		} 
		
		holder.tvBetOwner.setText(bet.getOwner().getName());
		
		holder.tvBetSubject.setText(bet.getSubject());
		holder.tvBetReward.setText(bet.getReward());
		
		for(int i = 0; i<predictionSize ; i++ ) {
			Prediction prediction = predictions.get(i);
			prediction.getUser().setProfilePhoto(holder.rlPredictionItems[i].ivParticipantProfPic);
	    	holder.rlPredictionItems[i].tvParticipantName.setText(prediction.getUser().getName());
	    	holder.rlPredictionItems[i].tvParticipantPrediction.setText(prediction.getPrediction());
	    }
		
		return v;
	}
	
	private class ViewHolder {
				
		ImageView ivProfPic;
		TextView betDueDate;
		TextView tvBetOwner;
		
		// internal frame 
		TextView tvBetSubject;
		TextView tvBetReward;
		ImageView ivNavArrow;
		
		LinearLayout lvPredictions;
		PredictionHolder[] rlPredictionItems;
	}
	
	private class PredictionHolder {
		RelativeLayout layout;
		ImageView ivParticipantProfPic;
		TextView tvParticipantName;
		TextView tvParticipantPrediction;
	}

}
