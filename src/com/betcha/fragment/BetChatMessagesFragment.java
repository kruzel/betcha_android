package com.betcha.fragment;

import java.sql.SQLException;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.R;
import com.betcha.adapter.ChatMessageAdapter;
import com.betcha.model.Bet;
import com.betcha.model.ChatMessage;
import com.betcha.model.User;

public class BetChatMessagesFragment extends SherlockFragment {
	private User curUser;
	private Bet bet;
	private ListView lvMessages;
	private ChatMessageAdapter chatMessageAdapter;
	
	private Button btnSend;
	private EditText etNewMessage;

	public void init(Bet bet, User curUser) {
		this.bet = bet;
		this.curUser = curUser;
		populate();
	}
	
	public void refresh() {
		lvMessages.invalidate();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.chat_message_fragment, container);
		lvMessages = (ListView) view.findViewById(R.id.lv_chat_messages);
		
		btnSend = (Button) view.findViewById(R.id.buttonChatMessageSend);
		etNewMessage = (EditText) view.findViewById(R.id.et_chat_message);
		
		btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
	            
	            if(etNewMessage.getText().toString()!=null && etNewMessage.getText().toString().length()>0) {
					ChatMessage msg = new ChatMessage();
					msg.setUser(curUser);
					msg.setBet(bet);
					msg.setMessage(etNewMessage.getText().toString());
					msg.create();
					try {
						Bet.getModelDao().refresh(bet);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					chatMessageAdapter.add(msg);
					
					populate();
				}
			}
		});
		
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
						
		return view;
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
	}

	protected void populate() {
		if(chatMessageAdapter==null) {
			chatMessageAdapter = new ChatMessageAdapter(getActivity(), R.layout.chat_message_item, bet.getChatMessages());
			lvMessages.setAdapter(chatMessageAdapter);
		} else {
			chatMessageAdapter.notifyDataSetChanged();
		}
	}

}
