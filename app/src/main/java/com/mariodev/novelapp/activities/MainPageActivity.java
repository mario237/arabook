package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.fragments.HomeFragment;
import com.mariodev.novelapp.fragments.LibraryFragment;
import com.mariodev.novelapp.fragments.MyNovelFragment;
import com.mariodev.novelapp.fragments.NewsFragment;
import com.mariodev.novelapp.fragments.ProfileFragment;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.Objects;

public class MainPageActivity extends AppCompatActivity {

    SharedPref sharedPref;

    public BottomNavigationView bottomNavigationView;

    private long lastPressedTime;
    private static final int PERIOD = 2000;

    View notificationsBadge;
    BottomNavigationItemView itemView;

    DatabaseReference newsRef;
    String currUserId;
    private int REQUEST_CODE = 11;

    ReviewManager reviewManager;
    ReviewInfo reviewInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MyApplication.setLocale(this);
        checkLightMode();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        bottomNavigationView = findViewById(R.id.nav_buttons);


        String fragmentIntent = getIntent().getStringExtra("fragment");


        if (fragmentIntent != null && fragmentIntent.equals("MyNovel")) {
            setFragment(new MyNovelFragment());
            bottomNavigationView.setSelectedItemId(R.id.myNovelFragment);
        }
        else if (fragmentIntent != null && fragmentIntent.equals("Profile")) {
            setFragment(new ProfileFragment());
            bottomNavigationView.setSelectedItemId(R.id.profileFragment);
        }
        else if (fragmentIntent != null && fragmentIntent.equals("Home")) {
            setFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.homeFragment);
        }
        else if (fragmentIntent != null && fragmentIntent.equals("library")) {
            setFragment(new LibraryFragment());
            bottomNavigationView.setSelectedItemId(R.id.libraryFragment);
        }
        else if (fragmentIntent != null && fragmentIntent.equals("news")) {
            setFragment(new NewsFragment());
            bottomNavigationView.setSelectedItemId(R.id.newsFragment);
        }
        else {
            setFragment(new HomeFragment());
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragment = null;
                switch (item.getItemId()) {

                    case R.id.homeFragment:
                        fragment = new HomeFragment();
                        break;

                    case R.id.libraryFragment:
                        fragment = new LibraryFragment();
                        break;

                    case R.id.myNovelFragment:
                        fragment = new MyNovelFragment();
                        break;

                    case R.id.newsFragment:
                        fragment = new NewsFragment();
                        break;

                    case R.id.profileFragment:
                        fragment = new ProfileFragment();
                        break;
                }
                assert fragment != null;
                setFragment(fragment);
                return true;
            }
        });

        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        newsRef = FirebaseDatabase.getInstance().getReference("News");

        newsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sum = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.child("views").hasChild(currUserId)) {
                        sum++;
                    }
                }

                setNewsBadgeCount(sum);


                if (sum == 0) {
                    removeNewsBadgeCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void showReviewPopUp(){
        reviewManager = ReviewManagerFactory.create(MainPageActivity.this);

        Task<ReviewInfo> request = reviewManager.requestReviewFlow();

        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(Task<ReviewInfo> task) {
                if (task.isSuccessful()){
                    reviewInfo = task.getResult();
                    Task<Void> flow = reviewManager.launchReviewFlow(MainPageActivity.this , reviewInfo);

                    flow.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(MainPageActivity.this, "success", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(MainPageActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE){
            Toast.makeText(this, getResources().getString(R.string.start_download), Toast.LENGTH_SHORT).show();

            if (resultCode != RESULT_OK){
                Log.d("UPDATE" , "Update Flow Failed" + resultCode);
            }
        }
    }

    private void setNewsBadgeCount(int count) {
        itemView = bottomNavigationView.findViewById(R.id.newsFragment);

        notificationsBadge = LayoutInflater.from(this).inflate(R.layout.custom_badge_layout,
                bottomNavigationView, false);

        TextView text = notificationsBadge.findViewById(R.id.notifications_badge_counter);
        text.setText(String.valueOf(count));
        itemView.addView(notificationsBadge);
    }

    private void removeNewsBadgeCount() {
        itemView.removeView(notificationsBadge);

    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!bottomNavigationView.getMenu().findItem(R.id.homeFragment).isChecked()) {
            setFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.homeFragment);
        }
        else{  if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getDownTime() - lastPressedTime < PERIOD) {
                    finishAffinity();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.press_back_again,
                            Toast.LENGTH_SHORT).show();
                    lastPressedTime = event.getEventTime();
                }
                return true;
            }
        }
        }
        return false;
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