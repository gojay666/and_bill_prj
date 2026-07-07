package com.bill_prj.ui.budget;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.data.dao.BudgetDao;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.entity.BudgetEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetDao budgetDao;
    private final BillDao billDao;
    private final Executor executor;
    private final MediatorLiveData<List<BudgetEntity>> budgetsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<String> alertMessage = new MutableLiveData<>();
    private LiveData<List<BudgetEntity>> currentSource;

    public BudgetViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.budgetDao = db.budgetDao();
        this.billDao = db.billDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<BudgetEntity>> getBudgetsByType(long userId, String type) {
        LiveData<List<BudgetEntity>> source = budgetDao.findByType(userId, type);
        if (currentSource != null) {
            budgetsLiveData.removeSource(currentSource);
        }
        budgetsLiveData.addSource(source, budgets -> {
            if (budgets != null) {
                recalculateUsedAmounts(budgets);
            } else {
                budgetsLiveData.setValue(new ArrayList<>());
            }
        });
        currentSource = source;
        return budgetsLiveData;
    }

    /**
     * Force-refresh budget calculations on resume even if Room data hasn't changed.
     */
    public void refreshCurrentBudgets() {
        List<BudgetEntity> current = budgetsLiveData.getValue();
        if (current != null && !current.isEmpty()) {
            recalculateUsedAmounts(current);
        }
    }

    private void recalculateUsedAmounts(List<BudgetEntity> budgets) {
        executor.execute(() -> {
            for (BudgetEntity budget : budgets) {
                double used = 0;
                long budgetUserId = budget.getUserId();
                String category = budget.getCategory();
                String type = budget.getType();

                // Dynamically calculate period based on budget type and current time
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long periodStart, periodEnd;

                if ("DAILY".equals(type)) {
                    periodStart = cal.getTimeInMillis();
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    periodEnd = cal.getTimeInMillis();
                } else if ("WEEKLY".equals(type)) {
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
                } else { // MONTHLY
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    periodStart = cal.getTimeInMillis();
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    periodEnd = cal.getTimeInMillis();
                }

                if (category == null || category.isEmpty() || "全部类别".equals(category)) {
                    // All categories - sum all expenses in period
                    Double total = billDao.getTotalByDateRangeAndTypeSync(
                            budgetUserId, periodStart, periodEnd, false);
                    used = total != null ? total : 0;
                } else {
                    // Specific category - sum expenses for that category in period
                    List<BillEntity> bills = billDao.findByCategoryAndDateRangeSync(
                            budgetUserId, category, periodStart, periodEnd);
                    for (BillEntity bill : bills) {
                        if (!bill.isType()) { // expense only
                            used += bill.getAmount();
                        }
                    }
                }
                budget.setUsedAmount(used);
            }
            budgetsLiveData.postValue(new ArrayList<>(budgets));
        });
    }

    public LiveData<String> getAlertMessage() {
        return alertMessage;
    }

    public void saveBudget(BudgetEntity budget) {
        executor.execute(() -> {
            budgetDao.insert(budget);
        });
    }

    public void updateBudget(BudgetEntity budget) {
        executor.execute(() -> budgetDao.update(budget));
    }

    public void deleteBudget(BudgetEntity budget) {
        executor.execute(() -> budgetDao.delete(budget));
    }

    public void checkBudgetStatus(long budgetId) {
        executor.execute(() -> {
            BudgetEntity budget = budgetDao.findByIdSync(budgetId);
            if (budget != null && budget.getUsedAmount() > budget.getAmount()) {
                double excessPercent = ((budget.getUsedAmount() - budget.getAmount()) / budget.getAmount()) * 100;
                String msg = String.format("预算超支提醒：%s 已超额 %.1f%%！\n预算: ¥%.2f, 已用: ¥%.2f",
                        budget.getCategory() != null ? budget.getCategory() : "全部类别",
                        excessPercent, budget.getAmount(), budget.getUsedAmount());
                alertMessage.postValue(msg);
            } else if (budget != null) {
                alertMessage.postValue("预算状态正常，未超支");
            }
        });
    }

    public void updateBudgetAmount(long budgetId, double newAmount) {
        executor.execute(() -> {
            BudgetEntity budget = budgetDao.findByIdSync(budgetId);
            if (budget != null) {
                budget.setAmount(newAmount);
                budget.setUpdatedAt(System.currentTimeMillis());
                budgetDao.update(budget);
            }
        });
    }
}
