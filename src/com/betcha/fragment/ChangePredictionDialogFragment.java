package com.betcha.fragment;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.PredictionSuggestion;

public class ChangePredictionDialogFragment extends SherlockDialogFragment implements OnEditorActionListener {
    
	private static final String ARG_SUGGESTION_ID = "suggestionId";
    private static final String ARG_PREDICTION = "prediction";
	private static final String ARG_SUGGESTION_IDS = "suggestionIds";
	private static final String ARG_SUGGESTION_NAMES = "suggestionNames";
    private static final String ARG_SUGGESTION_DRAWABLES = "suggestionDrawables";
    
    private String mSuggestionId;
    private String mPrediction;
    private Suggestion[] mSuggestions;
    
    private OnPredictionSelectedListener mListener; 
    
    public static ChangePredictionDialogFragment newInstance(String suggestionId, String current, String[] suggestionIds, String[] suggestionNames, int[] suggestionDrawables) {
        ChangePredictionDialogFragment f = new ChangePredictionDialogFragment();
        
        Bundle args = new Bundle();
        args.putString(ARG_PREDICTION, current);
        args.putString(ARG_SUGGESTION_ID, suggestionId);
        args.putStringArray(ARG_SUGGESTION_IDS, suggestionIds);
        args.putStringArray(ARG_SUGGESTION_NAMES, suggestionNames);
        args.putIntArray(ARG_SUGGESTION_DRAWABLES, suggestionDrawables);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnPredictionSelectedListener {
        void onPredictionSelected(String suggestionId, String prediction);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnPredictionSelectedListener) {
            mListener = (OnPredictionSelectedListener) activity;
        }
    }
    
    public void setListener(OnPredictionSelectedListener mListener) {
		this.mListener = mListener;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setStyle(STYLE_NO_FRAME, R.style.Theme_Sherlock_Dialog);
        
        mPrediction = getArguments().getString(ARG_PREDICTION);
        mSuggestionId = getArguments().getString(ARG_SUGGESTION_ID);
    
        String[] suggestionIds = getArguments().getStringArray(ARG_SUGGESTION_IDS);
        String[] suggestionNames = getArguments().getStringArray(ARG_SUGGESTION_NAMES);
        int[] suggestionDrawables = getArguments().getIntArray(ARG_SUGGESTION_DRAWABLES);
        if (suggestionNames.length != suggestionDrawables.length) {
            throw new IllegalArgumentException();
        }
        
        mSuggestions = new Suggestion[suggestionNames.length];
        for (int i = 0; i < suggestionNames.length; i++) {
            Suggestion suggestion = new Suggestion();
            suggestion.id = suggestionIds[i];
            suggestion.name = suggestionNames[i];
            suggestion.drawable = suggestionDrawables[i];
            mSuggestions[i] = suggestion;
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x77000000));
        
        View view = inflater.inflate(R.layout.create_prediction_dialog, container, false);
        
        FontUtils.setTextViewTypeface(view, R.id.tv_prediction_label, CustomFont.HELVETICA_CONDENSED);
        
        ViewGroup suggestionsContainer1 = (ViewGroup) view.findViewById(R.id.ll_suggestions1);
        ViewGroup suggestionsContainer2 = (ViewGroup) view.findViewById(R.id.ll_suggestions2);
        for (int i = 0; i < mSuggestions.length; ++i) {
            final Suggestion suggestion = mSuggestions[i];
            View suggestionView = inflater.inflate(R.layout.create_dialog_prediction_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_CONDENSED_BOLD);
            textView.setText(suggestion.name);
            textView.setTag(suggestion.id);
            
            suggestionView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	TextView tv = (TextView) v.findViewById(R.id.tv_suggestion_text);
                	if(tv.getTag()!=null && tv.getText()!=null) 
                    onSuggestionClicked(tv.getTag().toString(),tv.getText().toString());
                }
            });
            
            ViewGroup suggestionsContainer = (i % 2 == 0 ? suggestionsContainer1 : suggestionsContainer2);
            suggestionsContainer.addView(suggestionView);
        }

        EditText editText = (EditText) view.findViewById(R.id.et_prediction);
        FontUtils.setTextViewTypeface(editText, CustomFont.HELVETICA_CONDENSED_BOLD);
        editText.setOnEditorActionListener(this);
        
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_PREDICTION)) {
            mPrediction = savedInstanceState.getString(ARG_PREDICTION);
            mSuggestionId = savedInstanceState.getString(ARG_SUGGESTION_ID);
        }
        if(mSuggestionId!=null && !mSuggestionId.equals("0"))
        	mPrediction = PredictionSuggestion.get(mSuggestionId).getName();
        
        if (mPrediction == null) {
            mPrediction = "";
        }
        editText.setText(mPrediction);
        editText.setTag(mSuggestionId);
        editText.setSelection(editText.getText().length());
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        EditText editText = getEditText();
        if (editText != null) {
            outState.putString(ARG_PREDICTION, editText.getText().toString());
            outState.putString(ARG_SUGGESTION_ID, editText.getTag().toString());
        }
    }
    
    private EditText getEditText() {
        View view = getView();
        if (view != null) {
            return (EditText) view.findViewById(R.id.et_prediction);
        }
        return null;
    }
    
    private void onSuggestionClicked(String suggestionId, String suggestion) {
        mPrediction = suggestion;
        mSuggestionId = suggestionId;
        
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(mPrediction);
            editText.setSelection(editText.getText().length());
        }
        
        submit(suggestionId, mPrediction);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        mPrediction = v.getText().toString();
        mSuggestionId = "0";
        
        if (TextUtils.isEmpty(mPrediction)) {
            v.setError(getString(R.string.error_missing_bet_prediction));
        } else {
            submit(mSuggestionId, mPrediction);
        }
        
        return true;
    }
    
    private void submit(String suggestionId, String prediction) {
        if (mListener != null) {
            mListener.onPredictionSelected(suggestionId, prediction);
        }
    }
    
    private class Suggestion {
    	String id;
        String name;
        int drawable;
    }
    
}
