package com.mariodev.novelapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mariodev.novelapp.adapters.AllTypesAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryTypeModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

public class AllTypesActivity extends AppCompatActivity implements AllTypesAdapter.OnItemClickListener{
    SharedPref sharedPref;
    RecyclerView allTypesRecycler;
    AllTypesAdapter allTypesAdapter;
    StoryTypeModel [] typeModelList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_types);
        allTypesRecycler = findViewById(R.id.allTypesRecycler);

        allTypesRecycler.setHasFixedSize(true);
        allTypesRecycler.setLayoutManager(new GridLayoutManager(this , 2));


        typeModelList = new StoryTypeModel[]{
                new StoryTypeModel(getResources().getString(R.string.horror), R.drawable.horror),
                new StoryTypeModel(getResources().getString(R.string.fiction), R.drawable.fiction),
                new StoryTypeModel(getResources().getString(R.string.adventures), R.drawable.adventures),
                new StoryTypeModel(getResources().getString(R.string.romantic), R.drawable.romantic),
                new StoryTypeModel(getResources().getString(R.string.science_fiction), R.drawable.scince_fiction),
                new StoryTypeModel(getResources().getString(R.string.psychological), R.drawable.psychological),
                new StoryTypeModel(getResources().getString(R.string.mystery), R.drawable.mystrey),
                new StoryTypeModel(getResources().getString(R.string.comedy), R.drawable.comedy),
                new StoryTypeModel(getResources().getString(R.string.police), R.drawable.police),
                new StoryTypeModel(getResources().getString(R.string.historical), R.drawable.historical),
                new StoryTypeModel(getResources().getString(R.string.drama), R.drawable.drama),
                new StoryTypeModel(getResources().getString(R.string.realistic), R.drawable.realistic),
                new StoryTypeModel(getResources().getString(R.string.short_stories), R.drawable.short_stories),
                new StoryTypeModel(getResources().getString(R.string.non_fiction), R.drawable.non_fiction)};

        allTypesAdapter = new AllTypesAdapter(this , typeModelList);

        allTypesRecycler.setAdapter(allTypesAdapter);

        allTypesAdapter.setOnItemClickListener(this);
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(AllTypesActivity.this, MainPageActivity.class));
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


    private void goToViewStoryType(String type){
        Intent intent = new Intent(AllTypesActivity.this  , ViewStoryTypesActivity.class);
        intent.putExtra("type",type);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);

    }

    @Override
    public void onItemClick(int position, StoryTypeModel[] typeModelList) {
        goToViewStoryType(typeModelList[position].getTypeName());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }


}