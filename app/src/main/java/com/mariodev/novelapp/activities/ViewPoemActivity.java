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
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.models.UserModel;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.util.Objects;

@SuppressWarnings("deprecation")
public class ViewPoemActivity extends AppCompatActivity {
    SharedPref sharedPref;

    String poemId , userId , currUserId;
    ImageView closePoemImg , profileImg , deletePoemImg;
    TextView poemTitleTv , usernameTv , poemTextTv , poemDateTimeTv , likePoemCounterTv , commentPoemCounterTv;
    ImageButton commentPoemBtn;
    SparkButton likePoemBtn;
    RelativeLayout poemLayout , userLayout;
    ProgressBar loadPoem;
    DatabaseReference  poemRef , userRef , poemLikeRef , allPoemCommentsRef , poemViewRef;
    Boolean clickChecker = false;
    int countLike = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_poem);

        poemId = getIntent().getStringExtra("poemId");

        poemLayout = findViewById(R.id.poemLayout);
        loadPoem = findViewById(R.id.loadPoem);
        closePoemImg = findViewById(R.id.closePoemImg);
        poemTitleTv = findViewById(R.id.poemTitleTv);
        profileImg = findViewById(R.id.profileImg);
        usernameTv = findViewById(R.id.usernameTv);
        poemTextTv = findViewById(R.id.poemTextTv);
        poemDateTimeTv = findViewById(R.id.poemDateTimeTv);
        likePoemBtn = findViewById(R.id.likePoemBtn);
        likePoemCounterTv = findViewById(R.id.likePoemCounterTv);
        commentPoemBtn = findViewById(R.id.commentPoemBtn);
        commentPoemCounterTv = findViewById(R.id.commentPoemCounterTv);
        deletePoemImg = findViewById(R.id.deletePoemImg);
        userLayout = findViewById(R.id.userLayout);

        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        poemRef = FirebaseDatabase.getInstance().getReference("Poems").child(poemId);


        poemLikeRef = FirebaseDatabase.getInstance().getReference("PoemsLikes").child(poemId);

        poemViewRef = FirebaseDatabase.getInstance().getReference("PoemsViews").child(poemId);

        setPostViews();

        poemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    final PoemModel poemModel = snapshot.child("data").getValue(PoemModel.class);

                    assert poemModel != null;
                    poemTitleTv.setText(poemModel.getPoemTitle());
                    poemTextTv.setText(poemModel.getPoemText());
                    poemDateTimeTv.setText(poemModel.getDateTime());
                    userId = poemModel.getUserId();

                    if (userId.equals(currUserId)||
                            currUserId.equals(getResources().getString(R.string.admin_id))){
                        deletePoemImg.setVisibility(View.VISIBLE);
                    }


                    userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            assert userModel != null;
                            usernameTv.setText(userModel.getUsername());

                            if (userModel.getUserProfileImg() != null){
                                Glide.with(getApplicationContext()).load(userModel.getUserProfileImg())
                                        .into(profileImg);
                            }

                            loadPoem.setVisibility(View.GONE);
                            poemLayout.setVisibility(View.VISIBLE);
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


        likePoemBtn.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                clickChecker = true;

                poemLikeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (clickChecker.equals(true)) {
                            if (dataSnapshot.hasChild(currUserId)) {
                                poemLikeRef.child(currUserId).removeValue();
                            } else {
                                poemLikeRef.child(currUserId).setValue(currUserId);

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


        closePoemImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        commentPoemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewPoemActivity.this , PoemCommentsActivity.class);
                intent.putExtra("poemId",poemId);
                startActivity(intent);
            }
        });

        deletePoemImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePoem();
            }
        });
        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewPoemActivity.this , UserViewProfileActivity.class);
                intent.putExtra("userId",userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        getPoemLikesCount();
        setPoemCommentsCount();
    }

    private void deletePoem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewPoemActivity.this , R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.delete_warning));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                poemRef.removeValue();
                poemLikeRef.removeValue();
                poemViewRef.removeValue();
                dialogInterface.dismiss();
                onBackPressed();

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel) , null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setPostViews() {
        poemViewRef.child(currUserId).setValue(currUserId);
    }

    private void getPoemLikesCount() {

        poemLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currUserId)) {
                    countLike = (int) snapshot.getChildrenCount();
                    likePoemBtn.setChecked(true);
                    likePoemCounterTv.setText(String.valueOf(countLike));
                    likePoemCounterTv.setTextColor(getResources().getColor(R.color.colorRed));

                } else {
                    countLike = (int) snapshot.getChildrenCount();
                    likePoemBtn.setChecked(false);
                    likePoemCounterTv.setText(String.valueOf(countLike));
                    likePoemCounterTv.setTextColor(getResources().getColor(R.color.colorWhite));
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPoemCommentsCount() {
        allPoemCommentsRef = poemRef.child("comments");

        allPoemCommentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.exists()) {
                      count  = (int) snapshot.getChildrenCount();
                }
                commentPoemCounterTv.setText(String.valueOf(count));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        if (getIntent().getStringExtra("from")!=null){
            Intent intent = new Intent(ViewPoemActivity.this , MainPageActivity.class);
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