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
import com.mariodev.novelapp.adapters.StoryTypeAdapter;
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
public class ViewStoryTypesActivity extends AppCompatActivity implements StoryTypeAdapter.OnItemClickListener , SwipeRefreshLayout.OnRefreshListener{
    SharedPref sharedPref;

    SwipeRefreshLayout refreshStoryTypesLayout;
    String storyType;
    TextView typeTv;
    ImageView arrowBack;
    ProgressBar loadAllTypes;
    RecyclerView storyTypeRecyclerView;
    DatabaseReference storyRef;
    StoryTypeAdapter storyTypeAdapter;
    List<StoryModel> storyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_story_types);
        storyType = getIntent().getStringExtra("type");

        refreshStoryTypesLayout = findViewById(R.id.refreshStoryTypesLayout);
        typeTv = findViewById(R.id.typeTv);
        arrowBack = findViewById(R.id.arrowBack);
        loadAllTypes = findViewById(R.id.loadAllTypes);
        storyTypeRecyclerView = findViewById(R.id.storyTypeRecyclerView);

        typeTv.setText(storyType);
        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        storyTypeRecyclerView.setHasFixedSize(true);
        storyTypeRecyclerView.setLayoutManager(new GridLayoutManager(this , 3));
        storyList = new ArrayList<>();

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        getData();

        refreshStoryTypesLayout.setOnRefreshListener(this);
    }

    private void getData(){
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                storyList.clear();


                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                    assert storyModel != null;
                    if (storyModel.getStoryWriter() != null) {
                        if (storyModel.getStoryType().equals(storyType)){
                            storyList.add(storyModel);
                        }
                    }
                }

                Collections.sort(storyList, new Comparator<StoryModel>() {
                    @Override
                    public int compare(StoryModel lhs, StoryModel rhs) {
                        if(lhs.getStoryFollowers() > rhs.getStoryFollowers()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });


                storyTypeAdapter = new StoryTypeAdapter(ViewStoryTypesActivity.this , storyList , storyType);
                storyTypeRecyclerView.setAdapter(storyTypeAdapter);
                loadAllTypes.setVisibility(View.GONE);
                storyTypeRecyclerView.setVisibility(View.VISIBLE);
                storyTypeAdapter.setOnItemClickListener(ViewStoryTypesActivity.this);
                storyTypeAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ViewStoryTypesActivity.this  , AllTypesActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left , R.anim.slide_out_right);

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
        Intent intent = new Intent(ViewStoryTypesActivity.this , UserViewStoryActivity.class);
        intent.putExtra("storyId",storyModelList.get(position).getStoryId());
        intent.putExtra("from","viewStoryTypes");
        intent.putExtra("type",storyType);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }


    @Override
    public void onRefresh() {
        getData();
        refreshStoryTypesLayout.setRefreshing(false);
    }
}