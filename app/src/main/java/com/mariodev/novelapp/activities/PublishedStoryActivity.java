package com.mariodev.novelapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.mariodev.novelapp.adapters.PublishedChapterAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PublishedStoryActivity extends AppCompatActivity implements PublishedChapterAdapter.OnItemClickListener {

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;

    SharedPref sharedPref;
    String storyId;
    ImageView storyImg, back, addNewChapterImage, deleteStoryIcon, saveStoryDataIcon;
    LinearLayout storyTitleLayout, storyDescLayout;
    TextView storyNameTv, storyDescTv, yourChaptersTxt, tagsTv, publishDaysTv, publishSecurityTv;
    Button storyTypeBtn;
    LinearLayout otherTags, publishDays, publishSecurity;
    SwitchCompat adultSwitch, completedSwitch;
    String storyImageUri, storyNameTxt, storyDescTxt, storyTypeTxt, url, storyTags, storySecurity, storyDays, storyWriterTxt;
    boolean completedState, adultState;
    DatabaseReference storyReference, chapterReference;
    RecyclerView chapterPublishedRecycler;
    PublishedChapterAdapter publishedChapterAdapter;
    List<ChapterModel> chapterList;
    ConstraintLayout storyDataLayout;
    ProgressBar reloadStoryData;
    Uri mImageUri , compressedUri;


    int selectedType = 0;

    String[] tagsItems;
    String[] daysItems;

    boolean[] checkedTagsItems;
    boolean[] checkedDaysItems;

    final ArrayList<Integer> mTagsItems = new ArrayList<>();
    final ArrayList<Integer> mDaysItems = new ArrayList<>();


    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    AnstronCoreHelper coreHelper;

    Dialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_published_chapters);

        back = findViewById(R.id.arrowBack);
        storyImg = findViewById(R.id.storyCover);
        storyNameTv = findViewById(R.id.storyNameTv);
        storyDescTv = findViewById(R.id.storyDescTv);
        storyTypeBtn = findViewById(R.id.storyTypeBtn);
        addNewChapterImage = findViewById(R.id.addNewChapterIcon);
        deleteStoryIcon = findViewById(R.id.deleteStoryIcon);
        storyTitleLayout = findViewById(R.id.storyTitleLayout);
        storyDescLayout = findViewById(R.id.storyDescLayout);
        yourChaptersTxt = findViewById(R.id.yourChaptersTxt);
        reloadStoryData = findViewById(R.id.reloadStoryData);
        storyDataLayout = findViewById(R.id.storyDataLayout);
        otherTags = findViewById(R.id.otherTags);
        tagsTv = findViewById(R.id.tagsTv);
        publishDays = findViewById(R.id.publishDays);
        publishDaysTv = findViewById(R.id.publishDaysTv);
        publishSecurity = findViewById(R.id.publishSecurity);
        publishSecurityTv = findViewById(R.id.publishSecurityTv);
        saveStoryDataIcon = findViewById(R.id.saveStoryDataIcon);
        adultSwitch = findViewById(R.id.adultSwitch);
        completedSwitch = findViewById(R.id.completedSwitch);

        if (getIntent().getStringExtra("chapterNo") != null) {
            Toast.makeText(this, getIntent().getStringExtra("chapterNo"), Toast.LENGTH_SHORT).show();

        }




        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        coreHelper = new AnstronCoreHelper(this);

        storyId = getIntent().getStringExtra("storyId");

        assert storyId != null;

        storyReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories").child(storyId).child("data");

        chapterReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(storyId);


        setStoryData();

        chapterPublishedRecycler = findViewById(R.id.chapterPublishedRecycler);
        chapterPublishedRecycler.setHasFixedSize(true);
        chapterPublishedRecycler.setLayoutManager(new LinearLayoutManager(this));
        chapterList = new ArrayList<>();

        tagsItems = new String[]
                {getResources().getString(R.string.horror),
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
                getResources().getString(R.string.short_stories),
                        getResources().getString(R.string.non_fiction)};

        daysItems = new String[]{
                getResources().getString(R.string.saturday),
                getResources().getString(R.string.sunday),
                getResources().getString(R.string.monday),
                getResources().getString(R.string.tuesday),
                getResources().getString(R.string.wednesday),
                getResources().getString(R.string.thursday),
                getResources().getString(R.string.friday),
        };


        checkedTagsItems = new boolean[tagsItems.length];
        checkedDaysItems = new boolean[daysItems.length];


        addNewChapterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewChapter();
            }
        });

        storyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        storyTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PublishedStoryActivity.this, StoryTitleOrDescActivity.class);
                intent.putExtra("from", "Published");
                intent.putExtra("storyId", storyId);
                intent.putExtra("storyName", storyNameTv.getText());
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        storyDescLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PublishedStoryActivity.this, StoryTitleOrDescActivity.class);
                intent.putExtra("from", "Published");
                intent.putExtra("storyId", storyId);
                intent.putExtra("storyDesc", storyDescTv.getText());
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);


            }
        });

        storyTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChooseTypeDialog();
            }
        });

        deleteStoryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStoryDialog();
            }
        });

        otherTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMultiChoiceTags();
            }
        });

        publishDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMultiChoiceDays();
            }
        });

        publishSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChoiceSecurity();
            }
        });

        saveStoryDataIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStoryData();
            }
        });
        setChaptersList();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        setChapterCount();

    }


    private void addNewChapter() {
        storyId = getIntent().getStringExtra("storyId");
        Intent intent = new Intent(PublishedStoryActivity.this, WriteChapterActivity.class);
        intent.putExtra("storyId", storyId);
        intent.putExtra("storyName", storyNameTxt);
        intent.putExtra("publish", "Published");
        intent.putExtra("storyImageUri", storyImageUri);
        intent.putExtra("storyDesc", storyDescTxt);
        intent.putExtra("storyType", storyTypeTxt);
        intent.putExtra("storyTags", storyTags);
        intent.putExtra("storyDays", storyDays);
        intent.putExtra("storyWriter", storyWriterTxt);
        intent.putExtra("completed", String.valueOf(completedState));
        intent.putExtra("adult", String.valueOf(adultState));
        intent.putExtra("chapterNo", String.valueOf(chapterList.size()));
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);


    }

    private void deleteStoryDialog() {


        storyId = getIntent().getStringExtra("storyId");
        final DatabaseReference storyReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories").child(storyId);
        final DatabaseReference chapterReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(storyId);


        assert storyImageUri != null;
        final StorageReference storyPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(storyImageUri);

        if (!chapterList.isEmpty()) {
            for (int position = 0; position < chapterList.size(); position++) {
                StorageReference chapterPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(chapterList.get(position).getChapterImage());
                chapterPhotoRef.delete();
            }

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PublishedStoryActivity.this, R.style.DialogTheme);
        String message = getResources().getString(R.string.delete_story_message);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (MyApplication.isOnline(getApplicationContext())) {
                            Toast.makeText(PublishedStoryActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            chapterReference.removeValue();


                            storyReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    storyPhotoRef.delete();
                                    Intent intent = new Intent(PublishedStoryActivity.this, MainPageActivity.class);
                                    intent.putExtra("fragment", "MyNovel");
                                    startActivity(intent);
                                    finish();

                                }
                            });
                        }


                    }
                })
                .setNegativeButton(R.string.no, null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateStoryData() {

        storyId = getIntent().getStringExtra("storyId");

        if (MyApplication.isOnline(getApplicationContext())) {
            Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        } else {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Novels")
                    .child("Stories").child(storyId);

            if (tagsTv.getText().toString().contains(storyTypeBtn.getText().toString())) {
                reloadStoryData.setVisibility(View.GONE);
                storyDataLayout.setVisibility(View.VISIBLE);
                Toast.makeText(this, getResources().getString(R.string.type_tags_error), Toast.LENGTH_SHORT).show();
            } else {
                if (mImageUri != null) {

                    loadingDialogMethod();


                    final StorageReference storyPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(storyImageUri);

                    // Defining the child of storageReference
                    final StorageReference ref = storageReference.child("stories/" + UUID.randomUUID().toString());

                    ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    url = uri.toString();

                                    boolean completedState = completedSwitch.isChecked();
                                    boolean adultState = adultSwitch.isChecked();

                                    reference.child("data").child("completed").setValue(completedState);
                                    reference.child("data").child("forAdult").setValue(adultState);


                                    reference.child("data").child("storyType").setValue(storyTypeBtn.getText().toString());
                                    if (tagsTv.getText().equals(getResources().getString(R.string.choose_tags))) {
                                        reference.child("data").child("tags").setValue("");
                                    } else {
                                        reference.child("data").child("tags").setValue(tagsTv.getText());
                                    }


                                    if (publishDaysTv.getText().equals(getResources().getString(R.string.choose_days))) {
                                        reference.child("data").child("days").setValue("");
                                    } else {
                                        reference.child("data").child("days").setValue(publishDaysTv.getText());
                                    }

                                    if (publishSecurityTv.getText().equals(getResources().getString(R.string.choose_security_type))) {
                                        reference.child("data").child("storySecurity").setValue("");
                                    } else {
                                        reference.child("data").child("storySecurity").setValue(publishSecurityTv.getText());
                                    }


                                    reference.child("data").child("storyImage").setValue(url).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            storyPhotoRef.delete();
                                            loadingDialog.dismiss();
                                            saveStoryDataIcon.setImageDrawable(getDrawable(R.drawable.ic_done_accent));
                                            Toast.makeText(PublishedStoryActivity.this, getResources().getString(R.string.data_updated), Toast.LENGTH_SHORT).show();


                                        }
                                    });


                                }
                            });

                        }
                    });


                } else {
                    loadingDialogMethod();

                    boolean completedState = completedSwitch.isChecked();
                    boolean adultState = adultSwitch.isChecked();

                    reference.child("data").child("completed").setValue(completedState);
                    reference.child("data").child("forAdult").setValue(adultState);

                    reference.child("data").child("storyType").setValue(storyTypeBtn.getText().toString());

                    if (tagsTv.getText().equals(getResources().getString(R.string.choose_tags))) {
                        reference.child("data").child("tags").setValue("");
                    } else {
                        reference.child("data").child("tags").setValue(tagsTv.getText());
                    }


                    if (publishDaysTv.getText().equals(getResources().getString(R.string.choose_days))) {
                        reference.child("data").child("days").setValue("");
                    } else {
                        reference.child("data").child("days").setValue(publishDaysTv.getText());
                    }

                    if (publishSecurityTv.getText().equals(getResources().getString(R.string.choose_security_type))) {
                        reference.child("data").child("storySecurity").setValue("");
                    } else {
                        reference.child("data").child("storySecurity").setValue(publishSecurityTv.getText());
                    }

                    saveStoryDataIcon.setImageDrawable(getDrawable(R.drawable.ic_done_accent));


                    Toast.makeText(this, getResources().getString(R.string.data_updated), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();

                }

            }
        }
    }


    private void openMultiChoiceTags() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PublishedStoryActivity.this);
        builder.setTitle(getResources().getString(R.string.choose_tags));


        builder.setMultiChoiceItems(tagsItems, checkedTagsItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {


                if (isChecked) {
                    if (mTagsItems.size() != 4) {
                        mTagsItems.add(position);
                    } else {
                        Toast.makeText(PublishedStoryActivity.this,
                                getResources().getString(R.string.cannot_choose_over_4), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mTagsItems.remove((Integer.valueOf(position)));
                }
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                StringBuilder item = new StringBuilder();

                for (int i = 0; i < mTagsItems.size(); i++) {
                    item.append(tagsItems[mTagsItems.get(i)]);
                    if (i != mTagsItems.size() - 1) {
                        item.append(" , ");
                    }
                }
                storyTags = item.toString();

                if (storyTags.isEmpty()) {
                    tagsTv.setText(getResources().getString(R.string.choose_tags));
                } else {
                    tagsTv.setText(storyTags);
                }


            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openMultiChoiceDays() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PublishedStoryActivity.this);
        builder.setTitle(getResources().getString(R.string.choose_days));


        builder.setMultiChoiceItems(daysItems, checkedDaysItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {


                if (isChecked) {
                    if (mDaysItems.size() != 4) {
                        mDaysItems.add(position);
                    } else {
                        Toast.makeText(PublishedStoryActivity.this,
                                getResources().getString(R.string.cannot_choose_over_4), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mDaysItems.remove((Integer.valueOf(position)));
                }
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                StringBuilder item = new StringBuilder();

                for (int i = 0; i < mDaysItems.size(); i++) {
                    item.append(daysItems[mDaysItems.get(i)]);
                    if (i != mDaysItems.size() - 1) {
                        item.append(" , ");
                    }
                }
                storyDays = item.toString();

                if (storyDays.isEmpty()) {
                    publishDaysTv.setText(getResources().getString(R.string.choose_days));
                } else {
                    publishDaysTv.setText(storyDays);
                }


            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openChoiceSecurity() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PublishedStoryActivity.this);

        View view = getLayoutInflater().inflate(R.layout.choose_security_style, null);

        final RelativeLayout firstSecurityLayout = view.findViewById(R.id.firstSecurityLayout);
        final RelativeLayout secondSecurityLayout = view.findViewById(R.id.secondSecurityLayout);
        final RelativeLayout thirdSecurityLayout = view.findViewById(R.id.thirdSecurityLayout);

        final TextView firstSecurityTv = view.findViewById(R.id.firstSecurityTv);
        final TextView secondSecurityTv = view.findViewById(R.id.secondSecurityTv);
        final TextView thirdSecurityTv = view.findViewById(R.id.thirdSecurityTv);

        final CheckBox firstSecurityCheck = view.findViewById(R.id.firstSecurityCheck);
        final CheckBox secondSecurityCheck = view.findViewById(R.id.secondSecurityCheck);
        final CheckBox thirdSecurityCheck = view.findViewById(R.id.thirdSecurityCheck);

        builder.setView(view);


        final AlertDialog dialog = builder.create();

        firstSecurityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishSecurityTv.setText(firstSecurityTv.getText().toString());
                dialog.dismiss();
            }
        });

        secondSecurityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishSecurityTv.setText(secondSecurityTv.getText().toString());
                dialog.dismiss();

            }
        });

        thirdSecurityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishSecurityTv.setText(thirdSecurityTv.getText().toString());
                dialog.dismiss();

            }
        });


        firstSecurityCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishSecurityTv.setText(firstSecurityTv.getText().toString());
                dialog.dismiss();
            }
        });

        secondSecurityCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishSecurityTv.setText(secondSecurityTv.getText().toString());
                dialog.dismiss();

            }
        });

        thirdSecurityCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishSecurityTv.setText(thirdSecurityTv.getText().toString());
                dialog.dismiss();

            }
        });

        if (publishSecurityTv.getText().equals(firstSecurityTv.getText())) {
            firstSecurityCheck.setChecked(true);
        } else if (publishSecurityTv.getText().equals(secondSecurityTv.getText())) {
            secondSecurityCheck.setChecked(true);
        } else if (publishSecurityTv.getText().equals(thirdSecurityTv.getText())) {
            thirdSecurityCheck.setChecked(true);
        }

        dialog.show();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(PublishedStoryActivity.this);
        builder.setTitle(getResources().getString(R.string.select_story_type))
                .setCancelable(true)
                .setSingleChoiceItems(storyTypesItems, selectedType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedType = i;
                        storyTypeBtn.setText(storyTypesItems[i]);
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();


    }

    private void chooseImage() {
        if (ActivityCompat.checkSelfPermission(PublishedStoryActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(PublishedStoryActivity.this , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_CODE);
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

            // Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);

            File file = new File(SiliCompressor.with(this).compress(FileUtils.getPath(this , mImageUri) ,
                    new File(this.getCacheDir() , "temp")));

            compressedUri = Uri.fromFile(file);
            storyImg.setImageURI(compressedUri);
            storyImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        }
    }

    private void setChaptersList() {

        chapterReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chapterList.clear();
                if (snapshot.exists()) {

                    yourChaptersTxt.setVisibility(View.VISIBLE);
                    chapterPublishedRecycler.setVisibility(View.VISIBLE);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ChapterModel chapterModel = dataSnapshot.child("data").getValue(ChapterModel.class);
                        assert chapterModel != null;
                        chapterList.add(chapterModel);

                    }

                    publishedChapterAdapter = new PublishedChapterAdapter
                            (PublishedStoryActivity.this, chapterList, storyImageUri);
                    chapterPublishedRecycler.setAdapter(publishedChapterAdapter);
                    publishedChapterAdapter.notifyDataSetChanged();
                    publishedChapterAdapter.setOnItemClickListener(PublishedStoryActivity.this);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void setStoryData() {


        storyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    StoryModel storyModel = snapshot.getValue(StoryModel.class);
                    assert storyModel != null;


                    storyNameTxt = storyModel.getStoryName();
                    storyDescTxt = storyModel.getStoryDescription();
                    storyTypeTxt = storyModel.getStoryType();
                    storyImageUri = storyModel.getStoryImage();
                    storyWriterTxt = storyModel.getStoryWriter();
                    completedState = storyModel.isCompleted();
                    adultState = storyModel.isForAdult();
                    storyTags = storyModel.getTags();
                    storyDays = storyModel.getDays();
                    storySecurity = storyModel.getStorySecurity();


                    //set data to views
                    storyNameTv.setText(storyNameTxt);
                    storyDescTv.setText(storyDescTxt);
                    storyTypeBtn.setText(storyTypeTxt);
                    Glide.with(getApplicationContext()).load(storyImageUri).into(storyImg);

                    if (storyTags.equals("")) {
                        tagsTv.setText(getResources().getString(R.string.choose_tags));
                    } else {
                        tagsTv.setText(storyTags);
                    }

                    if (storyDays.equals("")) {
                        publishDaysTv.setText(getResources().getString(R.string.choose_days));
                    } else {
                        publishDaysTv.setText(storyDays);
                    }

                    if (storySecurity.equals("")) {
                        publishSecurityTv.setText(getResources().getString(R.string.choose_security_type));
                    } else {
                        publishSecurityTv.setText(storySecurity);
                    }

                    completedSwitch.setChecked(storyModel.isCompleted());
                    adultSwitch.setChecked(storyModel.isForAdult());


                    reloadStoryData.setVisibility(View.GONE);
                    storyDataLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PublishedStoryActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "MyNovel");
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    private void loadingDialogMethod() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(PublishedStoryActivity.this);
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
                    Toast.makeText(PublishedStoryActivity.this, getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
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


    private void setChapterCount() {
        final DatabaseReference stReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories").child(storyId).child("data");

        DatabaseReference chReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(storyId);


        chReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    stReference.child("chaptersCount").setValue((int) dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onItemClick(int position, List<ChapterModel> chapterList) {


        Intent intent = new Intent(PublishedStoryActivity.this, ViewPublishedChapterActivity.class);
        intent.putExtra("storyId", chapterList.get(position).getStoryId());
        intent.putExtra("chapterId", chapterList.get(position).getChapterId());
        intent.putExtra("storyImage", storyImageUri);
        intent.putExtra("dateTime", chapterList.get(position).getChapterDateTime());
        intent.putExtra("chapterImage", chapterList.get(position).getChapterImage());
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

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