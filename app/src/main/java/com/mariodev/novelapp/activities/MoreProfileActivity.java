package com.mariodev.novelapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.fragments.BooksFragment;
import com.mariodev.novelapp.fragments.PoemsFragment;
import com.mariodev.novelapp.fragments.PostsFragment;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;

public class MoreProfileActivity extends AppCompatActivity {
    SharedPref sharedPref;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    ImageView backImg;
    TextView usernameTv;
    String userId;
    DatabaseReference userRef;
    String moreWhat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_profile);

        userId = getIntent().getStringExtra("userId");
        moreWhat = getIntent().getStringExtra("moreWhat");

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        backImg = findViewById(R.id.backImg);
        usernameTv = findViewById(R.id.usernameTv);

        tabLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new PoemsFragment(), getResources().getString(R.string.poems));
        viewPagerAdapter.addFragment(new BooksFragment(), getResources().getString(R.string.books));
        viewPagerAdapter.addFragment(new PostsFragment(), getResources().getString(R.string.posts));



        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.notifyDataSetChanged();



        tabLayout.setupWithViewPager(viewPager);

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);

                assert userModel != null;
                usernameTv.setText(userModel.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (moreWhat != null && moreWhat.equals("poems")) {
            viewPager.setCurrentItem(0);
        }
        else if (moreWhat != null && moreWhat.equals("books")) {
            viewPager.setCurrentItem(1);
        }
        else if (moreWhat != null && moreWhat.equals("posts")) {
            viewPager.setCurrentItem(2);
        }


    }

    @SuppressWarnings("deprecation")
    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final ArrayList<Fragment> fragments;
        private final ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }


    }

    @Override
    public void onBackPressed() {
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