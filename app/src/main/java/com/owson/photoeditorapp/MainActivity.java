package com.owson.photoeditorapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.autofit.et.lib.AutoFitEditText;
import com.autofit.et.lib.AutoFitEditTextUtil;
import com.esafirm.imagepicker.model.Image;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.jaygoo.widget.VerticalRangeSeekBar;
import com.owson.photoeditor.OnPhotoEditorListener;
import com.owson.photoeditor.PhotoEditorHelper;
import com.owson.photoeditor.PhotoEditorView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.owson.photoeditor.ViewType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final int GRAVITY_CENTER = Gravity.CENTER;
    private static final int GRAVITY_LEFT = Gravity.LEFT|Gravity.CENTER_VERTICAL;
    private static final int GRAVITY_RIGHT = Gravity.RIGHT|Gravity.CENTER_VERTICAL;

//    private static final int SLIDER_MIN_VALUE = 20;
//    private static final int SLIDER_MAX_VALUE = 60;
//    private static final int SLIDER_DEFAULT_VALUE = 40;

    private static final int SLIDER_MIN_VALUE = 10;
    private static final int SLIDER_MAX_VALUE = 35;
    private static final int SLIDER_DEFAULT_VALUE = 15;

    @BindView(R.id.mainContainer)
    View mainContainer;

    @BindView(R.id.photoEditorView)
    PhotoEditorView photoEditorView;

    @BindView(R.id.rationButton)
    Button rationButton;

    private ArrayList<Integer> colorPickerColors;

    private int colorCodeTextView = Color.WHITE;
    //private int bgColorSpannableTextView = Color.TRANSPARENT;
    private int gravityTextView = GRAVITY_CENTER;
//    private float widthTextView = -1;

    private boolean squareRatio = false;

    private TextView currentTextView;
    private int charsPerLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        ViewTreeObserver viewTreeObserver = mainContainer.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mainContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    changeSquareRatio();
                }
            });
        }

        photoEditorView.setOnPhotoEditorListener(onPhotoEditorSDKListener);
        photoEditorView.setImageResource(R.drawable.cat);
        photoEditorView.setRotateEnabled(true);
        photoEditorView.setMode(PhotoEditorView.Mode.FREE);
        photoEditorView.setMinScaleImage(0.3f);

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

    @OnClick(R.id.rationButton)
    void onRationButtonClick() {
        changeRatio();
    }

    private void changeRatio() {
        if(!squareRatio) {
            rationButton.setText("1:1");
            changeSquareRatio();
        } else {
            rationButton.setText("4:5");
            change_4_5_ratio();
        }
    }

    private void changeSquareRatio() {
        if(squareRatio)
            return;

        squareRatio = true;
        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) photoEditorView.getLayoutParams();
        lp.height = mainContainer.getWidth();
        photoEditorView.setLayoutParams(lp);
    }

    private void change_4_5_ratio() {
        if(!squareRatio)
            return;

        squareRatio = false;
        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) photoEditorView.getLayoutParams();
        lp.height = mainContainer.getWidth() * 5 / 4;
        photoEditorView.setLayoutParams(lp);
    }

    @OnClick(R.id.loadImageButton)
    void onLoadImageButtonClick() {
        loadPhotoFromGallery();
    }

    @OnClick(R.id.addTextButton)
    void onAddTextButtonClick() {
        currentTextView = null;
        //openAddTextPopupWindow("", Color.WHITE, GRAVITY_CENTER, TEXT_SIZE_DIP_MIN, Color.TRANSPARENT);
        openAddTextPopupWindow("1234567890", Color.WHITE, GRAVITY_CENTER, SLIDER_DEFAULT_VALUE);
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

    private void openAddTextPopupWindow(CharSequence text, int colorCode, int gravity, final float slider_value) {
        colorCodeTextView = colorCode;
        //bgColorSpannableTextView = bgColorSpannable;
        gravityTextView  = gravity;
//        widthTextView  = text_width_dip;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addTextPopupWindowRootView = inflater.inflate(R.layout.add_text_popup_window, null);

        final AutoFitEditText addTextEditText =  addTextPopupWindowRootView.findViewById(R.id.add_text_edit_text);
        addTextEditText.setEnableSizeCache(false);
        addTextEditText.setMovementMethod(null);
        addTextEditText.setMinTextSize(pixelToDp(5.0f));
        AutoFitEditTextUtil.setNormalization(this, addTextPopupWindowRootView, addTextEditText);
//        addTextEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, slider_value);
        addTextEditText.setGravity(gravityTextView);
        addTextEditText.setText(text);
        addTextEditText.setTextColor(colorCode);
        addTextEditText.setBackgroundColor(Color.GRAY);
        addTextEditText.addTextChangedListener(new TextChangeListener(addTextEditText));
        addTextEditText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                addTextEditText.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                applyAutoWrap(addTextEditText, (int)slider_value);
            }
        });
        //        int w = (int)(text_width_dip * TypedValue.COMPLEX_UNIT_DIP);
