package com.bill_prj.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bill_prj.data.entity.BudgetEntity;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);

    @Delete
    void delete(BudgetEntity budget);

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    LiveData<List<BudgetEntity>> findByUserId(long userId);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND type = :type")
    LiveData<List<BudgetEntity>> findByType(long userId, String type);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND startDate >= :startDate AND endDate <= :endDate")
    LiveData<List<BudgetEntity>> findByDateRange(long userId, long startDate, long endDate);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND endDate > :currentTime")
    LiveData<List<BudgetEntity>> findActiveBudgets(long userId, long currentTime);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND type = :type")
    List<BudgetEntity> findByUserIdAndTypeSync(long userId, String type);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND endDate > :currentTime")
    List<BudgetEntity> findActiveBudgetsSync(long userId, long currentTime);

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    BudgetEntity findByIdSync(long id);
}
