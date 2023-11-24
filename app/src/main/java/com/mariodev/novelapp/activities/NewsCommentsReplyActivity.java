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

import com.bumptech.glide.Glide;
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
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class NewsCommentsReplyActivity extends AppCompatActivity implements NewsCommentAdapter.OnDeleteClickListener{
    SharedPref sharedPref;

    String newsId, userId, commentId, userReplyId, userCommentText, userCommentDateTime, replyCountText;
    TextView replyCountTv, usernameTv, commentTextTv, commentDateTimeTv;
    ImageView userImg;
    RecyclerView commentsRecycler;
    RelativeLayout bottomCommentLayout;
    ImageButton btn_send;
    EditText writeCommentEdt;
    ProgressBar loadAllComments;
    DatabaseReference replyRef, userRef, allRepliesRef , deletedReplyRef;
    List<CommentModel> commentModelList;
    NewsCommentAdapter newsCommentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLightMode();
        MyApplication.setLocale(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_comments_reply);

        replyCountTv = findViewById(R.id.replyCountTv);
        userImg = findViewById(R.id.userImg);
        usernameTv = findViewById(R.id.usernameTv);
        commentTextTv = findViewById(R.id.commentTextTv);
        commentDateTimeTv = findViewById(R.id.commentDateTimeTv);
        commentsRecycler = findViewById(R.id.commentsRecycler);
        bottomCommentLayout = findViewById(R.id.bottomCommentLayout);
        btn_send = findViewById(R.id.btn_send);
        writeCommentEdt = findViewById(R.id.writeCommentEdt);
        loadAllComments = findViewById(R.id.loadAllComments);

        commentsRecycler.setHasFixedSize(true);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        userReplyId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        newsId = getIntent().getStringExtra("newsId");
        commentId = getIntent().getStringExtra("commentId");
        userId = getIntent().getStringExtra("userId");
        userCommentDateTime = getIntent().getStringExtra("commentDateTime");
        userCommentText = getIntent().getStringExtra("commentText");

        replyRef = FirebaseDatabase.getInstance().getReference("News")
                .child(newsId).child("comments").child(commentId);

        allRepliesRef = replyRef.child("replies");

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);


        btn_send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });

        commentModelList = new ArrayList<>();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    assert userModel != null;
                    usernameTv.setText(userModel.getUsername());

                    if (userModel.getUserProfileImg() != null) {

                        Glide.with(getApplicationContext()).load(userModel.getUserProfileImg()).centerCrop().into(userImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!userCommentText.equals("") && !userCommentDateTime.equals("")) {
            commentTextTv.setText(userCommentText);
            commentDateTimeTv.setText(userCommentDateTime);
        }

        allRepliesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    commentModelList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CommentModel commentModel = dataSnapshot.getValue(CommentModel.class);
                        commentModelList.add(commentModel);
                    }

                    newsCommentAdapter = new NewsCommentAdapter(NewsCommentsReplyActivity.this,
                            commentModelList,"reply");

                    commentsRecycler.setAdapter(newsCommentAdapter);
                    commentsRecycler.smoothScrollToPosition(commentModelList.size() - 1);
                    newsCommentAdapter.notifyDataSetChanged();
                    newsCommentAdapter.setOnDeleteClickListener(NewsCommentsReplyActivity.this);

                    loadAllComments.setVisibility(View.GONE);
                    commentsRecycler.setVisibility(View.VISIBLE);
                } else {
                    loadAllComments.setVisibility(View.GONE);

                }

                replyCountText = commentModelList.size() + " " + getResources().getString(R.string.reply);
                replyCountTv.setText(replyCountText);
                bottomCommentLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendComment() {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM");
        final String currentDate = simpleDateFormat2.format(calendar.getTime());
        final String currentTime = simpleDateFormat.format(calendar.getTime());


        String key = replyRef.push().getKey();
        CommentModel commentModel = new CommentModel();

        commentModel.setCommentId(key);
        commentModel.setCommentDateTime(currentDate + " " + currentTime);
        commentModel.setPoemOrPostId(newsId);
        commentModel.setCommentText(writeCommentEdt.getText().toString().trim());
        commentModel.setUserId(userReplyId);

        assert key != null;
        replyRef.child("replies").child(key).setValue(commentModel);
        writeCommentEdt.setText("");
        writeCommentEdt.setHint(getResources().getString(R.string.type_your_comment));

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
        deletedReplyRef = FirebaseDatabase.getInstance().getReference("News")
                .child(newsId).child("comments");

        AlertDialog.Builder builder = new AlertDialog.Builder(NewsCommentsReplyActivity.this , R.style.DialogTheme);
        builder.setMessage(getResources().getString(R.string.delete_warning));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                deletedReplyRef.child(commentId).child("replies").child(modelList.get(position).getCommentId()).removeValue();
                commentModelList.clear();
                newsCommentAdapter.notifyDataSetChanged();
                replyCountTv.setText(commentModelList.size() + " " + getResources().getString(R.string.reviews));

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