package com.mariodev.novelapp.activities;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anstrontechnologies.corehelper.AnstronCoreHelper;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.mariodev.novelapp.adapters.ChapterDraftAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("ALL")
public class DraftStoryDetailsActivity extends AppCompatActivity implements ChapterDraftAdapter.OnItemClickListener {

    private static final int CODE_IMAGE_GALLERY = 1;
    private static final int PERMISSION_CODE = 10;


    SharedPref sharedPref;
    String storyId;
    ImageView storyImg, back, addNewChapterImage, deleteStoryIcon, saveStoryDataIcon;
    LinearLayout storyTitleLayout, storyDescLayout;
    TextView storyNameTv, storyDescTv, yourChaptersTxt, tagsTv;
    Button storyTypeBtn;
    LinearLayout otherTags;
    String userId, storyImageUri, storyNameTxt, storyDescTxt, storyTypeTxt, chapterImageUri, url, storyTags , imagePath;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    RecyclerView recyclerView;
    ChapterDraftAdapter chapterDraftAdapter;
    List<ChapterModel> chapterList;
    ConstraintLayout storyDataLayout;
    ProgressBar reloadStoryData, reloadChapterData;
    Uri mImageUri  , compressedUri;


    int selectedType = 0;

    String[] tagsItems;
    boolean[] checkedItems;
    final ArrayList<Integer> mTagsItems = new ArrayList<>();

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    AnstronCoreHelper coreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_story_details);

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
        saveStoryDataIcon = findViewById(R.id.saveStoryDataIcon);
        reloadChapterData = findViewById(R.id.reloadChapterData);

        storyId = getIntent().getStringExtra("storyId");




        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        userId = firebaseUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("Drafts");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        coreHelper = new AnstronCoreHelper(this);



        recyclerView = findViewById(R.id.chapterPublishedRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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


        checkedItems = new boolean[tagsItems.length];

        setStoryData();
        setChaptersDataInList();
        setChapterCount();

        addNewChapterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewChapter();
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

        storyImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        storyTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DraftStoryDetailsActivity.this, StoryTitleOrDescActivity.class);
                intent.putExtra("storyId", storyId);
                intent.putExtra("storyName", storyNameTv.getText());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        storyDescLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DraftStoryDetailsActivity.this, StoryTitleOrDescActivity.class);
                intent.putExtra("storyId", storyId);
                intent.putExtra("storyDesc", storyDescTv.getText());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        otherTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMultiChoiceTags();
            }
        });

        saveStoryDataIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStoryData();
            }
        });
    }

    private void chooseImage() {
        if (ActivityCompat.checkSelfPermission(DraftStoryDetailsActivity.this , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DraftStoryDetailsActivity.this , new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE} , PERMISSION_CODE);
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


            File file = new File(SiliCompressor.with(this).compress(FileUtils.getPath(this , mImageUri) ,
                    new File(this.getCacheDir() , "temp")));

            compressedUri = Uri.fromFile(file);
            storyImg.setImageURI(compressedUri);
            storyImg.setScaleType(ImageView.ScaleType.CENTER_CROP);




        }
    }


    private void openMultiChoiceTags() {

        AlertDialog.Builder builder = new AlertDialog.Builder(DraftStoryDetailsActivity.this);
        builder.setTitle(getResources().getString(R.string.select_story_type));


        builder.setMultiChoiceItems(tagsItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {


                if (isChecked) {
                    if (mTagsItems.size() != 4) {
                        mTagsItems.add(position);
                    } else {
                        Toast.makeText(DraftStoryDetailsActivity.this,
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

        AlertDialog.Builder builder = new AlertDialog.Builder(DraftStoryDetailsActivity.this);
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


    private void setStoryData() {


        if (storyId != null) {
            reference = reference.child(storyId);

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        StoryModel storyModel = snapshot.getValue(StoryModel.class);

                        assert storyModel != null;

                        storyNameTxt = storyModel.getStoryName();
                        storyDescTxt = storyModel.getStoryDescription();
                        storyTypeTxt = storyModel.getStoryType();
                        storyImageUri = storyModel.getStoryImage();
                        //set data to views
                        storyNameTv.setText(storyNameTxt);
                        storyDescTv.setText(storyDescTxt);
                        storyTypeBtn.setText(storyTypeTxt);
                        Glide.with(getApplicationContext()).load(storyImageUri)
                                .into(storyImg);
                        if (storyModel.getTags().isEmpty()) {
                            tagsTv.setText(getResources().getString(R.string.choose_tags));
                        } else {
                            tagsTv.setText(storyModel.getTags());
                        }

                        reloadStoryData.setVisibility(View.GONE);
                        storyDataLayout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        } else {
            reloadStoryData.setVisibility(View.VISIBLE);
            storyDataLayout.setVisibility(View.GONE);
        }
    }


    private void setChaptersDataInList() {
        reference = reference.child("Chapters");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chapterList.clear();

                if (!snapshot.exists()) {
                    yourChaptersTxt.setVisibility(View.GONE);
                    reloadChapterData.setVisibility(View.GONE);
                } else {
                    yourChaptersTxt.setVisibility(View.VISIBLE);
                    reloadChapterData.setVisibility(View.VISIBLE);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        ChapterModel chapterModel = dataSnapshot.getValue(ChapterModel.class);
                        assert chapterModel != null;
                        if (chapterModel.getChapterImage() != null) {
                            chapterImageUri = chapterModel.getChapterImage();
                        }
                        chapterList.add(chapterModel);

                    }


                    chapterDraftAdapter = new ChapterDraftAdapter(DraftStoryDetailsActivity.this, chapterList, storyImageUri);
                    recyclerView.setAdapter(chapterDraftAdapter);
                    chapterDraftAdapter.notifyDataSetChanged();
                    chapterDraftAdapter.setOnItemClickListener(DraftStoryDetailsActivity.this);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNewChapter() {
        storyId = getIntent().getStringExtra("storyId");
        Intent intent = new Intent(DraftStoryDetailsActivity.this, WriteChapterActivity.class);
        intent.putExtra("storyId", storyId);
        intent.putExtra("fromActivity", "DraftStoryDetails");
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    private void deleteStoryDialog() {


        storyId = getIntent().getStringExtra("storyId");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Drafts").child(storyId);

        assert storyImageUri != null;
        assert chapterImageUri != null;
        final StorageReference storyPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(storyImageUri);

        if (!chapterList.isEmpty()) {
            for (int position = 0; position < chapterList.size(); position++) {
                StorageReference chapterPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(chapterList.get(position).getChapterImage());
                chapterPhotoRef.delete();
            }

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(DraftStoryDetailsActivity.this, R.style.DialogTheme);
        String message = getResources().getString(R.string.delete_story_message);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (MyApplication.isOnline(getApplicationContext())) {
                            Toast.makeText(DraftStoryDetailsActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    storyPhotoRef.delete();
                                    Intent intent = new Intent(DraftStoryDetailsActivity.this, MainPageActivity.class);
                                    intent.putExtra("fragment", "MyNovel");
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                                }
                            });
                        }


                    }
                })
                .setNegativeButton(R.string.no, null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void updateStoryData() {


        storyId = getIntent().getStringExtra("storyId");

        if (MyApplication.isOnline(getApplicationContext())) {
            Toast.makeText(this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        } else {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .child("Drafts").child(storyId);

            if (tagsTv.getText().toString().contains(storyTypeBtn.getText().toString())) {
                reloadStoryData.setVisibility(View.GONE);
                storyDataLayout.setVisibility(View.VISIBLE);
                Toast.makeText(this, getResources().getString(R.string.type_tags_error), Toast.LENGTH_SHORT).show();
            } else {
                if (mImageUri != null) {
                    reloadStoryData.setVisibility(View.VISIBLE);
                    storyDataLayout.setVisibility(View.GONE);


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


                                    reference.child("storyType").setValue(storyTypeBtn.getText().toString());
                                    reference.child("tags").setValue(tagsTv.getText());
                                    reference.child("storyImage").setValue(url).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            storyPhotoRef.delete();
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    reloadStoryData.setVisibility(View.GONE);
                                                    storyDataLayout.setVisibility(View.VISIBLE);
                                                }
                                            }, 1000);
                                        }
                                    });


                                }
                            });

                        }
                    });


                } else {
                    reference.child("storyType").setValue(storyTypeBtn.getText().toString());
                    reference.child("tags").setValue(tagsTv.getText());
                    Toast.makeText(this, getResources().getString(R.string.data_updated), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }



    @Override
    public void onItemClick(int position, List<ChapterModel> list) {


        Intent intent = new Intent(DraftStoryDetailsActivity.this, WriteChapterActivity.class);
        intent.putExtra("storyId", chapterList.get(position).getStoryId());
        intent.putExtra("chapterId", chapterList.get(position).getChapterId());
        intent.putExtra("storyImage", storyImageUri);
        intent.putExtra("chapterImage", chapterList.get(position).getChapterImage());
        intent.putExtra("chapterNo", String.valueOf(list.size()));
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);



    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DraftStoryDetailsActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "MyNovel");
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    private void setChapterCount() {
        final DatabaseReference stReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Drafts").child(storyId);

        DatabaseReference chReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Drafts").child(storyId).child("Chapters");

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