package com.mariodev.novelapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.mariodev.novelapp.adapters.NewsCommentAdapter;
import com.mariodev.novelapp.helpers.LocaleHelper;
import com.mariodev.novelapp.helpers.SharedPref;
import com.mariodev.novelapp.models.CommentModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class NewsCommentsActivity extends AppCompatActivity implements NewsCommentAdapter.OnDeleteClickListener{
    SharedPref sharedPref;

    String newsId , userId;
    ImageView arrowBack;
    RecyclerView commentsRecycler;
    RelativeLayout bottomCommentLayout;
    ProgressBar loadAllComments;
    ImageButton btn_send;
    TextView noCommentsTv;
    EditText writeCommentEdt;
    DatabaseReference commentRef ,  deletedCommentRef;
    List<CommentModel> commentModelList;
    NewsCommentAdapter newsCommentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_comments);


        newsId = getIntent().getStringExtra("newsId");

        arrowBack = findViewById(R.id.arrowBack);
        noCommentsTv = findViewById(R.id.noCommentsTv);
        commentsRecycler = findViewById(R.id.commentsRecycler);
        bottomCommentLayout = findViewById(R.id.bottomCommentLayout);
        loadAllComments = findViewById(R.id.loadAllComments);
        writeCommentEdt = findViewById(R.id.writeCommentEdt);
        btn_send = findViewById(R.id.btn_send);

        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        commentRef = FirebaseDatabase.getInstance().getReference("News")
                .child(newsId).child("comments");

        commentModelList = new ArrayList<>();

        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    commentModelList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CommentModel commentModel = dataSnapshot.getValue(CommentModel.class);
                        commentModelList.add(commentModel);
                    }

                    newsCommentAdapter = new NewsCommentAdapter(NewsCommentsActivity.this,
                            commentModelList , "comments");
                    commentsRecycler.setAdapter(newsCommentAdapter);
                    commentsRecycler.smoothScrollToPosition(commentModelList.size() - 1);
                    newsCommentAdapter.notifyDataSetChanged();
                    newsCommentAdapter.setOnDeleteClickListener(NewsCommentsActivity.this);

                    noCommentsTv.setVisibility(View.GONE);
                    loadAllComments.setVisibility(View.GONE);
                    commentsRecycler.setVisibility(View.VISIBLE);
                } else {
                    loadAllComments.setVisibility(View.GONE);
                    noCommentsTv.setVisibility(View.VISIBLE);
                }
                bottomCommentLayout.setVisibility(View.VISIBLE);
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
                sendComment();
            }
        });
    }

    private void sendComment() {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM");
        final String currentDate = simpleDateFormat2.format(calendar.getTime());
        final String currentTime = simpleDateFormat.format(calendar.getTime());


        String key = commentRef.push().getKey();
        CommentModel commentModel = new CommentModel();

        commentModel.setCommentId(key);
        commentModel.setCommentDateTime(currentDate + " " + currentTime);
        commentModel.setPoemOrPostId(newsId);
        commentModel.setCommentText(writeCommentEdt.getText().toString().trim());
        commentModel.setUserId(userId);

        assert key != null;
        commentRef.child(key).setValue(commentModel);
        writeCommentEdt.setText("");
        writeCommentEdt.setHint(getResources().getString(R.string.type_your_comment));    }

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
        deletedCommentRef =FirebaseDatabase.getInstance().getReference("News")
                .child(newsId).child("comments");

        AlertDialog.Builder builder = new AlertDialog.Builder(NewsCommentsActivity.this , R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.delete_warning));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                deletedCommentRef.child(modelList.get(position).getCommentId()).removeValue();
                commentModelList.clear();
                newsCommentAdapter.notifyDataSetChanged();
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