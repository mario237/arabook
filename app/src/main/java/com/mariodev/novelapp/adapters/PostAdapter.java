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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.models.PostModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    final Context context;
    final List<PostModel> postModelList;
    String userId;
    DatabaseReference userRef;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, List<PostModel> postModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PostAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item_style, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final PostModel postModel = postModelList.get(position);


        holder.postTextTv.setText(postModel.getPostText());

        userId = postModel.getUserId();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);

                    assert userModel != null;
                    holder.usernameTv.setText(userModel.getUsername());

                    if (userModel.getUserProfileImg() != null){
                        Glide.with(context.getApplicationContext()).load(userModel.getUserProfileImg())
                                .into(holder.profileImg);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return Math.min(postModelList.size(), 10);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView  postTextTv;
        final TextView usernameTv;
        final ImageView profileImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postTextTv = itemView.findViewById(R.id.postTextTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            profileImg = itemView.findViewById(R.id.profileImg);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position , postModelList);
                }
            }
        }
    }
}
