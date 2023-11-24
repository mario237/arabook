package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.activities.NewsCommentsReplyActivity;
import com.mariodev.novelapp.activities.UserViewProfileActivity;
import com.mariodev.novelapp.models.CommentModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.R;

import java.util.List;
import java.util.Objects;

public class NewsCommentAdapter extends RecyclerView.Adapter<NewsCommentAdapter.ViewHolder> {

    final Context context;
    final List<CommentModel> commentModelList;
    final String whichActivity;

    DatabaseReference userRef , allRepliesRef;
    String userId;
    private OnDeleteClickListener mListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, List<CommentModel> modelList);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        mListener = listener;
    }

    public NewsCommentAdapter(Context context, List<CommentModel> commentModelList, String whichActivity) {
        this.context = context;
        this.commentModelList = commentModelList;
        this.whichActivity = whichActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item_style , parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final CommentModel commentModel = commentModelList.get(position);

        holder.commentTextTv.setText(commentModel.getCommentText());
        holder.commentDateTimeTv.setText(commentModel.getCommentDateTime());

        userRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(commentModel.getUserId());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    assert userModel != null;
                    holder.usernameTv.setText(userModel.getUsername());


                    if (snapshot.hasChild("isAdmin")){
                        holder.verifiedImage.setVisibility(View.VISIBLE);
                    }

                    if (userModel.getUserProfileImg() != null){
                        Glide.with(context.getApplicationContext()).load(userModel.getUserProfileImg())
                                .into(holder.userImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        allRepliesRef = FirebaseDatabase.getInstance().getReference("News")
                .child(commentModel.getPoemOrPostId()).child("comments").child(commentModel.getCommentId())
                .child("replies");



        allRepliesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                if (snapshot.hasChildren()){
                    count = (int) snapshot.getChildrenCount();
                }
                holder.replyCountTv.setText(String.valueOf(count));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        if (commentModel.getUserId().equals(userId)
                || userId.equals(context.getResources().getString(R.string.admin_id))){
            holder.deleteTv.setVisibility(View.VISIBLE);
        }



        if (whichActivity.equals("reply")){
            holder.replyBtn.setVisibility(View.GONE);
            holder.replyLayout.setVisibility(View.GONE);
        }else {
            holder.replyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToReply(commentModel);
                }
            });

            holder.replyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToReply(commentModel);
                }
            });
        }
        holder.userImgCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context , UserViewProfileActivity.class);
                intent.putExtra("userId",commentModel.getUserId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final CardView userImgCard;
        final ImageView userImg;
        final ImageView verifiedImage;
        final TextView usernameTv;
        final TextView commentTextTv;
        final TextView commentDateTimeTv;
        final TextView replyCountTv;
        final TextView deleteTv;
        final ImageButton replyBtn;
        final RelativeLayout replyLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImgCard = itemView.findViewById(R.id.userImgCard);
            verifiedImage = itemView.findViewById(R.id.verifiedImage);
            userImg = itemView.findViewById(R.id.userImg);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            commentTextTv = itemView.findViewById(R.id.commentTextTv);
            commentDateTimeTv = itemView.findViewById(R.id.commentDateTimeTv);
            replyCountTv = itemView.findViewById(R.id.replyCountTv);
            replyBtn = itemView.findViewById(R.id.replyBtn);
            replyLayout = itemView.findViewById(R.id.replyLayout);
            deleteTv = itemView.findViewById(R.id.deleteTv);

            deleteTv.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onDeleteClick(position , commentModelList);
                }
            }
        }
    }

    private void goToReply(CommentModel commentModel){
        Intent intent = new Intent(context , NewsCommentsReplyActivity.class);
        intent.putExtra("newsId",commentModel.getPoemOrPostId());
        intent.putExtra("commentId",commentModel.getCommentId());
        intent.putExtra("userId",commentModel.getUserId());
        intent.putExtra("commentText",commentModel.getCommentText());
        intent.putExtra("commentDateTime",commentModel.getCommentDateTime());
        context.startActivity(intent);
    }
}
