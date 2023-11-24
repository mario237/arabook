package com.mariodev.novelapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.ImageViewCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.ChapterModel;

import java.io.File;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class ViewPublishedChapterActivity extends AppCompatActivity {

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;
    SharedPref sharedPref;
    LinearLayout addChapterImgLayout, fontStyleLayout;
    ConstraintLayout viewPublishedChapterLayout, chapterImageLayout;
    ImageView backImg, chapterImage, deleteImg, styleOption, updateChapterImg;
    EditText chapterNameEdt, chapterTextEdt;
    TextView boldText, italicText, underlinedText, normalText, counterTv, fontSettingTv;
    DatabaseReference databaseReference;
    String storyId, chapterId, chapterNameTxt, chapterImageUri, counterTxt, url;
    Uri mImageUri , compressedUri;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_published_chapter);

        viewPublishedChapterLayout = findViewById(R.id.viewPublishedChapterLayout);
        chapterImageLayout = findViewById(R.id.chapterImageLayout);
        fontStyleLayout = findViewById(R.id.fontStyleLayout);
        addChapterImgLayout = findViewById(R.id.addChapterImgLayout);
        deleteImg = findViewById(R.id.deleteImg);
        styleOption = findViewById(R.id.colorOptions);
        updateChapterImg = findViewById(R.id.updateChapterImg);
        backImg = findViewById(R.id.arrowBack);
        chapterImage = findViewById(R.id.chapterImage);
        chapterNameEdt = findViewById(R.id.partTitle);
        chapterTextEdt = findViewById(R.id.partText);
        boldText = findViewById(R.id.boldTxt);
        italicText = findViewById(R.id.italicTxt);
        underlinedText = findViewById(R.id.underlineTxt);
        normalText = findViewById(R.id.normalTxt);
        counterTv = findViewById(R.id.counterText);
        fontSettingTv = findViewById(R.id.fontSettingTv);

        storyId = getIntent().getStringExtra("storyId");
        chapterId = getIntent().getStringExtra("chapterId");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        databaseReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(storyId).child(chapterId);


