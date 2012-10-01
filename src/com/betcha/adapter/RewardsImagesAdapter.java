package com.betcha.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.betcha.model.Reward;

public class RewardsImagesAdapter extends BaseAdapter {
	
	private Context context;
	private List<Reward> items;
	
	/** Simple Constructor saving the 'parent' context. */
	public RewardsImagesAdapter(Context c, List<Reward> items) {
		this.context = c;
		this.items = items;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView iv = new ImageView(context);
		iv.setImageBitmap(items.get(position).getImage());
		
		iv.setLayoutParams(new Gallery.LayoutParams(95, 70));
		
		return iv;
	}

}
