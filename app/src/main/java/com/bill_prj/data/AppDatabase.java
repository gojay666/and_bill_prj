package com.bill_prj.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.bill_prj.data.dao.AccountDao;
import com.bill_prj.data.dao.BillDao;
import com.bill_prj.data.dao.BudgetDao;
import com.bill_prj.data.dao.BudgetHistoryDao;
import com.bill_prj.data.dao.CategoryDao;
import com.bill_prj.data.dao.UserDao;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.entity.BudgetEntity;
import com.bill_prj.data.entity.BudgetHistoryEntity;
import com.bill_prj.data.entity.CategoryEntity;
import com.bill_prj.data.entity.User;

import java.util.concurrent.Executors;

@Database(
        entities = {
                User.class,
                BillEntity.class,
                AccountEntity.class,
                BudgetEntity.class,
                BudgetHistoryEntity.class,
                CategoryEntity.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract BillDao billDao();

    public abstract AccountDao accountDao();

    public abstract BudgetDao budgetDao();

    public abstract BudgetHistoryDao budgetHistoryDao();

    public abstract CategoryDao categoryDao();

    public static AppDatabase createDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "bill_prj_database"
                    )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static AppDatabase getInstance(Context context) {
        return createDatabase(context);
    }

    /**
     * Ensure default BillType categories exist in the database.
     * Call this on first category load to handle both fresh install
     * and destructive migration after schema upgrade.
     */
    public static void ensureDefaultCategories(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                CategoryDao dao = getInstance(context).categoryDao();
                if (dao.countDefaults("income") == 0) {
                    for (CategoryEntity cat : BillType.getDefaultCategories()) {
                        dao.insert(cat);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
