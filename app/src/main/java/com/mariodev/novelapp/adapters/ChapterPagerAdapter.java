package com.mariodev.novelapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.List;

public class ChapterPagerAdapter extends PagerAdapter {

    private final Context context;
    private final List<ChapterModel> chapterModelList;
    private final LayoutInflater layoutInflater;
    DatabaseReference  viewRef , voteRef , commentRef;



    public ChapterPagerAdapter(Context context, List<ChapterModel> chapterModelList) {
        this.context = context;
        this.chapterModelList = chapterModelList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return chapterModelList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.view_chapter_item_sytle , container , false);

        ImageView chapterImg = view.findViewById(R.id.chapterImg);
        TextView chapterNameTv = view.findViewById(R.id.chapterNameTv);
        final TextView chapterTextTv = view.findViewById(R.id.chapterTextTv);
        final TextView seenTv = view.findViewById(R.id.seenTv);
        final TextView voteTv = view.findViewById(R.id.voteTv);
        final TextView commentTv = view.findViewById(R.id.commentTv);


        chapterTextTv.setMovementMethod(new ScrollingMovementMethod());


        final ChapterModel chapterModel = chapterModelList.get(position);

        Glide.with(context.getApplicationContext()).load(chapterModel.getChapterImage())
                .into(chapterImg);


        chapterNameTv.setText(chapterModel.getChapterName());
        chapterTextTv.setText(MyApplication.fromHtml(chapterModel.getChapterText()));


        viewRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(chapterModel.getStoryId()).child(chapterModel.getChapterId()).child("views");

        viewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int count = (int) snapshot.getChildrenCount();
                seenTv.setText(String.valueOf(count));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        voteRef = FirebaseDatabase.getInstance().getReference("Novels").child("Chapters")
                .child(chapterModel.getStoryId()).child(chapterModel.getChapterId()).child("votes");

        voteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

               if (snapshot.hasChildren()){
                   int count = (int) snapshot.getChildrenCount();
                   voteTv.setText(String.valueOf(count));
               }else {
                   voteTv.setText(context.getResources().getString(R.string._0));

               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        commentRef = FirebaseDatabase.getInstance().getReference("Novels")
                .child("Chapters").child(chapterModel.getStoryId()).child(chapterModel.getChapterId()).child("comments");

        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChildren()){
                    int count = (int) snapshot.getChildrenCount();
                    commentTv.setText(String.valueOf(count));
                }else {
                    commentTv.setText(context.getResources().getString(R.string._0));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }



}
