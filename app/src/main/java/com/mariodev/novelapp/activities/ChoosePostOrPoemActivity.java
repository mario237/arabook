package com.mariodev.novelapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

public class ChoosePostOrPoemActivity extends AppCompatActivity {
    SharedPref sharedPref;

    ImageView closeImg;
    ConstraintLayout postLayout,poemLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_post_or_poem);

        closeImg = findViewById(R.id.closeImg);
        postLayout = findViewById(R.id.postLayout);
        poemLayout = findViewById(R.id.poemLayout);


        closeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPostOrPoem("post");
            }
        });

        poemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPostOrPoem("poem");
            }
        });
    }

    private void goToPostOrPoem(String s) {
        Intent intent = new Intent(ChoosePostOrPoemActivity.this, PostOrPoemActivity.class);
        intent.putExtra("whichOne", s);
        startActivity(intent);
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
    public void onBackPressed() {
        Intent intent = new Intent(ChoosePostOrPoemActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "Profile");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }


}