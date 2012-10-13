package com.betcha;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;


public class FontUtils {
    
    public enum CustomFont {
        HELVETICA_NORMAL("fonts/HelveticaNeue-Roman.otf"),
        HELVETICA_CONDENSED("fonts/HelveticaNeue-Condensed.otf"),
        HELVETICA_BOLD("fonts/HelveticaNeue-Bold.otf"),
        HELVETICA_CONDENSED_BOLD("fonts/HelveticaNeue-BoldCond.otf");
        
        String mFilename;
        
        CustomFont(String filename) {
            mFilename = filename;
        }
    };
    
    private static final Map<CustomFont, WeakReference<Typeface>> sTypefaces = new HashMap<FontUtils.CustomFont, WeakReference<Typeface>>();
    
    public static synchronized Typeface getCustomFont(Context context, CustomFont font) {
        Typeface typeface = null;
        
        WeakReference<Typeface> ref = sTypefaces.get(font);
        if (ref != null) {
            typeface = ref.get();
        }

        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), font.mFilename);
            sTypefaces.put(font, new WeakReference<Typeface>(typeface));
        }
        
        return typeface;
    }
    
    public static void setTextViewTypeface(TextView textView, CustomFont font) {
        Typeface typeface = getCustomFont(textView.getContext(), font);
        if (typeface != null) {
            textView.setTypeface(typeface);
        }
    }
    
    public static void setTextViewTypeface(View view, int resId, CustomFont font) {
        View subview = view.findViewById(resId);
        if (subview instanceof TextView) {
            setTextViewTypeface((TextView) subview, font);
        }
    }
    
}
