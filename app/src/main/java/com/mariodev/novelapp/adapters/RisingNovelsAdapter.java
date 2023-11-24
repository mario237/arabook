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
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class RisingNovelsAdapter extends RecyclerView.Adapter<RisingNovelsAdapter.ViewHolder> {

    final Context context;
    final List<StoryModel> storyModelList;
    private  OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position , List<StoryModel> storyModelList);
    }

    public void setOnItemClickListener( OnItemClickListener listener) {
        mListener = listener;
    }
    public RisingNovelsAdapter(Context context, List<StoryModel> storyModelList) {
        this.context = context;
        this.storyModelList = storyModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rising_novel_item, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        final StoryModel storyModel = storyModelList.get(position);

        holder.storyNameTv.setText(storyModel.getStoryName());

        Glide.with(context.getApplicationContext()).load(storyModel.getStoryImage()).into(holder.storyCoverImg);

        if (position==0){
            holder.rankImg.setVisibility(View.VISIBLE);
            holder.rankImg.setImageResource(R.drawable.medal1);
        }
        else   if (position==1){
            holder.rankImg.setVisibility(View.VISIBLE);
            holder.rankImg.setImageResource(R.drawable.medal2);
        }
        else   if (position==2){
            holder.rankImg.setVisibility(View.VISIBLE);
            holder.rankImg.setImageResource(R.drawable.medal3);
        }

    }


    @Override
    public int getItemCount() {
        return Math.min(storyModelList.size(), 10);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView storyNameTv;
        final ImageView storyCoverImg;
        final ImageView rankImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            storyCoverImg = itemView.findViewById(R.id.storyImg);
            rankImg = itemView.findViewById(R.id.rankImg);

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
