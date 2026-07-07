package com.bill_prj.ui.bill;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.AccountDao;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.data.dao.BudgetDao;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.entity.BudgetEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BillViewModel extends AndroidViewModel {

    private final BillDao billDao;
    private final BudgetDao budgetDao;
    private final AccountDao accountDao;
    private final Executor executor;
    private final MutableLiveData<Long> userIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> startTimeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> endTimeLiveData = new MutableLiveData<>();

    private final LiveData<List<BillEntity>> bills;

    public BillViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.billDao = db.billDao();
        this.budgetDao = db.budgetDao();
        this.accountDao = db.accountDao();
        this.executor = Executors.newSingleThreadExecutor();

        bills = Transformations.switchMap(userIdLiveData, uid -> {
            Long start = startTimeLiveData.getValue();
            Long end = endTimeLiveData.getValue();
            if (uid != null && start != null && end != null) {
                return billDao.findByDateRange(uid, start, end);
            }
            return new MutableLiveData<>(null);
        });
    }

    public void loadBills(long userId, long startTime, long endTime) {
        userIdLiveData.setValue(userId);
        startTimeLiveData.setValue(startTime);
        endTimeLiveData.setValue(endTime);
    }

    public LiveData<List<BillEntity>> getBills() {
        return bills;
    }

    public LiveData<Double> getTotalIncome(long userId, long startTime, long endTime) {
        return billDao.getTotalByDateRangeAndType(userId, startTime, endTime, true);
    }

    public LiveData<Double> getTotalExpense(long userId, long startTime, long endTime) {
        return billDao.getTotalByDateRangeAndType(userId, startTime, endTime, false);
    }

    // Additional LiveData fields
    private final MutableLiveData<Double> todayIncomeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> todayExpenseLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<BillEntity>> recentBillsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> monthlyBudgetLiveData = new MutableLiveData<>();

    public void loadTodaySummary(long startTime, long endTime, String budgetType) {
        long userId = userIdLiveData.getValue() != null ? userIdLiveData.getValue() : 1L;
        executor.execute(() -> {
            try {
                // Get income and expense totals from DAO
                Double income = billDao.getTotalByDateRangeAndTypeSync(userId, startTime, endTime, true);
                Double expense = billDao.getTotalByDateRangeAndTypeSync(userId, startTime, endTime, false);
                List<BillEntity> recent = billDao.findByDateRangeSync(userId, startTime, endTime);

                Double finalIncome = income != null ? income : 0.0;
                Double finalExpense = expense != null ? expense : 0.0;
                todayIncomeLiveData.postValue(finalIncome);
                todayExpenseLiveData.postValue(finalExpense);
                recentBillsLiveData.postValue(recent);

                // Load budget matching the selected period type
                if ("CUSTOM".equals(budgetType)) {
                    monthlyBudgetLiveData.postValue(null);
                } else {
                    List<BudgetEntity> activeBudgets = budgetDao.findByUserIdAndTypeSync(userId, budgetType);
                    if (activeBudgets != null && !activeBudgets.isEmpty()) {
                        monthlyBudgetLiveData.postValue(activeBudgets.get(0).getAmount());
                    } else {
                        monthlyBudgetLiveData.postValue(null);
                    }
                }
            } catch (Exception e) {
                todayIncomeLiveData.postValue(0.0);
                todayExpenseLiveData.postValue(0.0);
                recentBillsLiveData.postValue(null);
            }
        });
    }

    public LiveData<Double> getTodayIncomeLiveData() {
        return todayIncomeLiveData;
    }

    public LiveData<Double> getTodayExpenseLiveData() {
        return todayExpenseLiveData;
    }

    public LiveData<List<BillEntity>> getRecentBillsLiveData() {
        return recentBillsLiveData;
    }

    public LiveData<Double> getMonthlyBudget() {
        return monthlyBudgetLiveData;
    }

    public LiveData<List<BillDao.CategoryTotal>> getCategoryTotals(long userId, long startTime, long endTime, boolean isIncome) {
        MutableLiveData<List<BillDao.CategoryTotal>> result = new MutableLiveData<>();
        executor.execute(() -> {
            List<BillDao.CategoryTotal> list = billDao.getTotalByCategorySync(userId, startTime, endTime, isIncome);
            result.postValue(list);
        });
        return result;
    }

    // 2-param version of getTotalExpense (uses userId from liveData)
    public LiveData<Double> getTotalExpense(long startTime, long endTime) {
        Long userId = userIdLiveData.getValue();
        if (userId != null) {
            return billDao.getTotalByDateRangeAndType(userId, startTime, endTime, false);
        }
        return new MutableLiveData<>(0.0);
    }

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /** Reverse the account balance change when a bill is deleted. */
    private void reverseAccountBalance(BillEntity bill) {
        double balanceChange = bill.isType() ? -bill.getAmount() : bill.getAmount();
        accountDao.addBalanceSync(bill.getAccountId(), balanceChange);
    }

    public void deleteBill(BillEntity bill) {
        executor.execute(() -> {
            try {
                reverseAccountBalance(bill);
                billDao.delete(bill);
            } catch (Exception e) {
                errorMessage.postValue("删除失败: " + e.getMessage());
            }
        });
    }

    public void deleteBills(List<BillEntity> bills) {
        executor.execute(() -> {
            try {
                for (BillEntity bill : bills) {
                    reverseAccountBalance(bill);
                    billDao.delete(bill);
                }
            } catch (Exception e) {
                errorMessage.postValue("删除失败: " + e.getMessage());
            }
        });
    }
}
