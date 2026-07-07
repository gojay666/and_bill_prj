package com.bill_prj.ui.statistics;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.repository.BillRepository;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatisticsViewModel extends AndroidViewModel {

    private final BillDao billDao;
    private final Executor executor;
    private final MutableLiveData<List<Entry>> yearlyIncomeData = new MutableLiveData<>();
    private final MutableLiveData<List<Entry>> yearlyExpenseData = new MutableLiveData<>();
    private final MutableLiveData<List<BarEntry>> monthlyComparisonData = new MutableLiveData<>();
    private final MutableLiveData<List<PieEntry>> categoryExpenseData = new MutableLiveData<>();
    private final MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    private final MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    private final MutableLiveData<List<BillDao.CategoryTotal>> categoryBreakdown = new MutableLiveData<>();
    private final MutableLiveData<List<MonthlyData>> monthlyDataList = new MutableLiveData<>();

    private long userId;

    public StatisticsViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.billDao = db.billDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public LiveData<List<Entry>> getYearlyIncomeData() {
        return yearlyIncomeData;
    }

    public LiveData<List<Entry>> getYearlyExpenseData() {
        return yearlyExpenseData;
    }

    public LiveData<List<BarEntry>> getMonthlyComparisonData() {
        return monthlyComparisonData;
    }

    public LiveData<List<PieEntry>> getCategoryExpenseData() {
        return categoryExpenseData;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<List<BillDao.CategoryTotal>> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public LiveData<List<MonthlyData>> getMonthlyDataList() {
        return monthlyDataList;
    }

    public void loadYearlyData(int year) {
        executor.execute(() -> {
            long startOfYear = getStartOfYear(year);
            long endOfYear = getEndOfYear(year);

            List<Entry> incomeEntries = new ArrayList<>();
            List<Entry> expenseEntries = new ArrayList<>();
            List<BarEntry> barEntries = new ArrayList<>();
            List<MonthlyData> monthlyList = new ArrayList<>();

            for (int month = 0; month < 12; month++) {
                long startOfMonth = getStartOfMonth(year, month);
                long endOfMonth = getEndOfMonth(year, month);

                Double income = billDao.getTotalByDateRangeAndTypeSync(userId, startOfMonth, endOfMonth, true);
                Double expense = billDao.getTotalByDateRangeAndTypeSync(userId, startOfMonth, endOfMonth, false);

                float incomeVal = income != null ? income.floatValue() : 0;
                float expenseVal = expense != null ? expense.floatValue() : 0;

                incomeEntries.add(new Entry(month, incomeVal));
                expenseEntries.add(new Entry(month, expenseVal));
                barEntries.add(new BarEntry(month, new float[]{incomeVal, expenseVal}));
                monthlyList.add(new MonthlyData(month, incomeVal, expenseVal, incomeVal - expenseVal));
            }

            yearlyIncomeData.postValue(incomeEntries);
            yearlyExpenseData.postValue(expenseEntries);
            monthlyComparisonData.postValue(barEntries);
            monthlyDataList.postValue(monthlyList);

            Double totalInc = billDao.getTotalByDateRangeAndTypeSync(userId, startOfYear, endOfYear, true);
            Double totalExp = billDao.getTotalByDateRangeAndTypeSync(userId, startOfYear, endOfYear, false);
            totalIncome.postValue(totalInc != null ? totalInc : 0);
            totalExpense.postValue(totalExp != null ? totalExp : 0);

            List<BillDao.CategoryTotal> expenseCategories = billDao.getTotalByCategorySync(
                    userId, startOfYear, endOfYear, false);
            categoryBreakdown.postValue(expenseCategories);

            List<PieEntry> pieEntries = new ArrayList<>();
            if (expenseCategories != null) {
                for (BillDao.CategoryTotal ct : expenseCategories) {
                    pieEntries.add(new PieEntry((float) ct.total, ct.category));
                }
            }
            categoryExpenseData.postValue(pieEntries);
        });
    }

    private long getStartOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    private long getStartOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static class MonthlyData {
        private final int month;
        private final float income;
        private final float expense;
        private final float balance;

        public MonthlyData(int month, float income, float expense, float balance) {
            this.month = month;
            this.income = income;
            this.expense = expense;
            this.balance = balance;
        }

        public int getMonth() { return month; }
        public float getIncome() { return income; }
        public float getExpense() { return expense; }
        public float getBalance() { return balance; }
    }
}
