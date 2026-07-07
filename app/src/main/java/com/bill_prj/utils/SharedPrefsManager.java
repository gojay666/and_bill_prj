package com.bill_prj.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.bill_prj.utils.Constants;

public class SharedPrefsManager {

    private static final String PREF_NAME = Constants.PREF_NAME;
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_BUDGET_NOTIFICATION = "budget_notification";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    private static final String KEY_REMEMBER_PASSWORD = "remember_password";
    private static final String KEY_SAVED_USERNAME = "saved_username";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    private final SharedPreferences prefs;

    public SharedPrefsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setUserId(long userId) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public void clearUserId() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply();
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, "system");
    }

    public void setBudgetNotificationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BUDGET_NOTIFICATION, enabled).apply();
    }

    public boolean isBudgetNotificationEnabled() {
        return prefs.getBoolean(KEY_BUDGET_NOTIFICATION, true);
    }

    public boolean isAutoLogin() {
        return prefs.getBoolean(KEY_AUTO_LOGIN, false);
    }

    public void setAutoLogin(boolean autoLogin) {
        prefs.edit().putBoolean(KEY_AUTO_LOGIN, autoLogin).apply();
    }

    public boolean isRememberPassword() {
        return prefs.getBoolean(KEY_REMEMBER_PASSWORD, false);
    }

    public void setRememberPassword(boolean rememberPassword) {
        prefs.edit().putBoolean(KEY_REMEMBER_PASSWORD, rememberPassword).apply();
    }

    public void setSavedUsername(String username) {
        prefs.edit().putString(KEY_SAVED_USERNAME, username).apply();
    }

    public String getSavedUsername() {
        return prefs.getString(KEY_SAVED_USERNAME, "");
    }

    public void setSavedPassword(String password) {
        prefs.edit().putString(KEY_SAVED_PASSWORD, password).apply();
    }

    public String getSavedPassword() {
        return prefs.getString(KEY_SAVED_PASSWORD, "");
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_AUTO_LOGIN)
                .remove(KEY_REMEMBER_PASSWORD)
                .remove(KEY_SAVED_USERNAME)
                .remove(KEY_SAVED_PASSWORD)
                .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
