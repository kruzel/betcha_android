package com.betcha.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;

public class CreateStakeFragment extends SherlockFragment implements OnEditorActionListener {
    
    private static final String ARG_SUGGESTION_NAMES = "suggestionNames";
    private static final String ARG_SUGGESTION_DRAWABLES = "suggestionDrawables";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_STAKE = "stake";
    
    private Suggestion[] mSuggestions;
    private String mSubject;
    
    private OnStakeSelectedListener mListener;
    
    public static CreateStakeFragment newInstance(Resources resources, int suggestionNamesId, int suggestionDrawablesId, String subject) {
        String[] suggestionNames = resources.getStringArray(suggestionNamesId);
        
        TypedArray drawablesArray = resources.obtainTypedArray(suggestionDrawablesId);
        int[] suggestionDrawables = new int[drawablesArray.length()];
        for (int i = 0; i < drawablesArray.length(); i++) {
            suggestionDrawables[i] = drawablesArray.getResourceId(i, 0);
        }
        drawablesArray.recycle();
        
        return newInstance(suggestionNames, suggestionDrawables, subject);
    }
    
    public static CreateStakeFragment newInstance(String[] suggestionNames, int[] suggestionDrawables, String subject) {
        CreateStakeFragment f = new CreateStakeFragment();
        
        Bundle args = new Bundle();
        args.putStringArray(ARG_SUGGESTION_NAMES, suggestionNames);
        args.putIntArray(ARG_SUGGESTION_DRAWABLES, suggestionDrawables);
        args.putString(ARG_SUBJECT, subject);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnStakeSelectedListener {
        void onStakeSelected(String stake);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnStakeSelectedListener) {
            mListener = (OnStakeSelectedListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String[] suggestionNames = getArguments().getStringArray(ARG_SUGGESTION_NAMES);
        int[] suggestionDrawables = getArguments().getIntArray(ARG_SUGGESTION_DRAWABLES);
        if (suggestionNames.length != suggestionDrawables.length) {
            throw new IllegalArgumentException();
        }
        
        mSuggestions = new Suggestion[suggestionNames.length];
        for (int i = 0; i < suggestionNames.length; i++) {
            Suggestion suggestion = new Suggestion();
            suggestion.name = suggestionNames[i];
            suggestion.drawable = suggestionDrawables[i];
            mSuggestions[i] = suggestion;
        }

        mSubject = getArguments().getString(ARG_SUBJECT);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_stake, container, false);
        
        ImageView profileView = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
        // TODO set user's image
        
        TextView subjectView = (TextView) view.findViewById(R.id.tv_bet_topic);
        FontUtils.setTextViewTypeface(subjectView, CustomFont.HELVETICA_CONDENSED_BOLD);
        subjectView.setText(mSubject);
        
        ViewGroup suggestionsContainer1 = (ViewGroup) view.findViewById(R.id.ll_suggestions);
        ViewGroup suggestionsContainer2 = (ViewGroup) view.findViewById(R.id.ll_suggestions2);
        for (int i = 0; i < mSuggestions.length; i++) {
            final Suggestion suggestion = mSuggestions[i];
            
            View suggestionView = inflater.inflate(R.layout.create_stake_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_NORMAL);
            textView.setText(suggestion.name);
            
            ImageView imageView = (ImageView) suggestionView.findViewById(R.id.iv_suggestion_image);
            imageView.setImageResource(suggestion.drawable);
            
            suggestionView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSuggestionClicked(suggestion.name);
                }
            });
            
            ViewGroup suggestionsContainer = (i % 2 == 0 ? suggestionsContainer1 : suggestionsContainer2);
            suggestionsContainer.addView(suggestionView);
        }

        EditText editText = (EditText) view.findViewById(R.id.et_bet_stake);
        FontUtils.setTextViewTypeface(editText, CustomFont.HELVETICA_CONDENSED);
        editText.setOnEditorActionListener(this);
        
        if (savedInstanceState != null) {
            String stake = savedInstanceState.getString(ARG_STAKE);
            if (stake != null) {
                editText.setText(stake);
                editText.setSelection(editText.getText().length());
            }
        }
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        EditText editText = getEditText();
        if (editText != null) {
            outState.putString(ARG_STAKE, editText.getText().toString());
        }
    }
    
    private EditText getEditText() {
        View view = getView();
        if (view != null) {
            return (EditText) view.findViewById(R.id.et_bet_stake);
        }
        return null;
    }
    
    private void onSuggestionClicked(String suggestion) {
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(suggestion);
            editText.setSelection(editText.getText().length());
        }
        submit(suggestion);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String stake = v.getText().toString();
        if (TextUtils.isEmpty(stake)) {
            v.setError(getString(R.string.error_missing_bet_reward));
        } else {
            submit(v.getText().toString());
        }
        return true;
    }
    
    private void submit(String stake) {
        if (mListener != null) {
            mListener.onStakeSelected(stake);
        }
    }
    
    private class Suggestion {
        String name;
        int drawable;
    }
    
}
