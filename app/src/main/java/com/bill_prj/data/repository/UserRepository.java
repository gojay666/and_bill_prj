package com.bill_prj.data.repository;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.bill_prj.data.AppDatabase;
import com.bill_prj.data.dao.UserDao;
import com.bill_prj.data.entity.User;

import java.util.List;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.userDao = db.userDao();
    }

    public void login(String username, String password, final RepositoryCallback<User> callback) {
        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                User user = userDao.findByUsernameSync(username);
                if (user != null && user.getPassword().equals(password)) {
                    return user;
                }
                return null;
            }

            @Override
            protected void onPostExecute(User result) {
                if (callback != null) {
                    if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError(new Exception("用户名或密码错误"));
                    }
                }
            }
        }.execute();
    }

    public void loginWithPhone(String phone, String password, final RepositoryCallback<User> callback) {
        new AsyncTask<Void, Void, User>() {
            @Override
            protected User doInBackground(Void... voids) {
                User user = userDao.findByPhoneSync(phone);
                if (user != null && user.getPassword().equals(password)) {
                    return user;
                }
                return null;
            }

            @Override
            protected void onPostExecute(User result) {
                if (callback != null) {
                    if (result != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError(new Exception("手机号或密码错误"));
                    }
                }
            }
        }.execute();
    }

    public void register(User user, final RepositoryCallback<Long> callback) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                // Check if username already exists
                boolean usernameExists = userDao.existsByUsernameSync(user.getUsername());
                if (usernameExists) {
                    return -1L;
                }
                // Check if phone already exists
                boolean phoneExists = userDao.existsByPhoneSync(user.getPhone());
                if (phoneExists) {
                    return -2L;
                }
                return userDao.insert(user);
            }

            @Override
            protected void onPostExecute(Long result) {
                if (callback != null) {
                    if (result > 0) {
                        callback.onSuccess(result);
                    } else if (result == -1L) {
                        callback.onError(new Exception("用户名已存在"));
                    } else if (result == -2L) {
                        callback.onError(new Exception("手机号已存在"));
                    } else {
                        callback.onError(new Exception("注册失败"));
                    }
                }
            }
        }.execute();
    }

    public void checkPhoneExists(String phone, final RepositoryCallback<Boolean> callback) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return userDao.existsByPhoneSync(phone);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }

    public void updatePassword(long userId, String oldPwd, String newPwd,
                                final RepositoryCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                User user = userDao.findByIdSync(userId);
                if (user != null && user.getPassword().equals(oldPwd)) {
                    user.setPassword(newPwd);
                    userDao.update(user);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (callback != null) {
                    User user = userDao.findByIdSync(userId);
                    if (user != null && user.getPassword().equals(newPwd)) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError(new Exception("原密码错误"));
                    }
                }
            }
        }.execute();
    }

    public void resetPassword(String phone, String newPwd, final RepositoryCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                User user = userDao.findByPhoneSync(phone);
                if (user != null) {
                    user.setPassword(newPwd);
                    userDao.update(user);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (callback != null) {
                    User user = userDao.findByPhoneSync(phone);
                    if (user != null && user.getPassword().equals(newPwd)) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError(new Exception("手机号不存在"));
                    }
                }
            }
        }.execute();
    }

    public LiveData<User> getUserById(long userId) {
        return userDao.findById(userId);
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    /** Update user fields (username, avatar, etc.) in the database. */
    public void updateUser(final User user, final RepositoryCallback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                userDao.update(user);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        }.execute();
    }
}
