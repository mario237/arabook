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

public class StoryTypeAdapter extends RecyclerView.Adapter<StoryTypeAdapter.ViewHolder> {

    final Context context;
    final List<StoryModel> storyModelList;
    final String storyType;
    private  OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, List<StoryModel> storyModelList);
    }

    public void setOnItemClickListener( OnItemClickListener listener) {
        mListener = listener;
    }


    public StoryTypeAdapter(Context context, List<StoryModel> storyModelList, String storyType) {
        this.context = context;
        this.storyModelList = storyModelList;
        this.storyType = storyType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rising_novel_item, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {


        final StoryModel storyModel = storyModelList.get(position);

        holder.storyNameTv.setText(storyModel.getStoryName());

        Glide.with(context.getApplicationContext()).load(storyModel.getStoryImage())
                .into(holder.storyCoverImg);

       if (storyType.equals(context.getResources().getString(R.string.adventures))){
           if (position==0){
               holder.rankImg.setVisibility(View.VISIBLE);
               holder.rankImg.setImageResource(R.drawable.adv_1st);
           }
           else if (position==1){
               holder.rankImg.setVisibility(View.VISIBLE);
               holder.rankImg.setImageResource(R.drawable.adv_2nd);
           }
           else if (position==2){
               holder.rankImg.setVisibility(View.VISIBLE);
               holder.rankImg.setImageResource(R.drawable.adv_3rd);
           }else {
               holder.rankImg.setVisibility(View.GONE);

           }
       }

       if (storyType.equals(context.getResources().getString(R.string.comedy))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.comedy_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.comedy_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.medal3);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

       if (storyType.equals(context.getResources().getString(R.string.drama))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.drama_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.drama_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.drama_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

       if (storyType.equals(context.getResources().getString(R.string.fiction))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.fiction_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.fiction_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.fiction_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

        if (storyType.equals(context.getResources().getString(R.string.horror))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.horror_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.horror_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.horror_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

        if (storyType.equals(context.getResources().getString(R.string.historical))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.histro_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.histro_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.histro_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

        if (storyType.equals(context.getResources().getString(R.string.mystery))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.mystery_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.mystery_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.mystery_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

        if (storyType.equals(context.getResources().getString(R.string.police))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.police_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.police_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.police_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

        if (storyType.equals(context.getResources().getString(R.string.romantic))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.romantic_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.romantic_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.romantic_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

        if (storyType.equals(context.getResources().getString(R.string.psychological))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.psychological_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.psychological_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.psychological_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }

        if (storyType.equals(context.getResources().getString(R.string.science_fiction))){
            if (position==0){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.science_fiction_1st);
            }
            else if (position==1){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.science_fiction_2nd);
            }
            else if (position==2){
                holder.rankImg.setVisibility(View.VISIBLE);
                holder.rankImg.setImageResource(R.drawable.science_fiction_3rd);
            }else {
                holder.rankImg.setVisibility(View.GONE);

            }
        }


    }


    @Override
    public int getItemCount() {
        return storyModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView storyNameTv;
        final ImageView storyCoverImg;
        final ImageView rankImg;
        final ProgressBar loadImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);

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