//        addTextEditText.getLayoutParams().width = w;

        TextView addTextDoneTextView = addTextPopupWindowRootView.findViewById(R.id.add_text_done_tv);

        final ImageView addTextAlignImageView = addTextPopupWindowRootView.findViewById(R.id.add_text_align_iv);
        final ImageView addTextBackgroundImageView = addTextPopupWindowRootView.findViewById(R.id.add_text_bg_iv);

        switch (gravityTextView) {
            case GRAVITY_LEFT:
                addTextAlignImageView.setImageResource(R.drawable.ic_align_left);
                break;
            case GRAVITY_CENTER:
                addTextAlignImageView.setImageResource(R.drawable.ic_align_center);
                break;
            case GRAVITY_RIGHT:
                addTextAlignImageView.setImageResource(R.drawable.ic_align_right);
                break;
        }

//        float text_size_min = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP_MIN, getResources().getDisplayMetrics());
//        float text_size_max = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP_MAX, getResources().getDisplayMetrics());

        VerticalRangeSeekBar fontSizeSeekBar = addTextPopupWindowRootView.findViewById(R.id.fontSizeSeekBar);
        fontSizeSeekBar.setRange(SLIDER_MIN_VALUE, SLIDER_MAX_VALUE);
        fontSizeSeekBar.setValue(Math.max(slider_value, SLIDER_MIN_VALUE));
        fontSizeSeekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
//                Log.i(TAG, "onRangeChanged: " + leftValue + ", " + rightValue);
                /*widthTextView = (int)leftValue;
                Log.i(TAG, "onRangeChanged: widthTextView="+widthTextView);
                ViewGroup.LayoutParams lp = addTextEditText.getLayoutParams();

                lp.width = (int)(TEXT_WIDTH_DIP_MAX  - (leftValue - TEXT_WIDTH_DIP_MIN));
                addTextEditText.setLayoutParams(lp);*/
