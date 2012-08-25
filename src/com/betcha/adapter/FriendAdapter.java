package com.betcha.adapter;

import java.util.List;

import org.springframework.util.StringUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.model.User;

public class FriendAdapter extends ArrayAdapter<User> {
	BetchaApp app;
	private List<User> items;
	
	public FriendAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		super(context, textViewResourceId,objects);
		this.items = objects;
		
		app = (BetchaApp) context.getApplicationContext();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.invite_list_item), null);
		}
		
		User user = items.get(position);
		
		TextView tvUserName = (TextView) v.findViewById(R.id.tv_invite_user);
		TextView tvContact = (TextView) v.findViewById(R.id.tv_invite_user_contact);
		CheckBox cbIsInvited = (CheckBox) v.findViewById(R.id.cb_is_invited);
		
		if(user.getName()!=null && user.getName()!=null) {
 	    	String[] splitEmail = StringUtils.split(user.getEmail(), "@");
 	    	String domain = null;
 	    	if (splitEmail!=null && splitEmail.length>1)
 	    		domain = splitEmail[1];
 	    	if(domain!=null) {
 	    		tvUserName.setText(user.getName());
 	    		tvContact.setText("@"+domain);
 	    	}
		}  
		
		cbIsInvited.setChecked(false); //true = send bet
		
		return v;		
	}
	
}
