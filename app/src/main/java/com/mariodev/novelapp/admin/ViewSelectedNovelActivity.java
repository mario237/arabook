package com.mariodev.novelapp.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;

public class ViewSelectedNovelActivity extends AppCompatActivity {

    TextView storyNameTv , storyTypeTv , storyTagsTv , storyCompletedTv , storyAdultTv;
    ImageView storyCover , deleteStoryIcon;
    ImageButton addDiscoverImg , deleteDiscoverImg;
    SharedPref sharedPref;
    String storyId , storyImageUri;
    DatabaseReference storyRef , chapterReference;
    List<ChapterModel> chapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_selected_novel);

        storyCover = findViewById(R.id.storyCover);
        storyNameTv = findViewById(R.id.storyNameTv);
        storyTypeTv = findViewById(R.id.storyTypeTv);
        storyTagsTv = findViewById(R.id.storyTagsTv);
        storyCompletedTv = findViewById(R.id.storyCompletedTv);
        storyAdultTv = findViewById(R.id.storyAdultTv);
        addDiscoverImg = findViewById(R.id.addDiscoverImg);
        deleteDiscoverImg = findViewById(R.id.deleteDiscoverImg);
        deleteStoryIcon = findViewById(R.id.deleteStoryIcon);

        storyId = getIntent().getStringExtra("adminStoryId");

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories")
                .child(storyId);


        chapterReference = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(storyId);

        chapterList = new ArrayList<>();


        storyRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){

                    StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);

                    assert storyModel != null;
                    Glide.with(ViewSelectedNovelActivity.this)
                            .load(storyModel.getStoryImage()).into(storyCover);

                    storyNameTv.setText(storyModel.getStoryName());

                    storyTypeTv.setText( storyModel.getStoryType());

                    storyTagsTv.setText( storyModel.getTags());


                    storyCompletedTv.setText(String.valueOf(storyModel.isCompleted()));

                    storyAdultTv.setText(String.valueOf( storyModel.isForAdult()));

                    if (storyModel.isDiscover()){
                        addDiscoverImg.setVisibility(View.GONE);
                        deleteDiscoverImg.setVisibility(View.VISIBLE);
                    }else {
                        addDiscoverImg.setVisibility(View.VISIBLE);
                        deleteDiscoverImg.setVisibility(View.GONE);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storyImageUri = getIntent().getStringExtra("storyImageUri");

        setChaptersList();


        addDiscoverImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storyRef.child("data").child("discover").setValue(true);
            }
        });

        deleteDiscoverImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storyRef.child("data").child("discover").setValue(false);
            }
        });

        deleteStoryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStoryDialog(storyId);
            }
        });
    }

    private void setChaptersList() {

        chapterReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chapterList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ChapterModel chapterModel = dataSnapshot.child("data").getValue(ChapterModel.class);
                        assert chapterModel != null;
                        chapterList.add(chapterModel);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void deleteStoryDialog(final String storyId) {

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

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewSelectedNovelActivity.this, R.style.DialogTheme);
        String message = getResources().getString(R.string.delete_story_message);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {

                        if (MyApplication.isOnline(getApplicationContext())) {
                            Toast.makeText(ViewSelectedNovelActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            chapterReference.removeValue();
                            storyReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    storyPhotoRef.delete();
                                  dialogInterface.dismiss();
                                  onBackPressed();

                                }
                            });
                        }


                    }
                })
                .setNegativeButton(R.string.no, null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @Override
    public void onBackPressed() {
        finish();
    }

    private void checkLightMode(){
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }

}