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
import com.betcha.model.Prediction;

public class PredictionAdapter extends ArrayAdapter<Prediction> {
	private List<Prediction> items;
	
	public PredictionAdapter(Context context, int textViewResourceId,
			List<Prediction> objects) {
		super(context, textViewResourceId, objects);
		this.items = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder = null; // to reference the child views for later actions
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.bet_prediction_short_item), null);
			
			holder = new ViewHolder();
			holder.ivParticipantProfPic = (ImageView) v.findViewById(R.id.iv_participant_pic);
			holder.tvParticipantName = (TextView) v.findViewById(R.id.tv_participant_name);
			holder.tvParticipantPrediction = (TextView) v.findViewById(R.id.tv_participant_prediction);
			
			// associate the holder with the view for later lookup
            v.setTag(holder);
		    
		} else {
            // view already exists, get the holder instance from the view
            holder = (ViewHolder)v.getTag();
        }
		
		Prediction prediction = items.get(position);
		
		if(prediction.getUser()!=null) { //should not happen
			prediction.getUser().setProfilePhoto(holder.ivParticipantProfPic);
			if(prediction.getUser().getName()==null)
				holder.tvParticipantName.setText(prediction.getUser().getEmail().substring(0, prediction.getUser().getEmail().indexOf('@')));
			else
				holder.tvParticipantName.setText(prediction.getUser().getName());
		}
		
		holder.tvParticipantPrediction.setText(prediction.getPrediction()==null ? "" : prediction.getPrediction() );
		
		return v;		
	}
	
	private class ViewHolder {
		ImageView ivParticipantProfPic;
		TextView tvParticipantName;
		TextView tvParticipantPrediction;
	}
}
