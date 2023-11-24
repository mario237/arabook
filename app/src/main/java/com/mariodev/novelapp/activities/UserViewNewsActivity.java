package com.mariodev.novelapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.NewsModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.Objects;

public class UserViewNewsActivity extends AppCompatActivity {
    SharedPref sharedPref;

    RelativeLayout newsDataLayout, commentsLayout;
    ProgressBar loadNewsData, loadImage;
    ImageView arrowBack, newsImage , deleteNewsIcon;
    TextView newsTitleTv, newsTextTv;
    String newsId , currUserId;
    DatabaseReference newsRef  , deleteNewsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view_news);

        newsId = getIntent().getStringExtra("newsId");

        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        newsDataLayout = findViewById(R.id.newsDataLayout);
        loadNewsData = findViewById(R.id.loadNewsData);
        arrowBack = findViewById(R.id.arrowBack);
        newsTitleTv = findViewById(R.id.newsTitleTv);
        newsImage = findViewById(R.id.newsImage);
        loadImage = findViewById(R.id.loadImage);
        newsTextTv = findViewById(R.id.newsTextTv);
        commentsLayout = findViewById(R.id.commentsLayout);
        deleteNewsIcon = findViewById(R.id.deleteNewsIcon);

        newsTextTv.setClickable(true);
        newsTextTv.setMovementMethod(LinkMovementMethod.getInstance());


        newsRef = FirebaseDatabase.getInstance().getReference("News").child(newsId).child("data");

        deleteNewsRef = FirebaseDatabase.getInstance().getReference("News").child(newsId);

        newsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    NewsModel newsModel = snapshot.getValue(NewsModel.class);

                    assert newsModel != null;
                    newsTitleTv.setText(newsModel.getNewsTitle());

                    Glide.with(getApplicationContext()).load(newsModel.getNewsImage())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    loadImage.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    loadImage.setVisibility(View.GONE);
                                    return false;
                                }
                            }).into(newsImage);

                    newsTextTv.setText(MyApplication.fromHtml(newsModel.getNewsText()));


                    loadNewsData.setVisibility(View.GONE);
                    newsDataLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference("News").child(newsId).child("views").child(currUserId).setValue(currUserId);


        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        commentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToComments();
            }
        });

        if (currUserId.equals(getResources().getString(R.string.admin_id))){
            deleteNewsIcon.setVisibility(View.VISIBLE);
        }

        deleteNewsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNewsDialog();
            }
        });
    }


    private void deleteNewsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(UserViewNewsActivity.this, R.style.DialogTheme);
        String message = getResources().getString(R.string.delete_warning);
        builder.setMessage(message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (MyApplication.isOnline(getApplicationContext())) {
                            Toast.makeText(UserViewNewsActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                        } else {
                            deleteNewsRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
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



    private void goToComments() {
        Intent intent = new Intent(UserViewNewsActivity.this , NewsCommentsActivity.class);
        intent.putExtra("newsId",newsId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UserViewNewsActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "news");
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