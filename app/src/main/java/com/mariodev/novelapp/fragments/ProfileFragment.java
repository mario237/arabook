package com.mariodev.novelapp.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.activities.ChoosePostOrPoemActivity;
import com.mariodev.novelapp.activities.MoreProfileActivity;
import com.mariodev.novelapp.activities.SettingsActivity;
import com.mariodev.novelapp.activities.UserViewStoryActivity;
import com.mariodev.novelapp.activities.ViewFollowUsersActivity;
import com.mariodev.novelapp.activities.ViewPoemActivity;
import com.mariodev.novelapp.activities.ViewPostActivity;
import com.mariodev.novelapp.adapters.LatestNovelsAdapter;
import com.mariodev.novelapp.adapters.PoemAdapter;
import com.mariodev.novelapp.adapters.PostAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.models.PostModel;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.models.UserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class ProfileFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    SharedPref sharedPref;

    RelativeLayout profileMainLayout, userPostsLayout, userPoemsLayout, userBooksLayout, followingCard, followersCard;
    ProgressBar progressBar;
    ImageView profileImg, settingImg, writeImg, coverImg, verifiedImage;
    CardView bioCard;
    TextView usernameTv, userFullNameTv, bioTv, novelNumTv, poemsNumTv, followNumTv, followingNumTv,
            morePostsTv, morePoemsTv, moreBooksTv;

    SwipeRefreshLayout refreshProfileFragment;
    RecyclerView userPostsRecycler, userPoemsRecycler, userBooksRecycler;
    List<PostModel> postModelList;
    List<PoemModel> poemModelList;
    List<StoryModel> storyModelList;
    PostAdapter postAdapter;
    PoemAdapter poemAdapter;
    LatestNovelsAdapter latestNovelsAdapter;

    //Firebase component
    DatabaseReference reference, novelNumRef, poemRef, postRef, storyRef, viewPoemRef, followingRef, followersRef;
    FirebaseUser firebaseUser;


    String currUserId, currUserName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        checkLightMode();
        MyApplication.setLocale(requireContext());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        settingImg = view.findViewById(R.id.settingImg);
        writeImg = view.findViewById(R.id.writeImg);
        refreshProfileFragment = view.findViewById(R.id.refreshProfileFragment);
        coverImg = view.findViewById(R.id.coverImg);
        profileMainLayout = view.findViewById(R.id.profileMainLayout);
        progressBar = view.findViewById(R.id.profileLoad);
        profileImg = view.findViewById(R.id.profileImg);
        userFullNameTv = view.findViewById(R.id.userFullNameTv);
        usernameTv = view.findViewById(R.id.username);
        bioCard = view.findViewById(R.id.bioCard);
        bioTv = view.findViewById(R.id.bioTv);
        novelNumTv = view.findViewById(R.id.novelNum);
        poemsNumTv = view.findViewById(R.id.poemsNum);
        followNumTv = view.findViewById(R.id.followNum);
        followingNumTv = view.findViewById(R.id.followingNum);
        userPostsLayout = view.findViewById(R.id.userPostsLayout);
        morePostsTv = view.findViewById(R.id.morePostsTv);
        userPostsRecycler = view.findViewById(R.id.userPostsRecycler);
        userPoemsLayout = view.findViewById(R.id.userPoemsLayout);
        morePoemsTv = view.findViewById(R.id.morePoemsTv);
        userPoemsRecycler = view.findViewById(R.id.userPoemsRecycler);
        userBooksLayout = view.findViewById(R.id.userBooksLayout);
        moreBooksTv = view.findViewById(R.id.moreBooksTv);
        userBooksRecycler = view.findViewById(R.id.userBooksRecycler);
        verifiedImage = view.findViewById(R.id.verifiedImage);
        followingCard = view.findViewById(R.id.followingCard);
        followersCard = view.findViewById(R.id.followersCard);

        //init firebase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //assert that current user id not empty
        assert firebaseUser != null;
        currUserId = firebaseUser.getUid();


        reference = FirebaseDatabase.getInstance().getReference("Users").child(currUserId);

        novelNumRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Stories");

        poemRef = FirebaseDatabase.getInstance().getReference("Poems");

        storyRef = FirebaseDatabase.getInstance().getReference("Novels").child("Stories");

        postRef = FirebaseDatabase.getInstance().getReference("Posts");

        viewPoemRef = FirebaseDatabase.getInstance().getReference("Poems");

        followersRef = FirebaseDatabase.getInstance().getReference("Follow").child(currUserId).child("followers");
        followingRef = FirebaseDatabase.getInstance().getReference("Follow").child(currUserId).child("following");

        refreshProfileFragment.setOnRefreshListener(this);

        settingImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                requireActivity().finish();

            }
        });

        writeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), ChoosePostOrPoemActivity.class));
                requireActivity().finish();
            }
        });


        userPostsRecycler.setHasFixedSize(true);
        userPostsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        userPoemsRecycler.setHasFixedSize(true);
        userPoemsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        userBooksRecycler.setHasFixedSize(true);
        userBooksRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        postModelList = new ArrayList<>();
        poemModelList = new ArrayList<>();
        storyModelList = new ArrayList<>();




        getData();

        morePostsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMore();
            }
        });

        morePoemsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMore();

            }
        });

        moreBooksTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMore();

            }
        });

        followingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), ViewFollowUsersActivity.class);
                intent.putExtra("userId", currUserId);
                intent.putExtra("which", "following");
                startActivity(intent);
            }
        });

        followersCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), ViewFollowUsersActivity.class);
                intent.putExtra("userId", currUserId);
                intent.putExtra("which", "followers");
                startActivity(intent);
            }
        });

        return view;
    }

    private void goToMore() {
        Intent intent = new Intent(requireContext(), MoreProfileActivity.class);
        intent.putExtra("userId", currUserId);
        intent.putExtra("from", "fragmentProfile");
        startActivity(intent);
    }

    private void getData() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if (snapshot.exists()) {
                    if (getActivity() == null) {
                        return;
                    }

                    UserModel userModel = snapshot.getValue(UserModel.class);

                    //get data from firebase and store it to string
                    assert userModel != null;
                    currUserName = "@" + userModel.getUsername();


                    if (snapshot.hasChild("isAdmin")) {
                        verifiedImage.setVisibility(View.VISIBLE);
                    }
                    //set data to views
                    usernameTv.setText(currUserName);

                    if (userModel.getUserProfileImg() != null) {
                        Glide.with(Objects.requireNonNull(getActivity())).load(userModel.getUserProfileImg()).into(profileImg);
                    }


                    if (userModel.getUserFullName() != null) {
                        if (userModel.getUserFullName().equals("")) {
                            userFullNameTv.setText(userModel.getUsername());
                        } else {
                            userFullNameTv.setText(userModel.getUserFullName());
                        }
                    } else {
                        userFullNameTv.setText(userModel.getUsername());
                    }

                    if (userModel.getUserBio() != null) {
                        if (!userModel.getUserBio().equals("")) {
                            bioCard.setVisibility(View.VISIBLE);
                            bioTv.setText(userModel.getUserBio());
                        }
                    }

                    if (userModel.getCoverNumber() == null) {
                        coverImg.setImageResource(R.drawable.cover1);

                    } else {
                        setUserCoverImage(userModel.getCoverNumber());
                    }

                    profileMainLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        novelNumRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int counter = 0;

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        StoryModel storyModel = snapshot.child("data").getValue(StoryModel.class);
                        assert storyModel != null;

                        if (storyModel.getStoryWriter() != null) {
                            if (storyModel.getStoryWriter().equals(currUserId)){
                                counter++;
                            }
                        }
                    }

                }

                novelNumTv.setText(String.valueOf(counter));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;

                if (snapshot.exists()) {
                    counter = (int) snapshot.getChildrenCount();
                }


                followNumTv.setText(String.valueOf(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;

                if (snapshot.exists()) {
                    counter = (int) snapshot.getChildrenCount();
                }

                followingNumTv.setText(String.valueOf(counter));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        poemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), currUserId)) {
                            count++;
                        }
                    }


                }
                poemsNumTv.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), currUserId)) {
                            PostModel postModel = dataSnapshot.child("data").getValue(PostModel.class);
                            postModelList.add(postModel);
                        }

                    }
                    if (postModelList.size() >= 10) {
                        morePostsTv.setVisibility(View.VISIBLE);
                    }

                    postAdapter = new PostAdapter(getContext(), postModelList);
                    userPostsRecycler.setAdapter(postAdapter);
                    postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<PostModel> postModelList) {
                            Intent intent = new Intent(getContext(), ViewPostActivity.class);
                            intent.putExtra("postId", postModelList.get(position).getPostId());
                            intent.putExtra("userId", currUserId);
                            intent.putExtra("from", "viewProfile");
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    });
                    postAdapter.notifyDataSetChanged();

                    if (!postModelList.isEmpty()) {
                        userPostsLayout.setVisibility(View.VISIBLE);
                        userPostsRecycler.setVisibility(View.VISIBLE);
                    } else {
                        userPostsLayout.setVisibility(View.GONE);
                        userPostsRecycler.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewPoemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    poemModelList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("userId").getValue(String.class), currUserId)) {
                            PoemModel poemModel = dataSnapshot.child("data").getValue(PoemModel.class);
                            poemModelList.add(poemModel);
                        }

                    }
                    if (poemModelList.size() >= 10) {
                        morePoemsTv.setVisibility(View.VISIBLE);
                    }

                    poemAdapter = new PoemAdapter(getContext(), poemModelList);
                    userPoemsRecycler.setAdapter(poemAdapter);
                    poemAdapter.setOnItemClickListener(new PoemAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<PoemModel> poemModelList) {
                            Intent intent = new Intent(getContext(), ViewPoemActivity.class);
                            intent.putExtra("poemId", poemModelList.get(position).getPoemId());
                            intent.putExtra("userId", currUserId);
                            intent.putExtra("from", "viewProfile");
                            startActivity(intent);
                            requireActivity().finish();

                        }
                    });
                    poemAdapter.notifyDataSetChanged();


                    if (!poemModelList.isEmpty()) {
                        userPoemsLayout.setVisibility(View.VISIBLE);
                        userPoemsRecycler.setVisibility(View.VISIBLE);
                    } else {
                        userPoemsLayout.setVisibility(View.GONE);
                        userPoemsRecycler.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    storyModelList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(dataSnapshot.child("data").child("storyWriter").getValue(String.class), currUserId)) {
                            StoryModel storyModel = dataSnapshot.child("data").getValue(StoryModel.class);
                            assert storyModel != null;
                            if (storyModel.getStoryWriter() != null) {
                                storyModelList.add(storyModel);
                            }
                        }

                    }
                    if (storyModelList.size() >= 10) {
                        moreBooksTv.setVisibility(View.VISIBLE);
                    }
                    Collections.reverse(storyModelList);

                    latestNovelsAdapter = new LatestNovelsAdapter(getContext(), storyModelList);
                    userBooksRecycler.setAdapter(latestNovelsAdapter);
                    latestNovelsAdapter.setOnItemClickListener(new LatestNovelsAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position, List<StoryModel> storyModelList) {
                            Intent intent = new Intent(getContext(), UserViewStoryActivity.class);
                            intent.putExtra("storyId", storyModelList.get(position).getStoryId());
                            intent.putExtra("from", "viewProfile");
                            intent.putExtra("userId", currUserId);
                            startActivity(intent);
                        }
                    });
                    latestNovelsAdapter.notifyDataSetChanged();

                    if (!storyModelList.isEmpty()) {
                        userBooksLayout.setVisibility(View.VISIBLE);
                        userBooksRecycler.setVisibility(View.VISIBLE);
                    } else {
                        userBooksLayout.setVisibility(View.GONE);
                        userBooksRecycler.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setUserCoverImage(Integer number) {
        if (number == 1) {
            coverImg.setImageResource(R.drawable.cover1);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
        } else if (number == 2) {
            coverImg.setImageResource(R.drawable.cover2);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
        } else if (number == 3) {
            coverImg.setImageResource(R.drawable.cover3);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));
        } else if (number == 4) {
            coverImg.setImageResource(R.drawable.cover4);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));
        } else if (number == 5) {
            coverImg.setImageResource(R.drawable.cover5);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));

        } else if (number == 6) {
            coverImg.setImageResource(R.drawable.cover6);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
        } else if (number == 7) {
            coverImg.setImageResource(R.drawable.cover7);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));
        } else if (number == 8) {
            coverImg.setImageResource(R.drawable.cover8);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysWhite));

        } else if (number == 9) {
            coverImg.setImageResource(R.drawable.cover9);
            userFullNameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
            usernameTv.setTextColor(Objects.requireNonNull(getContext()).getColor(R.color.colorAlwaysBlack));
        }
    }


    private void checkLightMode() {
        sharedPref = new SharedPref(Objects.requireNonNull(getContext()));
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }

    @Override
    public void onRefresh() {
        getData();
        refreshProfileFragment.setRefreshing(false);
    }

}