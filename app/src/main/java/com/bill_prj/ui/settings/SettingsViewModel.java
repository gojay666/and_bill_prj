package com.bill_prj.ui.settings;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.entity.AccountEntity;
import com.bill_prj.data.entity.BillEntity;
import com.bill_prj.data.entity.BudgetEntity;
import com.bill_prj.data.entity.User;
import com.bill_prj.data.repository.AccountRepository;
import com.bill_prj.data.repository.BillRepository;
import com.bill_prj.data.repository.BudgetRepository;
import com.bill_prj.data.repository.RepositoryCallback;
import com.bill_prj.data.repository.UserRepository;
import com.bill_prj.utils.SharedPrefsManager;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final SharedPrefsManager prefsManager;
    private final Executor executor;
    private final Gson gson;
    private final AppDatabase db;

    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    public SettingsViewModel(Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        userRepository = new UserRepository(application);
        accountRepository = new AccountRepository(application);
        prefsManager = new SharedPrefsManager(application);
        executor = Executors.newSingleThreadExecutor();
        gson = new Gson();
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<User> getUserById(long userId) {
        return userRepository.getUserById(userId);
    }

    public void changePassword(long userId, String oldPassword, String newPassword) {
        userRepository.updatePassword(userId, oldPassword, newPassword, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                messageLiveData.postValue("密码修改成功");
                operationSuccess.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                messageLiveData.postValue("密码修改失败: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }

    public void exportData(long userId) {
        executor.execute(() -> {
            try {
                ExportData data = new ExportData();

                // Get user bills
                List<BillEntity> bills = db.billDao().findByDateRangeSync(userId, 0, Long.MAX_VALUE);
                data.bills = bills;

                // Get accounts
                List<AccountEntity> accounts = db.accountDao().findByUserIdSync(userId);
                data.accounts = accounts;

                // Get budgets
                List<BudgetEntity> budgets = db.budgetDao().findByUserIdAndTypeSync(userId, "MONTHLY");
                data.budgets = budgets;

                String json = gson.toJson(data);

                Context context = getApplication();
                File exportDir = new File(context.getExternalFilesDir(null), "exports");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                String fileName = "bill_data_backup_" + System.currentTimeMillis() + ".json";
                File exportFile = new File(exportDir, fileName);

                FileOutputStream fos = new FileOutputStream(exportFile);
                fos.write(json.getBytes("UTF-8"));
                fos.close();

                messageLiveData.postValue(exportFile.getAbsolutePath());
                operationSuccess.postValue(true);
            } catch (Exception e) {
                messageLiveData.postValue("导出失败: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }

    public void importData(Uri fileUri) {
        executor.execute(() -> {
            try {
                Context context = getApplication();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(context.getContentResolver().openInputStream(fileUri), "UTF-8"));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                String json = jsonBuilder.toString();
                ExportData data = gson.fromJson(json, ExportData.class);

                if (data == null) {
                    messageLiveData.postValue("导入失败：数据格式不正确");
                    operationSuccess.postValue(false);
                    return;
                }

                long currentUserId = prefsManager.getUserId();

                if (data.accounts != null) {
                    for (AccountEntity account : data.accounts) {
                        account.setUserId(currentUserId);
                        account.setId(0);
                        db.accountDao().insert(account);
                    }
                }

                if (data.bills != null) {
                    for (BillEntity bill : data.bills) {
                        bill.setUserId(currentUserId);
                        bill.setId(0);
                        db.billDao().insert(bill);
                    }
                }

                if (data.budgets != null) {
                    for (BudgetEntity budget : data.budgets) {
                        budget.setUserId(currentUserId);
                        budget.setId(0);
                        db.budgetDao().insert(budget);
                    }
                }

                messageLiveData.postValue("数据导入成功");
                operationSuccess.postValue(true);
            } catch (Exception e) {
                messageLiveData.postValue("导入失败: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }

    public void clearAllData(long userId) {
        executor.execute(() -> {
            try {
                db.billDao().findByDateRangeSync(userId, 0, Long.MAX_VALUE).forEach(
                        bill -> db.billDao().delete(bill));
                db.accountDao().findByUserIdSync(userId).forEach(
                        account -> db.accountDao().delete(account));
                db.budgetDao().findByUserIdAndTypeSync(userId, "MONTHLY").forEach(
                        budget -> db.budgetDao().delete(budget));

                messageLiveData.postValue("所有数据已清除");
                operationSuccess.postValue(true);
            } catch (Exception e) {
                messageLiveData.postValue("清除失败: " + e.getMessage());
                operationSuccess.postValue(false);
            }
        });
    }

    public void logout() {
        prefsManager.clearSession();
    }

    /** Update the user's avatar selection and persist to database. */
    public void updateUserAvatar(long userId, String avatarIndex) {
        executor.execute(() -> {
            try {
                User user = db.userDao().findByIdSync(userId);
                if (user != null) {
                    user.setAvatar(avatarIndex);
                    user.setUpdatedAt(System.currentTimeMillis());
                    db.userDao().update(user);
                    messageLiveData.postValue("avatar_updated");
                }
            } catch (Exception e) {
                messageLiveData.postValue("头像更新失败: " + e.getMessage());
            }
        });
    }

    /** Update the user's display name and persist to database. */
    public void updateUsername(long userId, String newUsername) {
        executor.execute(() -> {
            try {
                User user = db.userDao().findByIdSync(userId);
                if (user != null) {
                    user.setUsername(newUsername);
                    user.setUpdatedAt(System.currentTimeMillis());
                    db.userDao().update(user);
                    messageLiveData.postValue("username_updated");
                }
            } catch (Exception e) {
                messageLiveData.postValue("用户名更新失败: " + e.getMessage());
            }
        });
    }

    static class ExportData {
        List<BillEntity> bills;
        List<AccountEntity> accounts;
        List<BudgetEntity> budgets;
    }
}
