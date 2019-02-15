package com.owson.photoeditor;

/**
 * Created by Ahmed Adel on 02/06/2017.
 */

public interface OnPhotoEditorListener {

    void onEditTextChangeListener(String text, int colorCode);

//    void onAddViewListener(ViewType viewType, int numberOfAddedViews);

//    void onRemoveViewListener(int numberOfAddedViews);

    void onStartViewChangeListener(ViewType viewType);

    void onStopViewChangeListener(ViewType viewType);
}