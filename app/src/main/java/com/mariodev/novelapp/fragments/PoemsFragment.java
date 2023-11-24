package com.mariodev.novelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.activities.ViewPoemActivity;
import com.mariodev.novelapp.adapters.PoemFragmentAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PoemsFragment extends Fragment {
    SharedPref sharedPref;

    String userId;
    RecyclerView userPoemsRecycler;
    ProgressBar loadAllPoems;
    List<PoemModel> poemModelList;
    PoemFragmentAdapter poemFragmentAdapter;
    DatabaseReference poemRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        checkLightMode();
        MyApplication.setLocale(requireContext());

        View view  = inflater.inflate(R.layout.fragment_poems, container, false);

        userId = requireActivity().getIntent().getStringExtra("userId");

        poemRef = FirebaseDatabase.getInstance().getReference("Poems");

        userPoemsRecycler = view.findViewById(R.id.userPoemsRecycler);
        loadAllPoems = view.findViewById(R.id.loadAllPoems);

        userPoemsRecycler.setHasFixedSize(true);
        userPoemsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        poemModelList = new ArrayList<>();

        poemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    poemModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), userId)){
                            PoemModel poemModel = dataSnapshot.child("data").getValue(PoemModel.class);
                            poemModelList.add(poemModel);
                        }

                    }


                    poemFragmentAdapter = new PoemFragmentAdapter(getContext() , poemModelList);
                    userPoemsRecycler.setAdapter(poemFragmentAdapter);
                    poemFragmentAdapter.setOnItemClickListener(new PoemFragmentAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<PoemModel> poemModelList) {
                            Intent intent = new Intent(getContext() , ViewPoemActivity.class);
                            intent.putExtra("poemId",poemModelList.get(position).getPoemId());
                            intent.putExtra("userId",userId);
                            startActivity(intent);
                        }
                    });
                    poemFragmentAdapter.notifyDataSetChanged();

                    if (!poemModelList.isEmpty()){
                        loadAllPoems.setVisibility(View.GONE);
                        userPoemsRecycler.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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

}