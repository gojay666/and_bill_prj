package com.bill_prj.ui.bill;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bill_prj.R;
import com.bill_prj.ui.account.AccountManageActivity;
import com.bill_prj.utils.Constants;
import com.bill_prj.utils.SharedPrefsManager;
import com.bill_prj.data.preferences.AppPreferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    /** Flag set by theme setting screens to signal the main activity needs to recreate */
    public static boolean sThemeChanged = false;

    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar toolbar;
    private FragmentManager fragmentManager;

    // Fragment references
    private HomeFragment homeFragment;
    private Fragment billsFragment;
    private Fragment statisticsFragment;
    private Fragment settingsFragment;

    // Active fragment tag
    private String activeFragmentTag = Constants.TAG_HOME;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            applySavedTheme();
            super.onCreate(savedInstanceState);

            // Check login state
            preferences = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            if (!isLoggedIn()) {
                redirectToLogin();
                return;
            }

            setContentView(R.layout.activity_main);

            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            fragmentManager = getSupportFragmentManager();

            setupBottomNavigation();
            setupFragments();
        } catch (Throwable t) {
            android.util.Log.e("MAIN_CRASH", "MainActivity onCreate failed", t);
            String errMsg = "MainActivity.onCreate: " + t.getClass().getSimpleName() + ": " + t.getMessage();
            redirectToLoginWithError(errMsg);
        }
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
        } catch (Throwable t) {
            android.util.Log.e("MAIN_CRASH", "MainActivity onStart failed", t);
            String errMsg = "MainActivity.onStart: " + t.getClass().getSimpleName() + ": " + t.getMessage();
            redirectToLoginWithError(errMsg);
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            // If theme was changed in SettingsActivity, recreate silently to apply
            if (sThemeChanged) {
                sThemeChanged = false;
                restartSilently();
            }
        } catch (Throwable t) {
            android.util.Log.e("MAIN_CRASH", "MainActivity onResume failed", t);
            String errMsg = "MainActivity.onResume: " + t.getClass().getSimpleName() + ": " + t.getMessage();
            redirectToLoginWithError(errMsg);
        }
    }

    private void applySavedTheme() {
        SharedPrefsManager prefsManager = new SharedPrefsManager(this);
        String themeMode = prefsManager.getThemeMode();
        switch (themeMode) {
            case Constants.THEME_MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constants.THEME_MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private boolean isLoggedIn() {
        return preferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    private void redirectToLogin() {
        try {
            Class<?> loginActivity = Class.forName("com.bill_prj.ui.auth.LoginActivity");
            Intent intent = new Intent(this, loginActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, getString(R.string.login_required), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /** Restart this activity with a smooth crossfade transition to avoid flicker. */
    private void restartSilently() {
        Intent intent = getIntent();
        // Start new activity first (it picks up the theme set via setDefaultNightMode),
        // then crossfade: this activity fades out while the new one fades in — no blank frames.
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void redirectToLoginWithError(String errorMsg) {
        try {
            SharedPreferences.Editor editor = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit();
            editor.putBoolean(Constants.KEY_IS_LOGGED_IN, false);
            editor.putBoolean("auto_login", false);
            editor.commit();
        } catch (Exception ignored) {}
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName(this, "com.bill_prj.ui.auth.LoginActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("main_error", errorMsg);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            finish();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.nav_home) {
                            switchFragment(Constants.TAG_HOME);
                            updateToolbarTitle(getString(R.string.nav_home));
                            return true;
                        } else if (itemId == R.id.nav_bills) {
                            switchFragment(Constants.TAG_BILLS);
                            updateToolbarTitle(getString(R.string.nav_bills));
                            return true;
                        } else if (itemId == R.id.nav_statistics) {
                            switchFragment(Constants.TAG_STATISTICS);
                            updateToolbarTitle(getString(R.string.nav_statistics));
                            return true;
                        } else if (itemId == R.id.nav_settings) {
                            switchFragment(Constants.TAG_SETTINGS);
                            updateToolbarTitle(getString(R.string.nav_settings));
                            return true;
                        }
                        return false;
                    }
                });
    }

    private void setupFragments() {
        // Check if fragments already exist (e.g. after Activity recreation)
        Fragment existingHome = fragmentManager.findFragmentByTag(Constants.TAG_HOME);
        if (existingHome != null) {
            // Fragments already restored by FragmentManager — no need to recreate
            homeFragment = (HomeFragment) existingHome;
            return;
        }

        homeFragment = new HomeFragment();

        try {
            Class<?> billsFragClass = Class.forName("com.bill_prj.ui.bill.BillsFragment");
            billsFragment = (Fragment) billsFragClass.newInstance();
        } catch (Exception e) {
            billsFragment = null;
        }

        try {
            Class<?> statsFragClass = Class.forName("com.bill_prj.ui.statistics.StatisticsFragment");
            statisticsFragment = (Fragment) statsFragClass.newInstance();
        } catch (Exception e) {
            statisticsFragment = null;
        }

        try {
            Class<?> settingsFragClass = Class.forName("com.bill_prj.ui.settings.SettingsFragment");
            settingsFragment = (Fragment) settingsFragClass.newInstance();
        } catch (Exception e) {
            settingsFragment = null;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragmentContainerView, homeFragment, Constants.TAG_HOME);

        if (billsFragment != null) {
            transaction.add(R.id.fragmentContainerView, billsFragment, Constants.TAG_BILLS);
            transaction.hide(billsFragment);
        }
        if (statisticsFragment != null) {
            transaction.add(R.id.fragmentContainerView, statisticsFragment, Constants.TAG_STATISTICS);
            transaction.hide(statisticsFragment);
        }
        if (settingsFragment != null) {
            transaction.add(R.id.fragmentContainerView, settingsFragment, Constants.TAG_SETTINGS);
            transaction.hide(settingsFragment);
        }

        transaction.commit();
    }

    private void switchFragment(String tag) {
        if (tag.equals(activeFragmentTag)) return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment targetFragment = fragmentManager.findFragmentByTag(tag);

        Fragment currentFragment = fragmentManager.findFragmentByTag(activeFragmentTag);
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        if (targetFragment != null) {
            transaction.show(targetFragment);
        }

        transaction.commit();
        activeFragmentTag = tag;
    }

    private void updateToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    public void setToolbarTitle(String title) {
        updateToolbarTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Fragment activeFragment = fragmentManager.findFragmentByTag(activeFragmentTag);
            if (activeFragment != null) {
                activeFragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Apply theme change at runtime and persist the setting.
     */
    public void switchTheme(String themeMode) {
        SharedPrefsManager prefsManager = new SharedPrefsManager(this);
        prefsManager.setThemeMode(themeMode);

        switch (themeMode) {
            case Constants.THEME_MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case Constants.THEME_MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
