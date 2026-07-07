package com.bill_prj.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bill_prj.data.entity.BillEntity;

import java.util.List;

@Dao
public interface BillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BillEntity bill);

    @Update
    void update(BillEntity bill);

    @Delete
    void delete(BillEntity bill);

    @Query("SELECT * FROM bills WHERE userId = :userId")
    LiveData<List<BillEntity>> findByUserId(long userId);

    @Query("SELECT * FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<BillEntity>> findByDateRange(long userId, long startDate, long endDate);

    @Query("SELECT * FROM bills WHERE userId = :userId AND category = :category ORDER BY date DESC")
    LiveData<List<BillEntity>> findByCategory(long userId, String category);

    @Query("SELECT * FROM bills WHERE accountId = :accountId ORDER BY date DESC")
    LiveData<List<BillEntity>> findByAccountId(long accountId);

    @Query("SELECT * FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND type = :isIncome ORDER BY date DESC")
    LiveData<List<BillEntity>> findByDateRangeAndType(long userId, long startDate, long endDate, boolean isIncome);

    @Query("SELECT SUM(amount) FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    LiveData<Double> getTotalByDateRange(long userId, long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND type = :isIncome")
    LiveData<Double> getTotalByDateRangeAndType(long userId, long startDate, long endDate, boolean isIncome);

    @Query("SELECT category, SUM(amount) AS total FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND type = :isIncome GROUP BY category")
    LiveData<List<CategoryTotal>> getTotalByCategory(long userId, long startDate, long endDate, boolean isIncome);

    @Query("SELECT SUM(amount) FROM bills WHERE userId = :userId AND type = :isIncome AND date >= :startDate AND date <= :endDate GROUP BY category")
    LiveData<List<Double>> getCategoryTotals(long userId, long startDate, long endDate, boolean isIncome);

    @Query("SELECT strftime('%Y-%m', date / 1000, 'unixepoch') AS month, SUM(amount) AS total FROM bills WHERE userId = :userId AND type = :isIncome AND date >= :startYear AND date <= :endYear GROUP BY month ORDER BY month")
    LiveData<List<MonthlyStat>> getMonthlyStats(long userId, long startYear, long endYear, boolean isIncome);

    @Query("SELECT strftime('%Y', date / 1000, 'unixepoch') AS year, SUM(amount) AS total FROM bills WHERE userId = :userId AND type = :isIncome GROUP BY year ORDER BY year")
    LiveData<List<YearlyStat>> getYearlyStats(long userId, boolean isIncome);

    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<BillEntity>> getBillsByDateDesc(long userId);

    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY amount DESC")
    LiveData<List<BillEntity>> getBillsByAmountDesc(long userId);

    // Synchronous methods for repository use

    @Query("SELECT * FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    List<BillEntity> findByDateRangeSync(long userId, long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND type = :isIncome")
    Double getTotalByDateRangeAndTypeSync(long userId, long startDate, long endDate, boolean isIncome);

    @Query("SELECT category, SUM(amount) AS total FROM bills WHERE userId = :userId AND date >= :startDate AND date <= :endDate AND type = :isIncome GROUP BY category")
    List<CategoryTotal> getTotalByCategorySync(long userId, long startDate, long endDate, boolean isIncome);

    @Query("SELECT * FROM bills WHERE userId = :userId AND category = :category AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    List<BillEntity> findByCategoryAndDateRangeSync(long userId, String category, long startDate, long endDate);

    @Query("SELECT * FROM bills WHERE accountId = :accountId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    List<BillEntity> findByAccountIdAndDateRangeSync(long accountId, long startDate, long endDate);

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    BillEntity getBillByIdSync(long id);

    static class CategoryTotal {
        public String category;
        public double total;
    }

    static class MonthlyStat {
        public String month;
        public double total;
    }

    static class YearlyStat {
        public String year;
        public double total;
    }
}
