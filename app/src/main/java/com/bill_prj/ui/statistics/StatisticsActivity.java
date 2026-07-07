package com.bill_prj.ui.statistics;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.utils.CurrencyUtils;
import com.bill_prj.utils.DateUtils;
import com.bill_prj.utils.SharedPrefsManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private View chartsContainer, reportsContainer;

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvYear;
    private ImageButton btnPrevYear, btnNextYear;

    private TextView tvTotalIncome, tvTotalExpense, tvBalance;
    private RecyclerView recyclerCategoryBreakdown;
    private RecyclerView recyclerMonthlyTable;

    private StatisticsViewModel viewModel;
    private SharedPrefsManager prefsManager;
    private CategoryBreakdownAdapter categoryBreakdownAdapter;
    private MonthlyTableAdapter monthlyTableAdapter;

    private int currentYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        prefsManager = new SharedPrefsManager(this);
        long userId = prefsManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this, new StatisticsViewModelFactory(this)).get(StatisticsViewModel.class);
        viewModel.setUserId(userId);

        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        initViews();
        setupToolbar();
        setupTabLayout();
        setupYearPicker();
        setupCharts();
        setupReports();
        setupShareButton();
        loadData();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        chartsContainer = findViewById(R.id.layout_charts);
        reportsContainer = findViewById(R.id.layout_reports);

        lineChart = findViewById(R.id.line_chart);
        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        tvYear = findViewById(R.id.tv_year);
        btnPrevYear = findViewById(R.id.btn_prev_year);
        btnNextYear = findViewById(R.id.btn_next_year);

        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);
        recyclerCategoryBreakdown = findViewById(R.id.recycler_category_breakdown);
        recyclerMonthlyTable = findViewById(R.id.recycler_monthly_table);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("统计分析");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("图表"));
        tabLayout.addTab(tabLayout.newTab().setText("报表"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean isCharts = tab.getPosition() == 0;
                chartsContainer.setVisibility(isCharts ? View.VISIBLE : View.GONE);
                reportsContainer.setVisibility(isCharts ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupYearPicker() {
        tvYear.setText(String.valueOf(currentYear));
        btnPrevYear.setOnClickListener(v -> {
            currentYear--;
            tvYear.setText(String.valueOf(currentYear));
            loadData();
        });
        btnNextYear.setOnClickListener(v -> {
            currentYear++;
            tvYear.setText(String.valueOf(currentYear));
            loadData();
        });
    }

    private void setupCharts() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setTextSize(12f);

        XAxis lineXAxis = lineChart.getXAxis();
        lineXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXAxis.setGranularity(1f);
        lineXAxis.setValueFormatter(new IndexAxisValueFormatter(getMonthLabels()));

        YAxis lineLeftAxis = lineChart.getAxisLeft();
        lineLeftAxis.setAxisMinimum(0f);
        lineChart.getAxisRight().setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setDrawGridBackground(false);
        barChart.setFitBars(true);

        XAxis barXAxis = barChart.getXAxis();
        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barXAxis.setGranularity(1f);
        barXAxis.setValueFormatter(new IndexAxisValueFormatter(getMonthLabels()));

        YAxis barLeftAxis = barChart.getAxisLeft();
        barLeftAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);

        Legend barLegend = barChart.getLegend();
        barLegend.setTextSize(12f);

        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.getLegend().setTextSize(12f);
        pieChart.setUsePercentValues(true);
    }

    private void setupReports() {
        categoryBreakdownAdapter = new CategoryBreakdownAdapter(new ArrayList<>());
        recyclerCategoryBreakdown.setLayoutManager(new LinearLayoutManager(this));
        recyclerCategoryBreakdown.setAdapter(categoryBreakdownAdapter);

        monthlyTableAdapter = new MonthlyTableAdapter(new ArrayList<>());
        recyclerMonthlyTable.setLayoutManager(new LinearLayoutManager(this));
        recyclerMonthlyTable.setAdapter(monthlyTableAdapter);
    }

    private void setupShareButton() {
        FloatingActionButton btnShare = findViewById(R.id.fab_share);
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "支出统计报告 - " + currentYear);
            StringBuilder report = new StringBuilder();
            report.append("=== ").append(currentYear).append(" 年度财务报告 ===\n\n");

            Double income = viewModel.getTotalIncome().getValue();
            Double expense = viewModel.getTotalExpense().getValue();
            report.append("总收入: ¥").append(income != null ? String.format("%.2f", income) : "0.00").append("\n");
            report.append("总支出: ¥").append(expense != null ? String.format("%.2f", expense) : "0.00").append("\n");
            double bal = (income != null ? income : 0) - (expense != null ? expense : 0);
            report.append("结余: ¥").append(String.format("%.2f", bal)).append("\n\n");

            List<StatisticsViewModel.MonthlyData> monthlyList = viewModel.getMonthlyDataList().getValue();
            if (monthlyList != null) {
                report.append("月度明细:\n");
                for (StatisticsViewModel.MonthlyData md : monthlyList) {
                    report.append(DateUtils.getMonthNameChinese(md.getMonth()))
                            .append(": 收入¥").append(String.format("%.2f", md.getIncome()))
                            .append(", 支出¥").append(String.format("%.2f", md.getExpense()))
                            .append("\n");
                }
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, report.toString());
            startActivity(Intent.createChooser(shareIntent, "分享统计报告"));
        });
    }

    private void loadData() {
        viewModel.loadYearlyData(currentYear);

        viewModel.getYearlyIncomeData().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineDataSet incomeSet = new LineDataSet(entries, "收入");
                incomeSet.setColor(0xFF4CAF50);
                incomeSet.setCircleColor(0xFF4CAF50);
                incomeSet.setLineWidth(2f);
                incomeSet.setCircleRadius(4f);
                incomeSet.setValueTextSize(10f);
                incomeSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                LineData lineData = lineChart.getLineData();
                if (lineData == null) {
                    lineData = new LineData();
                    lineChart.setData(lineData);
                }
                if (lineData.getDataSetByLabel("收入", false) != null) {
                    lineData.removeDataSet(lineData.getDataSetByLabel("收入", false));
                }
                lineData.addDataSet(incomeSet);
                lineChart.invalidate();
            }
        });

        viewModel.getYearlyExpenseData().observe(this, entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineData lineData = lineChart.getLineData();
                if (lineData != null && lineData.getDataSetByLabel("支出", false) != null) {
                    lineData.removeDataSet(lineData.getDataSetByLabel("支出", false));
                }

                LineDataSet expenseSet = new LineDataSet(entries, "支出");
                expenseSet.setColor(0xFFF44336);
                expenseSet.setCircleColor(0xFFF44336);
                expenseSet.setLineWidth(2f);
                expenseSet.setCircleRadius(4f);
                expenseSet.setValueTextSize(10f);
                expenseSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                if (lineData == null) {
                    lineData = new LineData();
                    lineChart.setData(lineData);
                }
                lineData.addDataSet(expenseSet);
                lineChart.invalidate();
            }
        });

        viewModel.getMonthlyComparisonData().observe(this, barEntries -> {
            if (barEntries != null && !barEntries.isEmpty()) {
                BarDataSet dataSet = new BarDataSet(barEntries, "月度收支对比");
                dataSet.setColors(new int[]{0xFF4CAF50, 0xFFF44336});
                dataSet.setStackLabels(new String[]{"收入", "支出"});
                dataSet.setValueTextSize(10f);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.6f);
                barChart.setData(barData);
                barChart.invalidate();
            }
        });

        viewModel.getCategoryExpenseData().observe(this, pieEntries -> {
            if (pieEntries != null && !pieEntries.isEmpty()) {
                PieDataSet dataSet = new PieDataSet(pieEntries, "支出分类");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setValueTextSize(12f);
                dataSet.setValueFormatter(new PercentFormatter(pieChart));

                PieData pieData = new PieData(dataSet);
                pieChart.setData(pieData);
                pieChart.invalidate();
            }
        });

        viewModel.getTotalIncome().observe(this, income -> {
            double val = income != null ? income : 0;
            tvTotalIncome.setText(CurrencyUtils.formatWithSymbol(val));
        });

        viewModel.getTotalExpense().observe(this, expense -> {
            double val = expense != null ? expense : 0;
            tvTotalExpense.setText(CurrencyUtils.formatWithSymbol(val));
            updateReportBalance();
        });

        viewModel.getCategoryBreakdown().observe(this, categoryTotals -> {
            if (categoryTotals != null) {
                categoryBreakdownAdapter.updateList(categoryTotals);
            }
        });

        viewModel.getMonthlyDataList().observe(this, monthlyData -> {
            if (monthlyData != null) {
                monthlyTableAdapter.updateList(monthlyData);
            }
        });
    }

    private void updateReportBalance() {
        Double income = viewModel.getTotalIncome().getValue();
        Double expense = viewModel.getTotalExpense().getValue();
        double balance = (income != null ? income : 0) - (expense != null ? expense : 0);
        tvBalance.setText("结余: " + CurrencyUtils.formatWithSymbol(balance));
        tvBalance.setTextColor(balance >= 0 ? 0xFF4CAF50 : 0xFFF44336);
    }

    private String[] getMonthLabels() {
        return new String[]{"1月", "2月", "3月", "4月", "5月", "6月",
                "7月", "8月", "9月", "10月", "11月", "12月"};
    }
}
