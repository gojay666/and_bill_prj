package com.bill_prj.ui.bill;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.utils.CurrencyUtils;
import com.bill_prj.utils.DateUtils;
import com.bill_prj.utils.SharedPrefsManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

public class BillListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private BillViewModel viewModel;
    private SharedPrefsManager prefsManager;

    private TextView tvIncome, tvExpense, tvBalance;
    private View emptyStateView;
    private ChipGroup chipGroup;
    private Spinner spinnerSort;

    private String currentFilter = "today";
    private boolean sortByDate = true;
    private long customStartTime = 0;
    private long customEndTime = 0;

    private long userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);

        prefsManager = new SharedPrefsManager(this);
        userId = prefsManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this, new BillViewModelFactory(this)).get(BillViewModel.class);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupSortSpinner();
        setupFab();
        applyFilter();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_bills);
        tvIncome = findViewById(R.id.tv_income);
        tvExpense = findViewById(R.id.tv_expense);
        tvBalance = findViewById(R.id.tv_balance);
        emptyStateView = findViewById(R.id.layout_empty);
        chipGroup = findViewById(R.id.chip_group_time);
        spinnerSort = findViewById(R.id.spinner_sort);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new BillAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(bill -> {
            Intent intent = new Intent(BillListActivity.this, BillDetailActivity.class);
            intent.putExtra("bill_id", bill.getId());
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener(bill -> {
            new AlertDialog.Builder(BillListActivity.this)
                    .setTitle("删除账单")
                    .setMessage("确定要删除这笔账单吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        viewModel.deleteBill(bill);
                        Toast.makeText(BillListActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });
    }

    private void setupFilterChips() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_today) currentFilter = "today";
            else if (id == R.id.chip_week) currentFilter = "week";
            else if (id == R.id.chip_month) currentFilter = "month";
            else if (id == R.id.chip_quarter) currentFilter = "quarter";
            else if (id == R.id.chip_year) currentFilter = "year";
            else if (id == R.id.chip_custom) {
                currentFilter = "custom";
                showCustomDatePicker();
                return;
            }
            applyFilter();
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortByDate = (position == 0);
                adapter.setSortByDate(sortByDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_bill);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(BillListActivity.this, AddBillActivity.class);
            startActivity(intent);
        });
    }

    private void applyFilter() {
        long startTime, endTime;
        Calendar cal = Calendar.getInstance();
        long now = System.currentTimeMillis();

        switch (currentFilter) {
            case "today":
                startTime = DateUtils.getStartOfDay(now);
                endTime = DateUtils.getEndOfDay(now);
                break;
            case "week":
                startTime = DateUtils.getStartOfWeek(now);
                endTime = DateUtils.getEndOfWeek(now);
                break;
            case "month":
                startTime = DateUtils.getStartOfMonth(now);
                endTime = DateUtils.getEndOfMonth(now);
                break;
            case "quarter": {
                int month = cal.get(Calendar.MONTH);
                int quarterStartMonth = (month / 3) * 3;
                cal.set(Calendar.MONTH, quarterStartMonth);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startTime = DateUtils.getStartOfMonth(cal.getTimeInMillis());
                cal.set(Calendar.MONTH, quarterStartMonth + 2);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endTime = DateUtils.getEndOfMonth(cal.getTimeInMillis());
                break;
            }
            case "year":
                startTime = DateUtils.getStartOfYear(now);
                endTime = DateUtils.getEndOfYear(now);
                break;
            case "custom":
                if (customStartTime == 0 || customEndTime == 0) {
                    Toast.makeText(this, "请选择自定义日期范围", Toast.LENGTH_SHORT).show();
                    showCustomDatePicker();
                    return;
                }
                startTime = customStartTime;
                endTime = customEndTime;
                break;
            default:
                startTime = DateUtils.getStartOfDay(now);
                endTime = DateUtils.getEndOfDay(now);
                break;
        }

        viewModel.loadBills(userId, startTime, endTime);
        viewModel.getBills().observe(this, bills -> {
            if (bills != null) {
                adapter.updateList(new ArrayList<>(bills));
                emptyStateView.setVisibility(bills.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        LiveData<Double> incomeLiveData = viewModel.getTotalIncome(userId, startTime, endTime);
        incomeLiveData.observe(this, income -> {
            double incomeVal = income != null ? income : 0;
            tvIncome.setText(CurrencyUtils.formatIncome(incomeVal));
            updateBalance();
        });

        LiveData<Double> expenseLiveData = viewModel.getTotalExpense(userId, startTime, endTime);
        expenseLiveData.observe(this, expense -> {
            double expenseVal = expense != null ? expense : 0;
            tvExpense.setText(CurrencyUtils.formatExpense(expenseVal));
            updateBalance();
        });
    }

    private void updateBalance() {
        String incomeStr = tvIncome.getText().toString().replace("+¥", "").replace(",", "");
        String expenseStr = tvExpense.getText().toString().replace("-¥", "").replace(",", "");
        try {
            double income = Double.parseDouble(incomeStr);
            double expense = Double.parseDouble(expenseStr);
            double balance = income - expense;
            tvBalance.setText(CurrencyUtils.formatWithSymbol(balance));
            tvBalance.setTextColor(balance >= 0 ? 0xFF4CAF50 : 0xFFF44336);
        } catch (NumberFormatException ignored) {
        }
    }

    private void showCustomDatePicker() {
        final Calendar cal = Calendar.getInstance();

        DatePickerDialog startPicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    customStartTime = DateUtils.getStartOfDay(cal.getTimeInMillis());

                    DatePickerDialog endPicker = new DatePickerDialog(this,
                            (view2, year2, month2, dayOfMonth2) -> {
                                cal.set(Calendar.YEAR, year2);
                                cal.set(Calendar.MONTH, month2);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth2);
                                customEndTime = DateUtils.getEndOfDay(cal.getTimeInMillis());

                                if (customStartTime > customEndTime) {
                                    Toast.makeText(BillListActivity.this, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show();
                                    customStartTime = 0;
                                    customEndTime = 0;
                                    chipGroup.check(R.id.chip_today);
                                    return;
                                }
                                applyFilter();
                            },
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                    endPicker.setTitle("选择结束日期");
                    endPicker.show();
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        startPicker.setTitle("选择开始日期");
        startPicker.show();
    }
}
