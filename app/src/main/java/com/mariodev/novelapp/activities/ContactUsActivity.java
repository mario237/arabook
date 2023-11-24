package com.mariodev.novelapp.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.List;

public class ContactUsActivity extends AppCompatActivity implements View.OnClickListener {
    SharedPref sharedPref;

    ImageView backImg , facebookIcon , instagramIcon , gmailIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        backImg = findViewById(R.id.backImg);
        facebookIcon = findViewById(R.id.facebookIcon);
        instagramIcon = findViewById(R.id.instagramIcon);
        gmailIcon = findViewById(R.id.gmailIcon);


        backImg.setOnClickListener(this);
        facebookIcon.setOnClickListener(this);
        instagramIcon.setOnClickListener(this);
        gmailIcon.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backImg : onBackPressed(); break;

            case R.id.facebookIcon : openFacebookPage(); break;

            case R.id.instagramIcon : openInstagramPage(); break;

            case R.id.gmailIcon : openGmailApp(); break;
        }
    }


    private void openFacebookPage() {
        final String urlFb = "fb://page/"+"111323434049481";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlFb));

        // If a Facebook app is installed, use it. Otherwise, launch
        // a browser
        final PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() == 0) {
            final String urlBrowser = "https://www.facebook.com/"+"Arabook-111323434049481/";
            intent.setData(Uri.parse(urlBrowser));
        }

        startActivity(intent);
    }

    private void openInstagramPage() {
        Uri uri = Uri.parse("https://instagram.com/arabook_?igshid=1x3czkyrx1s7e");
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage("com.instagram.android");

        try {
            startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/arabook_?igshid=1x3czkyrx1s7e")));
        }
    }


    private void openGmailApp() {
        Intent intent=new Intent(Intent.ACTION_SEND);
        String[] recipients={"arabookapp@gmail.com"};
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.setType("text/html");
        intent.setPackage("com.google.android.gm");
        startActivity(Intent.createChooser(intent, "Send mail"));
    }


    private void checkLightMode() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }


}