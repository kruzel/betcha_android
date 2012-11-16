package com.betcha.fragment;

import java.util.Collection;
import java.util.List;

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
import com.betcha.model.PredictionSuggestion;
import com.betcha.model.Topic;
import com.betcha.model.User;

public class CreateSubjectFragment extends SherlockFragment implements OnEditorActionListener {
    
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_CATEGORY = "categoryId";
    private static final String ARG_USER = "userId";
    
    private String mCategoryId;
    private String mUserId;
    
    private OnSubjectSelectedListener mListener; 
    
    public static CreateSubjectFragment newInstance(String categorId, String userId) {
        CreateSubjectFragment f = new CreateSubjectFragment();
        
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, categorId);
        args.putString(ARG_USER, userId);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnSubjectSelectedListener {
        void onSubjectSelected(String subjectId, String subject);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSubjectSelectedListener) {
            mListener = (OnSubjectSelectedListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCategoryId = getArguments().getString(ARG_CATEGORY);
        mUserId = getArguments().getString(ARG_USER);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_subject, container, false);
        
        ImageView profileView = (ImageView) view.findViewById(R.id.iv_bet_owner_profile_pic);
        User.get(mUserId).setProfilePhoto(profileView);
        
        ImageView categoryView = (ImageView) view.findViewById(R.id.iv_bet_category);
        categoryView.setImageBitmap(Category.get(mCategoryId).getImage());
        
        List<Topic> suggestions = Topic.getForCategory(getActivity(), mCategoryId);
        
        ViewGroup suggestionsContainer = (ViewGroup) view.findViewById(R.id.ll_suggestions);
        for (Topic topic : suggestions) {
        	View suggestionView = inflater.inflate(R.layout.create_subject_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_NORMAL);
            textView.setText(topic.getName());
            textView.setTag(topic.getId());
            
            ImageView[] logos = new ImageView[2];
            logos[0] = (ImageView) suggestionView.findViewById(R.id.iv_team_logo_1);
            logos[1] = (ImageView) suggestionView.findViewById(R.id.iv_team_logo_2);
            
            Collection<PredictionSuggestion> colPredictions = topic.getSuggestions();
            int i = 0;
            if(colPredictions!=null) {
	            for (PredictionSuggestion prediction : topic.getSuggestions()) {
					if(prediction!=null && prediction.getImage()!=null && !prediction.getName().equals("Tie")) {
						logos[i].setImageBitmap(prediction.getImage());
						i++;
					}
					if(i>1)
						break;
				}
            }
            
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	TextView tv = (TextView) v;
                	String id = "0";
                	if(v.getTag()!=null)
                		id = v.getTag().toString();
                    onSuggestionClicked(id, tv.getText().toString());
                }
            });
            
            suggestionsContainer.addView(suggestionView);
		}

        EditText editText = (EditText) view.findViewById(R.id.et_bet_subject);
        FontUtils.setTextViewTypeface(editText, CustomFont.HELVETICA_CONDENSED);
        editText.setOnEditorActionListener(this);
        
        if (savedInstanceState != null) {
            String subject = savedInstanceState.getString(ARG_SUBJECT);
            if (subject != null) {
                editText.setText(subject);
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
            outState.putString(ARG_SUBJECT, editText.getText().toString());
        }
    }
    
    private EditText getEditText() {
        View view = getView();
        if (view != null) {
            return (EditText) view.findViewById(R.id.et_bet_subject);
        }
        return null;
    }
    
    private void onSuggestionClicked(String suggestionId, String suggestion) {
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(suggestion);
            editText.setSelection(editText.getText().length());
        }
        submit(suggestionId, suggestion);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        String subject = v.getText().toString();
        if (TextUtils.isEmpty(subject)) {
            v.setError(getString(R.string.error_missing_bet_subject));
        } else {
        	String id = "0";
        	if(v.getTag()!=null)
        		id = v.getTag().toString();
        	
            submit(id , v.getText().toString());
        }
        return true;
    }
    
    private void submit(String suggestionId, String subject) {
        if (mListener != null) {
            mListener.onSubjectSelected(suggestionId, subject);
        }
    }
    
}
