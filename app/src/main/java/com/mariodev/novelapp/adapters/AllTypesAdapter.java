package com.mariodev.novelapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mariodev.novelapp.models.StoryTypeModel;
import com.mariodev.novelapp.R;


public class AllTypesAdapter extends RecyclerView.Adapter<AllTypesAdapter.ViewHolder> {

    final Context context;
    final StoryTypeModel [] typeModelList;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position ,  StoryTypeModel [] typeModelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }


    public AllTypesAdapter(Context context, StoryTypeModel [] typeModelList) {
        this.context = context;
        this.typeModelList = typeModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_types_item , parent , false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.typeTv.setText(typeModelList[position].getTypeName());

        holder.typeImgV.setImageResource(typeModelList[position].getTypeImg());
    }

    @Override
    public int getItemCount() {
        return typeModelList.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView typeImgV;
        final TextView typeTv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeImgV = itemView.findViewById(R.id.typeImgV);
            typeTv = itemView.findViewById(R.id.typeTv);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position , typeModelList);
                }
            }

        }
    }


}
