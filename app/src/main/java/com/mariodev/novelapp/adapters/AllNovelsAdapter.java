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

public class AllNovelsAdapter extends RecyclerView.Adapter<AllNovelsAdapter.ViewHolder>  {

    final Context mContext;
    List<StoryModel> storyList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position , List<StoryModel> storyModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public AllNovelsAdapter(Context mContext, List<StoryModel> storyList) {
        this.mContext = mContext;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.all_novels_item , parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final StoryModel storyModel = storyList.get(position);

        holder.storyNameTv.setText(storyModel.getStoryName());
        Glide.with(mContext.getApplicationContext())
                .load(storyModel.getStoryImage())
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

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public void filterList (List<StoryModel> list){
        storyList = list;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView storyNameTv;
        final ImageView storyImg;
        final ProgressBar loadImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);
            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            storyImg = itemView.findViewById(R.id.storyImg);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position , storyList);
                }
            }
        }
    }
}
