package com.bill_prj.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bill_prj.R;
import com.bill_prj.data.preferences.AppPreferences;
import com.bill_prj.ui.auth.LoginActivity;
import com.bill_prj.ui.bill.MainActivity;
import com.bill_prj.ui.budget.BudgetActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class SettingsFragment extends Fragment {

    // Avatar drawable resources
    private static final int[] AVATAR_DRAWABLES = {
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6
    };

    private AppPreferences appPreferences;
    private TextView tvUsername;
    private ImageView ivAvatar;
    private RadioGroup rgTheme;
    private SwitchCompat switchAutoLogin;
    private MaterialButton btnLogout;
    private MaterialCardView cardBudget;
    private MaterialCardView cardMoreSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        appPreferences = new AppPreferences(requireContext());

        tvUsername = root.findViewById(R.id.tv_username);
        ivAvatar = root.findViewById(R.id.iv_avatar);
        rgTheme = root.findViewById(R.id.rg_theme);
        switchAutoLogin = root.findViewById(R.id.switch_auto_login);
        btnLogout = root.findViewById(R.id.btn_logout);
        cardBudget = root.findViewById(R.id.card_budget);
        cardMoreSettings = root.findViewById(R.id.card_more_settings);

        // Load user info
        String username = appPreferences.getUsername();
        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
        }
        // Load avatar
        applyAvatar(appPreferences.getAvatar());

        // Setup card clicks
        cardBudget.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BudgetActivity.class);
            startActivity(intent);
        });

        cardMoreSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.bill_prj.ui.settings.SettingsActivity.class);
            startActivity(intent);
        });

        // Restore saved theme state first (sets radio before listener — no flag needed)
        applyCurrentThemeSelection();

        // Listen for user-initiated theme changes
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_light) {
                appPreferences.saveThemeModeString("light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.rb_dark) {
                appPreferences.saveThemeModeString("dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                appPreferences.saveThemeModeString("system");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            // Notify MainActivity to check theme on return from SettingsActivity
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).sThemeChanged = true;
            }
            // Restart hosting activity with zero transition to avoid flicker
            if (getActivity() != null) {
                restartHostActivity();
            }
        });

        // Load auto login setting
        switchAutoLogin.setChecked(appPreferences.isAutoLoginEnabled());
        switchAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appPreferences.saveAutoLogin(isChecked);
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("退出登录")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("退出", (dialog, which) -> {
                        appPreferences.clearAll();
                        Toast.makeText(getContext(), R.string.logout_confirm, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finishAffinity();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        return root;
    }

    private void applyCurrentThemeSelection() {
        appPreferences = new AppPreferences(requireContext());
        String themeMode = appPreferences.getThemeModeString();
        if ("light".equals(themeMode)) {
            rgTheme.check(R.id.rb_light);
        } else if ("dark".equals(themeMode)) {
            rgTheme.check(R.id.rb_dark);
        } else {
            rgTheme.check(R.id.rb_system);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user info from SharedPreferences (updated by SettingsActivity)
        appPreferences = new AppPreferences(requireContext());
        String username = appPreferences.getUsername();
        if (username != null && !username.isEmpty()) {
            tvUsername.setText(username);
        }
        applyAvatar(appPreferences.getAvatar());
        // Refresh theme radio state when returning from SettingsActivity
        applyCurrentThemeSelection();
    }

    /** Apply the avatar drawable based on the stored index. */
    private void applyAvatar(String avatarIndex) {
        int index = 0;
        if (avatarIndex != null && !avatarIndex.isEmpty()) {
            try {
                index = Integer.parseInt(avatarIndex) - 1;
                if (index < 0 || index >= AVATAR_DRAWABLES.length) {
                    index = 0;
                }
            } catch (NumberFormatException e) {
                index = 0;
            }
        }
        ivAvatar.setImageResource(AVATAR_DRAWABLES[index]);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAvatar.setPadding(0, 0, 0, 0);
    }

    /** Restart the hosting activity with a smooth crossfade transition to avoid flicker. */
    private void restartHostActivity() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;
        Intent intent = activity.getIntent();
        // Start the new activity FIRST (it will pick up the new theme via setDefaultNightMode)
        // then crossfade: old activity fades out while new activity fades in
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.finish();
    }
}
