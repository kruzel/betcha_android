package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.fragment.ProfileDetailsFragment;
import com.betcha.model.Badge;
import com.betcha.model.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class LeadersBoardAdapter extends ArrayAdapter<User>{
	
	private BetchaApp app;
	private FragmentActivity thisActivity;
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	
	private static ImageLoader imageLoaderBadge1;
	private static ImageLoader imageLoaderBadge2;
	private static ImageLoader imageLoaderBadge3;
	private static ImageLoader imageLoaderBadge4;
	
	public LeadersBoardAdapter(FragmentActivity context, int textViewResourceId,
			List<User> friends) {
		super(context, textViewResourceId, friends);
		
		app = (BetchaApp) context.getApplicationContext();
		thisActivity = context;
		if(imageLoader==null) {
			imageLoader = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoader.init(ImageLoaderConfiguration.createDefault(app));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
		
		if(imageLoaderBadge1==null) {
			imageLoaderBadge1 = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoaderBadge1.init(ImageLoaderConfiguration.createDefault(app));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
		
		if(imageLoaderBadge2==null) {
			imageLoaderBadge2 = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoaderBadge1.init(ImageLoaderConfiguration.createDefault(app));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
		
		if(imageLoaderBadge3==null) {
			imageLoaderBadge3 = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoaderBadge1.init(ImageLoaderConfiguration.createDefault(app));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
		
		if(imageLoaderBadge4==null) {
			imageLoaderBadge4 = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoaderBadge1.init(ImageLoaderConfiguration.createDefault(app));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.friends_list_item), null);
		}
		
		User friend = getItem(position);
				
		TextView tvFriendName	= (TextView) v.findViewById(R.id.tv_profile_name);
		ImageView ivFriendPic	= (ImageView) v.findViewById(R.id.iv_profile_pic);
		LinearLayout llBadgesContainer = (LinearLayout) v.findViewById(R.id.profile_badges_container);
		
		List<Badge> badges = friend.getBadges();
				
		if(badges.size()>0) {
			setBadge(v, badges.get(0), R.id.iv_badge1);
			setBadge(v, badges.get(1), R.id.iv_badge2);
			setBadge(v, badges.get(2), R.id.iv_badge3);
			setBadge(v, badges.get(3), R.id.iv_badge4);
			
			TextView tvDots	= (TextView) v.findViewById(R.id.dots);
			if(badges.size()>4)
				tvDots.setVisibility(View.VISIBLE);
			else
				tvDots.setVisibility(View.INVISIBLE);
			
			TextView tvNoBadges	= (TextView) v.findViewById(R.id.tv_no_badges);
			tvNoBadges.setVisibility(View.INVISIBLE);
			
		} else {
			setBadge(v, null, R.id.iv_badge1);
			setBadge(v, null, R.id.iv_badge2);
			setBadge(v, null, R.id.iv_badge3);
			setBadge(v, null, R.id.iv_badge4);
			
			TextView tvDots	= (TextView) v.findViewById(R.id.dots);
			tvDots.setVisibility(View.INVISIBLE);
			
			TextView tvNoBadges	= (TextView) v.findViewById(R.id.tv_no_badges);
			tvNoBadges.setVisibility(View.VISIBLE);
		}
		
		String name = null;
		if(friend.getName()!=null)
			name = friend.getName();
		else
			name = "";
		tvFriendName.setText(name);
		friend.setProfilePhoto(ivFriendPic);
		
		ProfileDetailsFragment.injectBadgesToView(imageLoader, defaultOptions, llBadgesContainer, 
				friend.getBadges(), thisActivity, 3);
		return v;
	}
	
	void setBadge(View v, Badge badge, int imageId) {
		ImageView image = (ImageView) v.findViewById(imageId);
		
		if(badge!=null && badge.getImageUrl()!=null) {
			imageLoaderBadge1.displayImage(badge.getImageUrl(), image);
			LinearLayout.LayoutParams msgFramelayoutParams = (LinearLayout.LayoutParams) image.getLayoutParams();
			msgFramelayoutParams.setMargins(10,0,0,0);
			image.setLayoutParams(msgFramelayoutParams);
		} else {
			image.setImageResource(android.R.color.transparent);
		}
	}
}
