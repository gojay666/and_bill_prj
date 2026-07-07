package com.bill_prj.ui.budget;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.entity.BudgetEntity;
import com.bill_prj.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class BudgetActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private BudgetViewModel viewModel;
    private TabLayout tabLayout;
    private SharedPrefsManager prefsManager;
    private View emptyStateView;

    private long userId;
    private String currentType = "MONTHLY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        prefsManager = new SharedPrefsManager(this);
        userId = prefsManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this, new BudgetViewModelFactory(this)).get(BudgetViewModel.class);

        initViews();
        setupToolbar();
        setupTabLayout();
        setupRecyclerView();
        setupFab();
        observeAlerts();
        setupObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshCurrentBudgets();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_budgets);
        tabLayout = findViewById(R.id.tab_layout);
        emptyStateView = findViewById(R.id.layout_empty_state);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentType = "DAILY";
                        break;
                    case 1:
                        currentType = "WEEKLY";
                        break;
                    case 2:
                        currentType = "MONTHLY";
                        break;
                }
                loadBudgets();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        tabLayout.getTabAt(2).select();
    }

    private void setupRecyclerView() {
        adapter = new BudgetAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(budget -> showEditBudgetDialog(budget));

        adapter.setOnItemLongClickListener(budget -> {
            new AlertDialog.Builder(BudgetActivity.this)
                    .setTitle("删除预算")
                    .setMessage("确定要删除该预算吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        viewModel.deleteBudget(budget);
                        Toast.makeText(BudgetActivity.this, "预算已删除", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_budget);
        fab.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void loadBudgets() {
        viewModel.getBudgetsByType(userId, currentType);
    }

    private void setupObserver() {
        viewModel.getBudgetsByType(userId, currentType).observe(this, budgets -> {
            if (budgets != null) {
                adapter.submitList(new ArrayList<>(budgets));
                emptyStateView.setVisibility(budgets.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void observeAlerts() {
        viewModel.getAlertMessage().observe(this, message -> {
            if (message != null) {
                new AlertDialog.Builder(BudgetActivity.this)
                        .setTitle("预算超支提醒")
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);
        builder.setTitle("添加预算");

        EditText etAmount = dialogView.findViewById(R.id.et_amount);

        AutoCompleteTextView actvBudgetType = dialogView.findViewById(R.id.actv_budget_type);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actv_category);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"日预算", "周预算", "月预算"});
        actvBudgetType.setAdapter(typeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"全部类别", "餐饮", "交通", "购物", "娱乐", "住房", "通讯", "医疗", "教育", "其他"});
        actvCategory.setAdapter(categoryAdapter);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(BudgetActivity.this, "请输入预算金额", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(BudgetActivity.this, "金额格式不正确", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedType = actvBudgetType.getText().toString().trim();
            String budgetType;
            switch (selectedType) {
                case "日预算":
                    budgetType = "DAILY";
                    break;
                case "周预算":
                    budgetType = "WEEKLY";
                    break;
                case "月预算":
                    budgetType = "MONTHLY";
                    break;
                default:
                    budgetType = currentType;
                    break;
            }

            String selectedCategory = actvCategory.getText().toString().trim();
            String category = "全部类别".equals(selectedCategory) || selectedCategory.isEmpty() ? null : selectedCategory;

            BudgetEntity budget = new BudgetEntity();
            budget.setUserId(userId);
            budget.setType(budgetType);
            budget.setAmount(amount);
            budget.setUsedAmount(0);
            budget.setCategory(category);
            budget.setStartDate(System.currentTimeMillis());
            budget.setEndDate(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
            budget.setCreatedAt(System.currentTimeMillis());
            budget.setUpdatedAt(System.currentTimeMillis());

            viewModel.saveBudget(budget);
            Toast.makeText(BudgetActivity.this, "预算已添加", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showEditBudgetDialog(BudgetEntity budget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_budget, null);
        builder.setView(dialogView);
        builder.setTitle("编辑预算");

        EditText etAmount = dialogView.findViewById(R.id.et_budget_amount);
        etAmount.setText(String.valueOf((int) budget.getAmount()));

        builder.setPositiveButton("保存", (dialog, which) -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(BudgetActivity.this, "请输入预算金额", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(BudgetActivity.this, "金额格式不正确", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.updateBudgetAmount(budget.getId(), amount);
            Toast.makeText(BudgetActivity.this, "预算已更新", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", null);
        builder.setNeutralButton("检查预算状态", (dialog, which) -> {
            viewModel.checkBudgetStatus(budget.getId());
        });

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_budget, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_budget_history) {
            Toast.makeText(this, "预算历史", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
