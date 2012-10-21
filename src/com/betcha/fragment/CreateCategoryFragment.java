package com.betcha.fragment;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.betcha.R;
import com.betcha.adapter.CategoryAdapter;
import com.betcha.model.Category;

public class CreateCategoryFragment extends SherlockListFragment {
    
    private static final String ARG_SECTIONS = "sections";
    
    private String[] mSections;
    
    private OnCategorySelectedListener mListener;
    
    public static CreateCategoryFragment newInstance(String[] sections) {
        CreateCategoryFragment f = new CreateCategoryFragment();
        
        Bundle args = new Bundle();
        args.putStringArray(ARG_SECTIONS, sections);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnCategorySelectedListener) {
            mListener = (OnCategorySelectedListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSections = getArguments().getStringArray(ARG_SECTIONS);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_menu, container, false);
        
        Context context = view.getContext();
        
        List<Object> items = new LinkedList<Object>();
        for (String section : mSections) {
            if (!items.isEmpty()) {
                items.add(CategoryAdapter.sSpace);
            }
            items.addAll(Category.getCategories(context, section));
        }

        CategoryAdapter adapter = new CategoryAdapter(context);
        adapter.setObjects(items);
        setListAdapter(adapter);
        
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Object o = l.getAdapter().getItem(position);
        if (o instanceof Category) {
            Category category = (Category) o;
            submit(category);
        }
    }
    
    private void submit(Category category) {
        if (mListener != null) {
            mListener.onCategorySelected(category);
        }
    }
    
}
