package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.Objects;

public class UserViewStoryActivity extends AppCompatActivity {

    SharedPref sharedPref;
    ScrollView storyViewLayout;
    ConstraintLayout userImageLayout;
    Button readNowBtn;
    ProgressBar loadStoryData;
    ImageView arrowBack, storyImg, showDescImg, userImg , bookmarkImg , verifiedImage;
    TextView storyNameTv, storyTypeTv, storyCompletedTv, storyVoteTv, chapterCountTv, viewsCountTv, commentCountTv, usernameTv,
            followersCountTv, storyDescTv, tagsTv, publishDaysTv, publishSecurityTv , libraryStateTv;
    LinearLayout tagsLayout, daysLayout;
    RelativeLayout reviewsLayout , descLayout;
    DatabaseReference storyRef, storyVoteRef , chapterRef, chapterViewRef,
            writerRef, chapterVoteRef , libraryRef;
    String storyId, userId , writerId;
    Boolean clickChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view_story);

        storyId = getIntent().getStringExtra("storyId");


        storyViewLayout = findViewById(R.id.storyViewLayout);
        readNowBtn = findViewById(R.id.readNowBtn);
        loadStoryData = findViewById(R.id.loadStoryData);
        arrowBack = findViewById(R.id.arrowBack);
        userImageLayout = findViewById(R.id.userImageLayout);
        userImg = findViewById(R.id.userImg);
        usernameTv = findViewById(R.id.usernameTv);
        storyImg = findViewById(R.id.storyImg);
        storyNameTv = findViewById(R.id.storyNameTv);
        storyTypeTv = findViewById(R.id.storyTypeTv);
        storyCompletedTv = findViewById(R.id.storyCompletedTv);
        storyVoteTv = findViewById(R.id.storyVoteTv);
        chapterCountTv = findViewById(R.id.chapterCountTv);
        viewsCountTv = findViewById(R.id.viewsCountTv);
        followersCountTv = findViewById(R.id.followersCountTv);
        commentCountTv = findViewById(R.id.commentCountTv);
        storyDescTv = findViewById(R.id.storyDescTv);
        showDescImg = findViewById(R.id.showDescImg);
        tagsTv = findViewById(R.id.tagsTv);
        publishDaysTv = findViewById(R.id.publishDaysTv);
        publishSecurityTv = findViewById(R.id.publishSecurityTv);
        tagsLayout = findViewById(R.id.tagsLayout);
        daysLayout = findViewById(R.id.daysLayout);
        reviewsLayout = findViewById(R.id.reviewsLayout);
        bookmarkImg = findViewById(R.id.bookmarkImg);
        libraryStateTv = findViewById(R.id.libraryStateTv);
        verifiedImage = findViewById(R.id.verifiedImage);
        descLayout = findViewById(R.id.descLayout);

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories")
                .child(storyId);


        storyVoteRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories")
                .child(storyId).child("votes");

        chapterRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(storyId);

        writerRef = FirebaseDatabase.getInstance().getReference("Users");

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        storyRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              if (snapshot.exists()){
                  StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);
                  assert storyModel != null;

                  if (storyModel.getStoryWriter()!=null){
                      writerId = storyModel.getStoryWriter();
                  }

                  Glide.with(getApplicationContext())
                          .load(storyModel.getStoryImage())
                          .into(storyImg);


                  storyNameTv.setText(storyModel.getStoryName());
                  storyTypeTv.setText(storyModel.getStoryType());
                  if (storyModel.isCompleted()) {
                      storyCompletedTv.setText(getResources().getString(R.string.complete));
                  } else {
                      storyCompletedTv.setText(getResources().getString(R.string.not_completed));
                  }


                  chapterCountTv.setText(String.valueOf(storyModel.getChaptersCount()));


                  viewsCountTv.setText(String.valueOf(storyModel.getStoryViews()));

                  followersCountTv.setText(String.valueOf(storyModel.getStoryFollowers()));

                  storyVoteTv.setText(String.valueOf(storyModel.getStoryVotes()));

                  commentCountTv.setText(String.valueOf(storyModel.getStoryComments()));

                  storyDescTv.setText(storyModel.getStoryDescription());

                  if (!storyModel.getTags().equals("")) {
                      tagsLayout.setVisibility(View.VISIBLE);
                      tagsTv.setText(storyModel.getTags());
                  }

                  if (!storyModel.getDays().equals("")) {
                      daysLayout.setVisibility(View.VISIBLE);
                      publishDaysTv.setText(storyModel.getDays());
                  }

                  if (!storyModel.getStorySecurity().equals("")) {
                      publishSecurityTv.setVisibility(View.VISIBLE);
                      publishSecurityTv.setText("\u00a9" + " " + getResources().getString(R.string.security_view));
                  }

                  writerRef = writerRef.child(storyModel.getStoryWriter());

                  writerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {

                          if (snapshot.exists()) {
                              UserModel userModel = snapshot.getValue(UserModel.class);

                              assert userModel != null;
                              if (userModel.getUserProfileImg() != null) {
                                  Glide.with(getApplicationContext())
                                          .load(userModel.getUserProfileImg())
                                          .diskCacheStrategy(DiskCacheStrategy.DATA)
                                          .into(userImg);
                              }

                              if (snapshot.hasChild("isAdmin")){
                                  verifiedImage.setVisibility(View.VISIBLE);
                              }
                              usernameTv.setText(userModel.getUsername());
                          }

                          loadStoryData.setVisibility(View.GONE);
                          storyViewLayout.setVisibility(View.VISIBLE);
                          readNowBtn.setVisibility(View.VISIBLE);
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {

                      }
                  });

              }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        readNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserViewStoryActivity.this, UserViewChapterActivity.class);
                intent.putExtra("storyId", storyId);
                intent.putExtra("storyName", storyNameTv.getText().toString());
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

        userImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserViewStoryActivity.this, UserViewProfileActivity.class);
                intent.putExtra("from", "viewStory");
                intent.putExtra("storyId",storyId);
                intent.putExtra("userId",writerId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        setStoryChaptersCount();
        setStoryViews();
        setStoryVotes();
        setStoryComments();
        setStoryFollowers();

        descLayout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {

                if (showDescImg.getTag().equals("down")) {
                    showDescImg.setTag("up");
                    showDescImg.setImageResource(R.drawable.ic_drop_up);
                    storyDescTv.setVisibility(View.VISIBLE);

                } else {
                    showDescImg.setTag("down");
                    showDescImg.setImageResource(R.drawable.ic_drop_down);
                    storyDescTv.setVisibility(View.GONE);


                }
            }
        });

        reviewsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserViewStoryActivity.this , UserReviewsActivity.class);
                intent.putExtra("storyId",storyId);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
            }
        });

        bookmarkImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickChecker = true;

                libraryRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories")
                        .child(storyId).child("Users");

                libraryRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (clickChecker.equals(true)) {
                            if (dataSnapshot.hasChild(userId)) {

                                libraryRef.child(userId).removeValue();
                            } else {
                                libraryRef.child(userId).setValue(true);

                            }
                            clickChecker = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        checkLibraryState();

        if (getIntent().getStringExtra("from")!=null &&
                Objects.equals(getIntent().getStringExtra("from"), "library")){
            chapterRef.keepSynced(true);
            chapterVoteRef.keepSynced(true);
            chapterRef.keepSynced(true);
            storyRef.keepSynced(true);
            writerRef.keepSynced(true);
            storyVoteRef.keepSynced(true);
        }
    }


    private void checkLibraryState(){
        libraryRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories")
                .child(storyId).child("Users");

        libraryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(userId)){
                        bookmarkImg.setImageResource(R.drawable.ic_bookmark);
                        libraryStateTv.setText(getResources().getString(R.string.added_to_library));
                    }else {
                        bookmarkImg.setImageResource(R.drawable.ic_bookmark_border);
                        libraryStateTv.setText(getResources().getString(R.string.add_to_library));
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setStoryFollowers() {
        chapterVoteRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories")
                .child(storyId);

        chapterVoteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countFollowers = 0;

                if (snapshot.exists()) {

                    StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);

                    assert storyModel != null;
                    if ( storyModel.getChaptersCount() != 0){
                        countFollowers = storyModel.getStoryViews() / storyModel.getChaptersCount();
                    }

                    storyRef.child("data").child("storyFollowers").setValue(countFollowers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setStoryChaptersCount() {

        chapterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countChapters;

                if (snapshot.exists()) {

                    //StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);

                    countChapters = (int) snapshot.getChildrenCount();

                    storyRef.child("data").child("chaptersCount").setValue(countChapters);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setStoryVotes() {
        chapterVoteRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(storyId);

        chapterVoteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countVote = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        int count = (int) dataSnapshot.child("votes").getChildrenCount();
                        countVote += count;
                    }
                    storyRef.child("data").child("storyVotes").setValue(countVote);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setStoryComments() {
        chapterVoteRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(storyId);

        chapterVoteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countComments = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        int count = (int) dataSnapshot.child("comments").getChildrenCount();
                        countComments += count;

                    }
                    storyRef.child("data").child("storyComments").setValue(countComments);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setStoryViews() {

        chapterViewRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(storyId);

        chapterViewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sum = 0;

                if (snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        int count = (int) dataSnapshot.child("views").getChildrenCount();
                        sum += count;

                    }
                    storyRef.child("data").child("storyViews").setValue(sum);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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