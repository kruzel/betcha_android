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
import com.betcha.BetchaApp;
import com.betcha.R;
import com.betcha.adapter.CategoryAdapter;
import com.betcha.model.Bet;
import com.betcha.model.Category;

public class CreateBetCategoryFragment extends SherlockListFragment {
    
	private BetchaApp app;
	
	List<Object> items;
	private CategoryAdapter adapter;
	
	private OnBetCategorySelectionListener2 listener;
	
	public interface OnBetCategorySelectionListener2 {
		public void OnBetCategorySelected();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		app = BetchaApp.getInstance();
		
		List<Category> customCategoriesList = Category.getCategories(getActivity(),"Custom");
		List<Category> featuredCategoriesList = Category.getCategories(getActivity(),"Sport");
		
		items = new LinkedList<Object>();
		items.addAll(customCategoriesList);
		items.add(CategoryAdapter.sSpace);
		items.addAll(featuredCategoriesList);
				
		super.onCreate(savedInstanceState);
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
		
		this.adapter = new CategoryAdapter(view.getContext());
		setListAdapter(this.adapter);
		this.adapter.setObjects(items);
		
		return view;
	}

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object o = this.adapter.getItem(position);
        if (o instanceof Category) {
            Category cat = (Category) o;
            app.getCurBet().setCategoryId(cat.getId());
            listener.OnBetCategorySelected();
        }
    }

}
