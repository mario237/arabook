package com.mariodev.novelapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SearchAllNovelsActivity extends AppCompatActivity implements AllNovelsAdapter.OnItemClickListener
, SwipeRefreshLayout.OnRefreshListener{
    SharedPref sharedPref;
    SwipeRefreshLayout refreshSearchLayout;
    ImageView arrowBack;
    EditText storyNameSearchEdt;

    ProgressBar loadSearchAllNovels;
    RecyclerView searchAllNovelsRecycler;
    AllNovelsAdapter allNovelsAdapter;
    List<StoryModel> storyList;

    DatabaseReference storyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all_novels);

        refreshSearchLayout = findViewById(R.id.refreshSearchLayout);
        arrowBack = findViewById(R.id.arrowBack);
        storyNameSearchEdt = findViewById(R.id.storyNameSearchEdt);
        loadSearchAllNovels = findViewById(R.id.loadSearchAllNovels);
        searchAllNovelsRecycler = findViewById(R.id.searchAllNovelsRecycler);

        searchAllNovelsRecycler.setHasFixedSize(true);
        searchAllNovelsRecycler.setLayoutManager(new GridLayoutManager(this , 3));

        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        storyList = new ArrayList<>();

        getData();

        storyNameSearchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        refreshSearchLayout.setOnRefreshListener(this);
    }

    private void getData(){
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                storyList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                    assert storyModel != null;
                    if (storyModel.getStoryWriter()!= null){
                        storyList.add(storyModel);
                    }
                }

                Collections.reverse(storyList);


                allNovelsAdapter = new AllNovelsAdapter(SearchAllNovelsActivity.this , storyList);
                searchAllNovelsRecycler.setAdapter(allNovelsAdapter);
                loadSearchAllNovels.setVisibility(View.GONE);
                searchAllNovelsRecycler.setVisibility(View.VISIBLE);
                allNovelsAdapter.setOnItemClickListener(SearchAllNovelsActivity.this);
                allNovelsAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void filter (String text){

        List<StoryModel> filteredList = new ArrayList<>();


        for (StoryModel item : storyList){

            if (item.getStoryName().toLowerCase().trim().contains(text.toLowerCase().trim())){
                filteredList.add(item);
            }
        }

        allNovelsAdapter.filterList(filteredList);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchAllNovelsActivity.this, MainPageActivity.class);
        intent.putExtra("fragment", "Home");
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
    public void onItemClick(int position, List<StoryModel> storyModelList) {
        Intent intent = new Intent(SearchAllNovelsActivity.this , UserViewStoryActivity.class);
        intent.putExtra("storyId",storyModelList.get(position).getStoryId());
        intent.putExtra("from","search");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left , R.anim.slide_out_right);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }


    @Override
    public void onRefresh() {
        getData();
        refreshSearchLayout.setRefreshing(false);

    }
}