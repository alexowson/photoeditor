package com.owson.photoeditorapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.owson.photoeditor.PhotoEditorView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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
    }
}
