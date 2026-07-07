package com.bill_prj.ui.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.utils.CurrencyUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CategoryBreakdownAdapter extends RecyclerView.Adapter<CategoryBreakdownAdapter.ViewHolder> {

    private List<BillDao.CategoryTotal> categoryTotals;
    private double maxTotal = 1;

    public CategoryBreakdownAdapter(List<BillDao.CategoryTotal> categoryTotals) {
        this.categoryTotals = new ArrayList<>(categoryTotals);
        calculateMax();
    }

    private void calculateMax() {
        maxTotal = 1;
        for (BillDao.CategoryTotal ct : categoryTotals) {
            if (ct.total > maxTotal) {
                maxTotal = ct.total;
            }
        }
    }

    public void updateList(List<BillDao.CategoryTotal> newList) {
        this.categoryTotals = new ArrayList<>(newList);
        calculateMax();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_breakdown, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillDao.CategoryTotal ct = categoryTotals.get(position);
        holder.tvCategoryName.setText(ct.category);
        holder.tvAmount.setText(CurrencyUtils.formatWithSymbol(ct.total));

        int progress = (int) ((ct.total / maxTotal) * 100);
        holder.progressBar.setProgress(progress);

        DecimalFormat df = new DecimalFormat("#0.0");
        double totalAll = 0;
        for (BillDao.CategoryTotal s : categoryTotals) {
            totalAll += s.total;
        }
        double percent = totalAll > 0 ? (ct.total / totalAll) * 100 : 0;
        holder.tvPercent.setText(df.format(percent) + "%");
    }

    @Override
    public int getItemCount() {
        return categoryTotals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCategoryName;
        final TextView tvAmount;
        final TextView tvPercent;
        final ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPercent = itemView.findViewById(R.id.tv_percent);
            progressBar = itemView.findViewById(R.id.progress_category);
        }
    }
}
