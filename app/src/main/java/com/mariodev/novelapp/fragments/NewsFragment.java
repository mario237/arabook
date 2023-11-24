package com.mariodev.novelapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.mariodev.novelapp.activities.UserViewNewsActivity;
import com.mariodev.novelapp.adapters.NewsAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.NewsModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NewsFragment extends Fragment {


    SharedPref sharedPref;

    RecyclerView newsRecyclerView;
    ProgressBar loadAllNews;
    TextView noNewsTv;
    List<NewsModel> newsModelList;
    NewsAdapter newsAdapter;
    DatabaseReference newsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(requireContext());

        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_news, container, false);

        newsRecyclerView = view.findViewById(R.id.newsRecyclerView);
        loadAllNews = view.findViewById(R.id.loadAllNews);
        noNewsTv = view.findViewById(R.id.noNewsTv);

        newsRecyclerView.setHasFixedSize(true);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        newsModelList = new ArrayList<>();

        newsRef = FirebaseDatabase.getInstance().getReference("News");

        newsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsModelList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        NewsModel newsModel = dataSnapshot.child("data").getValue(NewsModel.class);
                        newsModelList.add(newsModel);
                    }


                    newsRecyclerView.setVisibility(View.VISIBLE);
                    loadAllNews.setVisibility(View.GONE);
                    noNewsTv.setVisibility(View.GONE);


                    Collections.reverse(newsModelList);

                    newsAdapter = new NewsAdapter(getContext() , newsModelList);
                    newsRecyclerView.setAdapter(newsAdapter);
                    newsAdapter.notifyDataSetChanged();




                    newsAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<NewsModel> newsModelList) {
                            Intent intent = new Intent(requireContext() , UserViewNewsActivity.class);
                            intent.putExtra("newsId",newsModelList.get(position).getNewsId());
                            startActivity(intent);
                            requireActivity().overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
                            requireActivity().finish();
                        }
                    });
                }
                else {
                   newsRecyclerView.setVisibility(View.GONE);
                   loadAllNews.setVisibility(View.GONE);
                    noNewsTv.setVisibility(View.VISIBLE);
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