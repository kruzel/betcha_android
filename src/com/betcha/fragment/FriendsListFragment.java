package com.betcha.fragment;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.BetchaApp;
import com.betcha.R;

import com.betcha.adapter.FriendProfileAdapter;
import com.betcha.model.Friend;


public class FriendsListFragment extends SherlockFragment{
	private BetchaApp app;
	private TextView tvNumOfFriends;
	private ListView lvFriendsList;
	private FriendProfileAdapter friendAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = BetchaApp.getInstance();
                
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view 		= (ViewGroup) inflater.inflate(R.layout.friends_list_fragment,container);
		tvNumOfFriends	= (TextView) view.findViewById(R.id.num_of_friends);
		lvFriendsList	= (ListView) view.findViewById(R.id.lv_friends_list);
		return view;
	}
	
	@Override
	public void onResume() {
		populate();
		super.onResume();
	}
	
	public void refresh() {
		populate();
	}
	
	protected void populate() {
		
		List<Friend> friends = app.getCurUser().getFriends();
		Integer size = 0;
		if (friends != null)
			size = friends.size();
		tvNumOfFriends.setText(size.toString());
		
		friendAdapter = new FriendProfileAdapter(getActivity(), R.layout.friends_list_item, friends);
		lvFriendsList.setAdapter(friendAdapter);
	}
	
}

