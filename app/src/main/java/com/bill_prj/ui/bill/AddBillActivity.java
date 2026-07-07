package com.bill_prj.ui.bill;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bill_prj.R;
import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.AccountDao;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.data.dao.BudgetDao;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.entity.BudgetEntity;
import com.bill_prj.utils.DateUtils;
import com.bill_prj.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddBillActivity extends AppCompatActivity {

    private Spinner spinnerType, spinnerCategory, spinnerAccount;
    private EditText etAmount, etNote;
    private Button btnDate;
    private long selectedDate = System.currentTimeMillis();
    private SharedPrefsManager prefsManager;
    private Executor executor;
    private BillDao billDao;
    private BudgetDao budgetDao;
    private AccountDao accountDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        prefsManager = new SharedPrefsManager(this);
        executor = Executors.newSingleThreadExecutor();
        billDao = AppDatabase.getInstance(this).billDao();
        budgetDao = AppDatabase.getInstance(this).budgetDao();
        accountDao = AppDatabase.getInstance(this).accountDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("添加账单");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        spinnerType = findViewById(R.id.spinner_type);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerAccount = findViewById(R.id.spinner_account);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        btnDate = findViewById(R.id.btn_date);
        Button btnSave = findViewById(R.id.btn_save);

        setupTypeSpinner();
        setupDateButton();
        loadAccounts();

        btnSave.setOnClickListener(v -> saveBill());
    }

    private void setupTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bill_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] categories;
                if (position == 0) {
                    categories = new String[]{"餐饮", "交通", "购物", "娱乐", "住房", "医疗", "教育", "其他"};
                } else {
                    categories = new String[]{"工资", "兼职", "投资", "红包", "其他"};
                }
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(AddBillActivity.this,
                        android.R.layout.simple_spinner_item, categories);
                catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(catAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupDateButton() {
        updateDateButtonText();
        btnDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDate);
            DatePickerDialog picker = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate = cal.getTimeInMillis();
                        updateDateButtonText();
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });
    }

    private void updateDateButtonText() {
        btnDate.setText(DateUtils.formatDate(selectedDate));
    }

    private void loadAccounts() {
        executor.execute(() -> {
            long userId = prefsManager.getUserId();
            List<AccountEntity> accounts = accountDao.findByUserIdSync(userId);

            // Ensure all 4 default accounts exist
            java.util.Set<String> existingNames = new java.util.HashSet<>();
            if (accounts != null) {
                for (AccountEntity a : accounts) {
                    existingNames.add(a.getName());
                }
            }

            long now = System.currentTimeMillis();

            if (!existingNames.contains("现金")) {
                AccountEntity a = new AccountEntity();
                a.setUserId(userId);
                a.setName("现金");
                a.setType("cash");
                a.setBalance(0);
                a.setCreatedAt(now);
                long id = accountDao.insert(a);
                a.setId(id);
                if (accounts != null) accounts.add(a);
            }

            if (!existingNames.contains("微信支付")) {
                AccountEntity a = new AccountEntity();
                a.setUserId(userId);
                a.setName("微信支付");
                a.setType("wechat");
                a.setBalance(0);
                a.setCreatedAt(now);
                long id = accountDao.insert(a);
                a.setId(id);
                if (accounts != null) accounts.add(a);
            }

            if (!existingNames.contains("支付宝")) {
                AccountEntity a = new AccountEntity();
                a.setUserId(userId);
                a.setName("支付宝");
                a.setType("alipay");
                a.setBalance(0);
                a.setCreatedAt(now);
                long id = accountDao.insert(a);
                a.setId(id);
                if (accounts != null) accounts.add(a);
            }

            if (!existingNames.contains("银行卡")) {
                AccountEntity a = new AccountEntity();
                a.setUserId(userId);
                a.setName("银行卡");
                a.setType("bank");
                a.setBalance(0);
                a.setCreatedAt(now);
                long id = accountDao.insert(a);
                a.setId(id);
                if (accounts != null) accounts.add(a);
            }

            List<AccountEntity> finalAccounts = accounts;
            if (finalAccounts == null || finalAccounts.isEmpty()) {
                finalAccounts = new java.util.ArrayList<>();
            }
            List<AccountEntity> displayAccounts = finalAccounts;
            runOnUiThread(() -> {
                ArrayAdapter<AccountEntity> adapter = new ArrayAdapter<>(AddBillActivity.this,
                        android.R.layout.simple_spinner_item, displayAccounts);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAccount.setAdapter(adapter);
            });
        });
    }

    private void saveBill() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "金额格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isIncome = spinnerType.getSelectedItemPosition() == 1;
        String category = spinnerCategory.getSelectedItem() != null ?
                spinnerCategory.getSelectedItem().toString() : "其他";
        AccountEntity account = (AccountEntity) spinnerAccount.getSelectedItem();

        if (account == null) {
            Toast.makeText(this, "请选择账户", Toast.LENGTH_SHORT).show();
            return;
        }

        BillEntity bill = new BillEntity();
        bill.setUserId(prefsManager.getUserId());
        bill.setType(isIncome);
        bill.setCategory(category);
        bill.setAmount(amount);
        bill.setAccountId(account.getId());
        bill.setNote(etNote.getText().toString().trim());
        bill.setDate(selectedDate);
        bill.setCreatedAt(System.currentTimeMillis());

        executor.execute(() -> {
            billDao.insert(bill);

            // Update account balance directly in DB
            double balanceChange = isIncome ? amount : -amount;
            accountDao.addBalanceSync(account.getId(), balanceChange);

            // Check budgets after adding the bill
            boolean isExpense = !isIncome;
            List<String> overBudgetMessages = new ArrayList<>();
            if (isExpense) {
                long userId = prefsManager.getUserId();
                List<BudgetEntity> activeBudgets = budgetDao.findActiveBudgetsSync(userId, selectedDate);
                for (BudgetEntity budget : activeBudgets) {
                    if (budget.getStartDate() <= selectedDate) {
                        // Compute period boundaries based on budget type
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(selectedDate);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        long periodStart, periodEnd;
                        switch (budget.getType()) {
                            case "DAILY":
                                periodStart = cal.getTimeInMillis();
                                cal.set(Calendar.HOUR_OF_DAY, 23);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.SECOND, 59);
                                cal.set(Calendar.MILLISECOND, 999);
                                periodEnd = cal.getTimeInMillis();
                                break;
                            case "WEEKLY":
                                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                                int daysSinceMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                                cal.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);
                                periodStart = cal.getTimeInMillis();
                                cal.add(Calendar.DAY_OF_MONTH, 6);
                                cal.set(Calendar.HOUR_OF_DAY, 23);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.SECOND, 59);
                                cal.set(Calendar.MILLISECOND, 999);
                                periodEnd = cal.getTimeInMillis();
                                break;
                            case "MONTHLY":
                                cal.set(Calendar.DAY_OF_MONTH, 1);
                                periodStart = cal.getTimeInMillis();
                                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                                cal.set(Calendar.HOUR_OF_DAY, 23);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.SECOND, 59);
                                cal.set(Calendar.MILLISECOND, 999);
                                periodEnd = cal.getTimeInMillis();
                                break;
                            default:
                                periodStart = budget.getStartDate();
                                periodEnd = budget.getEndDate();
                                break;
                        }
                        Double totalExpense = billDao.getTotalByDateRangeAndTypeSync(userId, periodStart, periodEnd, false);
                        double actualExpense = totalExpense != null ? totalExpense : 0;
                        if (actualExpense > budget.getAmount()) {
                            String periodName;
                            switch (budget.getType()) {
                                case "DAILY":    periodName = "今日"; break;
                                case "WEEKLY":   periodName = "本周"; break;
                                case "MONTHLY":  periodName = "本月"; break;
                                default:         periodName = budget.getType(); break;
                            }
                            String categoryInfo = "";
                            if (budget.getCategory() != null) {
                                categoryInfo = "（" + budget.getCategory() + "）";
                            }
                            overBudgetMessages.add("请注意！您已超出" + periodName + categoryInfo + "预算");
                        }
                    }
                }
            }

            List<String> finalMessages = overBudgetMessages;
            runOnUiThread(() -> {
                Toast.makeText(AddBillActivity.this, "账单已添加", Toast.LENGTH_SHORT).show();
                if (!finalMessages.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String msg : finalMessages) {
                        sb.append(msg).append("\n");
                    }
                    new AlertDialog.Builder(AddBillActivity.this)
                            .setTitle("预算超支提醒")
                            .setMessage(sb.toString().trim())
                            .setPositiveButton("知道了", (dialog, which) -> finish())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    finish();
                }
            });
        });
    }
}
