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
import com.mariodev.novelapp.activities.UserViewStoryActivity;
import com.mariodev.novelapp.adapters.BookFragmentAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BooksFragment extends Fragment {
    SharedPref sharedPref;

    String userId;
    RecyclerView userBooksRecycler;
    ProgressBar loadAllBooks;
    List<StoryModel> storyModelList;
    DatabaseReference storyRef;
    BookFragmentAdapter bookFragmentAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        checkLightMode();
        MyApplication.setLocale(requireContext());

        View view = inflater.inflate(R.layout.fragment_books, container, false);

        userId = requireActivity().getIntent().getStringExtra("userId");

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        userBooksRecycler = view.findViewById(R.id.userBooksRecycler);
        loadAllBooks = view.findViewById(R.id.loadAllBooks);

        userBooksRecycler.setHasFixedSize(true);
        userBooksRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        storyModelList = new ArrayList<>();

        storyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    storyModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("storyWriter").getValue(String.class), userId)) {
                            StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                            assert storyModel != null;
                            if (storyModel.getStoryWriter() != null) {
                                storyModelList.add(storyModel);
                            }
                        }

                    }

                    Collections.reverse(storyModelList);

                    bookFragmentAdapter = new BookFragmentAdapter(getContext(), storyModelList);
                    userBooksRecycler.setAdapter(bookFragmentAdapter);
                    bookFragmentAdapter.setOnItemClickListener(new BookFragmentAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<StoryModel> storyModelList) {
                            Intent intent = new Intent(getContext(), UserViewStoryActivity.class);
                            intent.putExtra("storyId", storyModelList.get(position).getStoryId());
                            intent.putExtra("from", "booksFragment");
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                        }
                    });
                    bookFragmentAdapter.notifyDataSetChanged();

                    if (!storyModelList.isEmpty()) {
                        loadAllBooks.setVisibility(View.GONE);
                        userBooksRecycler.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void checkLightMode() {
        sharedPref = new SharedPref(Objects.requireNonNull(getContext()));
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }
}