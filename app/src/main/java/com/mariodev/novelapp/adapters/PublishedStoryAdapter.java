package com.mariodev.novelapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.mariodev.novelapp.activities.PublishedStoryActivity;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class PublishedStoryAdapter extends RecyclerView.Adapter<PublishedStoryAdapter.ViewHolder> {
    private final Context mContext;
    final Activity mActivity;
    private final List<StoryModel> mPublishedList;

    public PublishedStoryAdapter(Context mContext, Activity mActivity, List<StoryModel> mPublishedList) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mPublishedList = mPublishedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.draft_container, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final StoryModel storyModel =mPublishedList.get(position);
        holder.draftStoryName.setText(storyModel.getStoryName());
        holder.draftStoryType.setText(storyModel.getStoryType());
        Glide.with(mContext.getApplicationContext()).load(storyModel.getStoryImage())
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
                .into(holder.draftStoryImg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToStoryDetails = new Intent(mContext , PublishedStoryActivity.class);
                intentToStoryDetails.putExtra("storyId" , storyModel.getStoryId());
                mActivity.startActivity(intentToStoryDetails);
                mActivity.overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);
                mActivity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPublishedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView draftStoryImg;
        final TextView draftStoryName;
        final TextView draftStoryType;
        final ProgressBar loadImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);
            draftStoryImg = itemView.findViewById(R.id.storyCover);
            draftStoryName = itemView.findViewById(R.id.storyNameDraft);
            draftStoryType = itemView.findViewById(R.id.storyTypeDraft);
        }
    }
}

