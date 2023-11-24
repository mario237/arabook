package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.adapters.ReviewsAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.CommentModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserReviewsActivity extends AppCompatActivity implements ReviewsAdapter.OnDeleteClickListener {
    SharedPref sharedPref;

    String storyId , userId;
    ImageView arrowBack;
    EditText writeReviewEdt;
    ImageButton btn_send;
    CheckBox spoilersState;
    TextView countOfReviewsTv;
    RecyclerView reviewsRecycler;
    ProgressBar loadAllReviews;
    DatabaseReference reviewRef , deletedReviewRef , deletedReplyRef;
    List<CommentModel> reviewsList;
    ReviewsAdapter reviewsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reviews);

        storyId = getIntent().getStringExtra("storyId");
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        arrowBack = findViewById(R.id.arrowBack);
        writeReviewEdt = findViewById(R.id.writeReviewEdt);
        btn_send = findViewById(R.id.btn_send);
        spoilersState = findViewById(R.id.spoilersState);
        countOfReviewsTv = findViewById(R.id.countOfReviewsTv);
        reviewsRecycler = findViewById(R.id.reviewsRecycler);
        loadAllReviews = findViewById(R.id.loadAllReviews);

        reviewsRecycler.setHasFixedSize(true);
        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));


        reviewRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Reviews").child(storyId);


        reviewsList = new ArrayList<>();



        reviewRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
                    reviewsList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        CommentModel commentModel = dataSnapshot.getValue(CommentModel.class);
                        reviewsList.add(commentModel);
                    }



                    Collections.reverse(reviewsList);


                    reviewsAdapter = new ReviewsAdapter(UserReviewsActivity.this, reviewsList);
                    reviewsRecycler.setAdapter(reviewsAdapter);
                    reviewsRecycler.smoothScrollToPosition(0);
                    reviewsAdapter.notifyDataSetChanged();
                    reviewsAdapter.setOnDeleteClickListener(UserReviewsActivity.this);

                    countOfReviewsTv.setText((int) snapshot.getChildrenCount() + " " + getResources().getString(R.string.reviews));

                    loadAllReviews.setVisibility(View.GONE);
                    reviewsRecycler.setVisibility(View.VISIBLE);
//                } else {
//                    loadAllReviews.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                sendReview();
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendReview() {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM");
        final String currentDate = simpleDateFormat2.format(calendar.getTime());
        final String currentTime = simpleDateFormat.format(calendar.getTime());


        String key = reviewRef.push().getKey();
        CommentModel commentModel = new CommentModel();

        commentModel.setCommentId(key);
        commentModel.setCommentDateTime(currentDate + " " + currentTime);
        commentModel.setStoryId(storyId);
        commentModel.setCommentText(writeReviewEdt.getText().toString().trim());
        commentModel.setUserId(userId);
        commentModel.setSpoilersState(spoilersState.isChecked());


        assert key != null;
        reviewRef.child(key).setValue(commentModel);
        writeReviewEdt.setText("");
        writeReviewEdt.setHint(getResources().getString(R.string.add_review));
        spoilersState.setChecked(false);
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    private void checkLightMode() {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadLightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
    }


    @Override
    public void onDeleteClick(final int position, final List<CommentModel> modelList) {

        deletedReviewRef = FirebaseDatabase.getInstance().getReference("Novels").child("Reviews").child(storyId);
        deletedReplyRef = FirebaseDatabase.getInstance().getReference("Novels").child("ReviewsReply");

        AlertDialog.Builder builder = new AlertDialog.Builder(UserReviewsActivity.this , R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.delete_warning));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                deletedReviewRef.child(modelList.get(position).getCommentId()).removeValue();
                deletedReplyRef.child(modelList.get(position).getCommentId()).child("replies").removeValue();
                    reviewsList.clear();
                    reviewsAdapter.notifyDataSetChanged();
                countOfReviewsTv.setText(reviewsList.size() + " " + getResources().getString(R.string.reviews));

                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel) , null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base , "ar"));
    }


}