//        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
//            @Override
//            public void onVisibilityChanged(boolean isOpen) {
//                if (isOpen) {
//                    fontStyleLayout.setVisibility(View.VISIBLE);
//                } else {
//                    fontStyleLayout.setVisibility(View.INVISIBLE);
//                }
//            }
//        });

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setChapterData();

        chapterImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chapterImage.setImageBitmap(null);
                addChapterImgLayout.setVisibility(View.VISIBLE);
                chapterImage.setVisibility(View.GONE);
                deleteImg.setVisibility(View.GONE);
                mImageUri = null;

            }
        });

        styleOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initStyleOption();
            }
        });

        boldText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBoldText();
            }
        });
        italicText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setItalicText();
            }
        });
        underlinedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUnderlinedText();
            }
        });
        normalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBackToNormalText();
            }
        });

        chapterTextEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                String[] WC = chapterTextEdt.getText().toString().split("\\s+");
                counterTv.setText(getResources().getString(R.string.word_count) + WC.length);
            }
        });


        updateChapterImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChapterData();

            }
        });

    }

    private void initStyleOption() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ViewPublishedChapterActivity.this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_miscellaneous, (LinearLayout) findViewById(R.id.layout_miscellaneous));

        final ImageView imageColor1 = bottomSheetView.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = bottomSheetView.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = bottomSheetView.findViewById(R.id.imageColor3);
        final TextView addTxt = findViewById(R.id.add_txt);

        if (((ColorDrawable) viewPublishedChapterLayout.getBackground()).getColor()
                == getColor(R.color.colorPurple)) {
            imageColor1.setImageResource(R.drawable.ic_done);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
        } else if (((ColorDrawable) viewPublishedChapterLayout.getBackground()).getColor()
                == getColor(R.color.colorAlwaysBlack)) {
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done);
            imageColor3.setImageResource(0);
        } else {
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done);
        }

        bottomSheetView.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                viewPublishedChapterLayout.setBackgroundColor(getColor(R.color.colorPurple));
                backImg.setColorFilter(getResources().getColor(R.color.colorAlwaysWhite),
                        PorterDuff.Mode.SRC_IN);
                ImageViewCompat.setImageTintList(styleOption, ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                addChapterImgLayout.setBackground(getResources().getDrawable(R.drawable.white_border));
                ImageViewCompat.setImageTintList((ImageView) findViewById(R.id.add_img), ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                addTxt.setTextColor(getColor(R.color.colorAlwaysWhite));
                chapterNameEdt.setBackgroundColor(getColor(R.color.colorPurple));
                chapterNameEdt.setTextColor(getColor(R.color.colorAlwaysWhite));
                chapterTextEdt.setBackgroundColor(getColor(R.color.colorPurple));
                chapterTextEdt.setTextColor(getColor(R.color.colorAlwaysWhite));
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                boldText.setTextColor(getColor(R.color.colorAlwaysWhite));
                italicText.setTextColor(getColor(R.color.colorAlwaysWhite));
                underlinedText.setTextColor(getColor(R.color.colorAlwaysWhite));
                normalText.setTextColor(getColor(R.color.colorAlwaysWhite));
                counterTv.setTextColor(getColor(R.color.colorAlwaysWhite));
                fontSettingTv.setTextColor(getColor(R.color.colorPink));
            }
        });

        bottomSheetView.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                viewPublishedChapterLayout.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                backImg.setColorFilter(getResources().getColor(R.color.colorAlwaysWhite),
                        PorterDuff.Mode.SRC_IN);
                ImageViewCompat.setImageTintList(styleOption, ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                addChapterImgLayout.setBackground(getResources().getDrawable(R.drawable.white_border));
                ImageViewCompat.setImageTintList((ImageView) findViewById(R.id.add_img), ColorStateList.valueOf(getColor(R.color.colorAlwaysWhite)));
                addTxt.setTextColor(getColor(R.color.colorAlwaysWhite));
                chapterNameEdt.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                chapterNameEdt.setTextColor(getColor(R.color.colorAlwaysWhite));
                chapterTextEdt.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                chapterTextEdt.setTextColor(getColor(R.color.colorAlwaysWhite));
                chapterTextEdt.setBackgroundColor(getColor(R.color.colorAlwaysBlack));
                chapterTextEdt.setTextColor(getColor(R.color.colorAlwaysWhite));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                boldText.setTextColor(getColor(R.color.colorAlwaysWhite));
                italicText.setTextColor(getColor(R.color.colorAlwaysWhite));
                underlinedText.setTextColor(getColor(R.color.colorAlwaysWhite));
                normalText.setTextColor(getColor(R.color.colorAlwaysWhite));
                counterTv.setTextColor(getColor(R.color.colorAlwaysWhite));
                fontSettingTv.setTextColor(getColor(R.color.colorAlwaysWhite));


            }
        });

        bottomSheetView.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                viewPublishedChapterLayout.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                backImg.setColorFilter(getResources().getColor(R.color.colorAlwaysBlack),
                        PorterDuff.Mode.SRC_IN);
                ImageViewCompat.setImageTintList(styleOption, ColorStateList.valueOf(getColor(R.color.colorAlwaysBlack)));
                ImageViewCompat.setImageTintList((ImageView) findViewById(R.id.add_img), ColorStateList.valueOf(getColor(R.color.colorAlwaysBlack)));
                addTxt.setTextColor(getColor(R.color.colorAlwaysBlack));
                chapterNameEdt.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                chapterNameEdt.setTextColor(getColor(R.color.colorAlwaysBlack));
                chapterTextEdt.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                chapterTextEdt.setTextColor(getColor(R.color.colorAlwaysBlack));
                chapterTextEdt.setBackgroundColor(getColor(R.color.colorAlwaysWhite));
                chapterTextEdt.setTextColor(getColor(R.color.colorAlwaysBlack));
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                boldText.setTextColor(getColor(R.color.colorAlwaysBlack));
                italicText.setTextColor(getColor(R.color.colorAlwaysBlack));
                underlinedText.setTextColor(getColor(R.color.colorAlwaysBlack));
                normalText.setTextColor(getColor(R.color.colorAlwaysBlack));
                fontSettingTv.setTextColor(getColor(R.color.colorAlwaysBlack));


            }
        });


        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


    private void setBoldText() {
        int startSelection = chapterTextEdt.getSelectionStart();
        int endSelection = chapterTextEdt.getSelectionEnd();
        String selectedTxt = chapterTextEdt.getText().subSequence(startSelection, endSelection).toString();
        String formatted = "<b>" + selectedTxt + "</b>";
        chapterTextEdt.setSelection(startSelection, endSelection);
        Editable editable = chapterTextEdt.getText();
        editable.replace(startSelection, endSelection, MyApplication.fromHtml(formatted)); //Html.fromHtml(formatted));


    }

    private void setUnderlinedText() {
        int startSelection = chapterTextEdt.getSelectionStart();
        int endSelection = chapterTextEdt.getSelectionEnd();
        String selectedTxt = chapterTextEdt.getText().subSequence(startSelection, endSelection).toString();
        String formatted = "<u>" + selectedTxt + "</u>";
        Editable editable = chapterTextEdt.getEditableText();
        editable.replace(startSelection, endSelection, MyApplication.fromHtml(formatted)); //Html.fromHtml(formatted));
    }

    private void setItalicText() {
        int startSelection = chapterTextEdt.getSelectionStart();
        int endSelection = chapterTextEdt.getSelectionEnd();
        String selectedTxt = chapterTextEdt.getText().subSequence(startSelection, endSelection).toString();
        String formatted = "<i>" + selectedTxt + "</i>";
        Editable editable = chapterTextEdt.getEditableText();
        editable.replace(startSelection, endSelection, MyApplication.fromHtml(formatted)); //Html.fromHtml(formatted));

    }


    private void getBackToNormalText() {

        int startSelection = chapterTextEdt.getSelectionStart();
        int endSelection = chapterTextEdt.getSelectionEnd();

        Spannable str = chapterTextEdt.getText();
        StyleSpan[] ss = str.getSpans(startSelection, endSelection, StyleSpan.class);
        UnderlineSpan[] ss2 = str.getSpans(startSelection, endSelection, UnderlineSpan.class);


        for (StyleSpan s : ss) {
            if (s.getStyle() == Typeface.BOLD) {
                str.removeSpan(s);
            }
            if (s.getStyle() == Typeface.ITALIC) {
                str.removeSpan(s);
            }

        }

        for (UnderlineSpan underlineSpan : ss2) {

            str.removeSpan(underlineSpan);
        }

        chapterTextEdt.setText(str);

        chapterTextEdt.setSelection(startSelection, endSelection);

    }


    private void updateChapterData() {

        String storyImage = getIntent().getStringExtra("storyImage");
        String chapterImage = getIntent().getStringExtra("chapterImage");

        if (MyApplication.isOnline(getApplicationContext())) {
            Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        } else {
            final String chapterNameAlter = chapterNameEdt.getText().toString();
            final Spanned chapterTextAlter = chapterTextEdt.getText();

            final String storyID = getIntent().getStringExtra("storyId");
            final String chapterId = getIntent().getStringExtra("chapterId");
            assert storyID != null;
            assert chapterId != null;

            final String dateTimeTxt = getIntent().getStringExtra("dateTime");


            if (mImageUri != null) {


                if (storyImage != null && chapterImage != null) {
                    if (!chapterImage.equals(storyImage)) {
                        final StorageReference storyPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(chapterImage);
                        storyPhotoRef.delete();
                    }
                }
                loadingDialogMethod();

                final StorageReference ref = storageReference.child("chapters/" + UUID.randomUUID().toString());
                // Defining the child of storageReference

                ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(Uri uri) {

                                url = uri.toString();

                                final ChapterModel chapterModel = new ChapterModel();
                                chapterModel.setChapterDateTime(dateTimeTxt);
                                chapterModel.setStoryId(storyID);
                                chapterModel.setChapterId(chapterId);
                                chapterModel.setChapterName(chapterNameAlter);
                                chapterModel.setChapterImage(url);
                                chapterModel.setChapterText(MyApplication.toHtml(chapterTextAlter));
                                chapterModel.setWordCounter(counterTv.getText().toString());
                                //(Html.toHtml(chapterTextAlter, Html.FROM_HTML_MODE_LEGACY));

                                databaseReference.child("data").setValue(chapterModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        loadingDialog.dismiss();
                                        Intent intent = new Intent(ViewPublishedChapterActivity.this, PublishedStoryActivity.class);
                                        intent.putExtra("storyId", chapterModel.getStoryId());
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                });


                            }
                        });

                    }
                });


            } else {


                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String storyId = getIntent().getStringExtra("storyId");
                        if (snapshot.exists()) {
                            final ChapterModel chapterModel = snapshot.child("data").getValue(ChapterModel.class);
                            assert chapterModel != null;

                            String chapterName = chapterModel.getChapterName();
                            String chapterText = chapterModel.getChapterText();

                            String currChapterName = chapterNameEdt.getText().toString();
                            Spanned currChapterText = chapterTextEdt.getText();


                            if (currChapterName.equals(chapterName) && MyApplication.toHtml(currChapterText).equals(chapterText)) {

                                //Html.toHtml(currChapterText,Html.FROM_HTML_MODE_LEGACY)
                                Intent intent = new Intent(ViewPublishedChapterActivity.this, PublishedStoryActivity.class);
                                intent.putExtra("storyId", storyId);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                            } else {

                                loadingDialogMethod();

                                chapterModel.setChapterDateTime(dateTimeTxt);
                                chapterModel.setStoryId(storyID);
                                chapterModel.setChapterId(chapterId);
                                chapterModel.setChapterName(chapterNameAlter);
                                chapterModel.setChapterImage(chapterModel.getChapterImage());
                                chapterModel.setChapterText(MyApplication.toHtml(chapterTextAlter));
                                chapterModel.setWordCounter(counterTv.getText().toString());

                                //(Html.toHtml(chapterTextAlter, Html.FROM_HTML_MODE_LEGACY));

                                databaseReference.child("data").setValue(chapterModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingDialog.dismiss();
                                        Intent intent = new Intent(ViewPublishedChapterActivity.this, PublishedStoryActivity.class);
                                        intent.putExtra("storyId", storyId);
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        loadingDialog.dismiss();
                                        Toast.makeText(ViewPublishedChapterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingDialog.dismiss();

                        Toast.makeText(ViewPublishedChapterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                });


            }

        }


    }

    private void setChapterData() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ChapterModel chapterModel = snapshot.child("data").getValue(ChapterModel.class);

                    assert chapterModel != null;
                    chapterImageUri = chapterModel.getChapterImage();
                    chapterNameTxt = chapterModel.getChapterName();
                    counterTxt = chapterModel.getWordCounter();

                    if (chapterImageUri != null) {
                        Glide.with(getApplicationContext()).load(chapterImageUri)
                                .into(chapterImage);
                    }


                    chapterNameEdt.setText(chapterNameTxt);

                    chapterTextEdt.setText(MyApplication.fromHtml(chapterModel.getChapterText()));
                    //(Html.fromHtml(chapterModel.getChapterText()));

                    counterTv.setText(counterTxt);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void chooseImage() {
        if (ActivityCompat.checkSelfPermission(ViewPublishedChapterActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ViewPublishedChapterActivity.this , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_CODE);
        }else {
            //            startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT)
//                    .setType("image/*") , CODE_IMAGE_GALLERY);
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), CODE_IMAGE_GALLERY);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_IMAGE_GALLERY && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            mImageUri = data.getData();

            // Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
            File file = new File(SiliCompressor.with(this).compress(FileUtils.getPath(this , mImageUri) ,
                    new File(this.getCacheDir() , "temp")));

            compressedUri = Uri.fromFile(file);
            addChapterImgLayout.setVisibility(View.GONE);
            chapterImage.setVisibility(View.VISIBLE);
            deleteImg.setVisibility(View.VISIBLE);
            chapterImage.setImageURI(compressedUri);
            chapterImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(ViewPublishedChapterActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                    Toast.makeText(ViewPublishedChapterActivity.this, getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }
            }
        };

        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 15000);

    }

    @Override
    public void onBackPressed() {
        if (MyApplication.isOnline(getApplicationContext())) {
            showGoBackDialog();
        } else {
            updateChapterData();
        }
    }

    private void showGoBackDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(ViewPublishedChapterActivity.this, R.style.DialogTheme);
        String message = getResources().getString(R.string.no_internet) + " " + getResources().getString(R.string.go_back);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(ViewPublishedChapterActivity.this, PublishedStoryActivity.class);
                        intent.putExtra("storyId", storyId);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                    }
                })
                .setNegativeButton(R.string.no, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void checkLightMode() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "ar"));
    }


}