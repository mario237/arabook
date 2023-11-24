package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mariodev.novelapp.activities.MainPageActivity;
import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.MyApplication;
import com.mariodev.novelapp.R;

import java.util.List;
import java.util.Objects;

public class PublishedChapterAdapter extends RecyclerView.Adapter<PublishedChapterAdapter.ViewHolder> {
    private final Context mContext;
    private final List<ChapterModel> mPublishedList;
    private OnItemClickListener mListener;
    DatabaseReference chapterRef;
    final String storyImageUri;

    public interface OnItemClickListener {
        void onItemClick(int position , List<ChapterModel> chapterList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PublishedChapterAdapter(Context mContext, List<ChapterModel> mPublishedList, String storyImageUri) {
        this.mContext = mContext;
        this.mPublishedList = mPublishedList;
        this.storyImageUri = storyImageUri;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.chapter_draft_item, parent, false);
        return new PublishedChapterAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ChapterModel chapterModel =mPublishedList.get(position);


        holder.publishedChapterName.setText(chapterModel.getChapterName());
        Glide.with(mContext.getApplicationContext()).load(chapterModel.getChapterImage())
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
                .into(holder.publishedChapterImg);

        chapterRef = FirebaseDatabase.getInstance().getReference("Novels");

        holder.deleteChapterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mPublishedList.size() == 1){
                    holder.deleteStoryDialog(chapterModel.getStoryId());
                }else {
                    deleteChapter(chapterRef , chapterModel);
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return mPublishedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView publishedChapterImg;
        final ImageView deleteChapterIcon;
        final TextView publishedChapterName;
        final ProgressBar loadImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.loadImage);
            publishedChapterImg = itemView.findViewById(R.id.draftChapterImg);
            publishedChapterName = itemView.findViewById(R.id.draftChapterName);
            deleteChapterIcon = itemView.findViewById(R.id.deleteChapterIcon);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position , mPublishedList);
                }
            }
        }

        private void deleteStoryDialog(final String storyId) {


            final DatabaseReference storyReference = FirebaseDatabase.getInstance().getReference("Novels")
                    .child("Stories").child(storyId);
            final DatabaseReference chapterReference = FirebaseDatabase.getInstance().getReference("Novels")
                    .child("Chapters").child(storyId);

            final DatabaseReference publishedRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .child("Published");



            assert storyImageUri != null;
            final StorageReference storyPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(storyImageUri);

            if (!mPublishedList.isEmpty()) {
                for (int position = 0; position < mPublishedList.size(); position++) {
                    StorageReference chapterPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(mPublishedList.get(position).getChapterImage());
                    chapterPhotoRef.delete();
                }

            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.DialogTheme);
            String message = mContext.getResources().getString(R.string.delete_story_message);
            builder.setMessage(message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (MyApplication.isOnline(mContext.getApplicationContext())) {
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            } else {
                                chapterReference.removeValue();


                                storyReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        publishedRef.child(storyId).removeValue();
                                        storyPhotoRef.delete();
                                        Intent intent = new Intent(mContext, MainPageActivity.class);
                                        intent.putExtra("fragment", "MyNovel");
                                        mContext.startActivity(intent);

                                    }
                                });
                            }


                        }
                    })
                    .setNegativeButton(R.string.no, null);
            AlertDialog dialog = builder.create();
            dialog.show();

        }

    }

    private void deleteChapter(DatabaseReference databaseReference , ChapterModel chapterModel){
        databaseReference = databaseReference.child("Chapters").child(chapterModel.getStoryId())
        .child(chapterModel.getChapterId());

        if (storyImageUri!=null && storyImageUri.equals(chapterModel.getChapterImage())){


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext ,  R.style.DialogTheme);
            String message = mContext.getResources().getString(R.string.delete_chapter_message);
            final DatabaseReference finalDatabaseReference = databaseReference;
            builder.setMessage(message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (MyApplication.isOnline(mContext)){
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            }else {
                                finalDatabaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                        }
                    })
                    .setNegativeButton(R.string.no, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else {
            final StorageReference photoRef = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(chapterModel.getChapterImage());

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext ,  R.style.DialogTheme);
            String message = mContext.getResources().getString(R.string.delete_chapter_message);
            final DatabaseReference finalDatabaseReference = databaseReference;
            builder.setMessage(message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (MyApplication.isOnline(mContext)){
                                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                            }else {
                                finalDatabaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        notifyDataSetChanged();
                                        photoRef.delete();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }


                        }
                    })
                    .setNegativeButton(R.string.no, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }


    }




}

