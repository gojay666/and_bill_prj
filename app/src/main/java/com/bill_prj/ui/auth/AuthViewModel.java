package com.bill_prj.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.UserDao;
import com.bill_prj.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {

    private final UserDao userDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Result<User>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Result<User>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Result<Void>> resetPasswordResult = new MutableLiveData<>();
    private final MutableLiveData<Result<Boolean>> phoneCheckResult = new MutableLiveData<>();
    private final MutableLiveData<Result<Boolean>> usernameCheckResult = new MutableLiveData<>();
    private final MutableLiveData<Result<String>> verificationCodeResult = new MutableLiveData<>();
    private final MutableLiveData<Result<Boolean>> sendCodeResult = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        this.userDao = database.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Result<User>> getLoginResult() {
        return loginResult;
    }

    public LiveData<Result<User>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<Result<Void>> getResetPasswordResult() {
        return resetPasswordResult;
    }

    public LiveData<Result<Boolean>> getPhoneCheckResult() {
        return phoneCheckResult;
    }

    public LiveData<Result<Boolean>> getUsernameCheckResult() {
        return usernameCheckResult;
    }

    public LiveData<Result<String>> getVerificationCodeResult() {
        return verificationCodeResult;
    }

    public LiveData<Result<Boolean>> getSendCodeResult() {
        return sendCodeResult;
    }

    public void login(String username, String password) {
        executorService.execute(() -> {
            loginResult.postValue(Result.loading());
            try {
                User user;
                // If input looks like a phone number, try phone login first
                if (username != null && username.matches("1\\d{10}")) {
                    user = userDao.loginByPhone(username, password);
                } else {
                    user = userDao.login(username, password);
                }
                // Fallback: try username login if phone login failed
                if (user == null && username != null && username.matches("1\\d{10}")) {
                    user = userDao.login(username, password);
                }
                // Fallback: try phone login if username login failed
                if (user == null && username != null && !username.matches("1\\d{10}")) {
                    user = userDao.loginByPhone(username, password);
                }
                if (user != null) {
                    loginResult.postValue(Result.success("登录成功", user));
                } else {
                    loginResult.postValue(Result.error("用户名或密码错误"));
                }
            } catch (Exception e) {
                loginResult.postValue(Result.error("登录失败: " + e.getMessage()));
            }
        });
    }

    public void loginWithPhone(String phone, String code) {
        executorService.execute(() -> {
            loginResult.postValue(Result.loading());
            try {
                // Verify code first
                boolean codeValid = verifyCodeInternal(phone, code);
                if (!codeValid) {
                    loginResult.postValue(Result.error("验证码错误或已过期"));
                    return;
                }
                User user = userDao.getUserByPhoneSync(phone);
                if (user != null) {
                    loginResult.postValue(Result.success("登录成功", user));
                } else {
                    loginResult.postValue(Result.error("该手机号未注册"));
                }
            } catch (Exception e) {
                loginResult.postValue(Result.error("登录失败: " + e.getMessage()));
            }
        });
    }

    public void register(User user) {
        executorService.execute(() -> {
            registerResult.postValue(Result.loading());
            try {
                // Check if username already exists
                if (userDao.countByUsername(user.getUsername()) > 0) {
                    registerResult.postValue(Result.error("用户名已被使用"));
                    return;
                }
                // Check if phone already exists
                if (userDao.countByPhone(user.getPhone()) > 0) {
                    registerResult.postValue(Result.error("手机号已被注册"));
                    return;
                }
                long id = userDao.insert(user);
                if (id > 0) {
                    user.setId(id);
                    registerResult.postValue(Result.success("注册成功", user));
                } else {
                    registerResult.postValue(Result.error("注册失败，请重试"));
                }
            } catch (Exception e) {
                registerResult.postValue(Result.error("注册失败: " + e.getMessage()));
            }
        });
    }

    public void resetPassword(String phone, String newPassword) {
        executorService.execute(() -> {
            resetPasswordResult.postValue(Result.loading());
            try {
                userDao.updatePasswordByPhone(phone, newPassword);
                resetPasswordResult.postValue(Result.success("密码重置成功", null));
            } catch (Exception e) {
                resetPasswordResult.postValue(Result.error("密码重置失败: " + e.getMessage()));
            }
        });
    }

    public void checkPhoneExists(String phone) {
        executorService.execute(() -> {
            phoneCheckResult.postValue(Result.loading());
            try {
                boolean exists = userDao.countByPhone(phone) > 0;
                phoneCheckResult.postValue(Result.success(exists));
            } catch (Exception e) {
                phoneCheckResult.postValue(Result.error("检查失败: " + e.getMessage()));
            }
        });
    }

    public void checkUsernameExists(String username) {
        executorService.execute(() -> {
            usernameCheckResult.postValue(Result.loading());
            try {
                boolean exists = userDao.countByUsername(username) > 0;
                usernameCheckResult.postValue(Result.success(exists));
            } catch (Exception e) {
                usernameCheckResult.postValue(Result.error("检查失败: " + e.getMessage()));
            }
        });
    }

    public void sendVerificationCode(String phone) {
        executorService.execute(() -> {
            sendCodeResult.postValue(Result.loading());
            try {
                // Generate a 6-digit code and store it in-memory
                String code = String.format("%06d", new java.util.Random().nextInt(999999));
                codeStore.put(phone, code);
                codeTimestamps.put(phone, System.currentTimeMillis());
                sendCodeResult.postValue(Result.success("验证码已发送", true));
                // Post the code for development/debug purposes
                verificationCodeResult.postValue(Result.success(code));
            } catch (Exception e) {
                sendCodeResult.postValue(Result.error("验证码发送失败: " + e.getMessage()));
            }
        });
    }

    // In-memory verification code storage
    private final java.util.Map<String, String> codeStore = new java.util.HashMap<>();
    private final java.util.Map<String, Long> codeTimestamps = new java.util.HashMap<>();
    private static final long CODE_VALID_DURATION = 5 * 60 * 1000L; // 5 minutes

    private boolean verifyCodeInternal(String phone, String code) {
        String storedCode = codeStore.get(phone);
        Long timestamp = codeTimestamps.get(phone);
        if (storedCode == null || timestamp == null) {
            return false;
        }
        if (System.currentTimeMillis() - timestamp > CODE_VALID_DURATION) {
            codeStore.remove(phone);
            codeTimestamps.remove(phone);
            return false;
        }
        boolean valid = storedCode.equals(code);
        if (valid) {
            codeStore.remove(phone);
            codeTimestamps.remove(phone);
        }
        return valid;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
