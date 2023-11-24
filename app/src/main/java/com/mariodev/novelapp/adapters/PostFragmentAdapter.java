package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.activities.PostCommentsActivity;
import com.mariodev.novelapp.models.PostModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.R;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.util.List;
import java.util.Objects;

import ru.embersoft.expandabletextview.ExpandableTextView;

public class PostFragmentAdapter extends RecyclerView.Adapter<PostFragmentAdapter.ViewHolder> {

    final Context context;
    final List<PostModel> postModelList;
    String userId, currUserId;
    DatabaseReference userRef, postLikeRef, postCommentRef, postViewRef , postViewCountRef  , allPostCommentsRef;
    Boolean clickChecker = false;
    int countLike = 0;


    public PostFragmentAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.posts_fragment_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final PostModel postModel = postModelList.get(position);


        holder.postDateTimeTv.setText(postModel.getDateTime());

        userId = postModel.getUserId();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    assert userModel != null;
                    holder.usernameTv.setText(userModel.getUsername());

                    if (userModel.getUserProfileImg() != null) {
                        Glide.with(context.getApplicationContext()).load(userModel.getUserProfileImg()).into(holder.profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        postLikeRef = FirebaseDatabase.getInstance().getReference("PostsLikes");


        postViewRef = FirebaseDatabase.getInstance().getReference("PostsViews");

        postViewCountRef = postViewRef.child(postModel.getPostId());

        postCommentRef = FirebaseDatabase.getInstance().getReference("Posts");

        holder.postTextTv.setText(postModel.getPostText());

        holder.postTextTv.setOnStateChangeListener(new ExpandableTextView.OnStateChangeListener() {
            @Override
            public void onStateChange(boolean isShrink) {
                PostModel contentItem = postModelList.get(position);
                contentItem.setShrink(isShrink);
                postModelList.set(position, contentItem);
                postViewRef.child(postModel.getPostId()).child(currUserId).setValue(currUserId);

            }
        });
        holder.postTextTv.setText(postModel.getPostText());
        holder.postTextTv.resetState(postModel.isShrink());

        holder.commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostCommentsActivity.class);
                intent.putExtra("postId", postModel.getPostId());
                context.startActivity(intent);
            }
        });

        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        holder.likePostBtn.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                clickChecker = true;
                postLikeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (clickChecker.equals(true)) {
                            if (dataSnapshot.child(postModel.getPostId()).hasChild(currUserId)) {
                                postLikeRef.child(postModel.getPostId()).child(currUserId).removeValue();
                            } else {
                                postLikeRef.child(postModel.getPostId()).child(currUserId).setValue(currUserId);

                            }
                            clickChecker = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });


        holder.setPostLikesCount(postModel.getPostId());

        holder.setPostCommentsCount(postModel.getPostId());

       holder.setPostViewsCount();

    }

    @Override
    public int getItemCount() {
        return postModelList.size();
    }

    @SuppressWarnings("deprecation")
    public class ViewHolder extends RecyclerView.ViewHolder {

        final TextView postDateTimeTv;
        final TextView usernameTv;
        final TextView seenTv;
        final TextView likeTv;
        final TextView commentTv;
        final ExpandableTextView postTextTv;
        final ImageView profileImg;
        final SparkButton likePostBtn;
        final ImageButton commentPostBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postDateTimeTv = itemView.findViewById(R.id.postDateTimeTv);
            postTextTv = itemView.findViewById(R.id.postTextTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            profileImg = itemView.findViewById(R.id.profileImg);
            seenTv = itemView.findViewById(R.id.viewPostCounterTv);
            likeTv = itemView.findViewById(R.id.likePostCounterTv);
            commentTv = itemView.findViewById(R.id.commentPostCounterTv);
            likePostBtn = itemView.findViewById(R.id.likePostBtn);
            commentPostBtn = itemView.findViewById(R.id.commentPostBtn);
        }

        private void setPostLikesCount(final String postId) {

            postLikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(postId).hasChild(currUserId)) {
                        countLike = (int) snapshot.child(postId).getChildrenCount();
                        likePostBtn.setChecked(true);
                        likeTv.setText(String.valueOf(countLike));
                        likeTv.setTextColor(ContextCompat.getColor(context , R.color.darkGreyColor));

                    } else {
                        countLike = (int) snapshot.child(postId).getChildrenCount();
                        likePostBtn.setChecked(false);
                        likeTv.setText(String.valueOf(countLike));
                        likeTv.setTextColor(context.getResources().getColor(R.color.darkGreyColor));
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void setPostCommentsCount(String postId){
            allPostCommentsRef = postCommentRef.child(postId).child("comments");

            allPostCommentsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    if (snapshot.exists()) {
                        count  = (int) snapshot.getChildrenCount();
                    }
                    commentTv.setText(String.valueOf(count));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        private void setPostViewsCount(){
            postViewCountRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = 0;
                    if (snapshot.exists()) {
                       count  = (int) snapshot.getChildrenCount();
                    }
                    seenTv.setText(String.valueOf(count));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }
}
