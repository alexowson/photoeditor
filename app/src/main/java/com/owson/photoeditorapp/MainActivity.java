package com.owson.photoeditorapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.esafirm.imagepicker.model.Image;
import com.owson.photoeditor.OnPhotoEditorListener;
import com.owson.photoeditor.PhotoEditorView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.owson.photoeditor.ViewType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RESULT_LOAD_IMG_REQUEST_CODE = 778;
    private static final int SHARE_STORAGE_PERMS_REQUEST_CODE = 900;
    private final String[] sharePerms = { android.Manifest.permission.WRITE_EXTERNAL_STORAGE,  android.Manifest.permission.READ_EXTERNAL_STORAGE};


    @BindView(R.id.mainContainer)
    View mainContainer;

    @BindView(R.id.photoEditorView)
    PhotoEditorView photoEditorView;

    private int colorCodeTextView = -1;
    private ArrayList<Integer> colorPickerColors;

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
        photoEditorView.setOnPhotoEditorListener(onPhotoEditorSDKListener);
        photoEditorView.getAdjustButton().setText("");
        photoEditorView.getAdjustButton().getLayoutParams().width = getResources().getDimensionPixelSize(R.dimen.adjust_button_size);
        photoEditorView.getAdjustButton().getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.adjust_button_size);
        photoEditorView.getAdjustButton().setBackgroundResource(R.drawable.adjust_button_bg);
        photoEditorView.getAdjustButton().setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.ic_adjust),
                null, null, null);

        colorPickerColors = new ArrayList<>();
        colorPickerColors.add(getResources().getColor(R.color.black));
        colorPickerColors.add(getResources().getColor(R.color.blue_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.brown_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.green_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.orange_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.red_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.red_orange_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.sky_blue_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.violet_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.white));
        colorPickerColors.add(getResources().getColor(R.color.yellow_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.yellow_green_color_picker));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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

    @OnClick(R.id.addTextButton)
    void onAddTextButtonClick() {
        openAddTextPopupWindow("", -1);
    }

    @OnClick(R.id.shareImageButton)
    void onShareImageButtonClick() {
        boolean has_perms = EasyPermissions.hasPermissions(MainActivity.this, sharePerms);
        if (has_perms)
            shareImage();
        else {
            EasyPermissions.requestPermissions(
                    MainActivity.this,
                    getString(R.string.rationale_storage),
                    SHARE_STORAGE_PERMS_REQUEST_CODE,
                    sharePerms);
        }
    }

    @AfterPermissionGranted(SHARE_STORAGE_PERMS_REQUEST_CODE)
    private void shareImage() {
        Bitmap bmp = photoEditorView.getViewAdBitmapImage();
        if(bmp == null) {
            //Show no bitmap message
            return;
        }

        Uri uri = getUriImageFromBitmap(bmp, MainActivity.this);
        if(uri == null) {
            //Show no URI message
            return;
        }

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);

//        shareIntent.putExtra(Intent.EXTRA_TEXT, IMAGE_URL);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/png");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, "Share image using"));
    }

    private void openAddTextPopupWindow(String text, int colorCode) {
        colorCodeTextView = colorCode;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addTextPopupWindowRootView = inflater.inflate(R.layout.add_text_popup_window, null);
        final EditText addTextEditText = (EditText) addTextPopupWindowRootView.findViewById(R.id.add_text_edit_text);
        TextView addTextDoneTextView = (TextView) addTextPopupWindowRootView.findViewById(R.id.add_text_done_tv);
        RecyclerView addTextColorPickerRecyclerView = (RecyclerView) addTextPopupWindowRootView.findViewById(R.id.add_text_color_picker_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
        addTextColorPickerRecyclerView.setHasFixedSize(true);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(MainActivity.this, colorPickerColors);
        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
            @Override
            public void onColorPickerClickListener(int colorCode) {
                addTextEditText.setTextColor(colorCode);
                colorCodeTextView = colorCode;
            }
        });
        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);
        if (stringIsNotEmpty(text)) {
            addTextEditText.setText(text);
            addTextEditText.setTextColor(colorCode == -1 ? getResources().getColor(R.color.white) : colorCode);
        }
        final PopupWindow pop = new PopupWindow(MainActivity.this);
        pop.setContentView(addTextPopupWindowRootView);
        pop.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setFocusable(true);
        pop.setBackgroundDrawable(null);
        pop.showAtLocation(addTextPopupWindowRootView, Gravity.TOP, 0, 0);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        addTextDoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText(addTextEditText.getText().toString(), colorCodeTextView);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                pop.dismiss();
            }
        });
    }

    private void loadPhotoFromGallery(){
        ImagePicker.create(this)
                .folderMode(false)
                .single()
                .showCamera(false)
                .enableLog(false)
                .start(RESULT_LOAD_IMG_REQUEST_CODE);
    }

    private void addText(String text, int colorCodeTextView) {
        photoEditorView.addText(text, colorCodeTextView);
    }

    private boolean stringIsNotEmpty(String string) {
        if (string != null && !string.equals("null")) {
            if (!string.trim().equals("")) {
                return true;
            }
        }
        return false;
    }

    private OnPhotoEditorListener onPhotoEditorSDKListener = new OnPhotoEditorListener() {
        @Override
        public void onEditTextChangeListener(String text, int colorCode) {
            openAddTextPopupWindow(text, colorCode);
        }

        @Override
        public void onStartViewChangeListener(ViewType viewType) {

        }

        @Override
        public void onStopViewChangeListener(ViewType viewType) {

        }
    };

    private Uri getUriImageFromBitmap(Bitmap bmp, Context context) {
        if(bmp == null)
            return null;

        Uri bmpUri = null;

        try {

            File file =  new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            bmpUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
//            else
//                bmpUri = Uri.fromFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
