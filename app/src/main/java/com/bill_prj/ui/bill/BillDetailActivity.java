package com.bill_prj.ui.bill;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bill_prj.R;
import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.AccountDao;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.utils.CategoryIconUtils;
import com.bill_prj.utils.CurrencyUtils;
import com.bill_prj.utils.DateUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillDetailActivity extends AppCompatActivity {

    private TextView tvDetailCategory, tvDetailAmount, tvDetailAccount, tvDetailDate, tvDetailNote;
    private TextView tvDetailIconText, tvDetailTypeBadge;
    private View viewDetailIconBg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("账单详情");
        }

        tvDetailCategory = findViewById(R.id.tv_detail_category);
        tvDetailAmount = findViewById(R.id.tv_detail_amount);
        tvDetailAccount = findViewById(R.id.tv_detail_account);
        tvDetailDate = findViewById(R.id.tv_detail_date);
        tvDetailNote = findViewById(R.id.tv_detail_note);
        tvDetailIconText = findViewById(R.id.tv_detail_icon_text);
        tvDetailTypeBadge = findViewById(R.id.tv_detail_type_badge);
        viewDetailIconBg = findViewById(R.id.view_detail_icon_bg);

        long billId = getIntent().getLongExtra("bill_id", -1);
        if (billId == -1) {
            finish();
            return;
        }

        loadBill(billId);
    }

    private void loadBill(long billId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            BillEntity bill = db.billDao().getBillByIdSync(billId);

            if (bill == null) {
                runOnUiThread(this::finish);
                return;
            }

            // Load account names
            AccountDao accountDao = db.accountDao();
            List<AccountEntity> accounts = accountDao.findByUserIdSync(bill.getUserId());
            Map<Long, String> nameMap = new HashMap<>();
            if (accounts != null) {
                for (AccountEntity a : accounts) {
                    nameMap.put(a.getId(), a.getName());
                }
            }

            Map<Long, String> finalNameMap = nameMap;
            runOnUiThread(() -> bindData(bill, finalNameMap));
        }).start();
    }

    private void bindData(BillEntity bill, Map<Long, String> accountNames) {
        String category = bill.getCategory();
        boolean isIncome = bill.isType();

        // Category icon
        String iconText = CategoryIconUtils.getIcon(category);
        tvDetailIconText.setText(iconText);

        GradientDrawable iconBg = (GradientDrawable) viewDetailIconBg.getBackground();
        if (iconBg != null) {
            iconBg.setColor(CategoryIconUtils.getColorResId(category));
        }

        // Category name
        tvDetailCategory.setText(category != null ? category : "未分类");

        // Type badge
        tvDetailTypeBadge.setText(isIncome ? "收入" : "支出");
        GradientDrawable badgeBg = (GradientDrawable) tvDetailTypeBadge.getBackground();
        if (badgeBg != null) {
            badgeBg.setColor(isIncome ? 0xFF22C55E : 0xFFEF4444);
        }

        // Amount
        String amountText = isIncome
                ? CurrencyUtils.formatIncome(bill.getAmount())
                : CurrencyUtils.formatExpense(bill.getAmount());
        tvDetailAmount.setText(amountText);
        tvDetailAmount.setTextColor(isIncome ? 0xFF22C55E : 0xFFEF4444);

        // Account
        String accountName = accountNames != null ? accountNames.get(bill.getAccountId()) : null;
        tvDetailAccount.setText(accountName != null ? accountName : "账户 #" + bill.getAccountId());

        // Date
        tvDetailDate.setText(DateUtils.formatDateTime(bill.getDate()));

        // Note
        String note = bill.getNote();
        if (note != null && !note.isEmpty()) {
            tvDetailNote.setText(note);
            tvDetailNote.setTextColor(0xFF1E293B);
        } else {
            tvDetailNote.setText("无备注");
            tvDetailNote.setTextColor(0xFF94A3B8);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
