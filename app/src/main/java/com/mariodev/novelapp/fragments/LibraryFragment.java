package com.mariodev.novelapp.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.activities.UserViewChapterActivity;
import com.mariodev.novelapp.adapters.LibraryAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LibraryFragment extends Fragment {

    SharedPref sharedPref;

    ProgressBar loadLibraryNovels;
    RecyclerView libraryRecycler;
    ImageView deleteStoriesIcon;
    TextView countOfSelectedStoriesTv;
    LinearLayout noLibraryLayout;
    Button chooseStoryBtn;
    LibraryAdapter libraryAdapter;
    List<StoryModel> storyList;
    DatabaseReference storyRef;
    String userId;
    BottomNavigationView bottomNavigationView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(requireContext());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        deleteStoriesIcon = view.findViewById(R.id.deleteStoriesIcon);
        countOfSelectedStoriesTv = view.findViewById(R.id.countOfSelectedStoriesTv);
        libraryRecycler = view.findViewById(R.id.libraryRecycler);
        loadLibraryNovels = view.findViewById(R.id.loadLibraryNovels);
        noLibraryLayout = view.findViewById(R.id.noLibraryLayout);
        chooseStoryBtn = view.findViewById(R.id.chooseStoryBtn);
        bottomNavigationView = requireActivity().findViewById(R.id.nav_buttons);

        libraryRecycler.setHasFixedSize(true);
        libraryRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        storyList = new ArrayList<>();

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        storyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                storyList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if (dataSnapshot.hasChild("Users")){
                        if (dataSnapshot.child("Users").hasChild(userId)){
                            StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                            assert storyModel != null;
                            if (storyModel.getStoryWriter() != null){
                                storyList.add(storyModel);

                            }
                        }
                    }
                }


                libraryAdapter = new LibraryAdapter(getContext() , storyList);
                libraryRecycler.setAdapter(libraryAdapter);
                loadLibraryNovels.setVisibility(View.GONE);
                libraryRecycler.setVisibility(View.VISIBLE);


                if (storyList.isEmpty()){
                    noLibraryLayout.setVisibility(View.VISIBLE);
                    libraryRecycler.setVisibility(View.GONE);
                    loadLibraryNovels.setVisibility(View.GONE);
                }

                libraryAdapter.setListener(new LibraryAdapter.LibraryAdapterListener() {
                    @Override
                    public void onItemClick(int position) {
                        enableActionMode(position);
                    }

                    @Override
                    public void onItemLongClick(int position) {
                        enableActionMode(position);
                    }

                    @Override
                    public void onStoryClick(int position) {
                        Intent intent = new Intent(getContext() , UserViewChapterActivity.class);
                        intent.putExtra("storyId",storyList.get(position).getStoryId());
                       intent.putExtra("from","library");
                       intent.putExtra("storyName",storyList.get(position).getStoryName());
                        startActivity(intent);
                        requireActivity().overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
                    }
                });
                libraryAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        deleteStoriesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStories();
            }
        });

        chooseStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragment(new HomeFragment());
                bottomNavigationView.setSelectedItemId(R.id.homeFragment);

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

    private void enableActionMode(int position) {
        libraryAdapter.toggleSelection(position);
        final int size = libraryAdapter.selectedItems.size();
        if (size == 0) {
            countOfSelectedStoriesTv.setVisibility(View.GONE);
            deleteStoriesIcon.setVisibility(View.GONE);
        }else {
            countOfSelectedStoriesTv.setText( String.valueOf(size));
            countOfSelectedStoriesTv.setVisibility(View.VISIBLE);
            deleteStoriesIcon.setVisibility(View.VISIBLE);
        }
    }


    public void deleteStories() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext() , R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.delete_warning));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {

                for (StoryModel storyModel : storyList) {
                    if (storyModel.isSelected())
                        storyRef.child(storyModel.getStoryId()).child("Users").child(userId).removeValue();
                }

                libraryAdapter.selectedItems.clear();
                countOfSelectedStoriesTv.setVisibility(View.GONE);
                deleteStoriesIcon.setVisibility(View.GONE);
                libraryAdapter.notifyDataSetChanged();


                dialogInterface.dismiss();

            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel) , null);

        AlertDialog dialog = builder.create();
        dialog.show();



    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }
}