package com.bill_prj.ui.bill;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bill_prj.R;
import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.AccountDao;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.utils.CategoryIconUtils;
import com.bill_prj.utils.Constants;
import com.bill_prj.utils.DateUtils;
import com.bill_prj.utils.NumberUtils;
import com.bill_prj.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private BillViewModel billViewModel;

    private TextView tvDateDisplay;
    private static final int RANGE_CUSTOM = 3;

    private MaterialButton btnToday, btnThisWeek, btnThisMonth, btnPickDate;
    private TextView tvTodayIncome, tvTodayExpense;
    private CardView cardBudget;
    private TextView tvBudgetRemaining;
    private ProgressBar progressBudget;
    private LinearLayout layoutRecentBills;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddBill;

    private SharedPrefsManager prefsManager;
    private Map<Long, String> accountNames = new HashMap<>();

    // Date range state
    private int selectedDateRange = Constants.RANGE_TODAY;
    private Calendar currentCalendar = Calendar.getInstance();
    private long currentStartMillis, currentEndMillis;

    private String getBudgetTypeForRange() {
        switch (selectedDateRange) {
            case Constants.RANGE_TODAY: return "DAILY";
            case Constants.RANGE_WEEK:  return "WEEKLY";
            case Constants.RANGE_MONTH: return "MONTHLY";
            default:                    return "CUSTOM";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(root);
        setupViewModel();
        setupListeners();
        loadData();

        return root;
    }

    private void initViews(View root) {
        tvDateDisplay = root.findViewById(R.id.tvDateDisplay);
        btnToday = root.findViewById(R.id.btnToday);
        btnThisWeek = root.findViewById(R.id.btnThisWeek);
        btnThisMonth = root.findViewById(R.id.btnThisMonth);
        btnPickDate = root.findViewById(R.id.btnPickDate);
        tvTodayIncome = root.findViewById(R.id.tvTodayIncome);
        tvTodayExpense = root.findViewById(R.id.tvTodayExpense);
        cardBudget = root.findViewById(R.id.cardBudget);
        tvBudgetRemaining = root.findViewById(R.id.tvBudgetRemaining);
        progressBudget = root.findViewById(R.id.progressBudget);
        layoutRecentBills = root.findViewById(R.id.layoutRecentBills);
        tvEmptyState = root.findViewById(R.id.tvEmptyState);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        fabAddBill = root.findViewById(R.id.fabAddBill);

        prefsManager = new SharedPrefsManager(getActivity());

        updateDateDisplay();
    }

    private void loadAccountNames() {
        new Thread(() -> {
            AccountDao accountDao = AppDatabase.getInstance(requireContext()).accountDao();
            long userId = prefsManager.getUserId();
            List<AccountEntity> accounts = accountDao.findByUserIdSync(userId);
            Map<Long, String> nameMap = new HashMap<>();
            if (accounts != null) {
                for (AccountEntity a : accounts) {
                    nameMap.put(a.getId(), a.getName());
                }
            }
            accountNames = nameMap;
        }).start();
    }

    private void setupViewModel() {
        billViewModel = new ViewModelProvider(this).get(BillViewModel.class);

        // Load account names for display
        loadAccountNames();

        billViewModel.getTodayIncomeLiveData().observe(getViewLifecycleOwner(), income -> {
            tvTodayIncome.setText(NumberUtils.formatCurrency(income));
        });

        billViewModel.getTodayExpenseLiveData().observe(getViewLifecycleOwner(), expense -> {
            tvTodayExpense.setText(NumberUtils.formatCurrency(expense));
        });

        billViewModel.getRecentBillsLiveData().observe(getViewLifecycleOwner(), bills -> {
            updateRecentBills(bills);
        });

        billViewModel.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            updateBudgetProgress(budget);
        });
    }

    private void setupListeners() {
        btnToday.setOnClickListener(v -> {
            selectedDateRange = Constants.RANGE_TODAY;
            currentCalendar = Calendar.getInstance();
            updateDateSelection();
            loadData();
        });

        btnThisWeek.setOnClickListener(v -> {
            selectedDateRange = Constants.RANGE_WEEK;
            currentCalendar = Calendar.getInstance();
            updateDateSelection();
            loadData();
        });

        btnThisMonth.setOnClickListener(v -> {
            selectedDateRange = Constants.RANGE_MONTH;
            currentCalendar = Calendar.getInstance();
            updateDateSelection();
            loadData();
        });

        btnPickDate.setOnClickListener(v -> {
            int year = currentCalendar.get(Calendar.YEAR);
            int month = currentCalendar.get(Calendar.MONTH);
            int day = currentCalendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        currentCalendar.set(Calendar.YEAR, selectedYear);
                        currentCalendar.set(Calendar.MONTH, selectedMonth);
                        currentCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        selectedDateRange = RANGE_CUSTOM;
                        updateDateSelection();
                        loadData();
                    }, year, month, day);
            datePickerDialog.show();
        });

        fabAddBill.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddBillActivity.class);
            startActivityForResult(intent, Constants.REQUEST_ADD_BILL);
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void updateDateDisplay() {
        String dateStr = DateUtils.formatFullDate(currentCalendar.getTimeInMillis());
        tvDateDisplay.setText(dateStr);
    }

    private void updateDateSelection() {
        btnToday.setStrokeWidth(selectedDateRange == Constants.RANGE_TODAY ? 3 : 1);
        btnThisWeek.setStrokeWidth(selectedDateRange == Constants.RANGE_WEEK ? 3 : 1);
        btnThisMonth.setStrokeWidth(selectedDateRange == Constants.RANGE_MONTH ? 3 : 1);
        btnPickDate.setStrokeWidth(selectedDateRange == RANGE_CUSTOM ? 3 : 1);

        btnToday.setAlpha(selectedDateRange == Constants.RANGE_TODAY ? 1.0f : 0.6f);
        btnThisWeek.setAlpha(selectedDateRange == Constants.RANGE_WEEK ? 1.0f : 0.6f);
        btnThisMonth.setAlpha(selectedDateRange == Constants.RANGE_MONTH ? 1.0f : 0.6f);
        btnPickDate.setAlpha(selectedDateRange == RANGE_CUSTOM ? 1.0f : 0.6f);
    }

    private void loadData() {
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        switch (selectedDateRange) {
            case Constants.RANGE_TODAY:
                // Start of today
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                // End of today
                endCal.setTimeInMillis(startCal.getTimeInMillis());
                endCal.add(Calendar.DAY_OF_MONTH, 1);
                endCal.add(Calendar.MILLISECOND, -1);
                break;
            case Constants.RANGE_WEEK:
                // Start of this week (Monday) - manually calculate to avoid locale issues
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
                // daysSinceMonday: Monday=0, Tuesday=1, ..., Sunday=6
                int daysSinceMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
                startCal.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);
                // End of this week (Sunday)
                endCal.setTimeInMillis(startCal.getTimeInMillis());
                endCal.add(Calendar.DAY_OF_MONTH, 6);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
            case Constants.RANGE_MONTH:
                // Start of this month
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                // End of this month
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MILLISECOND, 999);
                break;
            case RANGE_CUSTOM:
                // Use the selected date
                startCal.setTimeInMillis(currentCalendar.getTimeInMillis());
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
        }

        long startMillis = startCal.getTimeInMillis();
        long endMillis = endCal.getTimeInMillis();
        currentStartMillis = startMillis;
        currentEndMillis = endMillis;

        // Pass the correct logged-in userId instead of defaulting to 1L
        long userId = prefsManager.getUserId();
        billViewModel.loadBills(userId, startMillis, endMillis);
        billViewModel.loadTodaySummary(startMillis, endMillis, getBudgetTypeForRange());

        // Update date display
        updateDateDisplay();
    }

    private void updateRecentBills(List<BillEntity> bills) {
        layoutRecentBills.removeAllViews();
        if (bills == null || bills.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            layoutRecentBills.setVisibility(View.GONE);
            return;
        }

        tvEmptyState.setVisibility(View.GONE);
        layoutRecentBills.setVisibility(View.VISIBLE);

        int count = Math.min(bills.size(), 5);
        for (int i = 0; i < count; i++) {
            BillEntity bill = bills.get(i);
            View billItemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_bill_item, layoutRecentBills, false);

            TextView tvCategoryName = billItemView.findViewById(R.id.tvCategoryName);
            TextView tvBillNote = billItemView.findViewById(R.id.tvBillNote);
            TextView tvBillAmount = billItemView.findViewById(R.id.tvBillAmount);
            TextView tvBillAccount = billItemView.findViewById(R.id.tvBillAccount);

            tvCategoryName.setText(bill.getCategoryName());
            tvBillNote.setText(bill.getNote() != null && !bill.getNote().isEmpty()
                    ? bill.getNote() : bill.getCategoryName());
            tvBillAmount.setText(NumberUtils.formatCurrency(bill.getAmount()));

            // Set account name
            String accountName = accountNames.get(bill.getAccountId());
            tvBillAccount.setText(accountName != null ? accountName : "");

            // Set type strip color
            View billStrip = billItemView.findViewById(R.id.view_bill_strip);
            if (billStrip != null) {
                int stripColor = BillEntity.TYPE_INCOME.equals(bill.getType())
                        ? 0xFF9CB8A8 : 0xFFC9887E;
                billStrip.setBackgroundColor(stripColor);
                billStrip.setVisibility(View.VISIBLE);
            }

            // Set color based on type
            if (BillEntity.TYPE_INCOME.equals(bill.getType())) {
                tvBillAmount.setTextColor(getResources().getColor(R.color.color_income, null));
            } else {
                tvBillAmount.setTextColor(getResources().getColor(R.color.color_expense, null));
            }

            // Set category icon
            View viewCategoryCircle = billItemView.findViewById(R.id.viewCategoryCircle);
            TextView tvCategoryLetter = billItemView.findViewById(R.id.tvCategoryLetter);

            String categoryName = bill.getCategoryName();
            tvCategoryLetter.setText(CategoryIconUtils.getIcon(categoryName));

            // Set icon background color
            GradientDrawable circleBg = (GradientDrawable) viewCategoryCircle.getBackground();
            if (circleBg != null) {
                circleBg.setColor(CategoryIconUtils.getColorResId(categoryName));
            }

            // Click to view detail
            long billId = bill.getId();
            billItemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), BillDetailActivity.class);
                intent.putExtra("bill_id", billId);
                startActivity(intent);
            });

            layoutRecentBills.addView(billItemView);
        }
    }

    private void updateBudgetProgress(Double budget) {
        if (budget == null || budget <= 0) {
            cardBudget.setVisibility(View.GONE);
            return;
        }

        cardBudget.setVisibility(View.VISIBLE);

        // Use the current period's date range (today/week/month)
        billViewModel.getTotalExpense(currentStartMillis, currentEndMillis)
                .observe(getViewLifecycleOwner(), totalExpense -> {
                    double totalExp = totalExpense != null ? totalExpense : 0.0;
                    double remaining = budget - totalExp;

                    tvBudgetRemaining.setText(remaining >= 0
                            ? NumberUtils.formatCurrency(remaining)
                            : getString(R.string.budget_remaining) + " -" + NumberUtils.formatCurrency(Math.abs(remaining)));

                    int progress = budget > 0 ? (int) ((totalExp / budget) * 100) : 0;
                    progressBudget.setProgress(Math.min(progress, 100));
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            loadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
