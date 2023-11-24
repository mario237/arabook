package com.mariodev.novelapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("ALL")
public class AddNewStoryActivity extends AppCompatActivity {

    SharedPref sharedPref;
    ScrollView storyDetailsActivityLayout;
    EditText storyNameTEdt, storyDescTEdt;
    ImageView back;
    CardView displayCoverLayout;
    ImageView storyCover;
    TextView addOrEditCoverTxt, proceedTxt;
    Button selectStoryTypeBtn;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    SwitchCompat adult, completed;
    ProgressBar progressBar;
    Uri mImageUri , compressedUri;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    AnstronCoreHelper coreHelper;

    DatabaseReference storyRef;

    String url, storyId;

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;


    int selectedType = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_details);

        storyDetailsActivityLayout = findViewById(R.id.storyDetailsActivityLayout);
        displayCoverLayout = findViewById(R.id.storyCoverCard);
        storyCover = findViewById(R.id.storyCover);
        addOrEditCoverTxt = findViewById(R.id.addOrEditCoverTxt);
        selectStoryTypeBtn = findViewById(R.id.chooseStoryTypeBtn);
        back = findViewById(R.id.arrowBack);
        storyNameTEdt = findViewById(R.id.storyNameTEdt);
        storyDescTEdt = findViewById(R.id.storyDescTEdt);
        proceedTxt = findViewById(R.id.proceedTxt);
        adult = findViewById(R.id.adultSwitch);
        completed = findViewById(R.id.completedSwitch);
        progressBar = findViewById(R.id.uploadStoryLoad);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        displayCoverLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });


        selectStoryTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChooseTypeDialog();
            }
        });




        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        storyRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Drafts");
        storyId = storyRef.push().getKey();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        coreHelper = new AnstronCoreHelper(this);

        proceedTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MyApplication.isOnline(getApplicationContext())) {
                    Toast.makeText(AddNewStoryActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
                else {

                    if (checkStoryDataFilled())
                        Toast.makeText(AddNewStoryActivity.this, getResources().getString(R.string.story_data_not_completed), Toast.LENGTH_SHORT).show();
                    else{
                        storyDetailsActivityLayout.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        goToWriteChapter();
                    }

                }

            }
        });



    }

    private boolean checkStoryDataFilled(){
        if (mImageUri==null || storyNameTEdt.getText().toString().trim().isEmpty() || storyDescTEdt.getText().toString().trim().isEmpty()
        || selectStoryTypeBtn.getText().toString().equals(getResources().getString(R.string.story_type)))
            return true;
        else
            return false;
    }


    private void openChooseTypeDialog() {
        final CharSequence[] storyTypesItems = {getResources().getString(R.string.horror),
                getResources().getString(R.string.fiction),
                getResources().getString(R.string.adventures),
                getResources().getString(R.string.romantic),
                getResources().getString(R.string.science_fiction),
                getResources().getString(R.string.psychological),
                getResources().getString(R.string.mystery),
                getResources().getString(R.string.police),
                getResources().getString(R.string.comedy),
                getResources().getString(R.string.historical),
                getResources().getString(R.string.drama),
                getResources().getString(R.string.realistic),
                getResources().getString(R.string.non_fiction)};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewStoryActivity.this);
        builder.setTitle(getResources().getString(R.string.select_story_type))
                .setCancelable(true)
                .setSingleChoiceItems(storyTypesItems, selectedType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedType = i;
                        selectStoryTypeBtn.setText(storyTypesItems[i]);
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();


    }

    private void chooseImage() {
        if (ActivityCompat.checkSelfPermission(AddNewStoryActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AddNewStoryActivity.this , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_CODE);
        }else {
//           startActivityForResult(new Intent().setAction(Intent.ACTION_GET_CONTENT)
//           .setType("image/*") , CODE_IMAGE_GALLERY);
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
            File file = new File(SiliCompressor.with(this).compress(FileUtils.getPath(this , mImageUri) ,
                    new File(this.getCacheDir() , "temp")));

            compressedUri = Uri.fromFile(file);
            storyCover.setImageURI(compressedUri);
            storyCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            addOrEditCoverTxt.setVisibility(View.GONE);



    }
    }


    private void goToWriteChapter() {
        if (mImageUri != null) {
            // Defining the child of storageReference
            final StorageReference ref = storageReference.child("stories/" + UUID.randomUUID().toString());

            ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            url = uri.toString();

                            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                            String storyName = storyNameTEdt.getText().toString().trim();
                            String storyDesc = storyDescTEdt.getText().toString().trim();
                            String storyType = selectStoryTypeBtn.getText().toString().trim();
                            boolean completedState = completed.isChecked();
                            boolean adultState = adult.isChecked();

                            final StoryModel storyModel = new StoryModel(
                                    storyId,
                                    userId,
                                    url,
                                    storyName,
                                    storyDesc,
                                    storyType,
                                    "",
                                    "",
                                    adultState,
                                    completedState,
                                    false,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    ""
                            );


                            assert storyId != null;
                            storyRef.child(storyId).setValue(storyModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent intent = new Intent(AddNewStoryActivity.this, WriteChapterActivity.class);
                                    intent.putExtra("storyId", storyId);
                                    intent.putExtra("fromActivity", "addNewStory");
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    storyDetailsActivityLayout.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(AddNewStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    storyDetailsActivityLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddNewStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddNewStoryActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "MyNovel");
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

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