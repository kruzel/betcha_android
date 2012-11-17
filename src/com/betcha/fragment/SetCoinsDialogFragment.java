package com.betcha.fragment;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.betcha.R;
import com.quietlycoding.android.picker.NumberPicker;

public class SetCoinsDialogFragment extends SherlockDialogFragment implements OnEditorActionListener {
    
	private static final String ARG_NUM_COINS = "numCoins";
    
    private Integer mNumCoins;
    
    private OnCoinsSelectedListener mListener; 
    
    public static SetCoinsDialogFragment newInstance(Integer numCoins) {
        SetCoinsDialogFragment f = new SetCoinsDialogFragment();
        
        Bundle args = new Bundle();
        args.putInt(ARG_NUM_COINS, numCoins);
        f.setArguments(args);
        
        return f;
    }
    
    public interface OnCoinsSelectedListener {
        void onCoinsSelected(Integer numCoins);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnCoinsSelectedListener) {
            mListener = (OnCoinsSelectedListener) activity;
        }
    }
    
    public void setListener(OnCoinsSelectedListener mListener) {
		this.mListener = mListener;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setStyle(STYLE_NO_FRAME, R.style.Theme_Sherlock_Dialog);
        
        mNumCoins = getArguments().getInt(ARG_NUM_COINS);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0x77000000));
        
        View view = inflater.inflate(R.layout.set_coins_dialog, container, false);
        
        //FontUtils.setTextViewTypeface(view, R.id.tv_prediction_label, CustomFont.HELVETICA_CONDENSED);
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numberPicker1);
        picker.setRange(1, 100);
        
        Button button = (Button) view.findViewById(R.id.buttonOK);
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submit(picker.getCurrent());
			}
		});
        
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_NUM_COINS)) {
            mNumCoins = savedInstanceState.getInt(ARG_NUM_COINS);
        }
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(ARG_NUM_COINS, mNumCoins);
    }
        
    private void onSuggestionClicked(Integer numCoins) {
              
        submit(numCoins);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        
    	submit(mNumCoins);
        
        return true;
    }
    
    private void submit(Integer numCoins) {
        if (mListener != null) {
            mListener.onCoinsSelected(numCoins);
        }
    }
    
}
