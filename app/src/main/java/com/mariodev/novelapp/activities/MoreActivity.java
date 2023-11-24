package com.mariodev.novelapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.adapters.AllNovelsAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("ALL")
public class MoreActivity extends AppCompatActivity implements AllNovelsAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    SharedPref sharedPref;

    String moreType;
    ImageView arrowBack;
    TextView typeTv;
    SwipeRefreshLayout refreshMoreActivity;
    RecyclerView moreRecyclerView;
    ProgressBar loadAllMore;
    DatabaseReference storyRef;
    AllNovelsAdapter allNovelsAdapter;
    List<StoryModel> storyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        moreType = getIntent().getStringExtra("type");

        arrowBack = findViewById(R.id.arrowBack);
        typeTv = findViewById(R.id.typeTv);
        loadAllMore = findViewById(R.id.loadAllMore);
        moreRecyclerView = findViewById(R.id.moreRecyclerView);
        refreshMoreActivity = findViewById(R.id.refreshMoreActivity);

        moreRecyclerView.setHasFixedSize(true);
        moreRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        storyList = new ArrayList<>();

        typeTv.setText(moreType);
        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        refreshMoreActivity.setOnRefreshListener(this);

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        getMoreListData();

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MoreActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "Home");
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
    public void onItemClick(int position, List<StoryModel> storyModelList) {
        Intent intent = new Intent(MoreActivity.this, UserViewStoryActivity.class);
        intent.putExtra("storyId", storyModelList.get(position).getStoryId());
        intent.putExtra("from", "more");
        intent.putExtra("type", moreType);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "ar"));
    }

    private void getMoreListData() {
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                storyList.clear();


                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                    assert storyModel != null;

                    if (moreType.equals(getResources().getString(R.string.complete))) {
                        if (storyModel.isCompleted() && storyModel.getStoryWriter()!= null) {
                            storyList.add(storyModel);
                        }

                    }
                    if (moreType.equals(getResources().getString(R.string.suggestions))) {
                        if (storyList.size() != 50  && storyModel.getStoryWriter()!= null) {
                            storyList.add(storyModel);
                        }
                    }
                    if (moreType.equals(getResources().getString(R.string.latest_novels)) && storyModel.getStoryWriter()!= null) {
                        if (storyList.size() != 50) {
                            storyList.add(storyModel);
                        }
                    }
                    if (moreType.equals(getResources().getString(R.string.rising_novel)) && storyModel.getStoryWriter()!= null) {
                        if (storyModel.getChaptersCount() > 6 && storyModel.getChaptersCount() < 20
                                && storyModel.getStoryFollowers() >= 50) {
                            storyList.add(storyModel);

                        }
                    }

                }


                if (moreType.equals(getResources().getString(R.string.complete))) {
                    Collections.sort(storyList, new Comparator<StoryModel>() {
                        @Override
                        public int compare(StoryModel lhs, StoryModel rhs) {
                            if (lhs.getStoryViews() > rhs.getStoryViews()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });
                }
                if (moreType.equals(getResources().getString(R.string.suggestions))) {
                    Collections.shuffle(storyList);

                }
                if (moreType.equals(getResources().getString(R.string.latest_novels))) {
                    Collections.reverse(storyList);
                }
                if (moreType.equals(getResources().getString(R.string.rising_novel))) {
                    Collections.sort(storyList, new Comparator<StoryModel>() {
                        @Override
                        public int compare(StoryModel lhs, StoryModel rhs) {
                            if (lhs.getStoryFollowers() > rhs.getStoryFollowers()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });

                }

                allNovelsAdapter = new AllNovelsAdapter(MoreActivity.this, storyList);
                moreRecyclerView.setAdapter(allNovelsAdapter);
                loadAllMore.setVisibility(View.GONE);
                moreRecyclerView.setVisibility(View.VISIBLE);
                allNovelsAdapter.setOnItemClickListener(MoreActivity.this);
                allNovelsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onRefresh() {
        getMoreListData();
        refreshMoreActivity.setRefreshing(false);
    }
}