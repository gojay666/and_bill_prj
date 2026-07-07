package com.bill_prj.ui.statistics;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bill_prj.R;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.ui.bill.BillViewModel;
import com.bill_prj.utils.NumberUtils;
import com.bill_prj.utils.SharedPrefsManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private BillViewModel billViewModel;
    private SharedPrefsManager prefsManager;

    private TextView tvMonthIncome, tvMonthExpense, tvBalance;
    private PieChart pieChart;
    private LinearLayout layoutLegend;
    private TextView tvChartEmpty;
    private MaterialButton btnToday, btnThisWeek, btnThisMonth;
    private MaterialButton btnChartExpense, btnChartIncome;

    private double lastIncome = 0.0;
    private double lastExpense = 0.0;

    private boolean isShowingExpense = true; // true=expense chart, false=income chart
    private int selectedPeriod = 2;          // 0=today, 1=week, 2=month

    // Category display names and colors
    private static final Map<String, CategoryInfo> CATEGORY_MAP = new HashMap<>();

    static {
        CATEGORY_MAP.put("餐饮", new CategoryInfo("餐饮", "#FFF97316"));
        CATEGORY_MAP.put("交通", new CategoryInfo("交通", "#FF3B82F6"));
        CATEGORY_MAP.put("购物", new CategoryInfo("购物", "#FFEC4899"));
        CATEGORY_MAP.put("娱乐", new CategoryInfo("娱乐", "#FF8B5CF6"));
        CATEGORY_MAP.put("住房", new CategoryInfo("住房", "#FF78716C"));
        CATEGORY_MAP.put("水电", new CategoryInfo("水电", "#FF64748B"));
        CATEGORY_MAP.put("通讯", new CategoryInfo("通讯", "#FF06B6D4"));
        CATEGORY_MAP.put("医疗", new CategoryInfo("医疗", "#FFEF4444"));
        CATEGORY_MAP.put("教育", new CategoryInfo("教育", "#FF6366F1"));
        CATEGORY_MAP.put("服饰", new CategoryInfo("服饰", "#FFE11D48"));
        CATEGORY_MAP.put("美妆", new CategoryInfo("美妆", "#FFD946EF"));
        CATEGORY_MAP.put("运动", new CategoryInfo("运动", "#FF22C55E"));
        CATEGORY_MAP.put("礼物", new CategoryInfo("礼物", "#FFEC4899"));
        CATEGORY_MAP.put("工资", new CategoryInfo("工资", "#FF22C55E"));
        CATEGORY_MAP.put("奖金", new CategoryInfo("奖金", "#FFA3E635"));
        CATEGORY_MAP.put("兼职", new CategoryInfo("兼职", "#FF34D399"));
        CATEGORY_MAP.put("投资", new CategoryInfo("投资", "#FF14B8A6"));
        CATEGORY_MAP.put("红包", new CategoryInfo("红包", "#FFEF4444"));
        CATEGORY_MAP.put("退款", new CategoryInfo("退款", "#FF3B82F6"));
        CATEGORY_MAP.put("其他", new CategoryInfo("其他", "#FF94A3B8"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);

        tvMonthIncome = root.findViewById(R.id.tv_month_income);
        tvMonthExpense = root.findViewById(R.id.tv_month_expense);
        tvBalance = root.findViewById(R.id.tv_balance);
        pieChart = root.findViewById(R.id.pie_chart);
        layoutLegend = root.findViewById(R.id.layout_legend);
        tvChartEmpty = root.findViewById(R.id.tv_chart_empty);
        btnToday = root.findViewById(R.id.btn_today);
        btnThisWeek = root.findViewById(R.id.btn_this_week);
        btnThisMonth = root.findViewById(R.id.btn_this_month);
        btnChartExpense = root.findViewById(R.id.btn_chart_expense);
        btnChartIncome = root.findViewById(R.id.btn_chart_income);

        prefsManager = new SharedPrefsManager(requireContext());
        billViewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);

        setupChart();
        setupButtons();
        loadChartData();

        return root;
    }

    private void setupChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setHoleRadius(55f);
        pieChart.setDrawCenterText(false);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setEntryLabelColor(Color.parseColor("#FF3D3530"));
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        pieChart.setCenterTextSize(14f);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        pieChart.animateY(800);
    }

    private void setupButtons() {
        // Period selector
        btnToday.setOnClickListener(v -> {
            selectedPeriod = 0;
            updatePeriodButtons();
            loadChartData();
        });
        btnThisWeek.setOnClickListener(v -> {
            selectedPeriod = 1;
            updatePeriodButtons();
            loadChartData();
        });
        btnThisMonth.setOnClickListener(v -> {
            selectedPeriod = 2;
            updatePeriodButtons();
            loadChartData();
        });

        // Chart type toggle
        btnChartExpense.setOnClickListener(v -> {
            isShowingExpense = true;
            updateChartTypeButtons();
            loadChartData();
        });
        btnChartIncome.setOnClickListener(v -> {
            isShowingExpense = false;
            updateChartTypeButtons();
            loadChartData();
        });

        // Default states
        updatePeriodButtons();
        updateChartTypeButtons();
    }

    private void updatePeriodButtons() {
        updateButtonStyle(btnToday, selectedPeriod == 0);
        updateButtonStyle(btnThisWeek, selectedPeriod == 1);
        updateButtonStyle(btnThisMonth, selectedPeriod == 2);
    }

    private void updateChartTypeButtons() {
        updateButtonStyle(btnChartExpense, isShowingExpense);
        updateButtonStyle(btnChartIncome, !isShowingExpense);
    }

    private void updateButtonStyle(MaterialButton button, boolean filled) {
        if (filled) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.md_primary_dark, null)));
            button.setTextColor(getResources().getColor(android.R.color.white, null));
            button.setStrokeWidth(0);
            button.setElevation(0f);
        } else {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.md_primary_light, null)));
            button.setTextColor(getResources().getColor(R.color.md_primary_dark, null));
            button.setStrokeWidth(0);
            button.setElevation(0f);
        }
    }

    private void loadChartData() {
        long userId = prefsManager.getUserId();

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        switch (selectedPeriod) {
            case 0: // Today
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                endCal.setTimeInMillis(startCal.getTimeInMillis());
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
            case 1: // Week (Monday to Sunday)
                int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                int daysSinceMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                startCal.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                endCal.setTimeInMillis(startCal.getTimeInMillis());
                endCal.add(Calendar.DAY_OF_MONTH, 6);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
            default: // Month
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);

                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
        }

        long start = startCal.getTimeInMillis();
        long end = endCal.getTimeInMillis();

        // Update summary numbers
        billViewModel.getTotalIncome(userId, start, end).observe(getViewLifecycleOwner(), income -> {
            lastIncome = income != null ? income : 0.0;
            tvMonthIncome.setText(NumberUtils.formatCurrency(lastIncome));
            updateBalance();
        });

        billViewModel.getTotalExpense(userId, start, end).observe(getViewLifecycleOwner(), expense -> {
            lastExpense = expense != null ? expense : 0.0;
            tvMonthExpense.setText(NumberUtils.formatCurrency(lastExpense));
            updateBalance();
        });

        // Load category totals for chart
        // isShowingExpense=true  → 显示支出饼图 → isIncome=false（查支出）
        // isShowingExpense=false → 显示收入饼图 → isIncome=true（查收入）
        billViewModel.getCategoryTotals(userId, start, end, !isShowingExpense)
                .observe(getViewLifecycleOwner(), this::updateChart);
    }

    private void updateChart(List<BillDao.CategoryTotal> categoryTotals) {
        if (categoryTotals == null || categoryTotals.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            layoutLegend.setVisibility(View.GONE);
            tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        layoutLegend.setVisibility(View.VISIBLE);
        tvChartEmpty.setVisibility(View.GONE);

        // Prepare entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        double totalAmount = 0;

        for (BillDao.CategoryTotal ct : categoryTotals) {
            if (ct.total > 0) {
                entries.add(new PieEntry((float) ct.total, ct.category));
                totalAmount += ct.total;
            }
        }

        if (entries.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            layoutLegend.setVisibility(View.GONE);
            tvChartEmpty.setVisibility(View.VISIBLE);
            return;
        }

        // Colors for entries
        ArrayList<Integer> colors = new ArrayList<>();
        for (PieEntry entry : entries) {
            String categoryName = entry.getLabel();
            CategoryInfo info = CATEGORY_MAP.get(categoryName);
            if (info != null) {
                colors.add(Color.parseColor(info.color));
            } else {
                colors.add(ColorTemplate.MATERIAL_COLORS[colors.size() % ColorTemplate.MATERIAL_COLORS.length]);
            }
        }

        double finalTotal = totalAmount;
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(13f);
        dataSet.setValueTextColor(Color.parseColor("#FF3D3530"));
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                double percent = (value / finalTotal) * 100;
                return String.format("%.1f%%", percent);
            }
        });
        dataSet.setSelectionShift(8f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(dataSet.getValueFormatter());

        pieChart.setData(data);
        pieChart.highlightValues(null);
        pieChart.invalidate();
        pieChart.animateY(600);

        // Update legend
        buildLegend(categoryTotals, totalAmount);
    }

    private void buildLegend(List<BillDao.CategoryTotal> categoryTotals, double totalAmount) {
        layoutLegend.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (BillDao.CategoryTotal ct : categoryTotals) {
            if (ct.total <= 0) continue;

            View itemView = inflater.inflate(R.layout.item_chart_legend, layoutLegend, false);

            View colorDot = itemView.findViewById(R.id.view_color_dot);
            TextView tvCatName = itemView.findViewById(R.id.tv_legend_category);
            TextView tvAmount = itemView.findViewById(R.id.tv_legend_amount);
            TextView tvPercent = itemView.findViewById(R.id.tv_legend_percent);

            CategoryInfo info = CATEGORY_MAP.get(ct.category);
            int color;
            if (info != null) {
                color = Color.parseColor(info.color);
            } else {
                color = ColorTemplate.MATERIAL_COLORS[0];
            }

            colorDot.setBackgroundColor(color);
            tvCatName.setText(ct.category);
            tvAmount.setText(NumberUtils.formatCurrency(ct.total));
            double percent = (ct.total / totalAmount) * 100;
            tvPercent.setText(String.format("%.1f%%", percent));

            layoutLegend.addView(itemView);
        }
    }

    private void updateBalance() {
        double balance = lastIncome - lastExpense;
        tvBalance.setText(NumberUtils.formatCurrency(balance));
        tvBalance.setTextColor(balance >= 0
                ? getResources().getColor(R.color.color_income, null)
                : getResources().getColor(R.color.color_expense, null));
    }

    private static class CategoryInfo {
        final String name;
        final String color;

        CategoryInfo(String name, String color) {
            this.name = name;
            this.color = color;
        }
    }
}
