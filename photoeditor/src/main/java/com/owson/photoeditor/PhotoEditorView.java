package com.owson.photoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.naver.android.helloyako.imagecrop.view.ImageCropView;

public class PhotoEditorView extends FrameLayout implements View.OnClickListener {
    private static final String TAG = "PhotoEditorView";

    private Button adjustButton;
    private ImageCropView photoImageView;
    private PhotoImageState photoImageState = PhotoImageState.NONE;
//    private Bitmap photoBitmap;

    public enum PhotoImageState {
        NONE,
        CENTER_CROP,
        CENTER_FIT
    }
    
    public PhotoEditorView(Context context) {
        super(context);
        init(context);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(Color.parseColor("#FFFFFF"));

        photoImageView = new ImageCropView(context);
        photoImageView.setGridInnerMode(ImageCropView.GRID_OFF);
        photoImageView.setGridOuterMode(ImageCropView.GRID_OFF);
        photoImageView.setTransparentLayerColor(Color.parseColor("#00000000"));
        photoImageView.setDoubleTapListener(doubleTapListener);
//        photoImageView.setDoubleTapEnabled(true);

        addView(photoImageView);

        //Adding button
        adjustButton = new Button(context);
        adjustButton.setText(getResources().getString(R.string.adjust));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
        lp.setMargins(getResources().getDimensionPixelSize(R.dimen.adjust_button_margin),
                getResources().getDimensionPixelSize(R.dimen.adjust_button_margin),
                getResources().getDimensionPixelSize(R.dimen.adjust_button_margin),
                getResources().getDimensionPixelSize(R.dimen.adjust_button_margin));
        adjustButton.setLayoutParams(lp);
        adjustButton.setOnClickListener(this);
        addView(adjustButton);
    }

    public void setImageFilePath(String imagePath) {
        photoImageView.setImageFilePath(imagePath);
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        photoImageView.setImageBitmap(imageBitmap);
        centerCropImageView();
    }

    public void setImageResource(int resId) {
        photoImageView.setImageResource(resId);
        centerCropImageView();
    }

    public void setImageDrawable(Drawable imageDrawable) {
        photoImageView.setImageDrawable(imageDrawable);
        centerCropImageView();
    }

    public Button getAdjustButton() {
        return adjustButton;
    }

    private void centerCropImageView() {
        photoImageView.setAspectRatio(1, 1);
        photoImageState = PhotoImageState.CENTER_CROP;
    }

    private void centerFitImageView() {
        photoImageView.setAspectRatio(720, 405);
        photoImageState = PhotoImageState.CENTER_FIT;
    }

    private void handleImageAdjust() {
        if(photoImageState != PhotoImageState.CENTER_CROP) {
            centerCropImageView();
        }else {
            centerFitImageView();
        }
    }

    @Override
    public void onClick(View v) {
        if(v == adjustButton) {
            handleImageAdjust();
        }
    }

    private ImageCropView.OnImageViewTouchDoubleTapListener doubleTapListener = new ImageCropView.OnImageViewTouchDoubleTapListener() {
        @Override
        public void onDoubleTap() {
            handleImageAdjust();
        }
    };
}
