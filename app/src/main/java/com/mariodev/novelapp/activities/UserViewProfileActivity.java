package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.adapters.LatestNovelsAdapter;
import com.mariodev.novelapp.adapters.PoemAdapter;
import com.mariodev.novelapp.adapters.PostAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.models.PostModel;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class UserViewProfileActivity extends AppCompatActivity {
    SharedPref sharedPref;

    RelativeLayout profileMainLayout, userPostsLayout, userPoemsLayout,
            userNovelsLayout, followUserLayout , followingCard , followersCard;
    ProgressBar progressBar;
    ImageView profileImg, backImg, coverImg, followIcon , verifiedImage;
    CardView bioCard;
    TextView usernameTv, userFullNameTv, bioTv, novelNumTv, poemsNumTv, followNumTv, followingNumTv,
            morePostsTv, morePoemsTv, moreNovelsTv, followStateTv;


    RecyclerView userPostsRecycler, userPoemsRecycler, userNovelsRecycler;
    List<PostModel> postModelList;
    List<PoemModel> poemModelList;
    PostAdapter postAdapter;
    PoemAdapter poemAdapter;

    LatestNovelsAdapter latestNovelsAdapter;
    List<StoryModel> storyList;

    //Firebase component
    DatabaseReference reference, novelNumRef, poemRef, postRef, viewPoemRef, storyRef , followingRef , followersRef;


    String userId , currUserId , currUserName, followerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view_profile);


        backImg = findViewById(R.id.backImg);
        coverImg = findViewById(R.id.coverImg);
        profileMainLayout = findViewById(R.id.profileMainLayout);
        progressBar = findViewById(R.id.profileLoad);
        profileImg = findViewById(R.id.profileImg);
        userFullNameTv = findViewById(R.id.userFullNameTv);
        usernameTv = findViewById(R.id.username);
        bioCard = findViewById(R.id.bioCard);
        bioTv = findViewById(R.id.bioTv);
        novelNumTv = findViewById(R.id.novelNum);
        poemsNumTv = findViewById(R.id.poemsNum);
        followNumTv = findViewById(R.id.followNum);
        followingNumTv = findViewById(R.id.followingNum);
        userPostsLayout = findViewById(R.id.userPostsLayout);
        morePostsTv = findViewById(R.id.morePostsTv);
        userPostsRecycler = findViewById(R.id.userPostsRecycler);
        userPoemsLayout = findViewById(R.id.userPoemsLayout);
        morePoemsTv = findViewById(R.id.morePoemsTv);
        userPoemsRecycler = findViewById(R.id.userPoemsRecycler);
        userNovelsLayout = findViewById(R.id.userNovelsLayout);
        userNovelsRecycler = findViewById(R.id.userNovelsRecycler);
        moreNovelsTv = findViewById(R.id.moreNovelsTv);
        followUserLayout = findViewById(R.id.followUserLayout);
        followIcon = findViewById(R.id.followIcon);
        followStateTv = findViewById(R.id.followStateTv);
        verifiedImage = findViewById(R.id.verifiedImage);
        followingCard = findViewById(R.id.followingCard);
        followersCard = findViewById(R.id.followersCard);

        userId = getIntent().getStringExtra("userId");
        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        novelNumRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories");

        poemRef = FirebaseDatabase.getInstance().getReference("Poems");


        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        viewPoemRef = FirebaseDatabase.getInstance().getReference("Poems");


        followersRef = FirebaseDatabase.getInstance().getReference("Follow").child(userId).child("followers");
        followingRef = FirebaseDatabase.getInstance().getReference("Follow").child(userId).child("following");



        followerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        userPostsRecycler.setHasFixedSize(true);
        userPostsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        userNovelsRecycler.setHasFixedSize(true);
        userNovelsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        userPoemsRecycler.setHasFixedSize(true);
        userPoemsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        postModelList = new ArrayList<>();
        storyList = new ArrayList<>();
        poemModelList = new ArrayList<>();


        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), userId)) {
                            PostModel postModel = dataSnapshot.child("data").getValue(PostModel.class);
                            postModelList.add(postModel);
                        }

                    }
                    if (postModelList.size() >= 10) {
                        morePostsTv.setVisibility(View.VISIBLE);
                    }

                    postAdapter = new PostAdapter(UserViewProfileActivity.this, postModelList);
                    userPostsRecycler.setAdapter(postAdapter);
                    postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<PostModel> postModelList) {
                            Intent intent = new Intent(UserViewProfileActivity.this, ViewPostActivity.class);
                            intent.putExtra("postId", postModelList.get(position).getPostId());
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                        }
                    });
                    postAdapter.notifyDataSetChanged();

                    if (!postModelList.isEmpty()) {
                        userPostsLayout.setVisibility(View.VISIBLE);
                        userPostsRecycler.setVisibility(View.VISIBLE);
                    }else {
                        userPostsLayout.setVisibility(View.GONE);
                        userPostsRecycler.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (Objects.equals(dataSnapshot.child("data").child("storyWriter").getValue(String.class), userId)) {
                        StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                        if (storyModel.getStoryWriter()!= null){
                            storyList.add(storyModel);
                        }
                    }
                }
                Collections.reverse(storyList);

                if (storyList.size() >= 10) {
                    moreNovelsTv.setVisibility(View.VISIBLE);
                }

                latestNovelsAdapter = new LatestNovelsAdapter(UserViewProfileActivity.this, storyList);
                userNovelsRecycler.setAdapter(latestNovelsAdapter);

                latestNovelsAdapter.setOnItemClickListener(new LatestNovelsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position, List<StoryModel> storyModelList) {
                        Intent intent = new Intent(UserViewProfileActivity.this, UserViewStoryActivity.class);
                        intent.putExtra("storyId", storyModelList.get(position).getStoryId());
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    }
                });

                latestNovelsAdapter.notifyDataSetChanged();

                if (!storyList.isEmpty()) {
                    userNovelsLayout.setVisibility(View.VISIBLE);
                    userNovelsRecycler.setVisibility(View.VISIBLE);
                }else {
                    userNovelsLayout.setVisibility(View.GONE);
                    userNovelsRecycler.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewPoemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    poemModelList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), userId)) {
                            PoemModel poemModel = dataSnapshot.child("data").getValue(PoemModel.class);
                            poemModelList.add(poemModel);
                        }

                    }
                    if (poemModelList.size() >= 10) {
                        morePoemsTv.setVisibility(View.VISIBLE);
                    }

                    poemAdapter = new PoemAdapter(UserViewProfileActivity.this, poemModelList);
                    userPoemsRecycler.setAdapter(poemAdapter);
                    poemAdapter.setOnItemClickListener(new PoemAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<PoemModel> poemModelList) {
                            Intent intent = new Intent(UserViewProfileActivity.this, ViewPoemActivity.class);
                            intent.putExtra("poemId", poemModelList.get(position).getPoemId());
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                        }
                    });
                    poemAdapter.notifyDataSetChanged();


                    if (!poemModelList.isEmpty()) {
                        userPoemsLayout.setVisibility(View.VISIBLE);
                        userPoemsRecycler.setVisibility(View.VISIBLE);
                    }else {
                        userPoemsLayout.setVisibility(View.GONE);
                        userPoemsRecycler.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        getData();


        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        moreNovelsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMore("books");
            }
        });
        morePoemsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMore("poems");
            }
        });
        morePostsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMore("posts");
            }
        });


        followUserLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (followStateTv.getText().toString().equals(getResources().getString(R.string.follow))){
                   FirebaseDatabase.getInstance().getReference("Follow").child(currUserId)
                           .child("following").child(userId).setValue(true);
                   FirebaseDatabase.getInstance().getReference("Follow").child(userId)
                           .child("followers").child(currUserId).setValue(true);
               }else {
                   FirebaseDatabase.getInstance().getReference("Follow").child(currUserId)
                           .child("following").child(userId).removeValue();
                   FirebaseDatabase.getInstance().getReference("Follow").child(userId)
                           .child("followers").child(currUserId).removeValue();
               }
            }
        });

        isFollowing();

        followingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(UserViewProfileActivity.this , ViewFollowUsersActivity.class);
               intent.putExtra("userId" , userId);
               intent.putExtra("which","following");
               startActivity(intent);
            }
        });

        followersCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserViewProfileActivity.this , ViewFollowUsersActivity.class);
                intent.putExtra("userId" , userId);
                intent.putExtra("which","followers");
                startActivity(intent);
            }
        });

    }

    private void isFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(currUserId).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userId).exists()){
                    followStateTv.setText(getResources().getString(R.string.followed));
                    followIcon.setImageResource(R.drawable.ic_person_check);
                }else {
                    followStateTv.setText(getResources().getString(R.string.follow));
                    followIcon.setImageResource(R.drawable.ic_person_add);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void goToMore(String moreWhat) {
        Intent intent = new Intent(UserViewProfileActivity.this, MoreProfileActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("from", "viewProfile");
        intent.putExtra("moreWhat", moreWhat);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    private void setUserCoverImage(Integer number) {
        if (number == 1) {
            coverImg.setImageResource(R.drawable.cover1);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
        } else if (number == 2) {
            coverImg.setImageResource(R.drawable.cover2);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
        } else if (number == 3) {
            coverImg.setImageResource(R.drawable.cover3);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));
        } else if (number == 4) {
            coverImg.setImageResource(R.drawable.cover4);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));
        } else if (number == 5) {
            coverImg.setImageResource(R.drawable.cover5);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));

        } else if (number == 6) {
            coverImg.setImageResource(R.drawable.cover6);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
        } else if (number == 7) {
            coverImg.setImageResource(R.drawable.cover7);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));
        } else if (number == 8) {
            coverImg.setImageResource(R.drawable.cover8);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysWhite));

        } else if (number == 9) {
            coverImg.setImageResource(R.drawable.cover9);
            userFullNameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(getResources().getColor(R.color.colorAlwaysBlack));
        }
    }

    private void getData() {
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if (snapshot.exists()) {

                    UserModel userModel = snapshot.getValue(UserModel.class);

                    //get data from firebase and store it to string
                    assert userModel != null;
                    currUserName = "@" + userModel.getUsername();


                    //set data to views
                    usernameTv.setText(currUserName);

                    if (userModel.getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        followUserLayout.setVisibility(View.GONE);
                    }

                    if (userModel.getUserProfileImg() != null) {
                        Glide.with(getApplicationContext()).load(userModel.getUserProfileImg()).into(profileImg);
                    }


                    if (userModel.getUserFullName() != null) {
                        userFullNameTv.setVisibility(View.VISIBLE);
                        userFullNameTv.setText(userModel.getUserFullName());
                    } else {
                        userFullNameTv.setVisibility(View.VISIBLE);
                        userFullNameTv.setText(userModel.getUsername());
                    }

                    if (snapshot.hasChild("isAdmin")){
                        verifiedImage.setVisibility(View.VISIBLE);
                    }

                    if (userModel.getUserBio() != null) {
                        if (!userModel.getUserBio().equals("")) {
                            bioCard.setVisibility(View.VISIBLE);
                            bioTv.setText(userModel.getUserBio());
                        }
                    }

                    if (userModel.getCoverNumber() == null) {
                        coverImg.setImageResource(R.drawable.cover1);

                    } else {
                        setUserCoverImage(userModel.getCoverNumber());
                    }

                    profileMainLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserViewProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        novelNumRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = 0;

                if (dataSnapshot.exists()){

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);
                        assert storyModel != null;

                        if (storyModel.getStoryWriter()!= null && storyModel.getStoryWriter().equals(userId)){
                            counter++;
                        }
                    }

                }

                novelNumTv.setText(String.valueOf(counter));




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;

                if (snapshot.exists()){
                    counter = (int) snapshot.getChildrenCount();
                }


                followNumTv.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;

                if (snapshot.exists()) {
                    counter = (int) snapshot.getChildrenCount();
                }

                followingNumTv.setText(String.valueOf(counter));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        poemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), userId)) {
                            count++;
                        }
                    }


                }
                poemsNumTv.setText(String.valueOf(count));
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
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!this.isFinishing()) {
            Glide.with(this).pauseRequests();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }



}