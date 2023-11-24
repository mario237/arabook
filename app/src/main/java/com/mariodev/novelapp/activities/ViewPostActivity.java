package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.PostModel;
import com.mariodev.novelapp.models.UserModel;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.util.Objects;

@SuppressWarnings("deprecation")
public class ViewPostActivity extends AppCompatActivity {
    SharedPref sharedPref;

    String postId , userId , currUserId;
    ImageView closePostImg , profileImg , deletePostImg;
    TextView  usernameTv , postTextTv , postDateTimeTv , likePostCounterTv , commentPostCounterTv;
    ImageButton commentPostBtn;
    SparkButton likePostBtn;
    RelativeLayout postLayout , userLayout;
    ProgressBar loadPost;
    DatabaseReference  postRef , userRef , postLikeRef , allPostCommentsRef , postViewRef;
    Boolean clickChecker = false;
    int countLike = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);


        postId = getIntent().getStringExtra("postId");

        postLayout = findViewById(R.id.postLayout);
        loadPost = findViewById(R.id.loadPost);
        closePostImg = findViewById(R.id.closePostImg);
        profileImg = findViewById(R.id.profileImg);
        usernameTv = findViewById(R.id.usernameTv);
        postTextTv = findViewById(R.id.postTextTv);
        postDateTimeTv = findViewById(R.id.postDateTimeTv);
        likePostBtn = findViewById(R.id.likePostBtn);
        likePostCounterTv = findViewById(R.id.likePostCounterTv);
        commentPostBtn = findViewById(R.id.commentPostBtn);
        commentPostCounterTv = findViewById(R.id.commentPostCounterTv);
        deletePostImg = findViewById(R.id.deletePostImg);
        userLayout  = findViewById(R.id.userLayout);

        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);


        postLikeRef = FirebaseDatabase.getInstance().getReference("PostsLikes").child(postId);

        postViewRef = FirebaseDatabase.getInstance().getReference("PostsViews").child(postId);



        setPostViews();

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel postModel = snapshot.child("data").getValue(PostModel.class);

                    assert postModel != null;
                    postTextTv.setText(postModel.getPostText());
                    postDateTimeTv.setText(postModel.getDateTime());
                    userId = postModel.getUserId();

                    if (userId.equals(currUserId) ||
                            currUserId.equals(getResources().getString(R.string.admin_id))){
                        deletePostImg.setVisibility(View.VISIBLE);
                    }

                    userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            assert userModel != null;
                            usernameTv.setText(userModel.getUsername());

                            if (userModel.getUserProfileImg() != null){
                                Glide.with(getApplicationContext()).load(userModel.getUserProfileImg()).into(profileImg);
                            }

                            loadPost.setVisibility(View.GONE);
                            postLayout.setVisibility(View.VISIBLE);
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

        likePostBtn.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                clickChecker = true;

                postLikeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (clickChecker.equals(true)) {
                            if (dataSnapshot.hasChild(currUserId)) {
                                postLikeRef.child(currUserId).removeValue();
                            } else {
                                postLikeRef.child(currUserId).setValue(currUserId);

                            }
                            clickChecker = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });


        closePostImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewPostActivity.this , PostCommentsActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);
            }
        });

        deletePostImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePost();
            }
        });

        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewPostActivity.this , UserViewProfileActivity.class);
                intent.putExtra("userId",userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
        });

        setPostLikesCount();
        setPostCommentsCount();
    }



    private void setPostViews() {
        postViewRef.child(currUserId).setValue(currUserId);
    }


    private void deletePost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewPostActivity.this , R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.delete_warning));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                postRef.removeValue();
                postLikeRef.removeValue();
                postViewRef.removeValue();
                dialogInterface.dismiss();
                onBackPressed();

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel) , null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setPostLikesCount() {
        postLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currUserId)) {
                    countLike = (int) snapshot.getChildrenCount();
                    likePostBtn.setChecked(true);
                    likePostCounterTv.setText(String.valueOf(countLike));
                    likePostCounterTv.setTextColor(getResources().getColor(R.color.colorRed));

                } else {
                    countLike = (int) snapshot.getChildrenCount();
                    likePostBtn.setChecked(false);
                    likePostCounterTv.setText(String.valueOf(countLike));
                    likePostCounterTv.setTextColor(getResources().getColor(R.color.colorWhite));
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPostCommentsCount() {
        allPostCommentsRef = postRef.child("comments");

        allPostCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                    count  = (int) snapshot.getChildrenCount();
                }
                commentPostCounterTv.setText(String.valueOf(count));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
       if (getIntent().getStringExtra("from")!=null){
           Intent intent = new Intent(ViewPostActivity.this , MainPageActivity.class);
           intent.putExtra("fragment" , "Profile");
           intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
           startActivity(intent);
       }
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