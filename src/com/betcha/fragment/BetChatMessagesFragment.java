package com.betcha.fragment;

import java.sql.SQLException;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.BetchaApp;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.adapter.ChatMessageAdapter;
import com.betcha.model.Bet;
import com.betcha.model.ChatMessage;

public class BetChatMessagesFragment extends SherlockFragment {
	private BetchaApp app;
	
	private FrameLayout frmMessagesContainer;
	private ListView lvMessages;
	private ChatMessageAdapter chatMessageAdapter;
	
	private Button btnSend;
	private EditText etNewMessage;
	
	public void refresh() {
		chatMessageAdapter = null;
		populate();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        app = BetchaApp.getInstance();
                
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.chat_message_fragment, container);
		frmMessagesContainer = (FrameLayout) view.findViewById(R.id.fl_message_list);
		lvMessages = (ListView) view.findViewById(R.id.lv_chat_messages);
		
		btnSend = (Button) view.findViewById(R.id.buttonChatMessageSend);
		etNewMessage = (EditText) view.findViewById(R.id.et_chat_message);
		etNewMessage.clearFocus();
		
		FontUtils.setTextViewTypeface(etNewMessage, CustomFont.HELVETICA_CONDENSED);
		FontUtils.setTextViewTypeface(btnSend, CustomFont.HELVETICA_CONDENSED_BOLD);
		
		btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
	            
	            if(etNewMessage.getText().toString()!=null && etNewMessage.getText().toString().length()>0) {
					ChatMessage msg = new ChatMessage();
					msg.setUser(app.getCurUser());
					msg.setBet(app.getCurBet());
					msg.setMessage(etNewMessage.getText().toString());
					msg.create();
					try {
						Bet.getModelDao().refresh(app.getCurBet());
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					chatMessageAdapter.add(msg);
					
					populate();
					
					etNewMessage.clearFocus();
					etNewMessage.setText("");
				}
			}
		});
		
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
						
		return view;
	}
	
	@Override
	public void onResume() {
		populate();
		super.onResume();
	}

	protected void populate() {
		LayoutParams msgFramelayoutParams = frmMessagesContainer.getLayoutParams();
		msgFramelayoutParams.height = app.getCurBet().getChatMessagesCount() > 4 ? 90 * app.getCurBet().getChatMessagesCount() : 90*4;
		frmMessagesContainer.setLayoutParams(msgFramelayoutParams);
		
		if(chatMessageAdapter==null) {
			chatMessageAdapter = new ChatMessageAdapter(getActivity(), R.layout.chat_message_item, app.getCurBet().getChatMessages());
			lvMessages.setAdapter(chatMessageAdapter);
		} else {
			chatMessageAdapter.notifyDataSetChanged();
		}
	}

}
