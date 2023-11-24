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
import com.mariodev.novelapp.activities.ReviewReplyActivity;
import com.mariodev.novelapp.activities.UserViewProfileActivity;
import com.mariodev.novelapp.models.CommentModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.R;

import java.util.List;
import java.util.Objects;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

     Context context;
    final List<CommentModel> commentModelList;
    DatabaseReference userRef , allRepliesRef;
    String userId;
    private OnDeleteClickListener mListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position , List<CommentModel> modelList);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        mListener = listener;
    }

    public ReviewsAdapter(Context context, List<CommentModel> commentModelList) {
        this.context = context;
        this.commentModelList = commentModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reviews_item_style , parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final CommentModel commentModel = commentModelList.get(position);

        holder.reviewTextTv.setText(commentModel.getCommentText());
        holder.reviewDateTimeTv.setText(commentModel.getCommentDateTime());

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

        if (commentModelList.get(position).getSpoilersState() != null && commentModelList.get(position).getSpoilersState()){
            holder.spoilersTextTv.setVisibility(View.VISIBLE);
            holder.reviewTextTv.setVisibility(View.GONE);
        }else {
            holder.spoilersTextTv.setVisibility(View.GONE);
            holder.reviewTextTv.setVisibility(View.VISIBLE);
        }

        holder.spoilersTextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.spoilersTextTv.setVisibility(View.GONE);
                holder.reviewTextTv.setVisibility(View.VISIBLE);
            }
        });


        allRepliesRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("ReviewsReply").child(commentModel.getCommentId()).child("replies");


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

        allRepliesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                  if (snapshot.hasChildren()){
                      int count = (int) snapshot.getChildrenCount();
                      holder.replyCountTv.setText(String.valueOf(count));
                  }else {
                      holder.replyCountTv.setText(context.getResources().getString(R.string._0));

                  }


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


        holder.userImgCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context , UserViewProfileActivity.class);
                intent.putExtra("userId",commentModel.getUserId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final CardView userImgCard;
        final ImageView userImg;
        final ImageView verifiedImage;
        final TextView usernameTv;
        final TextView reviewTextTv;
        final TextView spoilersTextTv;
        final TextView reviewDateTimeTv;
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
            reviewTextTv = itemView.findViewById(R.id.reviewTextTv);
            spoilersTextTv = itemView.findViewById(R.id.spoilersTextTv);
            reviewDateTimeTv = itemView.findViewById(R.id.reviewDateTimeTv);
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
        Intent intent = new Intent(context , ReviewReplyActivity.class);
        intent.putExtra("commentId",commentModel.getCommentId());
        intent.putExtra("chapterId",commentModel.getChapterId());
        intent.putExtra("storyId",commentModel.getStoryId());
        intent.putExtra("userId",commentModel.getUserId());
        intent.putExtra("commentText",commentModel.getCommentText());
        intent.putExtra("commentDateTime",commentModel.getCommentDateTime());
        context.startActivity(intent);
    }
}
