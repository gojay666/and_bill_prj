package com.bill_prj.ui.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.entity.BudgetEntity;
import com.bill_prj.utils.NumberUtils;

import java.text.DecimalFormat;

public class BudgetAdapter extends ListAdapter<BudgetEntity, BudgetAdapter.BudgetViewHolder> {

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public BudgetAdapter() {
        super(new BudgetDiffCallback());
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetEntity budget = getItem(position);
        holder.bind(budget);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick(budget);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onLongClick(budget);
            }
            return false;
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(BudgetEntity budget);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(BudgetEntity budget);
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCategoryName;
        private final TextView tvBudgetAmount;
        private final TextView tvRemainingAmount;
        private final TextView tvProgressPercent;
        private final ProgressBar progressBar;

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvBudgetAmount = itemView.findViewById(R.id.tv_total_amount);
            tvRemainingAmount = itemView.findViewById(R.id.tv_remaining);
            tvProgressPercent = itemView.findViewById(R.id.tv_percentage);
            progressBar = itemView.findViewById(R.id.progress_budget);
        }

        void bind(BudgetEntity budget) {
            if (budget == null) return;

            // Category name
            String categoryName = budget.getCategory() != null ? budget.getCategory() : "全部类别";
            tvCategoryName.setText(categoryName);

            // Budget amount
            tvBudgetAmount.setText("预算: " + NumberUtils.formatCurrency(budget.getAmount()));

            // Calculate progress
            double progress = 0;
            if (budget.getAmount() > 0) {
                progress = (budget.getUsedAmount() / budget.getAmount()) * 100;
                if (progress > 100) progress = 100;
            }

            // Remaining amount
            double remaining = budget.getAmount() - budget.getUsedAmount();
            tvRemainingAmount.setText("剩余: " + NumberUtils.formatCurrency(Math.max(0, remaining)));

            // Progress percentage
            DecimalFormat df = new DecimalFormat("#0.0");
            tvProgressPercent.setText(df.format(progress) + "%");

            // Progress bar
            progressBar.setProgress((int) progress);
            progressBar.setMax(100);

            // Color coding
            int progressColor;
            if (progress < 50) {
                progressColor = 0xFF4CAF50; // Green
            } else if (progress <= 80) {
                progressColor = 0xFFFFC107; // Yellow
            } else {
                progressColor = 0xFFF44336; // Red
            }
            progressBar.getProgressDrawable().setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    static class BudgetDiffCallback extends DiffUtil.ItemCallback<BudgetEntity> {

        @Override
        public boolean areItemsTheSame(@NonNull BudgetEntity oldItem, @NonNull BudgetEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BudgetEntity oldItem, @NonNull BudgetEntity newItem) {
            return oldItem.getId() == newItem.getId()
                    && oldItem.getAmount() == newItem.getAmount()
                    && oldItem.getUsedAmount() == newItem.getUsedAmount();
        }
    }
}
