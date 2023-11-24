package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

public class SplashActivity extends AppCompatActivity {

    public static final int splash_time_out = 2000;
    SharedPref sharedPref;
    //Animations
    Animation blinkAnimation, bottomAnimation;
    //init views
    TextView arabookTxt;
    ImageView aLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        blinkAnimation = AnimationUtils.loadAnimation(this, R.anim.blink);


        aLogo = findViewById(R.id.splashImg);
        arabookTxt = findViewById(R.id.splashTxt);

        aLogo.setAnimation(blinkAnimation);
        arabookTxt.setAnimation(bottomAnimation);



        new Handler().postDelayed(new Runnable() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                    showVersionDialog();
                }else {
                    Intent login_intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(login_intent);
                    finish();
                }

        }
        }, splash_time_out);
    }

    private void showVersionDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(SplashActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.version_dialog));
        dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        AlertDialog versionDialog = dialog.create();
        versionDialog.show();
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