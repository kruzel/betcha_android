package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.R;

import com.betcha.fragment.ProfileDetailsFragment;
import com.betcha.model.Friend;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class FriendProfileAdapter extends ArrayAdapter<Friend>{

	
	private BetchaApp app;
	private FragmentActivity thisActivity;
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	
	public FriendProfileAdapter(FragmentActivity context, int textViewResourceId,
			List<Friend> friends) {
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
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.badge_list_item), null);
		}
		
		Friend friend = getItem(position);
				
		TextView tvFriendName	= (TextView) v.findViewById(R.id.iv_profile_pic);
		ImageView ivFriendPic	= (ImageView) v.findViewById(R.id.tv_profile_name);
		LinearLayout llBadgesContainer = (LinearLayout) v.findViewById(R.id.profile_badges_container);
		
		tvFriendName.setText(friend.getFriend().getName());
		friend.getFriend().setProfilePhoto(ivFriendPic);
		
		ProfileDetailsFragment.injectBadgesToView(imageLoader, defaultOptions, llBadgesContainer, 
				friend.getFriend().getBadges(), thisActivity, 3);
		return v;
	}
}