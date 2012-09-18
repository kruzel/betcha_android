package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class FriendAdapter extends ArrayAdapter<User> {
	BetchaApp app;
	
	public FriendAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		super(context, textViewResourceId,objects);
	
		app = (BetchaApp) context.getApplicationContext();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.invite_list_item), null);
		}
	
		User user = app.getFriends().get(position); // getItem(position);
		
		TextView tvUserName = (TextView) v.findViewById(R.id.tv_invite_user);
		TextView tvContact = (TextView) v.findViewById(R.id.tv_invite_user_contact);
		CheckBox cbIsInvited = (CheckBox) v.findViewById(R.id.cb_is_invited);
		ImageView profPic = (ImageView) v.findViewById(R.id.contact_profile_picture);
		
		if(user.getEmail()!=null && user.getEmail().length()!=0) {
	    		tvContact.setText("");
	    		tvUserName.setText(user.getEmail());
 		} else {
 			tvContact.setText(user.getName());
	    	tvUserName.setText("f");
 		}
		
		cbIsInvited.setChecked(user.getIsInvitedToBet());
		cbIsInvited.setTag(user);
		
		//Load from contacts
		Bitmap bm = user.getProfilePhoto(this);
		profPic.setImageBitmap(bm);
	
		cbIsInvited.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v ;  
	            User user = (User) cb.getTag();  
	            user.setIsInvitedToBet(cb.isChecked() );  
				
			}
		});
		
		return v;		
	}
	
	
	
}
