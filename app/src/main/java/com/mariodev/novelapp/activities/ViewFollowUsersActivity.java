package com.mariodev.novelapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.adapters.FollowUsersAdapter;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.List;

public class ViewFollowUsersActivity extends AppCompatActivity implements FollowUsersAdapter.OnItemClickListener{
    SharedPref sharedPref;

    ImageView backImg;
    TextView followStateTv;
    TextView noUsersText;
    RecyclerView usersRecycler;
    String whichOne , userId ;
    FollowUsersAdapter followUsersAdapter;
    List<UserModel> userList;

    List<String> idList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_follow_users);

        whichOne = getIntent().getStringExtra("which");
        userId = getIntent().getStringExtra("userId");

        backImg = findViewById(R.id.backImg);
        followStateTv = findViewById(R.id.followStateTv);
        usersRecycler = findViewById(R.id.usersRecycler);
        noUsersText = findViewById(R.id.noUsersText);

        usersRecycler.setHasFixedSize(true);
        usersRecycler.setLayoutManager(new LinearLayoutManager(this));

        if (whichOne.equals("following")){
            followStateTv.setText(getResources().getString(R.string.following));
        }else {
            followStateTv.setText(getResources().getString(R.string.followers));
        }

        userList = new ArrayList<>();
        followUsersAdapter = new FollowUsersAdapter(ViewFollowUsersActivity.this , userList);
        usersRecycler.setAdapter(followUsersAdapter);
        followUsersAdapter.setOnItemClickListener(this);

        idList = new ArrayList<>();

        switch (whichOne){
            case "following" : getFollowing(); break;
            case "followers" : getFollowers(); break;
        }

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void getFollowers() {
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userId).child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    idList.add(dataSnapshot.getKey());
                }
                showUsers();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowing() {
        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userId).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    idList.add(dataSnapshot.getKey());
                }
                showUsers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    for (String id : idList){
                        assert userModel != null;
                        if (userModel.getUserId()!= null && userModel.getUserId().equals(id)){
                            userList.add(userModel);
                        }
                    }
                }
                if (userList.isEmpty()){
                    noUsersText.setVisibility(View.VISIBLE);
                }else {
                    noUsersText.setVisibility(View.GONE);
                }
                followUsersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onItemClick(int position, List<UserModel> userModelList) {
        Intent intent = new Intent(ViewFollowUsersActivity.this , UserViewProfileActivity.class);
        intent.putExtra("userId" , userModelList.get(position).getUserId());
        startActivity(intent);
    }

    private void checkLightMode() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }

}