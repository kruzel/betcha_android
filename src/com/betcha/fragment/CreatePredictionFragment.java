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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.betcha.FontUtils;
import com.betcha.FontUtils.CustomFont;
import com.betcha.R;
import com.betcha.model.Category;
import com.betcha.model.Reward;
import com.betcha.model.User;

public class CreatePredictionFragment extends SherlockFragment implements OnEditorActionListener {
    
	private static final String ARG_SUGGESTION_IDS = "suggestionIds";
    private static final String ARG_SUGGESTION_NAMES = "suggestionNames";
    private static final String ARG_SUGGESTION_DRAWABLES = "suggestionDrawables";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_STAKE = "stake";
    private static final String ARG_STAKE_ID = "stake_id";
    private static final String ARG_CATEGORY = "categoryId";
    private static final String ARG_USER = "userId";
    private static final String ARG_PREDICTION = "prediction";
    private static final String ARG_PREDICTION_ID = "predictionId";
    
    private Suggestion[] mSuggestions;
    private String mSubject;
    private String mCategoryId;
    private String mUserId;
    private String mStakeId;
    private String mStake;
    private String mPrediction;
    private String mPredictionId;
    
    private OnPredictionSelectedListener mListener;
    
    public static CreatePredictionFragment newInstance(String[] suggestionIds, String[] suggestionNames, int[] suggestionDrawables, String categorId, String userId, String subject, String stake, String stakeId) {
        CreatePredictionFragment f = new CreatePredictionFragment();
        
        Bundle args = new Bundle();
        args.putStringArray(ARG_SUGGESTION_IDS, suggestionIds);
        args.putStringArray(ARG_SUGGESTION_NAMES, suggestionNames);
        args.putIntArray(ARG_SUGGESTION_DRAWABLES, suggestionDrawables);
        args.putString(ARG_SUBJECT, subject);
        args.putString(ARG_CATEGORY, categorId);
        args.putString(ARG_USER, userId);
        args.putString(ARG_STAKE_ID, stakeId);
        args.putString(ARG_STAKE, stake);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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

        mSubject = getArguments().getString(ARG_SUBJECT);
        mCategoryId = getArguments().getString(ARG_CATEGORY);
        mUserId = getArguments().getString(ARG_USER);
        mStakeId = getArguments().getString(ARG_STAKE_ID);
        mStake = getArguments().getString(ARG_STAKE);
        mPrediction = getArguments().getString(ARG_PREDICTION);
        mPredictionId = getArguments().getString(ARG_PREDICTION_ID);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_prediction, container, false);
        
        ImageView profileView = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
        User.get(mUserId).setProfilePhoto(profileView);
        
        ImageView categoryView = (ImageView) view.findViewById(R.id.iv_bet_category);
        categoryView.setImageBitmap(Category.get(mCategoryId).getImage());
        
        TextView subjectView = (TextView) view.findViewById(R.id.tv_bet_topic);
        FontUtils.setTextViewTypeface(subjectView, CustomFont.HELVETICA_CONDENSED_BOLD);
        subjectView.setText(mSubject);
        
        ViewGroup suggestionsContainer = (ViewGroup) view.findViewById(R.id.ll_suggestions);
        for (final Suggestion suggestion : mSuggestions) {
            View suggestionView = inflater.inflate(R.layout.create_subject_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_NORMAL);
            textView.setText(suggestion.name);
            
            suggestionView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSuggestionClicked(suggestion);
                }
            });
            
            suggestionsContainer.addView(suggestionView);
        }
        
        TextView stakeView = (TextView) view.findViewById(R.id.tv_bet_reward);
        FontUtils.setTextViewTypeface(stakeView, CustomFont.HELVETICA_CONDENSED);
        stakeView.setText(mStake);
        
        ImageView stakeImageView = (ImageView) view.findViewById(R.id.iv_bet_reward);
        Reward r = Reward.get(mStakeId);
        if(r!=null)
        	stakeImageView.setImageResource(r.getDrawable_id());
        else 
        	stakeImageView.setImageResource(android.R.color.transparent);
        
        EditText editText = (EditText) view.findViewById(R.id.et_bet_prediction);
        FontUtils.setTextViewTypeface(editText, CustomFont.HELVETICA_CONDENSED);
        editText.setOnEditorActionListener(this);
        
        if (savedInstanceState != null) {
        	if(mStakeId!=null) {
        		editText.setTag(mPredictionId);
        	}
            if (mStake != null) {
                editText.setText(mPrediction);
                editText.setSelection(editText.getText().length());   
            }
        }
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        EditText editText = getEditText();
        if (editText != null && editText.getTag()!=null) {
            outState.putString(ARG_PREDICTION, editText.getText().toString());
            outState.putString(ARG_PREDICTION_ID, editText.getTag().toString());
        }
    }
    
    private EditText getEditText() {
        View view = getView();
        if (view != null) {
            return (EditText) view.findViewById(R.id.et_bet_prediction);
        }
        return null;
    }
    
    private void onSuggestionClicked(Suggestion suggestion) {
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(suggestion.name);
            editText.setTag(suggestion.id);
            editText.setSelection(editText.getText().length());
        }
        submit(suggestion.id, suggestion.name);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String stake = v.getText().toString();
        if (TextUtils.isEmpty(stake)) {
            v.setError(getString(R.string.error_missing_bet_reward));
        } else {
            submit("0", stake); //custome stake
        }
        return true;
    }
    
    private void submit(String suggestionId, String suggestionName) {
        if (mListener != null) {
            mListener.onPredictionSelected(suggestionId, suggestionName);
        }
    }
    
    private class Suggestion {
    	String id;
        String name;
        int drawable;
    }
    
}
