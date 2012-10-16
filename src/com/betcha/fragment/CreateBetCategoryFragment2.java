package com.betcha.fragment;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.betcha.R;
import com.betcha.adapter.CategoryAdapter2;
import com.betcha.model.Bet;
import com.betcha.model.Category;

public class CreateBetCategoryFragment2 extends SherlockListFragment {
    
	private Bet bet;
	private CategoryAdapter2 adapter;
	
	private OnBetCategorySelectionListener2 listener;
	
	public interface OnBetCategorySelectionListener2 {
		public void OnBetCategorySelected();
	}

	public void init(Bet bet, List<Category> customCategoriesList, List<Category> featuredCategoriesList ) {
		this.bet = bet;

		List<Object> items = new LinkedList<Object>();
		items.addAll(customCategoriesList);
		items.add(CategoryAdapter2.sSpace);
		items.addAll(featuredCategoriesList);
		
		this.adapter.setObjects(items);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
            listener = (OnBetCategorySelectionListener2) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnBetCategorySelectionListener");
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.create_menu, null, false);
		
		this.adapter = new CategoryAdapter2(view.getContext());
		setListAdapter(this.adapter);
		
		return view;
	}

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object o = this.adapter.getItem(position);
        if (o instanceof Category) {
            Category cat = (Category) o;
            bet.setCategoryId(cat.getId());
            listener.OnBetCategorySelected();
        }
    }

}
