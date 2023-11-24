package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mariodev.novelapp.models.ChapterModel;
import com.mariodev.novelapp.R;

import java.util.List;

@SuppressWarnings("ALL")
public class DrawerListAdapter extends RecyclerView.Adapter<DrawerListAdapter.ViewHolder> {

    final Context context;
    final List<ChapterModel> chapterModelList;
    private OnItemClickListener mListener;
    public int selected_position = 0; // You have to set this globally in the Adapter class


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public DrawerListAdapter(Context context, List<ChapterModel> chapterModelList) {
        this.context = context;
        this.chapterModelList = chapterModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.drawer_item_style , parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChapterModel chapterModel = chapterModelList.get(position);

        holder.chapterCountTv.setText(String.valueOf(position+1));

        holder.chapterNameTv.setText(chapterModel.getChapterName());

        holder.chapterCountTv.setTextColor(selected_position == position ? context.getResources().getColor(R.color.colorAlwaysWhite) : context.getResources().getColor(R.color.colorWhite));


        holder.chapterNameTv.setTextColor(selected_position == position ? context.getResources().getColor(R.color.colorAlwaysWhite) : context.getResources().getColor(R.color.colorWhite));

        holder.itemView.setBackgroundColor(selected_position == position ? context.getResources().getColor(R.color.colorAccent) : Color.TRANSPARENT);




    }


    @Override
    public int getItemCount() {
        return chapterModelList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView chapterCountTv;
        final TextView chapterNameTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chapterCountTv = itemView.findViewById(R.id.chapterCountTv);
            chapterNameTv = itemView.findViewById(R.id.chapterNameTv);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }

                // Updating old as well as new positions
                notifyItemChanged(selected_position);
                selected_position = getAdapterPosition();
                notifyItemChanged(selected_position);
            }
        }


    }
}
