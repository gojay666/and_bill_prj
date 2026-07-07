package com.bill_prj.ui.bill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.BillType;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(BillType category);
    }

    private final List<BillType> categories;
    private int selectedPosition = -1;
    private String selectedCategoryName = null;
    private OnCategorySelectedListener listener;

    public CategoryAdapter(List<BillType> categories) {
        this.categories = categories;
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public BillType getSelectedCategory() {
        if (selectedPosition >= 0 && selectedPosition < categories.size()) {
            return categories.get(selectedPosition);
        }
        return null;
    }

    public String getSelectedCategoryName() {
        return selectedCategoryName;
    }

    /**
     * Update the categories list and reset selection.
     */
    public void updateCategories(List<BillType> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        selectedPosition = -1;
        selectedCategoryName = null;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        BillType category = categories.get(position);
        holder.bind(category, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final View viewCategoryCircle;
        private final TextView tvCategoryLetter;
        private final TextView tvCategoryName;
        private final LinearLayout layoutCategoryItem;
        private final FrameLayout layoutCategoryIcon;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCategoryItem = itemView.findViewById(R.id.layoutCategoryItem);
            layoutCategoryIcon = itemView.findViewById(R.id.layoutCategoryIcon);
            viewCategoryCircle = itemView.findViewById(R.id.viewCategoryCircle);
            tvCategoryLetter = itemView.findViewById(R.id.tvCategoryLetter);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    int prevSelected = selectedPosition;
                    selectedPosition = pos;
                    selectedCategoryName = categories.get(pos).getName();
                    notifyItemChanged(pos);
                    if (prevSelected >= 0 && prevSelected != pos) {
                        notifyItemChanged(prevSelected);
                    }
                    if (listener != null) {
                        listener.onCategorySelected(categories.get(pos));
                    }
                }
            });
        }

        void bind(BillType category, boolean isSelected) {
            tvCategoryName.setText(category.getDisplayName());
            tvCategoryLetter.setText(category.getDisplayLetter());

            int colorResId = category.getColorResId();
            int color = ContextCompat.getColor(itemView.getContext(), colorResId);
            viewCategoryCircle.setBackgroundColor(color);

            if (isSelected) {
                layoutCategoryItem.setBackgroundResource(R.drawable.bg_category_selected);
            } else {
                layoutCategoryItem.setBackgroundResource(R.drawable.bg_category_normal);
            }
        }
    }
}
