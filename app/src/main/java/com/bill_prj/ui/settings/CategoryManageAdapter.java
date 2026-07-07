package com.bill_prj.ui.settings;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.entity.CategoryEntity;

import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.ViewHolder> {

    private List<CategoryEntity> categories;
    private OnDeleteClickListener onDeleteClickListener;
    private OnItemClickListener onItemClickListener;

    public interface OnDeleteClickListener {
        void onDelete(CategoryEntity category);
    }

    public interface OnItemClickListener {
        void onItemClick(CategoryEntity category);
    }

    public CategoryManageAdapter(List<CategoryEntity> categories) {
        this.categories = categories;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateData(List<CategoryEntity> newData) {
        this.categories = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryEntity category = categories.get(position);
        holder.tvName.setText(category.getName());
        holder.tvEmoji.setText(category.getEmoji());

        // Parse color
        int color;
        try {
            color = android.graphics.Color.parseColor(category.getColor());
        } catch (Exception e) {
            color = 0xFF94A3B8;
        }

        // Color dot
        GradientDrawable dotBg = (GradientDrawable) holder.vColorDot.getBackground().mutate();
        dotBg.setColor(color);

        // Emoji background
        GradientDrawable emojiBg = (GradientDrawable) holder.tvEmoji.getBackground().mutate();
        emojiBg.setColor(color);
        emojiBg.setAlpha(30);

        // Item click for editing
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(category);
            }
        });

        // Delete button — only visible for non-default categories
        if (category.isDefault()) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDelete(category);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View vColorDot;
        final TextView tvEmoji;
        final TextView tvName;
        final TextView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            vColorDot = itemView.findViewById(R.id.v_color_dot);
            tvEmoji = itemView.findViewById(R.id.tv_emoji);
            tvName = itemView.findViewById(R.id.tv_category_name);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
