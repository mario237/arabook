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
import com.mariodev.novelapp.models.PoemModel;
import com.mariodev.novelapp.models.UserModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class PoemAdapter extends RecyclerView.Adapter<PoemAdapter.ViewHolder> {

    final Context context;
    final List<PoemModel> poemModelList;
    String userId;
    DatabaseReference userRef;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, List<PoemModel> poemModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PoemAdapter(Context context, List<PoemModel> poemModelList) {
        this.context = context;
        this.poemModelList = poemModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poems_item_style, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final PoemModel poemModel = poemModelList.get(position);

        holder.poemTitleTv.setText(poemModel.getPoemTitle());

        holder.poemTextTv.setText(poemModel.getPoemText());

        userId = poemModel.getUserId();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
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
        return Math.min(poemModelList.size(), 10);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView poemTitleTv;
        final TextView poemTextTv;
        final TextView usernameTv;
        final ImageView profileImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            poemTitleTv = itemView.findViewById(R.id.poemTitleTv);
            poemTextTv = itemView.findViewById(R.id.poemTextTv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            profileImg = itemView.findViewById(R.id.profileImg);

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
    }
}
