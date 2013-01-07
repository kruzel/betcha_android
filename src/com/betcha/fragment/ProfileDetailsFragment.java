package com.betcha.fragment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.Badge;
import com.betcha.model.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class ProfileDetailsFragment extends SherlockFragment{
	
	private BetchaApp app;
	private User userToShow;
	
	//views to fill with data
	private ImageView	ivProfPic;
	private TextView	tvFirstName;
	private TextView	tvLastName;
	private TextView	tvStatus;
	private TextView	tvCoins;
	
	//private ListView lvBadges;
	private LinearLayout llBadgesContainer;
	
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = BetchaApp.getInstance();
   
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = (ViewGroup) inflater.inflate(R.layout.profile_details_fragment,container);
		
		ivProfPic	= (ImageView) view.findViewById(R.id.iv_profile_pic);
		tvFirstName	= (TextView) view.findViewById(R.id.tv_profile_first_name);
		tvLastName	= (TextView) view.findViewById(R.id.tv_profile_last_name);
		tvStatus	= (TextView) view.findViewById(R.id.tv_profile_status);
		tvCoins		= (TextView) view.findViewById(R.id.tv_profile_coins);
		//lvBadges	= (ListView) view.findViewById(R.id.lv_profile_badges);
		llBadgesContainer = (LinearLayout) view.findViewById(R.id.profile_badges_container);
		return view;
	}
	
	@Override
	public void onResume() {
		populate();
		super.onResume();
	}
	
	/*public void refresh() {
		populate();
	}*/
	
	protected void populate() {
		
		if (userToShow == null)
			userToShow = app.getCurUser();
		
		String[] names =userToShow.getName().split(" ", 2);
		
		userToShow.setProfilePhoto(ivProfPic);
		tvFirstName.setText(names[0]);
		tvLastName.setText(names[1]);
		tvStatus.setText("TODO: change this");//TODO: change this
		tvCoins.setText(coinsFromUser(userToShow));
		List<Badge> badgeList = userToShow.getBadges();
		injectBadgesToView(imageLoader,defaultOptions,llBadgesContainer,badgeList,app,4);
	}
	
	//TODO: change this
	private String coinsFromUser(User user){
		return "000";
	}
	
	
	
	public static void injectBadgesToView(ImageLoader imageLoader, DisplayImageOptions defaultOptions, 
			LinearLayout injectPoint, List<Badge> badgeList, Context context, int maxBadges){
		
		if (badgeList == null || badgeList.size() == 0)
			return;
		injectPoint.removeAllViews();
		
		Collections.sort(badgeList, new Comparator<Badge>(){
			@Override
			public int compare(Badge lhs, Badge rhs) {
				int lv,rv;
				if (lhs.getValue() == null)  
					lv=0;
				else 
					lv = lhs.getValue();
				if (rhs.getValue() == null)  
					rv=0;
				else 
					rv = rhs.getValue();
				return -(lv-rv);
			}
		});
		
		if (badgeList.size() < maxBadges)
			maxBadges = badgeList.size();
		
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for(int i=0; i<maxBadges; i++){
			Badge badge = badgeList.get(i);
			View v = vi.inflate(R.layout.badge_list_item, null);
			
			TextView tvBadgeAmount	= (TextView) v.findViewById(R.id.tv_badge_amount);
			ImageView ivBadgePic	= (ImageView) v.findViewById(R.id.iv_badge_pic);
			
			Integer value = badge.getValue();
			if (value != null)
				tvBadgeAmount.setText(value.toString());
				
			imageLoader.displayImage(badge.getImageUrl() , ivBadgePic,defaultOptions);
			ivBadgePic.setContentDescription(badge.getName());
			
			injectPoint.addView(v);
		}
	}
	
	public void setUserToShow(User user){
		userToShow = user;
	}
}
