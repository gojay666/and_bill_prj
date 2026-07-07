package com.bill_prj.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bill_prj.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> findById(long id);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User findByIdSync(long id);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    LiveData<User> findByUsername(String username);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsernameSync(String username);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    LiveData<User> findByPhone(String phone);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User findByPhoneSync(String phone);

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    User loginByPhone(String phone, String password);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    boolean existsByUsernameSync(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE phone = :phone)")
    boolean existsByPhoneSync(String phone);

    @Query("SELECT COUNT(*) FROM users WHERE phone = :phone")
    int countByPhone(String phone);

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    int countByUsername(String username);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User getUserByPhoneSync(String phone);

    @Query("UPDATE users SET password = :password WHERE phone = :phone")
    void updatePasswordByPhone(String phone, String password);

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId AND password = :oldPassword")
    int changePassword(long userId, String oldPassword, String newPassword);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();
}
