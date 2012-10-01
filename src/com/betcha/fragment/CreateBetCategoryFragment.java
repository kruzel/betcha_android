package com.betcha.fragment;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.R;
import com.betcha.adapter.CategoryAdapter;
import com.betcha.model.Bet;
import com.betcha.model.Category;

public class CreateBetCategoryFragment extends SherlockFragment {
	private Bet bet;
	private ListView lvCustomCategories;
	private ListView lvFeaturedCategories;
	private List<Category> customCategoriesList;
	private List<Category> featuredCategoriesList;
	private CategoryAdapter customCatgoryAdapter;
	private CategoryAdapter featuredCatgoryAdapter;
	
	private OnBetCategorySelectionListener listener;
	
	public interface OnBetCategorySelectionListener {
		public void OnBetCategorySelected();
	}

	public void init(Bet bet, List<Category> customCategoriesList, List<Category> featuredCategoriesList ) {
		this.bet = bet;
		this.customCategoriesList = customCategoriesList;
		this.featuredCategoriesList = featuredCategoriesList;
		
		populate();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
            listener = (OnBetCategorySelectionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnBetCategorySelectionListener");
        }
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = (ViewGroup) inflater.inflate(R.layout.create_bet_category_fragment, null,false);
		lvCustomCategories = (ListView) view.findViewById(R.id.custom_categories_list);
		lvFeaturedCategories = (ListView) view.findViewById(R.id.featured_categories_list);
		
		lvCustomCategories.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				bet.setCategoryId(customCategoriesList.get(position).getId());
				listener.OnBetCategorySelected();
			}
		});
		
		lvFeaturedCategories.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				bet.setCategoryId(featuredCategoriesList.get(position).getId());
				listener.OnBetCategorySelected();
			}
		});
		
		return view;
	}
	
	@Override
	public void onResume() {
		
		super.onResume();
	}

	protected void populate() {
		if(customCatgoryAdapter==null) {
			customCatgoryAdapter = new CategoryAdapter(getActivity(), R.layout.create_bet_category_item, customCategoriesList);
			lvCustomCategories.setAdapter(customCatgoryAdapter);
		} else {
			customCatgoryAdapter.notifyDataSetChanged();
		}
		
//		ViewGroup.LayoutParams layoutParams = lvCustomCategories.getLayoutParams();
//		layoutParams.height = layoutParams.height * customCategoriesList.size();
//		lvCustomCategories.setLayoutParams(layoutParams);
		
		if(featuredCatgoryAdapter==null) {
			featuredCatgoryAdapter = new CategoryAdapter(getActivity(), R.layout.create_bet_category_item, featuredCategoriesList);
			lvFeaturedCategories.setAdapter(featuredCatgoryAdapter);
		} else {
			featuredCatgoryAdapter.notifyDataSetChanged();
		}
		
		//TODO dynamic hieght of list view
//		layoutParams = lvFeaturedCategories.getLayoutParams();
//		layoutParams.height = layoutParams.height * featuredCategoriesList.size();
//		lvFeaturedCategories.setLayoutParams(layoutParams);
		
	}
}
