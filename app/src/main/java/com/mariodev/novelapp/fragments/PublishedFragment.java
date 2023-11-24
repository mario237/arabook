package com.mariodev.novelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.activities.AddNewStoryActivity;
import com.mariodev.novelapp.adapters.PublishedStoryAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PublishedFragment extends Fragment {

    SharedPref sharedPref;

    public LinearLayout haveNoStory;
    RecyclerView publishedRecycler;
    public ProgressBar loadPublished;
    Button writeStoryBtn;
    PublishedStoryAdapter publishedStoryAdapter;
    List<StoryModel> publishedStoryList;
    DatabaseReference currPublishedStoryRef;
    String userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(requireContext());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_published, container, false);


        haveNoStory = view.findViewById(R.id.haveNoPublishLayout);
        publishedRecycler = view.findViewById(R.id.publishedRecycler);
       loadPublished = view.findViewById(R.id.loadPublished);
        writeStoryBtn = view.findViewById(R.id.writeStoryBtn);

       publishedRecycler.setHasFixedSize(true);
        publishedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        publishedStoryList = new ArrayList<>();

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        currPublishedStoryRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories");



        setPublishedStoryList();


        writeStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext() , AddNewStoryActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
                requireActivity().finish();
            }
        });



        return view;
    }



    private void setPublishedStoryList(){
        currPublishedStoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    publishedStoryList.clear();
                    loadPublished.setVisibility(View.GONE);


                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);
                        assert storyModel != null;

                        if (storyModel.getStoryWriter() != null) {
                            if (storyModel.getStoryWriter().equals(userId)){
                                publishedStoryList.add(storyModel);
                            }
                        }
                    }

                    publishedRecycler.setVisibility(View.VISIBLE);
                    publishedStoryAdapter = new PublishedStoryAdapter(getContext(), getActivity(), publishedStoryList);
                    publishedRecycler.setAdapter(publishedStoryAdapter);
                    publishedStoryAdapter.notifyDataSetChanged();
                }

                if (publishedStoryList.isEmpty()){
                    haveNoStory.setVisibility(View.VISIBLE);
                    loadPublished.setVisibility(View.GONE);
                }else {
                    haveNoStory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkLightMode(){
        sharedPref = new SharedPref(Objects.requireNonNull(getContext()));
        if (sharedPref.loadLightModeState()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }


}


