package com.owson.photoeditor;

import android.view.View;

/**
 * Created by Ahmed Adel on 02/06/2017.
 */

public interface OnPhotoEditorListener {

    void onEditTextChangeListener(CharSequence text, int colorCode, int gravity, float text_size_dip, View refView);

//    void onAddViewListener(ViewType viewType, int numberOfAddedViews);

//    void onRemoveViewListener(int numberOfAddedViews);

    void onChangeModeListener(PhotoEditorView.Mode mode);

    void onStartViewChangeListener(ViewType viewType);

    void onStopViewChangeListener(ViewType viewType);
}
