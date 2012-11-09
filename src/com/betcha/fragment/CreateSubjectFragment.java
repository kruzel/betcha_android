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
import com.betcha.model.User;

public class CreateSubjectFragment extends SherlockFragment implements OnEditorActionListener {
    
    private static final String ARG_SUGGESTIONS = "suggestions";
    private static final String ARG_SUBJECT = "subject";
    private static final String ARG_CATEGORY = "categoryId";
    private static final String ARG_USER = "userId";
    
    private String[] mSuggestions;
    private String mCategoryId;
    private String mUserId;
    
    private OnSubjectSelectedListener mListener; 
    
    public static CreateSubjectFragment newInstance(String[] suggestions, String categorId, String userId) {
        CreateSubjectFragment f = new CreateSubjectFragment();
        
        Bundle args = new Bundle();
        args.putStringArray(ARG_SUGGESTIONS, suggestions);
        args.putString(ARG_CATEGORY, categorId);
        args.putString(ARG_USER, userId);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnSubjectSelectedListener {
        void onSubjectSelected(String subject);
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
        mSuggestions = getArguments().getStringArray(ARG_SUGGESTIONS);
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
        
        ViewGroup suggestionsContainer = (ViewGroup) view.findViewById(R.id.ll_suggestions);
        for (final String suggestion : mSuggestions) {
            View suggestionView = inflater.inflate(R.layout.create_subject_suggestion, null);
            
            TextView textView = (TextView) suggestionView.findViewById(R.id.tv_suggestion_text);
            FontUtils.setTextViewTypeface(textView, CustomFont.HELVETICA_NORMAL);
            textView.setText(suggestion);
            
            suggestionView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSuggestionClicked(suggestion);
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
        String subject = v.getText().toString();
        if (TextUtils.isEmpty(subject)) {
            v.setError(getString(R.string.error_missing_bet_subject));
        } else {
            submit(v.getText().toString());
        }
        return true;
    }
    
    private void submit(String subject) {
        if (mListener != null) {
            mListener.onSubjectSelected(subject);
        }
    }
    
}
