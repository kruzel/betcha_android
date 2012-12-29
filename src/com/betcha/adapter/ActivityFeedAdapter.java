package com.betcha.adapter;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.ActivityEvent;
import com.betcha.model.Bet;
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
		if(position > getCount()-1)
			return 5; //OTHER
			
		ActivityEvent event = (ActivityEvent) getItem(position);
		return event.getTypeInt();
	}

	@Override
	public int getViewTypeCount() {
		return 6;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        ActivityEvent activityItem = activities.get(position);
					
		switch (activityItem.getType()) {
		case BET_CREATE:
			v = getCreateBetView(position,v,parent);
			break;
		case BET_UPDATE:
			v = getCloseBetView(position,v,parent);
			break;
		case PREDICTION_CREATE:
			v = getCreatePredictionView(position,v,parent);
			break;
		case PREDICTION_UPDATE:
			v = getUpdatePredictionView(position,v,parent);
			break;
		case CHAT_CREATE:
			v = getCreateChatView(position,v,parent);
		}
		
		return v;
	}
	
	private class BetCreateViewHolder {
		
		ImageView ivProfPic;
		TextView betDate;
		TextView tvUser;
		TextView tvActivityDescription;
		ImageView ivBetCategory;
		
		ImageView ivUser1;
		ImageView ivUser2;
		ImageView ivUser3;
		
		TextView tvOthers;
		TextView tvStake;
		
		public void reset() {
			ivProfPic.setImageResource(android.R.color.transparent);
			
			betDate.setText("");
			tvUser.setText("");
			tvActivityDescription.setText("");
			
			ivBetCategory.setImageResource(android.R.color.transparent);
			
			if(ivUser1!=null)
				ivUser1.setImageResource(android.R.color.transparent);
			if(ivUser2!=null)
				ivUser2.setImageResource(android.R.color.transparent);
			if(ivUser3!=null)
				ivUser3.setImageResource(android.R.color.transparent);
			
			if(tvOthers!=null)
				tvOthers.setText("");
			if(tvStake!=null)
				tvStake.setText("");
		}
	}
	
	public View getCreateBetView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        BetCreateViewHolder holder = null; // to reference the child views for later actions
        
        ActivityEvent activityItem = activities.get(position);
				
		if (v == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	        v = inflater.inflate(R.layout.activity_feed_item_bet_create, parent, false);
		    		    
		    holder = new BetCreateViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvUser = (TextView) v.findViewById(R.id.tv_bet_owner);
		    holder.tvActivityDescription = (TextView) v.findViewById(R.id.tv_activity_description);
		    holder.ivBetCategory = (ImageView) v.findViewById(R.id.iv_bet_category);
		    
		    holder.ivUser1 = (ImageView) v.findViewById(R.id.imageViewUser1);
		    holder.ivUser2 = (ImageView) v.findViewById(R.id.imageViewUser2);
		    holder.ivUser3 = (ImageView) v.findViewById(R.id.imageViewUser3);
			
		    holder.tvOthers = (TextView) v.findViewById(R.id.tv_activity_others);
		    holder.tvStake = (TextView) v.findViewById(R.id.tv_activity_stake);
		    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            FontUtils.setTextViewTypeface(holder.betDate, CustomFont.HELVETICA_CONDENSED);
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (BetCreateViewHolder)v.getTag();
        }
		
		Bet bet = null;
		User user = null;
		DateTime datetime = null;
		
		holder.reset();
		
		bet = (Bet) activityItem.getObj();
				
		if(bet==null)
			return v;
		
		user = bet.getOwner();
		datetime = bet.getUpdated_at();
		
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
		
		String description;
		if(bet.getOwner().getId().equals(BetchaApp.getInstance().getCurUser().getId()))
			description = "You have created a bet \"" + bet.getTopicCustom() + "\"";
		else
			description = bet.getOwner().getName() + " has invited you to bet \"" + bet.getTopicCustom() + "\"";
		
		holder.tvActivityDescription.setText(description);
		FontUtils.setTextViewTypeface(holder.tvActivityDescription, CustomFont.HELVETICA_CONDENSED_BOLD);
						
		List<Prediction> predictions = bet.getPredictions();
		List<Prediction> predictionsToShow = new ArrayList<Prediction>();
		
		if(predictions.size()>1) {
			for (Prediction prediction : predictions) {
				if(!prediction.getUser().getId().equals(bet.getOwner().getId())) {
					predictionsToShow.add(prediction);
				}
			}
			
			if(predictionsToShow.size()>0) {
				if(predictionsToShow.get(0)!=null) {
					predictionsToShow.get(0).getUser().setProfilePhoto(holder.ivUser1);
				} 
			} else {
				LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
			    layoutParams.width = 0;
			    holder.ivUser1.setLayoutParams(layoutParams);
			}
			
			if(predictionsToShow.size()>1) {
				if(predictionsToShow.get(1)!=null) {
					predictionsToShow.get(1).getUser().setProfilePhoto(holder.ivUser2);
				} else {
					LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
				    layoutParams.width = 0;
				    holder.ivUser2.setLayoutParams(layoutParams);
				}
			}
			
			if(predictionsToShow.size()>2) {
				if(predictionsToShow.get(2)!=null) {
					predictionsToShow.get(2).getUser().setProfilePhoto(holder.ivUser3);
				} else {
					LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
				    layoutParams.width = 0;
				    holder.ivUser3.setLayoutParams(layoutParams);
				}
			}
			
			int numOthers = predictions.size() - predictionsToShow.size() - 1;
			if(predictionsToShow.size()<=3) {
				holder.tvOthers.setText(context.getString(R.string.invited));
			} else if(numOthers>0) {
				holder.tvOthers.setText("" + context.getString(R.string.and) + " " + numOthers + " " + context.getString(R.string.others));
			} else  {
				holder.tvOthers.setText("");
			}
			
			FontUtils.setTextViewTypeface(holder.tvOthers, CustomFont.HELVETICA_CONDENSED);
		} else {
			LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
		    layoutParams.width = 0;
		    holder.ivUser1.setLayoutParams(layoutParams);
		    
		    layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
		    layoutParams.width = 0;
		    holder.ivUser2.setLayoutParams(layoutParams);
		    
		    layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
		    layoutParams.width = 0;
		    holder.ivUser3.setLayoutParams(layoutParams);
		}
		
		holder.tvStake.setText(context.getString(R.string.betting_on) + " " + bet.getStakeCustom());
		FontUtils.setTextViewTypeface(holder.tvStake, CustomFont.HELVETICA_CONDENSED);
		
		return v;
	}

	public View getCloseBetView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        BetCreateViewHolder holder = null; // to reference the child views for later actions
        
        ActivityEvent activityItem = activities.get(position);
				
		if (v == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	        v = inflater.inflate(R.layout.activity_feed_item_bet_close, parent, false);
		    		    
		    holder = new BetCreateViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvUser = (TextView) v.findViewById(R.id.tv_bet_owner);
		    holder.tvActivityDescription = (TextView) v.findViewById(R.id.tv_activity_description);
		    holder.ivBetCategory = (ImageView) v.findViewById(R.id.iv_bet_category);
		    
		    holder.ivUser1 = (ImageView) v.findViewById(R.id.imageViewUser1);
		    holder.ivUser2 = (ImageView) v.findViewById(R.id.imageViewUser2);
		    holder.ivUser3 = (ImageView) v.findViewById(R.id.imageViewUser3);
			
		    holder.tvOthers = (TextView) v.findViewById(R.id.tv_activity_others);
		    holder.tvStake = (TextView) v.findViewById(R.id.tv_activity_stake);
		    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            FontUtils.setTextViewTypeface(holder.betDate, CustomFont.HELVETICA_CONDENSED);
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (BetCreateViewHolder)v.getTag();
        }
		
		Bet bet = null;
		User user = null;
		DateTime datetime = null;
		
		holder.reset();
		
		bet = (Bet) activityItem.getObj();
						
		if(bet==null)
			return v;
		
		user = bet.getOwner();
		datetime = bet.getUpdated_at();
		
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
		
		String description;
		if(bet.getOwner().getId().equals(BetchaApp.getInstance().getCurUser().getId()))
			description = "You have closed bet \"" + bet.getTopicCustom() + "\"";
		else
			description = bet.getOwner().getName() + " has closed bet \"" + bet.getTopicCustom() + "\"";
		
		holder.tvActivityDescription.setText(description);
		FontUtils.setTextViewTypeface(holder.tvActivityDescription, CustomFont.HELVETICA_CONDENSED_BOLD);
				
		List<Prediction> predictions = bet.getPredictions();
		List<Prediction> predictionsToShow = new ArrayList<Prediction>();
		
		
		for (Prediction prediction : predictions) {
			if(prediction.getResult()!=null && prediction.getResult()==true)
				predictionsToShow.add(prediction);
		}
		
		if(predictionsToShow.size()>0) {
			if(predictionsToShow.get(0)!=null) {
				predictionsToShow.get(0).getUser().setProfilePhoto(holder.ivUser1);
			} else {
				LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
			    layoutParams.width = 0;
			    holder.ivUser1.setLayoutParams(layoutParams);
			}
		}
		
		if(predictionsToShow.size()>1) {
			if(predictionsToShow.get(1)!=null) {
				predictionsToShow.get(1).getUser().setProfilePhoto(holder.ivUser2);
			} else {
				LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
			    layoutParams.width = 0;
			    holder.ivUser2.setLayoutParams(layoutParams);
			}
		}
		
		if(predictionsToShow.size()>2) {
			if(predictionsToShow.get(2)!=null) {
				predictionsToShow.get(2).getUser().setProfilePhoto(holder.ivUser3);
			} else {
				LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
			    layoutParams.width = 0;
			    holder.ivUser3.setLayoutParams(layoutParams);
			}
		}
		
		int numOthers = predictionsToShow.size() - 3;
		if(numOthers>0) {
			holder.tvOthers.setText("" + context.getString(R.string.and) + " " + numOthers + " " + context.getString(R.string.others));
		} else  {
			holder.tvOthers.setText("");
		}
		
		FontUtils.setTextViewTypeface(holder.tvOthers, CustomFont.HELVETICA_CONDENSED);
		
		if(predictionsToShow.size()>0) {
			holder.tvStake.setText(context.getString(R.string.won) + " " + bet.getStakeCustom());
		} else {
			holder.tvStake.setText(context.getString(R.string.no_one_won) + " " + bet.getStakeCustom());
		}
		
		FontUtils.setTextViewTypeface(holder.tvStake, CustomFont.HELVETICA_CONDENSED);
		
		return v;
	}
	
	public View getCreatePredictionView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        BetCreateViewHolder holder = null; // to reference the child views for later actions
        
        ActivityEvent activityItem = activities.get(position);
				
		if (v == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	        v = inflater.inflate(R.layout.activity_feed_item_bet_create, parent, false);
		    		    
		    holder = new BetCreateViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvUser = (TextView) v.findViewById(R.id.tv_bet_owner);
		    holder.tvActivityDescription = (TextView) v.findViewById(R.id.tv_activity_description);
		    holder.ivBetCategory = (ImageView) v.findViewById(R.id.iv_bet_category);
		    
		    holder.ivUser1 = (ImageView) v.findViewById(R.id.imageViewUser1);
		    holder.ivUser2 = (ImageView) v.findViewById(R.id.imageViewUser2);
		    holder.ivUser3 = (ImageView) v.findViewById(R.id.imageViewUser3);
			
		    holder.tvOthers = (TextView) v.findViewById(R.id.tv_activity_others);
		    holder.tvStake = (TextView) v.findViewById(R.id.tv_activity_stake);
		    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            FontUtils.setTextViewTypeface(holder.betDate, CustomFont.HELVETICA_CONDENSED);
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (BetCreateViewHolder)v.getTag();
        }
		
		Bet bet = null;
		User user = null;
		DateTime datetime = null;
		
		holder.reset();
		
		Prediction prediction = (Prediction) activityItem.getObj();
		if(prediction==null)
			return v;
		
		bet = prediction.getBet();
		user = bet.getOwner();
		datetime = prediction.getUpdated_at();
		
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
		
		String description;
		if(bet.getOwner().getId().equals(BetchaApp.getInstance().getCurUser().getId()))
			description = "You have added to bet \"" + bet.getTopicCustom() + "\"";
		else
			description = bet.getOwner().getName() + " has invited you to bet \"" + bet.getTopicCustom() + "\"";
		
		holder.tvActivityDescription.setText(description);
		FontUtils.setTextViewTypeface(holder.tvActivityDescription, CustomFont.HELVETICA_CONDENSED_BOLD);
						
		List<Prediction> predictions = bet.getPredictions();
		List<Prediction> predictionsToShow = new ArrayList<Prediction>();
		
		if(predictions.size()>1) {
			for (Prediction tmpPrediction : predictions) {
				if(!tmpPrediction.getUser().getId().equals(bet.getOwner().getId())) {
					predictionsToShow.add(tmpPrediction);
				}
			}
			
			if(predictionsToShow.size()>0) {
				if(predictionsToShow.get(0)!=null) {
					predictionsToShow.get(0).getUser().setProfilePhoto(holder.ivUser1);
				} 
			} else {
				LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
			    layoutParams.width = 0;
			    holder.ivUser1.setLayoutParams(layoutParams);
			}
			
			if(predictionsToShow.size()>1) {
				if(predictionsToShow.get(1)!=null) {
					predictionsToShow.get(1).getUser().setProfilePhoto(holder.ivUser2);
				} else {
					LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
				    layoutParams.width = 0;
				    holder.ivUser2.setLayoutParams(layoutParams);
				}
			}
			
			if(predictionsToShow.size()>2) {
				if(predictionsToShow.get(2)!=null) {
					predictionsToShow.get(2).getUser().setProfilePhoto(holder.ivUser3);
				} else {
					LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
				    layoutParams.width = 0;
				    holder.ivUser3.setLayoutParams(layoutParams);
				}
			}
			
			int numOthers = predictions.size() - predictionsToShow.size() - 1;
			if(predictionsToShow.size()<=3) {
				holder.tvOthers.setText(context.getString(R.string.invited));
			} else if(numOthers>0) {
				holder.tvOthers.setText("" + context.getString(R.string.and) + " " + numOthers + " " + context.getString(R.string.others));
			} else  {
				holder.tvOthers.setText("");
			}
			
			FontUtils.setTextViewTypeface(holder.tvOthers, CustomFont.HELVETICA_CONDENSED);
		} else {
			LayoutParams layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
		    layoutParams.width = 0;
		    holder.ivUser1.setLayoutParams(layoutParams);
		    
		    layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
		    layoutParams.width = 0;
		    holder.ivUser2.setLayoutParams(layoutParams);
		    
		    layoutParams = (LayoutParams) holder.tvOthers.getLayoutParams();
		    layoutParams.width = 0;
		    holder.ivUser3.setLayoutParams(layoutParams);
		}
		
		holder.tvStake.setText(context.getString(R.string.betting_on) + " " + bet.getStakeCustom());
		FontUtils.setTextViewTypeface(holder.tvStake, CustomFont.HELVETICA_CONDENSED);
		
		return v;
	}
	
	public View getUpdatePredictionView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        BetCreateViewHolder holder = null; // to reference the child views for later actions
        
        ActivityEvent activityItem = activities.get(position);
				
		if (v == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	        v = inflater.inflate(R.layout.activity_feed_item_prediction_update, parent, false);
		    		    
		    holder = new BetCreateViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvUser = (TextView) v.findViewById(R.id.tv_bet_owner);
		    holder.tvActivityDescription = (TextView) v.findViewById(R.id.tv_activity_description);
		    holder.ivBetCategory = (ImageView) v.findViewById(R.id.iv_bet_category);
		    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            FontUtils.setTextViewTypeface(holder.betDate, CustomFont.HELVETICA_CONDENSED);
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (BetCreateViewHolder)v.getTag();
        }
		
		Bet bet = null;
		User user = null;
		DateTime datetime = null;
		
		holder.reset();
		
		Prediction prediction = (Prediction) activityItem.getObj();
		if(prediction==null)
			return v;
		
		bet = prediction.getBet();
		user = prediction.getUser();
		datetime = prediction.getUpdated_at();
		
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
		
		String description = "You have updated your bet to \"" + activityItem.getDescription() + "\" for bet \"" + bet.getTopicCustom() + "\"" ;
		holder.tvActivityDescription.setText(description);
		FontUtils.setTextViewTypeface(holder.tvActivityDescription, CustomFont.HELVETICA_CONDENSED_BOLD);
		
		return v;
	}
	
	
	public View getCreateChatView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        BetCreateViewHolder holder = null; // to reference the child views for later actions
        
        ActivityEvent activityItem = activities.get(position);
				
		if (v == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	        v = inflater.inflate(R.layout.activity_feed_item_chat_create, parent, false);
		    		    
		    holder = new BetCreateViewHolder();
		    holder.ivProfPic = (ImageView) v.findViewById(R.id.iv_bet_owner_profile_pic);
		    holder.betDate = (TextView) v.findViewById(R.id.tv_bet_date);
		    holder.tvUser = (TextView) v.findViewById(R.id.tv_bet_owner);
		    holder.tvActivityDescription = (TextView) v.findViewById(R.id.tv_activity_description);
		    holder.ivBetCategory = (ImageView) v.findViewById(R.id.iv_bet_category);
		    
		 	// associate the holder with the view for later lookup
            v.setTag(holder);
            
            FontUtils.setTextViewTypeface(holder.betDate, CustomFont.HELVETICA_CONDENSED);
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (BetCreateViewHolder)v.getTag();
        }
		
		Bet bet = null;
		User user = null;
		DateTime datetime = null;
		
		holder.reset();
		
		ChatMessage chatMessage = (ChatMessage) activityItem.getObj();
		if(chatMessage==null)
			return v;
		
		bet = chatMessage.getBet();
		user = chatMessage.getUser();
		datetime = chatMessage.getUpdated_at();
		
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
	
}
