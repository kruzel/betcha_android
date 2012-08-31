package com.betcha.adapter;

import java.util.List;

import org.springframework.util.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.User;

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
	
		User user = getItem(position);
		
		TextView tvUserName = (TextView) v.findViewById(R.id.tv_invite_user);
		TextView tvContact = (TextView) v.findViewById(R.id.tv_invite_user_contact);
		CheckBox cbIsInvited = (CheckBox) v.findViewById(R.id.cb_is_invited);
		
		if(user.getName()!=null && user.getName()!=null) {
 	    	String[] splitEmail = StringUtils.split(user.getEmail(), "@");
 	    	String domain = null;
 	    	if (splitEmail!=null && splitEmail.length>1)
 	    		domain = splitEmail[1];
 	    	if(domain!=null) {
 	    		//tvUserName.setText(user.getName());
 	    		//tvContact.setText("@"+domain);
 	    		tvUserName.setText(user.getEmail());
 	    	}
		}  
		
		cbIsInvited.setChecked(user.getIsInvitedToBet());
		cbIsInvited.setTag(user);
		
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