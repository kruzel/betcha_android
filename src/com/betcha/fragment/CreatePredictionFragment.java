package com.betcha.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera.Size;
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
import com.betcha.model.TopicCategory;
import com.betcha.model.PredictionOption;
import com.betcha.model.Stake;
import com.betcha.model.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class CreatePredictionFragment extends SherlockFragment implements OnEditorActionListener {
    
	private static final String ARG_TOPIC_ID = "topicId";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_STAKE = "stake";
    private static final String ARG_STAKE_ID = "stake_id";
    private static final String ARG_CATEGORY = "categoryId";
    private static final String ARG_USER = "userId";
    private static final String ARG_PREDICTION = "prediction";
    private static final String ARG_PREDICTION_ID = "predictionId";
    
    //private Suggestion[] mSuggestions;
    private String mSubject;
    private String mCategoryId;
    private String mUserId;
    private String mStakeId;
    private String mStake;
    private String mPrediction;
    private String mSuggestionId;
    private String mTopicId;
    
    private OnPredictionSelectedListener mListener;
    
    private static ImageLoader categoryImageLoader;
    private static ImageLoader stakeImageLoader;
    private static List<ImageLoader> logoImageLoaders;
	private static DisplayImageOptions defaultOptions;
    
    public static CreatePredictionFragment newInstance(String topicIds, String categorId, String userId, String subject, String stake, String stakeId) {
        CreatePredictionFragment f = new CreatePredictionFragment();
        
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC_ID, topicIds);
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
        
        mTopicId = getArguments().getString(ARG_TOPIC_ID);
        mSubject = getArguments().getString(ARG_SUBJECT);
        mCategoryId = getArguments().getString(ARG_CATEGORY);
        mUserId = getArguments().getString(ARG_USER);
        mStakeId = getArguments().getString(ARG_STAKE_ID);
        mStake = getArguments().getString(ARG_STAKE);
        mPrediction = getArguments().getString(ARG_PREDICTION);
        mSuggestionId = getArguments().getString(ARG_PREDICTION_ID);
        
        defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory()
        .cacheOnDisc()
        .build();
        
        if(categoryImageLoader==null) {
			categoryImageLoader = ImageLoader.getInstance();
			categoryImageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));	
		}
        
        if(stakeImageLoader==null) {
        	stakeImageLoader = ImageLoader.getInstance();
        	stakeImageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
		}
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_prediction, container, false);
        
        ImageView profileView = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
        User.get(mUserId).setProfilePhoto(profileView);
        
        ImageView categoryView = (ImageView) view.findViewById(R.id.iv_bet_category);
        if(TopicCategory.get(mCategoryId).getImageUrl()!=null) {
        	categoryImageLoader.displayImage(TopicCategory.get(mCategoryId).getImageUrl() , categoryView,defaultOptions);
        } else {
        	categoryImageLoader.cancelDisplayTask(categoryView);
        	categoryView.setImageResource(android.R.color.transparent);
        }
        
        TextView subjectView = (TextView) view.findViewById(R.id.tv_bet_topic);
        FontUtils.setTextViewTypeface(subjectView, CustomFont.HELVETICA_CONDENSED_BOLD);
        subjectView.setText(mSubject);
        
        ViewGroup suggestionsContainer = (ViewGroup) view.findViewById(R.id.ll_suggestions);
        List<PredictionOption> suggestions = PredictionOption.getForTopic(getActivity(), mTopicId);
                
    	logoImageLoaders = new ArrayList<ImageLoader>();
        
        for (final PredictionOption suggestion : suggestions) {
            View suggestionView = inflater.inflate(R.layout.create_prediction_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_NORMAL);
            textView.setText(suggestion.getName());
            textView.setTag(suggestion.getId());
            
            ImageView logo1 = (ImageView) suggestionView.findViewById(R.id.iv_team_logo_1);
            
            ImageLoader tmpImageLoader = ImageLoader.getInstance();
        	tmpImageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
            
            if(suggestion.getImageUrl()!=null) {
            	tmpImageLoader.displayImage(suggestion.getImageUrl() , logo1,defaultOptions);
            } else {
            	tmpImageLoader.cancelDisplayTask(logo1);
            	logo1.setImageResource(android.R.color.transparent);
            }
            logoImageLoaders.add(tmpImageLoader);
            
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
        Stake r = Stake.get(mStakeId);
        if(r!=null) {
        	stakeImageLoader.displayImage(r.getImage_url(), stakeImageView, defaultOptions);
        } else {
        	stakeImageLoader.cancelDisplayTask(stakeImageView);
        	stakeImageView.setImageResource(android.R.color.transparent);
        }
        
        EditText editText = (EditText) view.findViewById(R.id.et_bet_prediction);
        FontUtils.setTextViewTypeface(editText, CustomFont.HELVETICA_CONDENSED);
        editText.setOnEditorActionListener(this);
        
        if (savedInstanceState != null) {
        	if(mSuggestionId!=null) {
        		editText.setTag(mSuggestionId);
        	}
            if (mPrediction != null) {
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
    
    private void onSuggestionClicked(PredictionOption suggestion) {
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(suggestion.getName());
            editText.setTag(suggestion.getId());
            editText.setSelection(editText.getText().length());
        }
        submit(suggestion.getId(), suggestion.getName());
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
    
}
