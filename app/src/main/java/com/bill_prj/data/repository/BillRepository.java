package com.bill_prj.data.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.data.entity.BillEntity;

import java.util.Calendar;
import java.util.List;

public class BillRepository {

    private final BillDao billDao;

    public BillRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.billDao = db.billDao();
    }

    public void insertBill(BillEntity bill, final RepositoryCallback<Long> callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                return billDao.insert(bill);
            }

            @Override
            protected void onPostExecute(Long result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void updateBill(BillEntity bill, final RepositoryCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                billDao.update(bill);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void deleteBill(BillEntity bill, final RepositoryCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                billDao.delete(bill);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public LiveData<List<BillEntity>> getBillsByUserId(long userId) {
        return billDao.findByUserId(userId);
    }

    public LiveData<List<BillEntity>> getBillsByDateRange(long userId, long startDate, long endDate) {
        return billDao.findByDateRange(userId, startDate, endDate);
    }

    public LiveData<List<BillEntity>> getBillsByCategory(long userId, String category) {
        return billDao.findByCategory(userId, category);
    }

    public LiveData<List<BillEntity>> getBillsByAccount(long accountId) {
        return billDao.findByAccountId(accountId);
    }

    public void getBillsByDateRangeSync(long userId, long startDate, long endDate,
                                         final RepositoryCallback<List<BillEntity>> callback) {
        new AsyncTask<Void, Void, List<BillEntity>>() {
            @Override
            protected List<BillEntity> doInBackground(Void... voids) {
                return billDao.findByDateRangeSync(userId, startDate, endDate);
            }

            @Override
            protected void onPostExecute(List<BillEntity> result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void getTotalIncome(long startDate, long endDate, long userId,
                                final RepositoryCallback<Double> callback) {
        new AsyncTask<Void, Void, Double>() {
            @Override
            protected Double doInBackground(Void... voids) {
                Double total = billDao.getTotalByDateRangeAndTypeSync(userId, startDate, endDate, true);
                return total != null ? total : 0.0;
            }

            @Override
            protected void onPostExecute(Double result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void getTotalExpense(long startDate, long endDate, long userId,
                                 final RepositoryCallback<Double> callback) {
        new AsyncTask<Void, Void, Double>() {
            @Override
            protected Double doInBackground(Void... voids) {
                Double total = billDao.getTotalByDateRangeAndTypeSync(userId, startDate, endDate, false);
                return total != null ? total : 0.0;
            }

            @Override
            protected void onPostExecute(Double result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void getIncomeByCategory(long userId, long startDate, long endDate,
                                     final RepositoryCallback<List<BillDao.CategoryTotal>> callback) {
        new AsyncTask<Void, Void, List<BillDao.CategoryTotal>>() {
            @Override
            protected List<BillDao.CategoryTotal> doInBackground(Void... voids) {
                return billDao.getTotalByCategorySync(userId, startDate, endDate, true);
            }

            @Override
            protected void onPostExecute(List<BillDao.CategoryTotal> result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void getExpenseByCategory(long userId, long startDate, long endDate,
                                      final RepositoryCallback<List<BillDao.CategoryTotal>> callback) {
        new AsyncTask<Void, Void, List<BillDao.CategoryTotal>>() {
            @Override
            protected List<BillDao.CategoryTotal> doInBackground(Void... voids) {
                return billDao.getTotalByCategorySync(userId, startDate, endDate, false);
            }

            @Override
            protected void onPostExecute(List<BillDao.CategoryTotal> result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void getMonthlyReport(int year, long userId,
                                  final RepositoryCallback<List<BillDao.CategoryTotal>> callback) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfYear = cal.getTimeInMillis();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfYear = cal.getTimeInMillis();

        getTotalExpense(startOfYear, endOfYear, userId, new RepositoryCallback<Double>() {
            @Override
            public void onSuccess(Double result) {
                // Used internally; yearly report can be expanded in the future
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getYearlyReport(int year, long userId,
                                 final RepositoryCallback<List<BillDao.CategoryTotal>> callback) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfYear = cal.getTimeInMillis();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfYear = cal.getTimeInMillis();

        getExpenseByCategory(userId, startOfYear, endOfYear, callback);
    }
}
