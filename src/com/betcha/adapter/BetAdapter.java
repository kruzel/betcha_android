package com.betcha.adapter;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.Bet;
import com.betcha.model.Category;
import com.betcha.model.Prediction;
import com.betcha.model.Reward;

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
	public int getItemViewType(int position) {
		return items.get(position).getPredictionsCount();
	}

	@Override
	public int getViewTypeCount() {
		return 6;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        ViewHolder holder = null; // to reference the child views for later actions
        
        Bet bet = items.get(position);
		if(bet==null || bet.getOwner()==null)
			return v;
		
		List<Prediction> predictions = bet.getPredictions();
		int predictionSize = 0;
		
		if(predictions!=null)
			predictionSize = predictions.size();
			
		
		if (v == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
	        v = inflater.inflate(R.layout.bets_list_item, parent, false);
		    		    
		    holder = new ViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDueDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvBetOwner = (TextView) v.findViewById(R.id.tv_bet_owner);
		    holder.ivBetCategory = (ImageView) v.findViewById(R.id.iv_bet_category);
		    
		    // internal frame 
		    holder.tvBetSubject = (TextView) v.findViewById(R.id.tv_bet_topic);
		    holder.tvBetReward = (TextView) v.findViewById(R.id.tv_bet_reward);
		    holder.ivBetRewardImage = (ImageView) v.findViewById(R.id.iv_bet_reward);
		    
		    //holder.buttonHide = (Button) v.findViewById(R.id.buttonHide);
		    
		    holder.lvPredictions = (LinearLayout) v.findViewById(R.id.lv_bet_predictions);
		    
		    if(predictionSize>0) {		    
			    holder.rlPredictionItems = new PredictionHolder[predictionSize];
			    
			    for(int i = 0; i<predictionSize ; i++ ) {
			    	holder.rlPredictionItems[i] = new PredictionHolder();
			    	holder.rlPredictionItems[i].layout = (RelativeLayout) inflater.inflate(R.layout.bet_prediction_short_item, holder.lvPredictions, false);
			    	holder.rlPredictionItems[i].ivParticipantProfPic = (ImageView) holder.rlPredictionItems[i].layout.findViewById(R.id.iv_participant_pic);
			    	holder.rlPredictionItems[i].tvParticipantName = (TextView) holder.rlPredictionItems[i].layout.findViewById(R.id.tv_participant_name);
			    	holder.rlPredictionItems[i].tvParticipantPrediction = (TextView) holder.rlPredictionItems[i].layout.findViewById(R.id.tv_participant_prediction);
		            FontUtils.setTextViewTypeface(holder.rlPredictionItems[i].tvParticipantName, CustomFont.HELVETICA_CONDENSED);
		            FontUtils.setTextViewTypeface(holder.rlPredictionItems[i].tvParticipantPrediction, CustomFont.HELVETICA_CONDENSED);
			    	holder.lvPredictions.addView(holder.rlPredictionItems[i].layout);
			    }
			    
			    LayoutParams itemLayoutParams = holder.rlPredictionItems[0].layout.getLayoutParams();
			    LayoutParams layoutParams = holder.lvPredictions.getLayoutParams();
			    layoutParams.height = 80 * predictionSize;
			    holder.lvPredictions.setLayoutParams(layoutParams);
		    }
					    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            FontUtils.setTextViewTypeface(holder.betDueDate, CustomFont.HELVETICA_CONDENSED);
            FontUtils.setTextViewTypeface(holder.tvBetSubject, CustomFont.HELVETICA_CONDENSED_BOLD);
            FontUtils.setTextViewTypeface(holder.tvBetReward, CustomFont.HELVETICA_CONDENSED);
                     
//            holder.buttonHide.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					Bet bet = (Bet) v.getTag();
//					bet.delete();
//					items.remove(bet);
//					BetAdapter.this.notifyDataSetChanged();
//				}
//			});
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (ViewHolder)v.getTag();
        }
			
		//holder.buttonHide.setTag(bet);
		
		bet.getOwner().setProfilePhoto(holder.ivProfPic);
		
		if(bet.getCategoryId()!=null)
			holder.ivBetCategory.setImageBitmap(Category.getCategory(bet.getCategoryId()).getImage());
		else
			holder.ivBetCategory.setImageResource(android.R.color.transparent);
		
		if(bet.getDueDate().isAfterNow()) {
			if(bet.getDueDate().minusHours(24).isBeforeNow()) {
				if(bet.getDueDate().minusMinutes(60).isBeforeNow()) {
					//less then 1 hr
					holder.betDueDate.setText(Integer.toString(Minutes.minutesBetween(DateTime.now(), bet.getDueDate()).getMinutes()) + " m");
				} else {
					//less then 24 hours left
					holder.betDueDate.setText(Integer.toString(Hours.hoursBetween(DateTime.now(), bet.getDueDate()).getHours()) + " hr");
				}
			} else {
				//more then 24 hours left
				holder.betDueDate.setText(Integer.toString(Days.daysBetween(DateTime.now(), bet.getDueDate()).getDays()) + " d");
	 		}
		} else {
			holder.betDueDate.setText("Due");
		}
				
		String name = bet.getOwner().getName();
		int spacePos = name.indexOf(" ");
		if(spacePos==-1)
			spacePos=name.length();
		holder.tvBetOwner.setText(name.substring(0, spacePos));		
		holder.tvBetSubject.setText(bet.getSubject());
		
		Reward r = bet.getReward();
		if(!r.getId().equals("0")) //not a customer reward
			holder.ivBetRewardImage.setImageResource(r.getDrawable_id());
		else
			holder.ivBetRewardImage.setImageResource(android.R.color.transparent);
		holder.tvBetReward.setText(r.getName());
		
		for(int i = 0; i<predictionSize ; i++ ) {
			Prediction prediction = predictions.get(i);
			prediction.getUser().setProfilePhoto(holder.rlPredictionItems[i].ivParticipantProfPic);
			String partName = prediction.getUser().getName();
			int partSpacePos = partName.indexOf(" ");
			if(partSpacePos==-1)
				partSpacePos=partName.length();
	    	holder.rlPredictionItems[i].tvParticipantName.setText(partName.substring(0, partSpacePos));
	    	
	    	if (!TextUtils.isEmpty(prediction.getPrediction())) {
	            holder.rlPredictionItems[i].tvParticipantPrediction.setText(prediction.getPrediction());
	    	} else {
	            holder.rlPredictionItems[i].tvParticipantPrediction.setText("----"); 
	    	}
	    }
		
		return v;
	}
	
	private class ViewHolder {
				
		ImageView ivProfPic;
		TextView betDueDate;
		TextView tvBetOwner;
		ImageView ivBetCategory;
		
		// internal frame 
		TextView tvBetSubject;
		TextView tvBetReward;
		ImageView ivBetRewardImage;
		
		Button buttonHide;
		
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
