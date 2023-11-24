package com.mariodev.novelapp.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

public class AdminActivity extends AppCompatActivity {

    Button viewAllNovelsBtn , addNewsBtn , deleteStoryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        viewAllNovelsBtn = findViewById(R.id.viewAllNovelsBtn);

        addNewsBtn = findViewById(R.id.addNewsBtn);

        deleteStoryBtn = findViewById(R.id.deleteStoryBtn);


        viewAllNovelsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this , ViewAllNovelsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


        addNewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this , AddNewsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        deleteStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this , ViewAllNovelsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}

