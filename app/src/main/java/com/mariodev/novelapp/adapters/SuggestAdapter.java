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
import com.google.firebase.database.annotations.Nullable;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.ViewHolder> {

    final Context context;
    final List<StoryModel> storyModelList;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position , List<StoryModel> storyModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public SuggestAdapter(Context context, List<StoryModel> storyModelList) {
        this.context = context;
        this.storyModelList = storyModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.suggest_item, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final StoryModel storyModel = storyModelList.get(position);

        holder.storyNameTv.setText(storyModel.getStoryName());
        holder.storyDescTv.setText(storyModel.getStoryDescription());
        holder.storyTypeTv.setText(storyModel.getStoryType());

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
                .into(holder.storyCoverImg);

    }

    @Override
    public int getItemCount() {
        return Math.min(storyModelList.size(), 10);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView storyNameTv;
        final TextView storyDescTv;
        final TextView storyTypeTv;
        final ImageView storyCoverImg;
        final ProgressBar loadImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);

            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            storyDescTv = itemView.findViewById(R.id.storyDescTv);
            storyTypeTv = itemView.findViewById(R.id.storyTypeTv);
            storyCoverImg = itemView.findViewById(R.id.storyCoverImg);

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
