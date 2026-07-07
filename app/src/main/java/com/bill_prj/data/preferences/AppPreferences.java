package com.bill_prj.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import androidx.appcompat.app.AppCompatDelegate;
import com.bill_prj.utils.Constants;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AppPreferences {

    private static final String PREFS_NAME = Constants.PREF_NAME;
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REMEMBER_PASSWORD = "remember_password";
    private static final String KEY_ENCRYPTED_PASSWORD = "encrypted_password";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_AUTO_LOGIN = "auto_login";
    private static final String KEY_AVATAR = "avatar";

    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";

    private final SharedPreferences prefs;
    private final SharedPreferences encryptedPrefs;

    public AppPreferences(Context context) {
        // Regular preferences for non-sensitive data
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // EncryptedSharedPreferences for sensitive data (password)
        SharedPreferences encrypted = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encrypted = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME + "_secure",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            encrypted = context.getSharedPreferences(PREFS_NAME + "_secure", Context.MODE_PRIVATE);
        }
        this.encryptedPrefs = encrypted;
    }

    // ===== Login State =====

    public void saveLoginState(long userId, String username) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .commit();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    // ===== Remember Password =====

    public void saveRememberPassword(boolean remember, String password) {
        prefs.edit().putBoolean(KEY_REMEMBER_PASSWORD, remember).apply();
        if (remember && password != null) {
            encryptedPrefs.edit().putString(KEY_ENCRYPTED_PASSWORD, password).apply();
        } else {
            encryptedPrefs.edit().remove(KEY_ENCRYPTED_PASSWORD).apply();
        }
    }

    public boolean isRememberPasswordEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_PASSWORD, false);
    }

    public String getSavedPassword() {
        return encryptedPrefs.getString(KEY_ENCRYPTED_PASSWORD, "");
    }

    // ===== Theme Mode =====

    public void saveThemeMode(boolean isDarkMode) {
        prefs.edit()
                .putString(KEY_THEME_MODE, isDarkMode ? THEME_DARK : THEME_LIGHT)
                .apply();
    }

    public void saveThemeModeString(String mode) {
        prefs.edit()
                .putString(KEY_THEME_MODE, mode)
                .apply();
    }

    public String getThemeModeString() {
        return prefs.getString(KEY_THEME_MODE, Constants.THEME_MODE_SYSTEM);
    }

    public boolean isDarkMode() {
        String mode = prefs.getString(KEY_THEME_MODE, Constants.THEME_MODE_SYSTEM);
        if (THEME_DARK.equals(mode)) return true;
        if (Constants.THEME_MODE_SYSTEM.equals(mode)) {
            int nightMode = getSystemNightMode();
            return nightMode == AppCompatDelegate.MODE_NIGHT_YES;
        }
        return false;
    }

    private int getSystemNightMode() {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
    }

    // ===== Auto Login =====

    public void saveAutoLogin(boolean autoLogin) {
        prefs.edit().putBoolean(KEY_AUTO_LOGIN, autoLogin).commit();
    }

    public boolean isAutoLoginEnabled() {
        return prefs.getBoolean(KEY_AUTO_LOGIN, false);
    }

    // ===== Avatar =====

    public void saveAvatar(String avatarIndex) {
        prefs.edit().putString(KEY_AVATAR, avatarIndex).apply();
    }

    public String getAvatar() {
        return prefs.getString(KEY_AVATAR, "1");
    }

    // ===== Username (update after login) =====

    public void saveUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    // ===== Clear All (Logout) =====

    public void clearAll() {
        prefs.edit().clear().apply();
        encryptedPrefs.edit().clear().apply();
    }
}
