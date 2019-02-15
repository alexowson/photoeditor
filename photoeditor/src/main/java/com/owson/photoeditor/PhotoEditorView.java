package com.owson.photoeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.naver.android.helloyako.imagecrop.view.ImageCropView;

public class PhotoEditorView extends FrameLayout {
    private static final String TAG = "PhotoEditorView";

    private Context context;

    private Button adjustButton;
    private ImageCropView photoImageView;
    private PhotoImageState photoImageState = PhotoImageState.NONE;

    private View addTextRootView;
    private View deleteView;

    private OnPhotoEditorListener onPhotoEditorListener;

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
        this.context = context;
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
        adjustButton.setOnClickListener(onClickListener);
        addView(adjustButton);

        /*deleteView = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        addView(deleteView, params);

        Te*/

        addDeleteView();
    }

    private void addDeleteView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        deleteView = inflater.inflate(R.layout.delete_view, null);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        addView(deleteView, params);

        deleteView.setVisibility(GONE);
    }

    public void addText(String text, int colorCodeTextView) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addTextRootView = inflater.inflate(R.layout.photo_editor_sdk_text_item_list, null);
        TextView addTextView = (TextView) addTextRootView.findViewById(R.id.photo_editor_sdk_text_tv);
        addTextView.setGravity(Gravity.CENTER);
        addTextView.setText(text);
        if (colorCodeTextView != -1)
            addTextView.setTextColor(colorCodeTextView);
        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                this, photoImageView, onPhotoEditorListener);
        multiTouchListener.setOnMultiTouchListener(onMultiTouchListener);
        addTextRootView.setOnTouchListener(multiTouchListener);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        addView(addTextRootView, params);
//        addedViews.add(addTextRootView);
//        if (onPhotoEditorListener != null)
//            onPhotoEditorListener.onAddViewListener(ViewType.TEXT, addedViews.size());
    }

    public Bitmap getViewAdBitmapImage() {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        draw(canvas);
        //return the bitmap
        return returnedBitmap;
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

    public void setDisabledAdjustCropAreaImage(boolean disabledAdjustCropAreaImage) {
        photoImageView.setDisabledAdjustCropAreaImage(disabledAdjustCropAreaImage);
    }

    public Button getAdjustButton() {
        return adjustButton;
    }

    public void setOnPhotoEditorListener(OnPhotoEditorListener onPhotoEditorListener) {
        this.onPhotoEditorListener = onPhotoEditorListener;
    }

    public void centerCropImageView() {
        photoImageView.setAspectRatio(1, 1);
        photoImageState = PhotoImageState.CENTER_CROP;
    }

    public void centerFitImageView() {
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

    private void viewUndo(View removedView) {
       /* if (addedViews.size() > 0) {
            if (addedViews.contains(removedView)) {*/
                removeView(removedView);
               /* addedViews.remove(removedView);
                if (onPhotoEditorListener != null)
                    onPhotoEditorListener.onRemoveViewListener(addedViews.size());
            }
        }*/
    }

    private MultiTouchListener.OnMultiTouchListener onMultiTouchListener = new MultiTouchListener.OnMultiTouchListener() {
        @Override
        public void onEditTextClickListener(String text, int colorCode) {
            if (addTextRootView != null) {
                removeView(addTextRootView);
//                addedViews.remove(addTextRootView);
            }
        }

        @Override
        public void onRemoveViewListener(View removedView) {
            viewUndo(removedView);
        }
    };

    private ImageCropView.OnImageViewTouchDoubleTapListener doubleTapListener = new ImageCropView.OnImageViewTouchDoubleTapListener() {
        @Override
        public void onDoubleTap() {
            handleImageAdjust();
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == adjustButton) {
                handleImageAdjust();
            }
        }
    };
}
