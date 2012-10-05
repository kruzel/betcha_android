package com.betcha.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.R;
import com.betcha.model.User;

public class FriendAdapter extends ArrayAdapter<User> implements Filterable {
	
	private final Object mLock = new Object();
    private ItemsFilter mFilter;
    List<User> mItems;
    List<User> mAllItems;
	
	public FriendAdapter(Context context, int textViewResourceId,
			List<User> objects) {
		super(context, textViewResourceId,objects);
		this.mAllItems = objects;
		mItems = objects;
	}
	
	@Override
	public int getCount() {
		synchronized (mLock) {
			if(mItems==null)
				return 0;
			else
				return mItems.size();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.invite_list_item), null);
		}
	
		User user = null;
		synchronized (mLock) {
			if(position<mItems.size())
				user = mItems.get(position); // getItem(position);
		}
		
		if(user==null)
			return null;
		
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
		user.cancelProfilePhotoUpdate(profPic);
		user.setProfilePhoto(profPic);
			
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
	
	/**
     * Implementing the Filterable interface.
     */
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ItemsFilter();
        }
        return mFilter;
    }
	
    /**
     * Custom Filter implementation for the items adapter.
     *
     */
    private class ItemsFilter extends Filter {
        protected FilterResults performFiltering(CharSequence prefix) {
            // Initiate our results object
            FilterResults results = new FilterResults();
            // If the adapter array is empty, check the actual items array and use it
            if (mAllItems == null) {
            	results.values = null;
                results.count = 0;
                return results;
            }
            // No prefix is sent to filter by so we're going to send back the original array
            if (prefix == null || prefix.length() == 0) {
                    results.values = mAllItems;
                    results.count = mAllItems.size();
            } else {
                    // Compare lower case strings
                String prefixString = prefix.toString().toLowerCase();
                // Local to here so we're not changing actual array
                final List<User> items = mAllItems;
                final int count = items.size();
                final List<User> newItems = new ArrayList<User>(count);
                for (int i = 0; i < count; i++) {
                    final User item = items.get(i);
                    final String itemName = item.getName().toString().toLowerCase();
                    // First match against the whole, non-splitted value
                    if (itemName.startsWith(prefixString)) {
                        newItems.add(item);
                    } else {} /* This is option and taken from the source of ArrayAdapter
                        final String[] words = itemName.split(" ");
                        final int wordCount = words.length;
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newItems.add(item);
                                break;
                            }
                        }
                    } */
                }
                // Set and return
                results.values = newItems;
                results.count = newItems.size();
            }
            return results;
        }
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence prefix, FilterResults results) {
            //noinspection unchecked
        	synchronized (mLock) {
        		mItems = (List<User>) results.values;
        	}
            // Let the adapter know about the updated list
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
    
}
