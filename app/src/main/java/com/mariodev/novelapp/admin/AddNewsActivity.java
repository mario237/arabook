package com.mariodev.novelapp.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.util.Linkify;
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

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.mariodev.novelapp.activities.WriteChapterActivity;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.NewsModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;


public class AddNewsActivity extends AppCompatActivity {

    SharedPref sharedPref;
    ConstraintLayout newsImageLayout;
    LinearLayout addNewsImgLayout;
    ImageView deleteImg, newsImage;
    TextView  publishTv;
    ImageView back;
    EditText newsTitle, newsText;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    AnstronCoreHelper coreHelper;

    DatabaseReference newsRef;

    String url;


    AlertDialog loadingDialog;



    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;
    Uri mImageUri ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        back = findViewById(R.id.arrowBack);
        newsImageLayout = findViewById(R.id.newsImageLayout);
        addNewsImgLayout = findViewById(R.id.addNewsImgLayout);
        newsImage = findViewById(R.id.newsImage);
        deleteImg = findViewById(R.id.deleteImg);
        publishTv = findViewById(R.id.publishTv);
        newsTitle = findViewById(R.id.newsTitle);
        newsText = findViewById(R.id.newsText);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        coreHelper = new AnstronCoreHelper(this);

        newsRef = FirebaseDatabase.getInstance().getReference("News");

        newsText.setLinksClickable(false);
        newsText.setAutoLinkMask(Linkify.WEB_URLS);
//If the edit text contains previous text with potential links
        Linkify.addLinks(newsText, Linkify.WEB_URLS);

        newsText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Linkify.addLinks(editable, Linkify.WEB_URLS);
            }
        });



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        newsImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newsImage.setImageBitmap(null);
                addNewsImgLayout.setVisibility(View.VISIBLE);
                newsImage.setVisibility(View.GONE);
                deleteImg.setVisibility(View.GONE);
                mImageUri = null;

            }
        });

        publishTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (MyApplication.isOnline(AddNewsActivity.this)){
                    Toast.makeText(AddNewsActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }else {
                    publishNews();
                }
            }
        });

    }

    private void publishNews() {

        final String newsTitleTxt = newsTitle.getText().toString().trim();
        final Spanned newsTextTxt = newsText.getText();

        if (newsTextTxt.toString().isEmpty() || newsTitleTxt.isEmpty() || mImageUri == null) {
            Toast.makeText(this, R.string.news_error, Toast.LENGTH_LONG).show();
        } else {

                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM");
                final String currentDate = simpleDateFormat2.format(calendar.getTime());
                final String currentTime = simpleDateFormat.format(calendar.getTime());


                loadingDialogMethod();


                // Defining the child of storageReference
                final StorageReference ref = storageReference.child("news/" + UUID.randomUUID().toString());

                ref.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {


                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onSuccess(Uri uri) {

                                url = uri.toString();
                                String newsId = newsRef.push().getKey();

                                 NewsModel newsModel = new NewsModel();

                                 newsModel.setNewsId(newsId);
                                 newsModel.setNewsImage(url);
                                 newsModel.setNewsText(MyApplication.toHtml(newsTextTxt));
                                 newsModel.setNewsDateTime(currentDate + " " + currentTime);
                                 newsModel.setNewsTitle(newsTitleTxt);




                                assert newsId != null;
                                newsRef.child(newsId).child("data").setValue(newsModel)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                loadingDialog.dismiss();
                                                onBackPressed();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                        Toast.makeText(AddNewsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(AddNewsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                            }
                        });


                    }
                });







        }
    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(AddNewsActivity.this);
        dialog.setCancelable(false);
        dialog.setView(R.layout.loading_dialog);
        loadingDialog = dialog.create();
        loadingDialog.show();

    }



    private void chooseImage() {
        if (ActivityCompat.checkSelfPermission(AddNewsActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddNewsActivity.this , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_CODE);
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

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                addNewsImgLayout.setVisibility(View.GONE);
                newsImage.setVisibility(View.VISIBLE);
                deleteImg.setVisibility(View.VISIBLE);
                newsImage.setImageBitmap(bitmap);
                newsImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            catch (IOException e)
            {
                Toast.makeText(this, e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        }



    }

    @Override
    public void onBackPressed() {
        finish();
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
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }

}