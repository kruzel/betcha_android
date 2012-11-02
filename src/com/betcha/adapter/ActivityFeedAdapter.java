package com.betcha.adapter;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.ActivityFeedItem;
import com.betcha.model.Bet;
import com.betcha.model.ChatMessage;
import com.betcha.model.Prediction;
import com.betcha.model.User;

public class ActivityFeedAdapter extends ArrayAdapter<ActivityFeedItem> {
	
	public ActivityFeedAdapter(Context context, int textViewResourceId, List<ActivityFeedItem> activities) {
		super(context, textViewResourceId, activities);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        ViewHolder holder = null; // to reference the child views for later actions
        
        ActivityFeedItem activityItem = getItem(position);
				
		if (v == null) {
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
	        v = inflater.inflate(R.layout.activity_feed_item, parent, false);
		    		    
		    holder = new ViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvUser = (TextView) v.findViewById(R.id.tv_bet_owner);
		    holder.tvActivityDescription = (TextView) v.findViewById(R.id.tv_activity_description);
		    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            FontUtils.setTextViewTypeface(holder.betDate, CustomFont.HELVETICA_CONDENSED);
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (ViewHolder)v.getTag();
        }
		
		Bet bet = null;
		Prediction prediction = null;
		ChatMessage chatMessage = null;
		User user = null;
		DateTime datetime = null;
		
		switch (activityItem.getType()) {
		case BET_CREATE:
		case BET_UPDATE:
			bet = (Bet) activityItem.getObj();
			user = bet.getOwner();
			datetime = bet.getUpdated_at();
			break;
		case PREDICTION_CREATE:
		case PREDICTION_UPDATE:
			prediction = (Prediction) activityItem.getObj();
			bet = prediction.getBet();
			user = prediction.getUser();
			datetime = prediction.getUpdated_at();
			break;
		case CHAT_CREATE:
			chatMessage = (ChatMessage) activityItem.getObj();
			bet = chatMessage.getBet();
			user = chatMessage.getUser();
			datetime = chatMessage.getUpdated_at();
		default:
			break;
		}
		
		user.setProfilePhoto(holder.ivProfPic);
		
		if(datetime.plusHours(24).isAfterNow()) {
			if(datetime.plusMinutes(60).isAfterNow()) {
				//less then 1 hr
				holder.betDate.setText(Integer.toString(Minutes.minutesBetween(DateTime.now(), datetime).getMinutes()) + " m");
			} else {
				//less then 24 hours 
				holder.betDate.setText(Integer.toString(Hours.hoursBetween(DateTime.now(), datetime).getHours()) + " hr");
			}
		} else {
			//more then 24 hours 
			holder.betDate.setText(Integer.toString(Days.daysBetween(DateTime.now(), datetime).getDays()) + " d");
 		}
				
		holder.tvUser.setText(bet.getOwner().getName());		
		
		//TODO fill in tvBetUpdate with latest bet updates, chat, prediction changes, etc...
		holder.tvActivityDescription.setText(activityItem.getText());
		
		return v;
	}
	
	private class ViewHolder {
				
		ImageView ivProfPic;
		TextView betDate;
		TextView tvUser;
		TextView tvActivityDescription;
	}

}
