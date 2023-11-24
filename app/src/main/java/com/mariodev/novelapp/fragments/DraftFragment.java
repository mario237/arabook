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
import com.mariodev.novelapp.adapters.DraftAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class DraftFragment extends Fragment {

    SharedPref sharedPref;

   public LinearLayout noDraftLayout , draftRecyclerLayout;
    RecyclerView recyclerView;
    public ProgressBar loadDrafts;
    Button writeStoryBtn , addDraftBtn;
    DraftAdapter draftAdapter;
    List<StoryModel> draftStoryList;
    DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(requireContext());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_draft, container, false);


        noDraftLayout = view.findViewById(R.id.noDraftLayout);
        draftRecyclerLayout = view.findViewById(R.id.draftRecyclerLayout);
        recyclerView = view.findViewById(R.id.draftRecycler);
        loadDrafts = view.findViewById(R.id.loadDrafts);
        writeStoryBtn = view.findViewById(R.id.writeStoryBtn);
        addDraftBtn = view.findViewById(R.id.addDraftBtn);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        draftStoryList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Drafts");


        setStoriesList(databaseReference);


        writeStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext() , AddNewStoryActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
                requireActivity().finish();
            }
        });

        addDraftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext() , AddNewStoryActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
                requireActivity().finish();
            }
        });

        return view;
    }

    private void setStoriesList(DatabaseReference reference){
        loadDrafts.setVisibility(View.VISIBLE);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()) {
                    noDraftLayout.setVisibility(View.VISIBLE);
                    draftRecyclerLayout.setVisibility(View.GONE);
                    addDraftBtn.setVisibility(View.GONE);
                    loadDrafts.setVisibility(View.GONE);
                }

              if (dataSnapshot.exists()){
                  draftStoryList.clear();

                  for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                      StoryModel storyModel = snapshot.getValue(StoryModel.class);
                      assert storyModel != null;
                      if (storyModel.getStoryWriter() != null) {
                          draftStoryList.add(storyModel);
                      }
                  }

                  loadDrafts.setVisibility(View.GONE);
                  draftRecyclerLayout.setVisibility(View.VISIBLE);
                  draftAdapter = new DraftAdapter(getContext(), getActivity(), draftStoryList);
                  recyclerView.setAdapter(draftAdapter);
                  draftAdapter.notifyDataSetChanged();
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




