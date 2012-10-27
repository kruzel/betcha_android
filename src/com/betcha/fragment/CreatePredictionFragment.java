package com.betcha.fragment;

import android.app.Activity;
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

public class CreatePredictionFragment extends SherlockDialogFragment implements OnEditorActionListener {
    
    private static final String ARG_PREDICTION = "prediction";
    private static final String ARG_SUGGESTIONS = "suggestions";
    
    private String mPrediction;
    private String[] mSuggestions;
    
    private OnPredictionSelectedListener mListener; 
    
    public static CreatePredictionFragment newInstance(String current, String[] suggestions) {
        CreatePredictionFragment f = new CreatePredictionFragment();
        
        Bundle args = new Bundle();
        args.putString(ARG_PREDICTION, current);
        args.putStringArray(ARG_SUGGESTIONS, suggestions);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnPredictionSelectedListener {
        void onPredictionSelected(String prediction);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnPredictionSelectedListener) {
            mListener = (OnPredictionSelectedListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setStyle(STYLE_NO_FRAME, 0);
        
        mPrediction = getArguments().getString(ARG_PREDICTION);
        mSuggestions = getArguments().getStringArray(ARG_SUGGESTIONS);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_prediction, container, false);
        
        ViewGroup suggestionsContainer1 = (ViewGroup) view.findViewById(R.id.ll_suggestions1);
        ViewGroup suggestionsContainer2 = (ViewGroup) view.findViewById(R.id.ll_suggestions2);
        for (int i = 0; i < mSuggestions.length; ++i) {
            final String suggestion = mSuggestions[i];
            View suggestionView = inflater.inflate(R.layout.create_prediction_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_CONDENSED);
            textView.setText(suggestion);
            
            suggestionView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSuggestionClicked(suggestion);
                }
            });
            
            ViewGroup suggestionsContainer = (i % 2 == 0 ? suggestionsContainer1 : suggestionsContainer2);
            suggestionsContainer.addView(suggestionView);
        }

        EditText editText = (EditText) view.findViewById(R.id.et_bet_prediction);
        FontUtils.setTextViewTypeface(editText, CustomFont.HELVETICA_CONDENSED);
        editText.setOnEditorActionListener(this);
        
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_PREDICTION)) {
            mPrediction = savedInstanceState.getString(ARG_PREDICTION);
        }
        if (mPrediction == null) {
            mPrediction = "";
        }
        editText.setText(mPrediction);
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        EditText editText = getEditText();
        if (editText != null) {
            outState.putString(ARG_PREDICTION, editText.getText().toString());
        }
    }
    
    private EditText getEditText() {
        View view = getView();
        if (view != null) {
            return (EditText) view.findViewById(R.id.et_bet_prediction);
        }
        return null;
    }
    
    private void onSuggestionClicked(String suggestion) {
        mPrediction = suggestion;
        
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(mPrediction);
        }
        
        submit(mPrediction);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        mPrediction = v.getText().toString();
        
        if (TextUtils.isEmpty(mPrediction)) {
            v.setError(getString(R.string.error_missing_bet_prediction));
        } else {
            submit(mPrediction);
        }
        
        return true;
    }
    
    private void submit(String prediction) {
        if (mListener != null) {
            mListener.onPredictionSelected(prediction);
        }
    }
    
}
