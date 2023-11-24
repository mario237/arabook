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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;
import com.mariodev.novelapp.models.NewsModel;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    final Context context;
    final List<NewsModel> newsModelList;
    DatabaseReference viewNewsReference;
    String currUserId;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position, List<NewsModel> newsModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public NewsAdapter(Context context, List<NewsModel> newsModelList) {
        this.context = context;
        this.newsModelList = newsModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final NewsModel newsModel = newsModelList.get(position);

        holder.newsTitleTv.setText(newsModel.getNewsTitle());

        holder.newsTextTv.setText(MyApplication.fromHtml(newsModel.getNewsText()));

        holder.newsDateTimeTv.setText(newsModel.getNewsDateTime());


        Glide.with(context.getApplicationContext())
                .load(newsModel.getNewsImage())
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
                .into(holder.newsImg);

        currUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        viewNewsReference = FirebaseDatabase.getInstance().getReference("News").child(newsModel.getNewsId()).child("views");

        viewNewsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild(currUserId)) {
                        holder.seenNewsImg.setColorFilter(context.getResources().getColor( R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                    else {
                        holder.seenNewsImg.setColorFilter(context.getResources().getColor( R.color.colorTextHint), android.graphics.PorterDuff.Mode.SRC_IN);
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
        return newsModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView  newsTitleTv;
        final TextView newsTextTv;
        final TextView newsDateTimeTv;
        final ImageView newsImg;
        final ImageView seenNewsImg;
        final ProgressBar loadImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);
            newsTitleTv = itemView.findViewById(R.id.newsTitleTv);
            newsTextTv = itemView.findViewById(R.id.newsTextTv);
            newsDateTimeTv = itemView.findViewById(R.id.newsDateTimeTv);
            newsImg = itemView.findViewById(R.id.newsImg);
            seenNewsImg = itemView.findViewById(R.id.seenNewsImg);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position , newsModelList);
                }
            }
        }
    }
}
