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
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class FollowUsersAdapter extends RecyclerView.Adapter<FollowUsersAdapter.ViewHolder> {

    final Context context;
    final List<UserModel> userModelList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position , List<UserModel> userModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public FollowUsersAdapter(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_follow_item , parent , false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        UserModel userModel = userModelList.get(position);

        holder.usernameTv.setText(userModel.getUsername());

        if (userModel.getUserProfileImg() != null){
            Glide.with(context.getApplicationContext()).load(userModel.getUserProfileImg()).into(holder.userImg);

        }

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView userImg;
        final TextView usernameTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.userImg);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position , userModelList);
                }
            }

        }
    }
}
