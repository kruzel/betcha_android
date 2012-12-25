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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.ActivityEvent;
import com.betcha.model.Bet;
import com.betcha.model.TopicCategory;
import com.betcha.model.ChatMessage;
import com.betcha.model.Prediction;
import com.betcha.model.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ActivityFeedAdapter extends BaseAdapter {
	
	private List<ActivityEvent> activities;
	private Context context;
	
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	
	public ActivityFeedAdapter(Context context, int textViewResourceId, List<ActivityEvent> activities) {
		super();
		this.activities = activities;
		this.context = context;
		
		if(imageLoader==null) {
			imageLoader = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoader.init(ImageLoaderConfiguration.createDefault(context));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
	}
	
	public void clear() {
		activities.clear();
	}
	
	public void addAll(List<ActivityEvent> activities) {
		this.activities.addAll(activities);
	}
	
	@Override
	public int getCount() {
		return activities.size();
	}

	@Override
	public Object getItem(int location) {
		return activities.get(location);
	}

	@Override
	public long getItemId(int location) {
		return location;
	}
	
	@Override
	public int getItemViewType(int position) {
		ActivityEvent item = (ActivityEvent) getItem(position);
		
		return item.getTypeInt();
	}

	@Override
	public int getViewTypeCount() {
		return 5;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        ViewHolder holder = null; // to reference the child views for later actions
        
        ActivityEvent activityItem = activities.get(position);        
		
		Bet bet = null;
		Prediction prediction = null;
		ChatMessage chatMessage = null;
		User user = null;
		DateTime datetime = null;
		
		switch (activityItem.getType()) {
		case BET_CREATE:
			if (v == null) {
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		        v = inflater.inflate(R.layout.activity_feed_item, parent, false);
			    holder = createHolder(v);
	            v.setTag(holder);			    
			} else {
	            // view already exists, get the holder instance from the view
	            holder = (ViewHolder)v.getTag();
	        }
			bet = (Bet) activityItem.getObj();
			user = bet.getOwner();
			datetime = bet.getUpdated_at();
			break;
		case BET_UPDATE:
			if (v == null) {
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		        v = inflater.inflate(R.layout.activity_feed_item, parent, false);
			    holder = createHolder(v);
	            v.setTag(holder);			    
			} else {
	            // view already exists, get the holder instance from the view
	            holder = (ViewHolder)v.getTag();
	        }
			bet = (Bet) activityItem.getObj();
			user = bet.getOwner();
			datetime = bet.getUpdated_at();
			break;
		case PREDICTION_CREATE:
			if (v == null) {
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		        v = inflater.inflate(R.layout.activity_feed_item, parent, false);
			    holder = createHolder(v);
	            v.setTag(holder);			    
			} else {
	            // view already exists, get the holder instance from the view
	            holder = (ViewHolder)v.getTag();
	        }
			prediction = (Prediction) activityItem.getObj();
			bet = prediction.getBet();
			user = bet.getOwner();
			datetime = prediction.getUpdated_at();
			break;
		case PREDICTION_UPDATE:
			if (v == null) {
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		        v = inflater.inflate(R.layout.activity_feed_item, parent, false);
			    holder = createHolder(v);
	            v.setTag(holder);			    
			} else {
	            // view already exists, get the holder instance from the view
	            holder = (ViewHolder)v.getTag();
	        }
			prediction = (Prediction) activityItem.getObj();
			bet = prediction.getBet();
			user = prediction.getUser();
			datetime = prediction.getUpdated_at();
			break;
		case CHAT_CREATE:
			if (v == null) {
				LayoutInflater inflater = ((Activity)context).getLayoutInflater();
		        v = inflater.inflate(R.layout.activity_feed_item, parent, false);
			    holder = createHolder(v);
	            v.setTag(holder);			    
			} else {
	            // view already exists, get the holder instance from the view
	            holder = (ViewHolder)v.getTag();
	        }
			chatMessage = (ChatMessage) activityItem.getObj();
			bet = chatMessage.getBet();
			user = chatMessage.getUser();
			datetime = chatMessage.getUpdated_at();
		default:
			break;
		}
		
		if(bet==null)
			return v;
		
		user.setProfilePhoto(holder.ivProfPic);
		
		if(bet.getCategory()!=null) {
			imageLoader.displayImage(bet.getCategory().getImageUrl(), holder.ivBetCategory, defaultOptions);
		} else {
			imageLoader.cancelDisplayTask(holder.ivBetCategory);
			holder.ivBetCategory.setImageResource(android.R.color.transparent);
		}
		
		if(datetime.plusHours(24).isAfterNow()) {
			if(datetime.plusMinutes(60).isAfterNow()) {
				//less then 1 hr
				holder.betDate.setText(Integer.toString(Minutes.minutesBetween(datetime,DateTime.now()).getMinutes()) + " m");
			} else {
				//less then 24 hours 
				holder.betDate.setText(Integer.toString(Hours.hoursBetween(datetime, DateTime.now()).getHours()) + " hr");
			}
		} else {
			//more then 24 hours 
			holder.betDate.setText(Integer.toString(Days.daysBetween(datetime, DateTime.now()).getDays()) + " d");
 		}
				
		holder.tvUser.setText(bet.getOwner().getName());		
		
		//TODO fill in tvBetUpdate with latest bet updates, chat, prediction changes, etc...
		holder.tvActivityDescription.setText(activityItem.getText());
		FontUtils.setTextViewTypeface(holder.tvActivityDescription, CustomFont.HELVETICA_CONDENSED_BOLD);
		
		return v;
	}
	
	private ViewHolder createHolder(View v) {
		ViewHolder holder;
		
		holder = new ViewHolder();
	    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
	    holder.betDate = (TextView) v.findViewById(R.id.tv_bet_date);
	    holder.tvUser = (TextView) v.findViewById(R.id.tv_bet_owner);
	    holder.tvActivityDescription = (TextView) v.findViewById(R.id.tv_activity_description);
	    holder.ivBetCategory = (ImageView) v.findViewById(R.id.iv_bet_category);
	    
	    FontUtils.setTextViewTypeface(holder.betDate, CustomFont.HELVETICA_CONDENSED);
		
		return holder;
	}
	
	private class ViewHolder {
				
		ImageView ivProfPic;
		TextView betDate;
		TextView tvUser;
		TextView tvActivityDescription;
		ImageView ivBetCategory;
	}

}
