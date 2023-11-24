package com.mariodev.novelapp.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

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
import java.util.List;

public class ViewAllNovelsActivity extends AppCompatActivity implements AllNovelsAdapter.OnItemClickListener , SwipeRefreshLayout.OnRefreshListener{

    SharedPref sharedPref;

    SwipeRefreshLayout refreshAdminViewNovelsLayout;
    EditText searchNovelsEdt;
    ProgressBar loadAllNovels;
    RecyclerView allNovelsRecycler;
    AllNovelsAdapter allNovelsAdapter;
    List<StoryModel> storyList;

    DatabaseReference storyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_novels);

        refreshAdminViewNovelsLayout = findViewById(R.id.refreshAdminViewNovelsLayout);
        searchNovelsEdt = findViewById(R.id.searchNovelsEdt);
        loadAllNovels = findViewById(R.id.loadAllNovels);
        allNovelsRecycler = findViewById(R.id.allNovelsRecycler);

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");


        allNovelsRecycler.setHasFixedSize(true);
        allNovelsRecycler.setLayoutManager(new GridLayoutManager(this , 3));
        storyList = new ArrayList<>();


        getData();

        searchNovelsEdt.addTextChangedListener(new TextWatcher() {
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

        refreshAdminViewNovelsLayout.setOnRefreshListener(this);
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


                allNovelsAdapter = new AllNovelsAdapter(ViewAllNovelsActivity.this , storyList);
                allNovelsRecycler.setAdapter(allNovelsAdapter);
                loadAllNovels.setVisibility(View.GONE);
                allNovelsRecycler.setVisibility(View.VISIBLE);
                allNovelsAdapter.setOnItemClickListener(ViewAllNovelsActivity.this);
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


    private void checkLightMode(){
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onItemClick(int position, List<StoryModel> storyModelList) {
        Intent intent =new Intent(ViewAllNovelsActivity.this , ViewSelectedNovelActivity.class);
        intent.putExtra("adminStoryId" , storyModelList.get(position).getStoryId());
        intent.putExtra("storyImageUri", storyModelList.get(position).getStoryImage());
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
        refreshAdminViewNovelsLayout.setRefreshing(false);
    }
}