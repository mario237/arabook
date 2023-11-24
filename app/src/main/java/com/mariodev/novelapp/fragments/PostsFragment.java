package com.mariodev.novelapp.fragments;

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
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.adapters.PostFragmentAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.PostModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostsFragment extends Fragment {
    SharedPref sharedPref;

    String userId;
    RecyclerView userPostsRecycler;
    ProgressBar loadAllPosts;
    List<PostModel> postModelList;
    PostFragmentAdapter postFragmentAdapter;
    DatabaseReference postRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        checkLightMode();
        MyApplication.setLocale(requireContext());

        View view =  inflater.inflate(R.layout.fragment_posts, container, false);

        userId = requireActivity().getIntent().getStringExtra("userId");

        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        userPostsRecycler = view.findViewById(R.id.userPostsRecycler);
        loadAllPosts = view.findViewById(R.id.loadAllPosts);

        userPostsRecycler.setHasFixedSize(true);
        userPostsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ((SimpleItemAnimator) Objects.requireNonNull(userPostsRecycler.getItemAnimator())).setSupportsChangeAnimations(false);

        postModelList = new ArrayList<>();

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    postModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), userId)){
                            PostModel postModel = dataSnapshot.child("data").getValue(PostModel.class);
                            postModelList.add(postModel);
                        }

                    }


                    postFragmentAdapter = new PostFragmentAdapter(getContext() , postModelList);
                    userPostsRecycler.setAdapter(postFragmentAdapter);

                    postFragmentAdapter.notifyDataSetChanged();

                    if (!postModelList.isEmpty()){
                        loadAllPosts.setVisibility(View.GONE);
                        userPostsRecycler.setVisibility(View.VISIBLE);
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