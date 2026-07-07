package com.bill_prj.ui.bill;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.utils.CategoryIconUtils;
import com.bill_prj.utils.CurrencyUtils;
import com.bill_prj.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BillAdapter extends ListAdapter<BillEntity, BillAdapter.BillViewHolder> {

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private boolean sortByDate = true;
    private final Set<Long> selectedIds = new HashSet<>();
    private boolean isSelectionMode = false;
    private Map<Long, String> accountNames = new HashMap<>();

    public BillAdapter() {
        super(new BillDiffCallback());
    }

    public void setAccountNames(Map<Long, String> accountNames) {
        this.accountNames = accountNames != null ? accountNames : new HashMap<>();
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        BillEntity bill = getItem(position);
        holder.bind(bill, accountNames);

        // Animate item entrance
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(),
                android.R.anim.fade_in);
        animation.setDuration(250);
        animation.setStartOffset(position * 30L);
        holder.itemView.startAnimation(animation);

        long billId = bill.getId();
        boolean isSelected = selectedIds.contains(billId);

        // Apply selection visual effect
        if (holder.itemView instanceof MaterialCardView) {
            MaterialCardView cardView = (MaterialCardView) holder.itemView;
            if (isSelectionMode && isSelected) {
                cardView.setCardBackgroundColor(0xFFE3F2FD);
                cardView.setStrokeWidth(2);
                cardView.setStrokeColor(0xFF2196F3);
            } else {
                cardView.setCardBackgroundColor(0xFFFFFFFF);
                cardView.setStrokeWidth(0);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(billId);
            } else if (onItemClickListener != null) {
                onItemClickListener.onClick(bill);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onLongClick(bill);
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

    public void updateList(List<BillEntity> newList) {
        List<BillEntity> sortedList = new ArrayList<>(newList);
        if (sortByDate) {
            Collections.sort(sortedList, (a, b) -> Long.compare(b.getDate(), a.getDate()));
        } else {
            Collections.sort(sortedList, (a, b) -> {
                if (a.isType() != b.isType()) {
                    return a.isType() ? -1 : 1;
                }
                return Double.compare(b.getAmount(), a.getAmount());
            });
        }
        submitList(sortedList);
    }

    public void setSortByDate(boolean sortByDate) {
        this.sortByDate = sortByDate;
        List<BillEntity> currentList = new ArrayList<>(getCurrentList());
        updateList(currentList);
    }

    // --- Selection mode methods ---

    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) {
            selectedIds.clear();
        }
        notifyDataSetChanged();
    }

    public boolean isInSelectionMode() {
        return isSelectionMode;
    }

    public Set<Long> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public void toggleSelection(long id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedIds.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public interface OnItemClickListener {
        void onClick(BillEntity bill);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(BillEntity bill);
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {

        private final View iconBackground;
        private final TextView tvIconText;
        private final TextView tvCategoryName;
        private final TextView tvNote;
        private final TextView tvAmount;
        private final TextView tvAccount;
        private final TextView tvDate;
        private final View viewTypeStrip;

        BillViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBackground = itemView.findViewById(R.id.view_icon_background);
            tvIconText = itemView.findViewById(R.id.tv_icon_text);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvAccount = itemView.findViewById(R.id.tv_account);
            tvDate = itemView.findViewById(R.id.tv_date);
            viewTypeStrip = itemView.findViewById(R.id.view_type_strip);
        }

        void bind(BillEntity bill, Map<Long, String> accountNames) {
            if (bill == null) return;

            // Set type strip color
            if (viewTypeStrip != null) {
                int stripColor = "income".equals(bill.getType()) ? 0xFF9CB8A8 : 0xFFC9887E;
                viewTypeStrip.setBackgroundColor(stripColor);
            }

            // Set category icon
            String category = bill.getCategory();
            String iconText = CategoryIconUtils.getIcon(category);
            tvIconText.setText(iconText);

            GradientDrawable drawable = (GradientDrawable) iconBackground.getBackground();
            if (drawable != null) {
                drawable.setColor(CategoryIconUtils.getColorResId(category));
            }

            // Set category name
            tvCategoryName.setText(category != null ? category : "未分类");

            // Set note (truncated)
            String note = bill.getNote();
            if (note != null && !note.isEmpty()) {
                if (note.length() > 20) {
                    note = note.substring(0, 20) + "...";
                }
                tvNote.setText(note);
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            // Set amount with +/- sign and color
            boolean isIncome = bill.isType();
            String amountText = CurrencyUtils.formatAmount(bill.getAmount(), isIncome);
            tvAmount.setText(amountText);
            tvAmount.setTextColor(isIncome ? 0xFF4CAF50 : 0xFFF44336);

            // Set account name
            String accountName = accountNames.get(bill.getAccountId());
            if (accountName != null) {
                tvAccount.setText(accountName);
            } else {
                tvAccount.setText("账户 #" + bill.getAccountId());
            }

            // Set date/time
            tvDate.setText(DateUtils.formatDateTime(bill.getDate()));
        }
    }

    static class BillDiffCallback extends DiffUtil.ItemCallback<BillEntity> {

        @Override
        public boolean areItemsTheSame(@NonNull BillEntity oldItem, @NonNull BillEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BillEntity oldItem, @NonNull BillEntity newItem) {
            return oldItem.getId() == newItem.getId()
                    && oldItem.getAmount() == newItem.getAmount()
                    && oldItem.getCategory() != null
                    && oldItem.getCategory().equals(newItem.getCategory())
                    && oldItem.getNote() != null
                    && oldItem.getNote().equals(newItem.getNote())
                    && oldItem.getDate() == newItem.getDate()
                    && oldItem.isType() == newItem.isType();
        }
    }
}
