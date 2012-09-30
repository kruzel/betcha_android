package com.betcha.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.R;
import com.betcha.adapter.ChatMessageAdapter;
import com.betcha.model.Bet;
import com.betcha.model.cache.IModelListener;

public class BetChatMessagesFragment extends SherlockFragment implements IModelListener {
	private Bet bet;
	private ListView lvMessages;
	private ChatMessageAdapter chatMessageAdapter;

	public void init(Bet bet) {
		this.bet = bet;
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
		
		Button btnSend = (Button) view.findViewById(R.id.buttonChatMessageSend);
		btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
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

	@Override
	public void onCreateComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetWithDependentsComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSyncComplete(Class clazz, Boolean success) {
		// TODO Auto-generated method stub
		
	}

}
