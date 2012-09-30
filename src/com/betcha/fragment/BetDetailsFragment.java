package com.betcha.fragment;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.R;
import com.betcha.adapter.PredictionWithCbxAdapter;
import com.betcha.model.Bet;
import com.betcha.model.cache.IModelListener;

public class BetDetailsFragment extends SherlockFragment implements IModelListener {
	private Bet bet;
	private ListView lvPredictions;
	private PredictionWithCbxAdapter predictionAdapter;
	
	private DateTimeFormatter fmt;
	private ImageView ivProfPic;
	private TextView tvBetDate;
	private TextView tvBetOwner;
	
	// internal frame 
	TextView tvBetSubject;
	TextView tvBetReward;

	public void init(Bet bet) {
		this.bet = bet;
		populate();
	}
	
	public void refresh() {
		lvPredictions.invalidate();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.bets_list_item, container);
		ImageView imgNav = (ImageView) view.findViewById(R.id.image_view_nav);
		imgNav.setVisibility(View.INVISIBLE);
		
		lvPredictions = (ListView) view.findViewById(R.id.lv_bet_predictions);
		
		ivProfPic = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
		tvBetDate = (TextView) view.findViewById(R.id.tv_bet_date);
		tvBetOwner = (TextView) view.findViewById(R.id.tv_bet_owner);
		
		// internal frame 
		tvBetSubject = (TextView) view.findViewById(R.id.tv_bet_topic);
		tvBetReward = (TextView) view.findViewById(R.id.tv_bet_reward);
				
		return view;
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
	}

	protected void populate() {
		//bet owner and other details (outer frame)
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM HH:mm");
		bet.getOwner().setProfilePhoto(ivProfPic);
		tvBetDate.setText(fmt.print(bet.getDate()));
		tvBetOwner.setText(bet.getOwner().getName());
		
		tvBetSubject.setText(bet.getSubject());
		tvBetReward.setText(bet.getReward());
		
		if(predictionAdapter==null) {
			predictionAdapter = new PredictionWithCbxAdapter(getActivity(), R.layout.bets_list_item, bet.getPredictions());
			lvPredictions.setAdapter(predictionAdapter);
		} else {
			predictionAdapter.notifyDataSetChanged();
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
