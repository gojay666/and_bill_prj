package com.bill_prj.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.AccountDao;
import com.bill_prj.data.entity.AccountEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AccountRepository {

    private final AccountDao accountDao;
    private final Executor executor;

    public AccountRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.accountDao = db.accountDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public long insert(AccountEntity account) {
        return accountDao.insert(account);
    }

    public void update(AccountEntity account) {
        executor.execute(() -> accountDao.update(account));
    }

    public void delete(AccountEntity account) {
        executor.execute(() -> accountDao.delete(account));
    }

    public LiveData<List<AccountEntity>> getAccountsByUser(long userId) {
        return accountDao.findByUserId(userId);
    }

    public List<AccountEntity> getAccountsByUserSync(long userId) {
        return accountDao.findByUserIdSync(userId);
    }
}
