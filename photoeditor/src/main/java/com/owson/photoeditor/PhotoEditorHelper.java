package com.owson.photoeditor;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;

public final class PhotoEditorHelper {

    public static SpannableString getSpannableString(String text, int backgroundColor) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new BackgroundColorSpan(backgroundColor), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

}
