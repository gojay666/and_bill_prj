package com.bill_prj.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.bill_prj.data.entity.BudgetHistoryEntity;

import java.util.List;

@Dao
public interface BudgetHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BudgetHistoryEntity history);

    @Query("SELECT * FROM budget_history WHERE budgetId = :budgetId ORDER BY changeTime DESC")
    LiveData<List<BudgetHistoryEntity>> findByBudgetId(long budgetId);

    @Query("SELECT * FROM budget_history WHERE budgetId = :budgetId ORDER BY changeTime DESC")
    List<BudgetHistoryEntity> findByBudgetIdSync(long budgetId);
}
