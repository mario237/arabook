package com.mariodev.novelapp.adapters;

import android.annotation.SuppressLint;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.annotations.Nullable;
import com.mariodev.novelapp.activities.UserViewStoryActivity;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class SliderAdapter  extends
        SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private final Context context;
    private final List<StoryModel> storyModelList;
    final Activity activity;

    public SliderAdapter(Context context, List<StoryModel> storyModelList, Activity activity) {
        this.context = context;
        this.storyModelList = storyModelList;
        this.activity = activity;
    }



    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        @SuppressLint("InflateParams") View inflate = LayoutInflater.from(context).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(final SliderAdapterVH viewHolder, final int position) {



        final StoryModel storyModel= storyModelList.get(position);

        Glide.with(context)
                .load(storyModel.getStoryImage())
                .into(viewHolder.imageViewBackground);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context , UserViewStoryActivity.class);
                intent.putExtra("storyId",storyModel.getStoryId());
                intent.putExtra("from","home");
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right , R.anim.slide_out_left);

            }
        });
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return storyModelList.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        final View itemView;
        final ImageView imageViewBackground;
//        final ProgressBar loadImage;

        public SliderAdapterVH(View itemView) {
            super(itemView);
//            loadImage = itemView.findViewById(R.id.loadImage);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            this.itemView = itemView;
        }
    }

}