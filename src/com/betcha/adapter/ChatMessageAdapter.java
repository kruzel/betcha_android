package com.betcha.adapter;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.FontUtils;
import com.betcha.R;
import com.betcha.FontUtils.CustomFont;
import com.betcha.model.ChatMessage;

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
		
		FontUtils.setTextViewTypeface(tvUserName, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvMessageText, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(tvDate, CustomFont.HELVETICA_CONDENSED);
		
		//TODO add name
		
		chatMessage.getUser().setProfilePhoto(ivParticipantProfPic);
		if(chatMessage.getUser().getName()==null)
			tvUserName.setText(chatMessage.getUser().getEmail());
		else
			tvUserName.setText(chatMessage.getUser().getName());
		tvMessageText.setText(chatMessage.getMessage()==null ? "" : chatMessage.getMessage() );
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM HH:mm");
		tvDate.setText(fmt.print(chatMessage.getUpdated_at()));
		
		return v;		
	}
	
}
