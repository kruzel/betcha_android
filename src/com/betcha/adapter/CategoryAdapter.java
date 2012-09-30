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
import com.betcha.model.Category;

public class CategoryAdapter extends ArrayAdapter<Category> {
	
	public CategoryAdapter(Context context, int textViewResourceId,
			List<Category> objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate((R.layout.create_bet_category_item), null);
		}
		
		Category category = getItem(position);
		
		ImageView ivCategoryImage = (ImageView) v.findViewById(R.id.iv_category_image);
		TextView tvDescription = (TextView) v.findViewById(R.id.tv_bet_category_description);
		TextView tvCategory = (TextView) v.findViewById(R.id.tv_bet_category);
		
		//ivCategoryImage.setImageBitmap(bm);
		tvDescription.setText(category.getDescription());
		tvCategory.setText(category.getCategory());
		
		return v;		
	}
	
}
