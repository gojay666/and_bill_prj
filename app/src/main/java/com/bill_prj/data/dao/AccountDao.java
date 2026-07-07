package com.bill_prj.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bill_prj.data.entity.AccountEntity;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AccountEntity account);

    @Update
    void update(AccountEntity account);

    @Delete
    void delete(AccountEntity account);

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    LiveData<List<AccountEntity>> findByUserId(long userId);

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    LiveData<AccountEntity> findById(long id);

    @Query("SELECT SUM(balance) FROM accounts WHERE userId = :userId")
    LiveData<Double> getTotalBalance(long userId);

    // Synchronous methods

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    List<AccountEntity> findByUserIdSync(long userId);

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    AccountEntity findByIdSync(long id);

    @Query("SELECT SUM(balance) FROM accounts WHERE userId = :userId")
    Double getTotalBalanceSync(long userId);

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    void addBalanceSync(long accountId, double amount);
}
