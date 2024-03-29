package com.betcha.fragment;

import java.util.List;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.PredictionOption;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ChangePredictionDialogFragment extends SherlockDialogFragment implements OnEditorActionListener {
    
	private static ImageLoader imageLoader;
	private static DisplayImageOptions defaultOptions;
	
	private static final String ARG_SUGGESTION_ID = "suggestionId";
    private static final String ARG_PREDICTION = "prediction";
    private static final String ARG_TOPIC_ID = "topicId";
    
    private String mSuggestionId;
    private String mPrediction;
    private String mTopicId;
    
    private OnPredictionSelectedListener mListener; 
    
    public static ChangePredictionDialogFragment newInstance(String topicId, String suggestionId, String currPrediction) {
        ChangePredictionDialogFragment f = new ChangePredictionDialogFragment();
        
        Bundle args = new Bundle();
        args.putString(ARG_PREDICTION, currPrediction);
        args.putString(ARG_SUGGESTION_ID, suggestionId);
        args.putString(ARG_TOPIC_ID, topicId);
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
        mTopicId = getArguments().getString(ARG_TOPIC_ID);
        
        if(imageLoader==null) {
			imageLoader = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
			defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory()
	        .cacheOnDisc()
	        .build();
		}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x77000000));
        
        View view = inflater.inflate(R.layout.create_prediction_dialog, container, false);
        
        FontUtils.setTextViewTypeface(view, R.id.tv_prediction_label, CustomFont.HELVETICA_CONDENSED);
        
        ViewGroup suggestionsContainer1 = (ViewGroup) view.findViewById(R.id.ll_suggestions1);
        ViewGroup suggestionsContainer2 = (ViewGroup) view.findViewById(R.id.ll_suggestions2);
        
        int i = 0;
        List<PredictionOption> suggestions = PredictionOption.getForTopic(getActivity(), mTopicId);
        for (PredictionOption predictionSuggestion : suggestions) {
        	View suggestionView = inflater.inflate(R.layout.create_dialog_prediction_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_CONDENSED_BOLD);
            textView.setText(predictionSuggestion.getName());
            textView.setTag(predictionSuggestion.getId());
            
            ImageView ivTeamLogo = (ImageView) suggestionView.findViewById(R.id.iv_team_logo);
            if(predictionSuggestion.getImageUrl()!=null) {
            	imageLoader.displayImage(predictionSuggestion.getImageUrl() , ivTeamLogo,defaultOptions);
            } else {
            	imageLoader.cancelDisplayTask(ivTeamLogo);
            }
            
            suggestionView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	TextView tv = (TextView) v.findViewById(R.id.tv_suggestion_text);
                	if(tv.getTag()!=null && tv.getText()!=null) 
                    onSuggestionClicked(tv.getTag().toString(),tv.getText().toString());
                }
            });
            
            ViewGroup suggestionsContainer = (i % 2 == 0 ? suggestionsContainer1 : suggestionsContainer2);
            i++;
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
        	mPrediction = PredictionOption.get(mSuggestionId).getName();
        
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
    
}
