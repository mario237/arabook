package com.mariodev.novelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.activities.AllTypesActivity;
import com.mariodev.novelapp.activities.MoreActivity;
import com.mariodev.novelapp.activities.SearchAllNovelsActivity;
import com.mariodev.novelapp.activities.UserViewStoryActivity;
import com.mariodev.novelapp.activities.ViewPoemActivity;
import com.mariodev.novelapp.adapters.CompletedNovelsAdapter;
import com.mariodev.novelapp.adapters.LatestNovelsAdapter;
import com.mariodev.novelapp.adapters.PoemAdapter;
import com.mariodev.novelapp.adapters.RisingNovelsAdapter;
import com.mariodev.novelapp.adapters.SliderAdapter;
import com.mariodev.novelapp.adapters.SuggestAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.models.StoryModel;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@SuppressWarnings("ALL")
public class HomeFragment extends Fragment  implements View.OnClickListener , SwipeRefreshLayout.OnRefreshListener {

    SharedPref sharedPref;
    ImageView searchALlNovelsIcon;
    FloatingActionButton allTypesIcon;
    SwipeRefreshLayout refreshHomeFragment;
    ProgressBar loadHomePage;
    ConstraintLayout homePageLayout;
    DatabaseReference storyRef , poemRef;
    List<StoryModel> sliderModelList , latestModelList , suggestModelList , completedModelList , risingModelList;
    RecyclerView latestNovelRecycler , suggestionsRecycler , risingNovelRecycler , completedNovelRecycler , poemsRecycler;
    TextView moreLatestTv , moreSuggestionsTv , moreCompletedTv , moreRisingTv , poemTv;
    SliderView imageSlider;
    SliderAdapter sliderAdapter;
    RelativeLayout latestNovelLayout , suggestionsLayout , completedLayout , risingNovelLayout;
    LatestNovelsAdapter latestNovelsAdapter;
    SuggestAdapter suggestAdapter;
    CompletedNovelsAdapter completedNovelsAdapter;
    RisingNovelsAdapter risingNovelsAdapter;
    List<PoemModel> poemModelList;
    PoemAdapter poemAdapter;
    String storyId;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(requireContext());

        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_home, container, false);

        homePageLayout = view.findViewById(R.id.homePageLayout);
        refreshHomeFragment = view.findViewById(R.id.refreshHomeFragment);
        loadHomePage = view.findViewById(R.id.loadHomePage);
        searchALlNovelsIcon = view.findViewById(R.id.searchALlNovelsIcon);
        imageSlider = view.findViewById(R.id.imageSlider);
        allTypesIcon = view.findViewById(R.id.allTypesIcon);
        latestNovelLayout = view.findViewById(R.id.latestNovelLayout);
        suggestionsLayout = view.findViewById(R.id.suggestionsLayout);
        completedLayout = view.findViewById(R.id.completedLayout);
        risingNovelLayout = view.findViewById(R.id.risingNovelLayout);

        moreLatestTv = view.findViewById(R.id.moreLatestTv);
        moreSuggestionsTv = view.findViewById(R.id.moreSuggestionsTv);
        moreCompletedTv = view.findViewById(R.id.moreCompletedTv);
        moreRisingTv = view.findViewById(R.id.moreRisingTv);
        poemTv = view.findViewById(R.id.poemTv);


        refreshHomeFragment.setOnRefreshListener(this);

        latestNovelRecycler = view.findViewById(R.id.latestNovelRecycler);
        latestNovelRecycler.setHasFixedSize(true);
        latestNovelRecycler.setLayoutManager(new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL , false));

        suggestionsRecycler = view.findViewById(R.id.suggestionsRecycler);
        suggestionsRecycler.setHasFixedSize(true);
        suggestionsRecycler.setLayoutManager(new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL , false));

        completedNovelRecycler = view.findViewById(R.id.completedRecycler);
        completedNovelRecycler.setHasFixedSize(true);
        completedNovelRecycler.setLayoutManager(new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL , false));

        risingNovelRecycler = view.findViewById(R.id.risingNovelRecycler);
        risingNovelRecycler.setHasFixedSize(true);
        risingNovelRecycler.setLayoutManager(new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL , false));

        poemsRecycler = view.findViewById(R.id.poemsRecycler);
        poemsRecycler.setHasFixedSize(true);
        poemsRecycler.setLayoutManager(new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL , false));

        sliderModelList = new ArrayList<>();
        latestModelList = new ArrayList<>();
        suggestModelList = new ArrayList<>();
        completedModelList = new ArrayList<>();
        risingModelList = new ArrayList<>();
        poemModelList = new ArrayList<>();

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        poemRef = FirebaseDatabase.getInstance().getReference("Poems");

        getHomeListsData();


        allTypesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext() , AllTypesActivity.class));
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
                getActivity().finish();
            }
        });


        moreLatestTv.setOnClickListener(this);
        moreCompletedTv.setOnClickListener(this);
        moreRisingTv.setOnClickListener(this);
        moreSuggestionsTv.setOnClickListener(this);
        searchALlNovelsIcon.setOnClickListener(this);

        return view;
    }


    private void checkLightMode(){
        sharedPref = new SharedPref(Objects.requireNonNull(getContext()));
        if (sharedPref.loadLightModeState()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }


    private void goToViewStory(List<StoryModel> storyModelList , int position){
        Intent intent = new Intent(getContext() , UserViewStoryActivity.class);
        intent.putExtra("storyId",storyModelList.get(position).getStoryId());
        intent.putExtra("from","home");
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.moreLatestTv : goToMoreActivity(getString(R.string.latest_novels)); break;
            case R.id.moreSuggestionsTv : goToMoreActivity(getResources().getString(R.string.suggestions)); break;
            case R.id.moreRisingTv : goToMoreActivity(getResources().getString(R.string.rising_novel)); break;
            case R.id.moreCompletedTv : goToMoreActivity(getResources().getString(R.string.complete)); break;
            case R.id.searchALlNovelsIcon : goTOSearchAllNovels(); break;


        }
    }

    private void goTOSearchAllNovels() {
        Intent intent = new Intent(getContext() , SearchAllNovelsActivity.class);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_left , R.anim.slide_out_right);
        getActivity().finish();
    }

    private void goToMoreActivity(String type) {
        Intent intent = new Intent(getContext() , MoreActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
        getActivity().finish();

    }


    @Override
    public void onRefresh() {
        getHomeListsData();
        refreshHomeFragment.setRefreshing(false);
    }

    private void getHomeListsData(){
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    sliderModelList.clear();
                    latestModelList.clear();
                    suggestModelList.clear();
                    completedModelList.clear();
                    risingModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                        StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                        assert storyModel != null;

                        if (storyModel.getStoryWriter() != null){
                            if (storyModel.isDiscover()){
                                sliderModelList.add(storyModel);
                            }

                            latestModelList.add(storyModel);
                            suggestModelList.add(storyModel);


                            if (storyModel.isCompleted()){
                                completedModelList.add(storyModel);
                            }

                            if (storyModel.getChaptersCount() > 6 && storyModel.getChaptersCount()<20
                                    && storyModel.getStoryFollowers() >= 50){
                                risingModelList.add(storyModel);
                            }
                        }

                    }

                    if (latestModelList.isEmpty()){
                        latestNovelLayout.setVisibility(View.GONE);
                        latestNovelRecycler.setVisibility(View.GONE);
                    }
                    if (suggestModelList.isEmpty()){
                        suggestionsLayout.setVisibility(View.GONE);
                        suggestionsRecycler.setVisibility(View.GONE);
                    }
                    if (completedModelList.isEmpty()){
                        completedLayout.setVisibility(View.GONE);
                        completedNovelRecycler.setVisibility(View.GONE);
                    }
                    if (risingModelList.isEmpty()){
                        risingNovelLayout.setVisibility(View.GONE);
                        risingNovelRecycler.setVisibility(View.GONE);
                    }


                    sliderAdapter = new SliderAdapter(getContext(), sliderModelList , getActivity());

                    imageSlider.setSliderAdapter(sliderAdapter);

                    imageSlider.setScrollTimeInSec(4); //set scroll delay in seconds :

                    Collections.reverse(latestModelList);

                    latestNovelsAdapter = new LatestNovelsAdapter(getContext() , latestModelList);

                    latestNovelRecycler.setAdapter(latestNovelsAdapter);

                    latestNovelsAdapter.setOnItemClickListener(new LatestNovelsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<StoryModel> storyModelList) {
                            goToViewStory(storyModelList , position);
                        }
                    });

                    latestNovelsAdapter.notifyDataSetChanged();

                    Collections.shuffle(suggestModelList);

                    suggestAdapter = new SuggestAdapter(getContext() , suggestModelList);
                    suggestionsRecycler.setAdapter(suggestAdapter);

                    suggestAdapter.setOnItemClickListener(new SuggestAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<StoryModel> storyModelList) {
                            goToViewStory(storyModelList , position);
                        }
                    });
                    suggestAdapter.notifyDataSetChanged();


                    Collections.sort(completedModelList, new Comparator<StoryModel>() {
                        @Override
                        public int compare(StoryModel lhs, StoryModel rhs) {
                            if(lhs.getStoryViews() > rhs.getStoryViews()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });


                    completedNovelsAdapter = new CompletedNovelsAdapter(getContext() , completedModelList);
                    completedNovelRecycler.setAdapter(completedNovelsAdapter);

                    completedNovelsAdapter.setOnItemClickListener(new CompletedNovelsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<StoryModel> storyModelList) {
                            goToViewStory(storyModelList , position);
                        }
                    });
                    completedNovelsAdapter.notifyDataSetChanged();


                    Collections.sort(risingModelList, new Comparator<StoryModel>() {
                        @Override
                        public int compare(StoryModel lhs, StoryModel rhs) {
                            if(lhs.getStoryFollowers() > rhs.getStoryFollowers()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        }
                    });


                    risingNovelsAdapter = new RisingNovelsAdapter(getContext() , risingModelList);
                    risingNovelRecycler.setAdapter(risingNovelsAdapter);

                    risingNovelsAdapter.setOnItemClickListener(new RisingNovelsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<StoryModel> storyModelList) {
                            goToViewStory(storyModelList , position);

                        }
                    });
                    risingNovelsAdapter.notifyDataSetChanged();


                    homePageLayout.setVisibility(View.VISIBLE);
                    loadHomePage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        poemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    poemModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        PoemModel poemModel = dataSnapshot.child("data").getValue(PoemModel.class);
                        poemModelList.add(poemModel);
                    }

                    if (poemModelList.isEmpty()){
                        poemTv.setVisibility(View.GONE);
                        poemsRecycler.setVisibility(View.GONE);
                    }

                    Collections.shuffle(poemModelList);

                    poemAdapter = new PoemAdapter(getContext() , poemModelList);

                    poemsRecycler.setAdapter(poemAdapter);


                    poemAdapter.setOnItemClickListener(new PoemAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<PoemModel> poemModelList) {
                            Intent intent = new Intent(getContext() , ViewPoemActivity.class);
                            intent.putExtra("poemId",poemModelList.get(position).getPoemId());
                            startActivity(intent);

                        }
                    });

                    poemAdapter.notifyDataSetChanged();

                    poemTv.setVisibility(View.VISIBLE);

                    poemsRecycler.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}