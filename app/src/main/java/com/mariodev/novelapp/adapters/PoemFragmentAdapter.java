package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.models.UserModel;

import java.util.List;
import java.util.Objects;

public class PoemFragmentAdapter extends RecyclerView.Adapter<PoemFragmentAdapter.ViewHolder> {

    final Context context;
    final List<PoemModel> poemModelList;
    String userId , currUserId;
    DatabaseReference userRef , poemLikeRef , poemCommentRef , allPoemCommentsRef , poemViewCountRef , poemViewRef;
    int countLike = 0;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, List<PoemModel> poemModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PoemFragmentAdapter(Context context, List<PoemModel> poemModelList) {
        this.context = context;
        this.poemModelList = poemModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poems_fragment_item, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final PoemModel poemModel = poemModelList.get(position);

        holder.poemTitleTv.setText(poemModel.getPoemTitle());

        holder.poemTextTv.setText(poemModel.getPoemText());

        userId = poemModel.getUserId();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    assert userModel != null;
                    holder.usernameTv.setText(userModel.getUsername());

                    if (userModel.getUserProfileImg() != null){
                        Glide.with(context.getApplicationContext()).load(userModel.getUserProfileImg()).into(holder.profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        poemLikeRef = FirebaseDatabase.getInstance().getReference("PoemsLikes");

        poemViewRef = FirebaseDatabase.getInstance().getReference("PoemsViews");

        poemViewCountRef = poemViewRef.child(poemModel.getPoemId());



        poemLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countLike = 0;
                if (snapshot.hasChild(currUserId)) {
                    countLike = (int) snapshot.getChildrenCount();
                }
                holder.likeTv.setText(String.valueOf(countLike));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        poemCommentRef = FirebaseDatabase.getInstance().getReference("Poems");


        holder.setPoemLikesCount(poemModel.getPoemId());

        holder.setPoemCommentsCount(poemModel.getPoemId());

        holder.setPoemViewsCount();

    }

    @Override
    public int getItemCount() {
       return poemModelList.size();
    }

    @SuppressWarnings("deprecation")
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView poemTitleTv;
        final TextView poemTextTv;
        final TextView usernameTv;
        final TextView seenTv;
        final TextView likeTv;
        final TextView commentTv;
        final ImageView profileImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            poemTitleTv = itemView.findViewById(R.id.poemTitleTv);
            poemTextTv = itemView.findViewById(R.id.poemTextTv);
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
                    mListener.onItemClick(position , poemModelList);
                }
            }
        }

        private void setPoemLikesCount(final String postId) {

            poemLikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    countLike = (int) snapshot.child(postId).getChildrenCount();
                    likeTv.setText(String.valueOf(countLike));
                    likeTv.setTextColor(context.getResources().getColor( R.color.darkGreyColor));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        private void setPoemCommentsCount(String postId){
            allPoemCommentsRef = poemCommentRef.child(postId).child("comments");

            allPoemCommentsRef.addValueEventListener(new ValueEventListener() {
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

        private void setPoemViewsCount(){
            poemViewCountRef.addValueEventListener(new ValueEventListener() {
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
