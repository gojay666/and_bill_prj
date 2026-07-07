package com.bill_prj.ui.account;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bill_prj.R;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.repository.AccountRepository;
import com.bill_prj.utils.NumberUtils;
import com.bill_prj.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AccountManageActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvTotalBalance;
    private RecyclerView rvAccounts;
    private FloatingActionButton fabAddAccount;

    private AccountAdapter accountAdapter;
    private AccountRepository accountRepository;
    private SharedPrefsManager prefsManager;
    private Executor executor = Executors.newSingleThreadExecutor();
    private List<AccountEntity> accountList = new ArrayList<>();
    private long userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);

        accountRepository = new AccountRepository(this);
        prefsManager = new SharedPrefsManager(this);
        userId = prefsManager.getUserId();
        if (userId == -1) userId = 1; // fallback

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadAccounts();
        setupFab();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        rvAccounts = findViewById(R.id.rvAccounts);
        fabAddAccount = findViewById(R.id.fabAddAccount);
    }

    private void setupToolbar() {
        toolbar.setTitle("管理账户");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        accountAdapter = new AccountAdapter(accountList);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvAccounts.setAdapter(accountAdapter);

        accountAdapter.setOnAccountLongClickListener((account, position) -> {
            showAccountActionDialog(account, position);
        });
    }

    private void loadAccounts() {
        executor.execute(() -> {
            try {
                List<AccountEntity> accounts = accountRepository.getAccountsByUserSync(userId);
                runOnUiThread(() -> {
                    accountList.clear();
                    accountList.addAll(accounts);
                    accountAdapter.notifyDataSetChanged();
                    updateTotalBalance();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "加载账户失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateTotalBalance() {
        double total = 0;
        for (AccountEntity account : accountList) {
            total += account.getBalance();
        }
        tvTotalBalance.setText(NumberUtils.formatCurrency(total));
    }

    private void setupFab() {
        fabAddAccount.setOnClickListener(v -> showAddAccountDialog());
    }

    private void showAddAccountDialog() {
        showAccountDialog(null, -1);
    }

    private void showAccountDialog(@Nullable AccountEntity existingAccount, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingAccount != null ? R.string.edit_account : R.string.add_account);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null);
        builder.setView(dialogView);

        EditText etAccountName = dialogView.findViewById(R.id.etAccountName);
        Spinner spinnerAccountType = dialogView.findViewById(R.id.spinnerAccountType);
        EditText etInitialBalance = dialogView.findViewById(R.id.etInitialBalance);

        // Setup account type spinner
        String[] accountTypes = {"现金", "银行卡", "信用卡", "支付宝", "微信支付", "其他"};
        String[] accountTypeValues = {"cash", "bank", "credit_card", "alipay", "wechat", "other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, accountTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setAdapter(typeAdapter);

        // Pre-fill if editing
        if (existingAccount != null) {
            etAccountName.setText(existingAccount.getName());

            String type = existingAccount.getType();
            for (int i = 0; i < accountTypeValues.length; i++) {
                if (accountTypeValues[i].equals(type)) {
                    spinnerAccountType.setSelection(i);
                    break;
                }
            }

            etInitialBalance.setText(NumberUtils.formatDecimal(existingAccount.getBalance()));
        } else {
            etInitialBalance.setText("0.00");
        }

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String name = etAccountName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, getString(R.string.account_name_required), Toast.LENGTH_SHORT).show();
                return;
            }

            int typeIndex = spinnerAccountType.getSelectedItemPosition();
            String typeValue = typeIndex >= 0 && typeIndex < accountTypeValues.length
                    ? accountTypeValues[typeIndex] : "cash";

            double balance = 0;
            try {
                balance = Double.parseDouble(etInitialBalance.getText().toString().trim());
            } catch (NumberFormatException ignored) {
            }

            AccountEntity account;
            if (existingAccount != null) {
                account = existingAccount;
                account.setName(name);
                account.setType(typeValue);
                account.setBalance(balance);
            } else {
                account = new AccountEntity();
                account.setName(name);
                account.setType(typeValue);
                account.setBalance(balance);
                account.setUserId(getDefaultUserId());
            }

            saveAccount(account);
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void showAccountActionDialog(final AccountEntity account, final int position) {
        String[] options = {getString(R.string.edit_account), getString(R.string.delete)};

        new AlertDialog.Builder(this)
                .setTitle(account.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit
                        showAccountDialog(account, position);
                    } else if (which == 1) {
                        // Delete
                        new AlertDialog.Builder(this)
                                .setTitle(getString(R.string.delete_account_confirm))
                                .setPositiveButton(R.string.delete, (confirmDialog, which1) -> {
                                    deleteAccount(account);
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                })
                .show();
    }

    private void saveAccount(AccountEntity account) {
        executor.execute(() -> {
            try {
                if (account.getId() > 0) {
                    accountRepository.update(account);
                } else {
                    accountRepository.insert(account);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.account_saved), Toast.LENGTH_SHORT).show();
                    loadAccounts();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void deleteAccount(AccountEntity account) {
        executor.execute(() -> {
            try {
                accountRepository.delete(account);
                runOnUiThread(() -> {
                    Toast.makeText(this, getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                    loadAccounts();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                    Toast.makeText(this, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private long getDefaultUserId() {
        return userId;
    }
}
