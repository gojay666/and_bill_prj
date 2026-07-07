package com.bill_prj.data.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.BudgetDao;
import com.bill_prj.data.dao.BudgetHistoryDao;
import com.bill_prj.data.entity.BudgetEntity;
import com.bill_prj.data.entity.BudgetHistoryEntity;

import java.util.List;

public class BudgetRepository {

    private final BudgetDao budgetDao;
    private final BudgetHistoryDao budgetHistoryDao;

    public BudgetRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.budgetDao = db.budgetDao();
        this.budgetHistoryDao = db.budgetHistoryDao();
    }

    public void insertBudget(BudgetEntity budget, final RepositoryCallback<Long> callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                return budgetDao.insert(budget);
            }

            @Override
            protected void onPostExecute(Long result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void updateBudget(BudgetEntity budget, final RepositoryCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                budgetDao.update(budget);
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

    public void deleteBudget(BudgetEntity budget, final RepositoryCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                budgetDao.delete(budget);
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

    public LiveData<List<BudgetEntity>> getBudgetsByUser(long userId) {
        return budgetDao.findByUserId(userId);
    }

    public LiveData<List<BudgetEntity>> getActiveBudgets(long userId) {
        return budgetDao.findActiveBudgets(userId, System.currentTimeMillis());
    }

    public void checkBudgetExceeded(long budgetId, final RepositoryCallback<Boolean> callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                BudgetEntity budget = budgetDao.findByIdSync(budgetId);
                if (budget == null) {
                    return false;
                }
                return budget.getUsedAmount() > budget.getAmount();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void getBudgetHistory(long budgetId, final RepositoryCallback<List<BudgetHistoryEntity>> callback) {
        new AsyncTask<Void, Void, List<BudgetHistoryEntity>>() {
            @Override
            protected List<BudgetHistoryEntity> doInBackground(Void... voids) {
                return budgetHistoryDao.findByBudgetIdSync(budgetId);
            }

            @Override
            protected void onPostExecute(List<BudgetHistoryEntity> result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void insertBudgetHistory(BudgetHistoryEntity history, final RepositoryCallback<Long> callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                return budgetHistoryDao.insert(history);
            }

            @Override
            protected void onPostExecute(Long result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }
}
