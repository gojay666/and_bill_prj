package com.bill_prj.ui.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.utils.CurrencyUtils;
import com.bill_prj.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class MonthlyTableAdapter extends RecyclerView.Adapter<MonthlyTableAdapter.ViewHolder> {

    private List<StatisticsViewModel.MonthlyData> monthlyData;

    public MonthlyTableAdapter(List<StatisticsViewModel.MonthlyData> monthlyData) {
        this.monthlyData = new ArrayList<>(monthlyData);
    }

    public void updateList(List<StatisticsViewModel.MonthlyData> newList) {
        this.monthlyData = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_monthly_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatisticsViewModel.MonthlyData md = monthlyData.get(position);
        holder.tvMonth.setText(DateUtils.getMonthNameChinese(md.getMonth()));
        holder.tvIncome.setText(CurrencyUtils.formatWithSymbol(md.getIncome()));
        holder.tvExpense.setText(CurrencyUtils.formatWithSymbol(md.getExpense()));

        String balanceStr = CurrencyUtils.formatWithSymbol(Math.abs(md.getBalance()));
        if (md.getBalance() >= 0) {
            holder.tvBalance.setText("+" + balanceStr);
            holder.tvBalance.setTextColor(0xFF4CAF50);
        } else {
            holder.tvBalance.setText("-" + balanceStr);
            holder.tvBalance.setTextColor(0xFFF44336);
        }
    }

    @Override
    public int getItemCount() {
        return monthlyData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMonth;
        final TextView tvIncome;
        final TextView tvExpense;
        final TextView tvBalance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvIncome = itemView.findViewById(R.id.tv_income);
            tvExpense = itemView.findViewById(R.id.tv_expense);
            tvBalance = itemView.findViewById(R.id.tv_balance);
        }
    }
}
