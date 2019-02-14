package com.owson.photoeditorapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.esafirm.imagepicker.model.Image;
import com.owson.photoeditor.PhotoEditorView;

import com.esafirm.imagepicker.features.ImagePicker;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RESULT_LOAD_IMG_REQUEST_CODE = 778;

    @BindView(R.id.mainContainer)
    View mainContainer;

    @BindView(R.id.photoEditorView)
    PhotoEditorView photoEditorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) photoEditorView.getLayoutParams();

        ViewTreeObserver viewTreeObserver = mainContainer.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mainContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    lp.height = mainContainer.getWidth();
                    photoEditorView.setLayoutParams(lp);
                }
            });
        }

        photoEditorView.setImageResource(R.drawable.cat);
        photoEditorView.getAdjustButton().setText("");
        photoEditorView.getAdjustButton().getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.adjust_button_size);
        photoEditorView.getAdjustButton().getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.adjust_button_size);
        photoEditorView.getAdjustButton().setBackgroundResource(R.drawable.adjust_button_bg);
        photoEditorView.getAdjustButton().setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.ic_adjust),
                null, null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMG_REQUEST_CODE  && resultCode == RESULT_OK) {
            List<Image> images =  ImagePicker.getImages(data);
            if(images.size() > 0) {
                photoEditorView.setImageFilePath(images.get(0).getPath());
            }
        }
    }

    @OnClick(R.id.loadImageButton)
    void onLoadImageButtonClick() {
        loadPhotoFromGallery();
    }

    private void loadPhotoFromGallery(){
        ImagePicker.create(this)
                .folderMode(false)
                .single()
                .showCamera(false)
                .enableLog(false)
                .start(RESULT_LOAD_IMG_REQUEST_CODE);
    }
}
