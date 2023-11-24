package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class BookFragmentAdapter extends RecyclerView.Adapter<BookFragmentAdapter.ViewHolder> {

    final Context context;
    final List<StoryModel> storyModelList;
    String userId;
    DatabaseReference userRef;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, List<StoryModel> poemModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public BookFragmentAdapter(Context context, List<StoryModel> storyModelList) {
        this.context = context;
        this.storyModelList = storyModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_fragment_item, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final StoryModel storyModel = storyModelList.get(position);

        if (storyModel.getStoryImage() != null){
            Glide.with(context.getApplicationContext()).load(storyModel.getStoryImage())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.loadImage.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.loadImage.setVisibility(View.GONE);
                            return false;
                        }


                    })
                    .into(holder.storyImg);
        }

        holder.bookNameTv.setText(storyModel.getStoryName());

        holder.bookTypeTv.setText(storyModel.getStoryType());

        userId = storyModel.getStoryWriter();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    assert userModel != null;
                    holder.usernameTv.setText(userModel.getUsername());

                    if (userModel.getUserProfileImg() != null){
                        Glide.with(context)
                                .load(userModel.getUserProfileImg())
                                .into(holder.profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.seenTv.setText(String.valueOf(storyModel.getStoryViews()));

        holder.likeTv.setText(String.valueOf(storyModel.getStoryVotes()));

        holder.commentTv.setText(String.valueOf(storyModel.getStoryComments()));





    }

    @Override
    public int getItemCount() {
       return storyModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView bookNameTv;
        final TextView bookTypeTv;
        final TextView usernameTv;
        final TextView seenTv;
        final TextView likeTv;
        final TextView commentTv;
        final ImageView profileImg;
        final ImageView storyImg;
        final ProgressBar loadImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);
            storyImg = itemView.findViewById(R.id.storyImg);
            bookNameTv = itemView.findViewById(R.id.bookNameTv);
            bookTypeTv = itemView.findViewById(R.id.bookTypeTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            profileImg = itemView.findViewById(R.id.profileImg);
            seenTv = itemView.findViewById(R.id.seenTv);
            likeTv = itemView.findViewById(R.id.likeTv);
            commentTv = itemView.findViewById(R.id.commentTv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position , storyModelList);
                }
            }
        }
    }
}
