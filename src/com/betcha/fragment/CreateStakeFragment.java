package com.betcha.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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
import com.betcha.fragment.SetCoinsDialogFragment.OnCoinsSelectedListener;
import com.betcha.model.Stake;
import com.betcha.model.TopicCategory;
import com.betcha.model.User;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class CreateStakeFragment extends SherlockFragment implements OnEditorActionListener, OnCoinsSelectedListener {
    
	private static final String ARG_SUGGESTION_IDS = "suggestionIds";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_STAKE = "stake";
    private static final String ARG_STAKE_ID = "stake_id";
    private static final String ARG_CATEGORY = "categoryId";
    private static final String ARG_USER = "userId";
    
    private String[] suggestionIds;
    private String mSubject;
    private String mCategoryId;
    private String mUserId;
    
    private Stake mSelectedStake;
    int amount;
    
    private OnStakeSelectedListener mListener;
    
    SetCoinsDialogFragment predictionDialog;
    
    private List<ImageLoader> stakeImageLoaders;
    
    private static ImageLoader categoryImageLoader;
	private static DisplayImageOptions defaultOptions;
    
    public static CreateStakeFragment newInstance(String[] suggestionIds, String categorId, String userId, String subject) {
        CreateStakeFragment f = new CreateStakeFragment();
        
        Bundle args = new Bundle();
        args.putStringArray(ARG_SUGGESTION_IDS, suggestionIds);
        args.putString(ARG_SUBJECT, subject);
        args.putString(ARG_CATEGORY, categorId);
        args.putString(ARG_USER, userId);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnStakeSelectedListener {
        void onStakeSelected(String stakeId, String stake, int amount);
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
        
        suggestionIds = getArguments().getStringArray(ARG_SUGGESTION_IDS);
       
    	stakeImageLoaders = new ArrayList<ImageLoader>();
    	for (String sugestion : suggestionIds) {
    		ImageLoader imageLoader = ImageLoader.getInstance();
    		imageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));
    		stakeImageLoaders.add(imageLoader);
		}

        mSubject = getArguments().getString(ARG_SUBJECT);
        mCategoryId = getArguments().getString(ARG_CATEGORY);
        mUserId = getArguments().getString(ARG_USER);
        
        defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory()
        .cacheOnDisc()
        .build();
        
        if(categoryImageLoader==null) {
			categoryImageLoader = ImageLoader.getInstance();
			// Initialize ImageLoader with configuration. Do it once.
			categoryImageLoader.init(ImageLoaderConfiguration.createDefault(getActivity()));	
		}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_stake, container, false);
        
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
        
        if(suggestionIds.length>0) {
	        ViewGroup suggestionsContainer1 = (ViewGroup) view.findViewById(R.id.ll_suggestions);
	        ViewGroup suggestionsContainer2 = (ViewGroup) view.findViewById(R.id.ll_suggestions2);
	        for (int i = 0; i < suggestionIds.length; i++) {
	            final Stake suggestion = Stake.get(suggestionIds[i]);
	            
	            View suggestionView = inflater.inflate(R.layout.create_stake_suggestion, null);
	            
	            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
	            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_NORMAL);
	            textView.setText(suggestion.getName());
	            
	            ImageView imageView = (ImageView) suggestionView.findViewById(R.id.iv_suggestion_image);
	            if(suggestion.getImage_url()!=null) {
	            	stakeImageLoaders.get(i).displayImage(suggestion.getImage_url() , imageView,defaultOptions);
	            } else {
	            	stakeImageLoaders.get(i).cancelDisplayTask(imageView);
	            	imageView.setImageResource(android.R.color.transparent);
	            }
	           
	            suggestionView.setOnClickListener(new OnClickListener() {
	                @Override
	                public void onClick(View v) {
	                    onSuggestionClicked(suggestion);
	                }
	            });
	            
	            ViewGroup suggestionsContainer = (i % 2 == 0 ? suggestionsContainer1 : suggestionsContainer2);
	            suggestionsContainer.addView(suggestionView);
	        }
        }

        EditText editText = (EditText) view.findViewById(R.id.et_bet_stake);
        FontUtils.setTextViewTypeface(editText, CustomFont.HELVETICA_CONDENSED);
        editText.setOnEditorActionListener(this);
        
        if (savedInstanceState != null) {
            String stake = savedInstanceState.getString(ARG_STAKE);
            String stake_id = savedInstanceState.getString(ARG_STAKE_ID);
            if (stake != null) {
            	editText.setTag(stake_id);
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
        if (editText != null && editText.getTag()!=null) {
            outState.putString(ARG_STAKE, editText.getText().toString());
            outState.putString(ARG_STAKE_ID, editText.getTag().toString());
        }
    }
    
    private EditText getEditText() {
        View view = getView();
        if (view != null) {
            return (EditText) view.findViewById(R.id.et_bet_stake);
        }
        return null;
    }
    
    private void onSuggestionClicked(Stake suggestion) {
    	mSelectedStake = suggestion;
    	
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(suggestion.getName());
            editText.setTag(suggestion.getId());
            editText.setSelection(editText.getText().length());
        }
        
        //if coins let user set #
        if(suggestion.getName().equals("Coins")) {
	        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			predictionDialog = SetCoinsDialogFragment.newInstance(100);
			predictionDialog.setListener(this);
			predictionDialog.show(ft, "dialog");
        } else 
        	submit(suggestion.getId(), suggestion.getName(), 1);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String stake = v.getText().toString();
        if (TextUtils.isEmpty(stake)) {
            v.setError(getString(R.string.error_missing_bet_reward));
        } else {
            submit("0", stake, 1); //custome stake
        }
        return true;
    }
    
    private void submit(String suggestionId, String suggestionName, int amount) {
        if (mListener != null) {
            mListener.onStakeSelected(suggestionId, suggestionName, amount);
        }
    }

	@Override
	public void onCoinsSelected(Integer numCoins) {
		if(predictionDialog!=null) {
			predictionDialog.dismiss();
			predictionDialog = null;
		}
		
		amount = numCoins;
		submit(mSelectedStake.getId(), mSelectedStake.getName(), numCoins);
	}
    
}
