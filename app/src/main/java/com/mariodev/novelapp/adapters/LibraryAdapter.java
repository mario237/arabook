package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mariodev.novelapp.models.StoryModel;
import com.mariodev.novelapp.R;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder>  {

    final Context mContext;
    final List<StoryModel> storyList;
    public final SparseBooleanArray selectedItems = new SparseBooleanArray();
    public int currentSelectedPos;
    private LibraryAdapterListener listener;

    public interface LibraryAdapterListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
        void onStoryClick(int position);

    }

    public void setListener(LibraryAdapterListener listener) {
        this.listener = listener;
    }



    public LibraryAdapter(Context mContext, List<StoryModel> storyList) {
        this.mContext = mContext;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.library_item , parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final StoryModel storyModel = storyList.get(position);

        holder.storyNameTv.setText(storyModel.getStoryName());
        Glide.with(mContext.getApplicationContext()).load(storyModel.getStoryImage())
                .into(holder.storyImg);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedItems.size() > 0 && listener != null)
                    listener.onItemClick(position);
                else {
                    assert listener != null;
                    listener.onStoryClick(position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (listener != null)
                    listener.onItemLongClick(position);
                return true;
            }
        });

        if (currentSelectedPos == position) currentSelectedPos = -1;

        if (storyModel.isSelected()){
            holder.libraryItemSelect.setVisibility(View.VISIBLE);
            holder.libraryItemSelect.setChecked(true);
        }else {
            holder.libraryItemSelect.setVisibility(View.GONE);
            holder.libraryItemSelect.setChecked(false);
        }

    }


    @Override
    public int getItemCount() {
        return storyList.size();
    }



    public void toggleSelection(int position) {
        currentSelectedPos = position;
        if (selectedItems.get(position)) {
            selectedItems.delete(position);
            storyList.get(position).setSelected(false);
        } else {
            selectedItems.put(position, true);
            storyList.get(position).setSelected(true);
        }
        notifyItemChanged(position);
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView storyNameTv;
        final ImageView storyImg;
        final CheckBox libraryItemSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            storyImg = itemView.findViewById(R.id.storyImg);
            libraryItemSelect = itemView.findViewById(R.id.libraryItemSelect);

        }


    }
}