//                addTextEditText.invalidate();
//                addTextEditText.setEnableSizeCache(false);
                //addTextEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeTextView);
                //addTextEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, leftValue);
                applyAutoWrap(addTextEditText, (int)leftValue);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

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

        final PopupWindow pop = new PopupWindow(MainActivity.this);
        pop.setContentView(addTextPopupWindowRootView);
        pop.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setFocusable(true);
        //pop.setBackgroundDrawable(null);
        pop.showAtLocation(addTextPopupWindowRootView, Gravity.TOP, 0, 0);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        addTextAlignImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (gravityTextView) {
                    case GRAVITY_LEFT:
                        gravityTextView = GRAVITY_CENTER;
                        addTextAlignImageView.setImageResource(R.drawable.ic_align_center);
                        break;
                    case GRAVITY_CENTER:
                        gravityTextView = GRAVITY_RIGHT;
                        addTextAlignImageView.setImageResource(R.drawable.ic_align_right);
                        break;
                    case GRAVITY_RIGHT:
                        gravityTextView = GRAVITY_LEFT;
                        addTextAlignImageView.setImageResource(R.drawable.ic_align_left);
                        break;
                }
                addTextEditText.setGravity(gravityTextView);
            }
        });
        addTextBackgroundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int bgColorSpannable = Color.TRANSPARENT;
                if( addTextEditText.getText() instanceof SpannableStringBuilder) {
                    SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder)addTextEditText.getText();
                    BackgroundColorSpan[] spans =  spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), BackgroundColorSpan.class);
                    if(spans != null && spans.length > 0)
                        bgColorSpannable = spans[0].getBackgroundColor();
                }

                if(bgColorSpannable == Color.TRANSPARENT) {
                    if(addTextEditText.getCurrentTextColor() != getResources().getColor(R.color.white)) {
                        bgColorSpannable = addTextEditText.getCurrentTextColor();
                        colorCodeTextView = getResources().getColor(R.color.white);
                    }else {
                        bgColorSpannable = getResources().getColor(R.color.white);
                        colorCodeTextView = getResources().getColor(R.color.black);
                    }

                    addTextEditText.setTextColor(colorCodeTextView);
//                    addTextEditText.setBackgroundColor(bgCodeTextView);

                    String text = addTextEditText.getText().toString();
                    addTextEditText.setText(PhotoEditorHelper.getSpannableString(text, bgColorSpannable));

                } else {
                    colorCodeTextView = bgColorSpannable;
                    addTextEditText.setTextColor(colorCodeTextView);
//                    addTextEditText.setBackground(null);

                    String text = addTextEditText.getText().toString();
                    addTextEditText.setText(PhotoEditorHelper.getSpannableString(text, Color.TRANSPARENT));

                    //bgColorSpannable = Color.TRANSPARENT;
                }
            }
        });
        addTextDoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float fontSizeDip = addTextEditText.getTextSize() / getResources().getDisplayMetrics().density;

                addText(addTextEditText.getText(), colorCodeTextView, gravityTextView, (int)fontSizeDip);
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

    //private void addText(CharSequence text, int colorCodeTextView, int gravity, int text_size_dip, int bgCodeTextView) {
    private void addText(CharSequence text, int colorCodeTextView, int gravity, int text_size_dip) {
        photoEditorView.addText(text, colorCodeTextView, gravity, text_size_dip, currentTextView);
        currentTextView = null;
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
        public void onEditTextChangeListener(CharSequence text, int colorCode, int gravity, float text_size_dip, View refView) {
            if(refView instanceof TextView)
                currentTextView = (TextView) refView;
            else
                currentTextView = null;
            openAddTextPopupWindow(text, colorCode, gravity, text_size_dip);
        }

        @Override
        public void onChangeModeListener(PhotoEditorView.Mode mode) {
            
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

    private void applyAutoWrap(AutoFitEditText et, int value){
//        InputFilter[] filters = et.getFilters();
//        InputFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
//        newFilters[filters.length] = new AutoWrapFilter(wrapLength);
//        et.setFilters(newFilters);

//        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, wrapLength);

        /*int w = (int)(wrapLength * et.getTextSize() * getResources().getDisplayMetrics().density);

//        et.setPadding(w, 0 , w, 0);
        ViewGroup.LayoutParams lp = et.getLayoutParams();
        lp.width = w;
        et.setLayoutParams(lp);*/

        charsPerLine = SLIDER_MAX_VALUE  - (value - SLIDER_MIN_VALUE);

        Log.e(TAG, "applyAutoWrap: ========");
        Log.e(TAG, "applyAutoWrap: charsPerLine=" +  charsPerLine);

        float parentWidth= pixelToDp( ((View)et.getParent()).getWidth() );
        Log.e(TAG, "applyAutoWrap: parentWidth=" +  parentWidth);

        if(et.getLayoutParams().width <= 0) {
           ViewGroup.LayoutParams lp = et.getLayoutParams();
            lp.width = ((View)et.getParent()).getWidth();
            et.setLayoutParams(lp);
        }

        adjustWidthEditText(et, et.getText().length(), et.getText().length());

        float textSize = parentWidth * 0.25f / charsPerLine ;
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        Log.e(TAG, "applyAutoWrap: textSize=" +  textSize);

//        float width = parentWidth * 1.70f / wrapLength
//
//        ViewGroup.LayoutParams lp = et.getLayoutParams();
//        lp.width = w;
//        et.setLayoutParams(lp);
    }

    private void adjustWidthEditText(AutoFitEditText et, int previousLength, int currentLength) {
        float textSize = et.getTextSize();
        int currentWidth = et.getLayoutParams().width;

        int changeToWidth = (int)(textSize * charsPerLine * 0.62);

        Log.e(TAG, "applyAutoWrap: previousLength="+previousLength);
        Log.e(TAG, "applyAutoWrap: currentLength="+currentLength);
        Log.e(TAG, "applyAutoWrap: charsPerLine="+charsPerLine);
        Log.e(TAG, "applyAutoWrap: currentWidth="+currentWidth);
        Log.e(TAG, "applyAutoWrap: changeToWidth="+changeToWidth);
        if(charsPerLine >= 30) {
            int parentWidth= ((View)et.getParent()).getWidth();
            ViewGroup.LayoutParams lp = et.getLayoutParams();
            lp.width = parentWidth;
            et.setLayoutParams(lp);
            Log.e(TAG, "applyAutoWrap: ========Applied Full Width");
        }
        else if( (previousLength < currentLength && currentWidth > changeToWidth) ||
                (previousLength > currentLength && currentWidth < changeToWidth) ) {
//            Log.e(TAG, "applyAutoWrap: ========");
//            Log.e(TAG, "applyAutoWrap: ========Start adjustWidthEditText");
//            Log.e(TAG, "applyAutoWrap: textSize=" + textSize);

            ViewGroup.LayoutParams lp = et.getLayoutParams();
            lp.width = changeToWidth;
            et.setLayoutParams(lp);

//            Log.e(TAG, "applyAutoWrap: " + charsPerLine + " lines currentWidth=" + currentWidth +
//                    " / changeToWidth=" + changeToWidth);
//            Log.e(TAG, "applyAutoWrap: ========Start adjustWidthEditText");
//            Log.e(TAG, "applyAutoWrap: ========");
            Log.e(TAG, "applyAutoWrap: ========Applied");
        }
    }

    private float pixelToDp(float size) {
//        return size / getResources().getDisplayMetrics().density;
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                size,
                getResources().getDisplayMetrics());
    }

    private class TextChangeListener implements TextWatcher {

        private AutoFitEditText et;
        private int lastTextLength;

        public TextChangeListener(@NonNull AutoFitEditText et) {
            this.et = et;
            lastTextLength = et.getText().length();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
//            Log.e(TAG, "afterTextChanged: afterTextChanged s=" + s);

            adjustWidthEditText(et, lastTextLength, lastTextLength = et.getText().length());
        }
    }

//    private class AutoWrapFilter implements InputFilter {
//        private final int mLineChars;
//
//        public AutoWrapFilter(int pLineChars) {
//            mLineChars = pLineChars;
//        }
//
//        @Override
//        public CharSequence filter(CharSequence src, int srcStart, int srcEnd, Spanned dest, int destStart, int destEnd) {
//            CharSequence original = dest.subSequence(0,destStart);
//            CharSequence replacement = src.subSequence(srcStart,srcEnd);
//
//            if(replacement.length() < 1){
//                return null;
//            }
//
//            int lastLineCharIndex = -1;
//
//            for (int j = destStart - 1; j >= 0; j--){
//                if(original.charAt(j) == '\n'){
//                    lastLineCharIndex = j;
//                    break;
//                }
//            }
//
//            int charsAfterLine = lastLineCharIndex < 0 ? original.length() : original.length() - lastLineCharIndex;
//
//            StringBuilder sb = new StringBuilder();
//
//            for (int k = 0; k < replacement.length(); k++){
//
//                if(charsAfterLine == mLineChars+1){
//                    charsAfterLine = 0;
//                    sb.append('\n');
//                }
//
//                sb.append(replacement.charAt(k));
//                charsAfterLine++;
//
//            }
//
//
//            return sb;
//        }
//    }
}
