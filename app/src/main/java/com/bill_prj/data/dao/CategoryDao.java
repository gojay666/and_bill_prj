package com.bill_prj.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bill_prj.data.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories WHERE (userId = 0 OR userId = :userId) AND type = :type ORDER BY isDefault DESC, id ASC")
    LiveData<List<CategoryEntity>> getCategories(long userId, String type);

    @Query("SELECT * FROM categories WHERE (userId = 0 OR userId = :userId) AND type = :type ORDER BY isDefault DESC, id ASC")
    List<CategoryEntity> getCategoriesSync(long userId, String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM categories WHERE name = :name AND (userId = 0 OR userId = :userId) LIMIT 1")
    CategoryEntity findByName(long userId, String name);

    @Query("SELECT COUNT(*) FROM categories WHERE userId = 0 AND type = :type")
    int countDefaults(String type);
}
