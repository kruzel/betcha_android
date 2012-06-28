package com.betcha.adapter;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.betcha.R;
import com.betcha.model.Bet;

public class BetAdapter extends ArrayAdapter<Bet> {

	private List<Bet> items;
	public BetAdapter(Context context, int textViewResourceId, List<Bet> bets) {
		super(context, textViewResourceId, bets);
		this.items = bets;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.bets_list_item), null);
		}
		
		Bet bet = items.get(position);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/mm HH:mm");
		
		TextView tvBetState = (TextView) v.findViewById(R.id.tv_bet_state);
		TextView tvBetDate = (TextView) v.findViewById(R.id.tv_bet_date);
		TextView tvBetSubject = (TextView) v.findViewById(R.id.tv_bet_topic);
		TextView tvBetOwner = (TextView) v.findViewById(R.id.tv_bet_owner);
		
		tvBetState.setText(bet.getState());
		tvBetDate.setText(fmt.print(bet.getDate()));
		tvBetSubject.setText(bet.getSubject());
		tvBetOwner.setText(bet.getOwner().getName());
		
		return v;
	}

	
}
