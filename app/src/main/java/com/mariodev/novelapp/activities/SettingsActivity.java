package com.mariodev.novelapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {


    CardView profileEditCard, lightModeCard, termsCard, callUsCard;
    TextView modeTv;
    Button logoutBtn;

    int selectedMode;

    SharedPref sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileEditCard = findViewById(R.id.profileEditCard);
        lightModeCard = findViewById(R.id.modeCard);
        termsCard = findViewById(R.id.termsCard);
        callUsCard = findViewById(R.id.callUsCard);
        modeTv = findViewById(R.id.modeStateTv);
        logoutBtn = findViewById(R.id.logoutBtn);


        if (sharedPref.loadLightModeState()) {
            modeTv.setText(getResources().getString(R.string.light_mode));
            selectedMode = 0;
        } else {
            modeTv.setText(getResources().getString(R.string.dark_mode));
            selectedMode = 1;

        }


        profileEditCard.setOnClickListener(this);
        lightModeCard.setOnClickListener(this);
        termsCard.setOnClickListener(this);
        callUsCard.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingsActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "Profile");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profileEditCard:
                goToEditProfile();
                break;

            case R.id.modeCard:
                openChooseTypeDialog();
                break;

            case R.id.termsCard:
                goToTerms();
                break;

            case R.id.callUsCard:
                goToContactUs();
                break;

            case R.id.logoutBtn:
                logout();
                break;


        }
    }


    private void goToContactUs() {
        startActivity(new Intent(getApplicationContext(), ContactUsActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void goToTerms() {
        String url = "https://docs.google.com/document/d/1Aexr9sbx9SQ6Ffp0wrDhPlngV2l4GrUYHE8BUhRJZp4/edit?usp=drivesdk";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    private void goToEditProfile() {
        startActivity(new Intent(getApplicationContext(), EditProfileActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }


    private void openChooseTypeDialog() {
        final CharSequence[] modesItems =
                {getResources().getString(R.string.light_mode),
                        getResources().getString(R.string.dark_mode)};

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setCancelable(true)
                .setSingleChoiceItems(modesItems, selectedMode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) {
                            sharedPref.setLightModeState(true);
                        } else {
                            sharedPref.setLightModeState(false);
                        }
                        dialogInterface.dismiss();
                        recreate();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();


    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getApplicationContext()), gso);
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().signOut(); // very important if you are using firebase.
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

                }
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "ar"));
    }


}