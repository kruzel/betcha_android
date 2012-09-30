package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.R;
import com.betcha.model.ChatMessage;
import com.betcha.model.Prediction;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
	private List<ChatMessage> items;
	
	public ChatMessageAdapter(Context context, int textViewResourceId,
			List<ChatMessage> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.chat_message_item), null);
		}
		
		ChatMessage chatMessage = items.get(position);
		
		ImageView ivParticipantProfPic = (ImageView) v.findViewById(R.id.iv_participant_pic);
		TextView tvUserName = (TextView) v.findViewById(R.id.tv_participant_name);
		TextView tvMessageText = (TextView) v.findViewById(R.id.tv_chat_message);
		TextView tvDate = (TextView) v.findViewById(R.id.tv_chat_message_date);
		
		//TODO add name
		
		chatMessage.getUser().setProfilePhoto(ivParticipantProfPic);
		if(chatMessage.getUser().getName()==null)
			tvUserName.setText(chatMessage.getUser().getEmail());
		else
			tvUserName.setText(chatMessage.getUser().getName());
		tvDate.setText(chatMessage.getMessage()==null ? "" : chatMessage.getMessage() );
		tvDate.setText(chatMessage.getUpdated_at().toString());
		
		return v;		
	}
	
}
