package com.betcha.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.betcha.FontUtils;
import com.betcha.R;
import com.betcha.FontUtils.CustomFont;
import com.betcha.model.Category;

public class CategoryAdapter extends BaseAdapter {
    
    public static final Object sSpace = new Object();
    
    private static final int TYPE_FULL = 0;
    private static final int TYPE_TOP = 1;
    private static final int TYPE_MID = 2;
    private static final int TYPE_BOTTOM = 3;
    private static final int TYPE_SPACE = 4;
    private static final int TYPE_COUNT = 5;
    
    private final Context mContext;
    
    private final List<Object> mObjects; 
	
	public CategoryAdapter(Context context) {
	    mContext = context;
	    mObjects = new ArrayList<Object>();
	    mObjects.add(sSpace);
	}
	
	public void setObjects(List<Object> objects) {
	    mObjects.clear();
        mObjects.add(sSpace);
        mObjects.addAll(objects);
        mObjects.add(sSpace);
        notifyDataSetChanged();
	}
	
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != TYPE_SPACE;
    }
    
    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return mObjects.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        Object o = getItem(position);
        
        if (o.equals(sSpace)) {
            return TYPE_SPACE;
        }
        
        // we always have a space as the top and bottom items
        Object prev = getItem(position - 1);
        Object next = getItem(position + 1);
        
        if (prev.equals(sSpace)) {
            if (next.equals(sSpace)) {
                return TYPE_FULL;
            } else {
                return TYPE_TOP;
            }
        } else {
            if (next.equals(sSpace)) {
                return TYPE_BOTTOM;
            } else {
                return TYPE_MID;
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }
    
    private int getItemMarkerColor(int position) {
        float hue = (position * 50) % 360;
        float saturation = 0.5f;
        float value = 0.85f;
        return Color.HSVToColor(new float[] {hue, saturation, value});
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        
		if (convertView == null) {
		    int resId = (type == TYPE_SPACE ? R.layout.create_menu_space : R.layout.create_menu_item);
		    convertView = LayoutInflater.from(mContext).inflate(resId, null);

		    FontUtils.setTextViewTypeface(convertView, R.id.tv_bet_category, CustomFont.HELVETICA_BOLD);
            FontUtils.setTextViewTypeface(convertView, R.id.tv_bet_category_description, CustomFont.HELVETICA_CONDENSED);
		}

		if (type == TYPE_SPACE) {
		    // nothing to do in space views
		    return convertView;
		}
		
		Category category = (Category) getItem(position);
		
		// content
		ImageView ivCategoryImage = (ImageView) convertView.findViewById(R.id.iv_category_image);
        ImageView ivMarker = (ImageView) convertView.findViewById(R.id.iv_marker);
		TextView tvCategory = (TextView) convertView.findViewById(R.id.tv_bet_category);
        TextView tvDescription = (TextView) convertView.findViewById(R.id.tv_bet_category_description);
		
		//ivCategoryImage.setImageBitmap(bm);
		ivMarker.setColorFilter(getItemMarkerColor(position), PorterDuff.Mode.MULTIPLY);
		tvCategory.setText(category.getName());
		if (!TextUtils.isEmpty(category.getDescription())) {
	        tvDescription.setText(category.getDescription());
	        tvDescription.setVisibility(View.VISIBLE);
		} else {
            tvDescription.setVisibility(View.GONE);
		}
        if (position % 2 == 0) {
            tvDescription.setText("Sets the string value of the TextView. TextView does not accept HTML-like formatting");
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }
		
		// presentation
		View dividerTop = convertView.findViewById(R.id.iv_divider_top);
        View dividerBottom = convertView.findViewById(R.id.iv_divider_bottom);
        View frame = convertView.findViewById(R.id.rl_category_frame);
        View content = convertView.findViewById(R.id.ll_category_content);

        switch (type) {
            case TYPE_FULL:
                dividerTop.setVisibility(View.GONE);
                dividerBottom.setVisibility(View.GONE);
                frame.setBackgroundResource(R.drawable.dark_frame_full);
                content.setBackgroundResource(R.drawable.offwhite_bg_selector_full);
                break;

            case TYPE_TOP:
                dividerTop.setVisibility(View.GONE);
                dividerBottom.setVisibility(View.VISIBLE);
                frame.setBackgroundResource(R.drawable.dark_frame_top);
                content.setBackgroundResource(R.drawable.offwhite_bg_selector_top);
                break;

            case TYPE_BOTTOM:
                dividerTop.setVisibility(View.VISIBLE);
                dividerBottom.setVisibility(View.GONE);
                frame.setBackgroundResource(R.drawable.dark_frame_bottom);
                content.setBackgroundResource(R.drawable.offwhite_bg_selector_bottom);
                break;

            default:
                dividerTop.setVisibility(View.VISIBLE);
                dividerBottom.setVisibility(View.VISIBLE);
                frame.setBackgroundResource(R.drawable.dark_frame_mid);
                content.setBackgroundResource(R.drawable.offwhite_bg_selector_mid);
                break;
        }
        
		return convertView;
	}
	
}
