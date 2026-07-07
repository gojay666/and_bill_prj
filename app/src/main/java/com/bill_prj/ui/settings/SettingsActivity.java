package com.bill_prj.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bill_prj.R;
import com.bill_prj.data.entity.User;
import com.bill_prj.data.preferences.AppPreferences;
import com.bill_prj.ui.bill.MainActivity;
import com.bill_prj.utils.SharedPrefsManager;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    // Avatar drawable resources — replace avatar_1.xml ~ avatar_6.xml with PNG files later
    private static final int[] AVATAR_DRAWABLES = {
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6
    };

    private SettingsViewModel viewModel;
    private SharedPrefsManager prefsManager;
    private AppPreferences appPreferences;

    private ImageView ivAvatar;
    private TextView tvUsername, tvPhone;
    private RadioGroup rgTheme;
    private SwitchCompat switchAutoLogin;
    private View layoutExport, layoutImport, layoutClear, layoutManageAccounts, layoutManageCategories,
            layoutChangePassword;
    private Button btnLogout;
    private View layoutAvatarClick, layoutUsernameClick;

    private ActivityResultLauncher<String> filePickerLauncher;

    // Cached current user info for avatar display
    private User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_settings);

            prefsManager = new SharedPrefsManager(this);
            appPreferences = new AppPreferences(this);
            long userId = prefsManager.getUserId();

            viewModel = new ViewModelProvider(this, new SettingsViewModelFactory(this)).get(SettingsViewModel.class);

            initViews();
            setupToolbar();
            setupProfile(userId);
            setupThemeToggle();
            setupLoginSettings();
            setupDataManagement();
            setupAccountSettings();
            setupSecurity();
            setupAbout();
            setupLogout();
            setupFilePicker();
            observeMessages();
        } catch (Exception e) {
            // Show error message
            TextView tv = new TextView(this);
            tv.setText("页面加载失败: " + e.getMessage());
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(32, 32, 32, 32);
            setContentView(tv);
            e.printStackTrace();
        }
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvPhone = findViewById(R.id.tv_phone);
        rgTheme = findViewById(R.id.rg_theme);
        switchAutoLogin = findViewById(R.id.switch_auto_login);
        layoutExport = findViewById(R.id.layout_export);
        layoutImport = findViewById(R.id.layout_import);
        layoutClear = findViewById(R.id.layout_clear);
        layoutManageAccounts = findViewById(R.id.layout_manage_accounts);
        layoutManageCategories = findViewById(R.id.layout_manage_categories);
        layoutChangePassword = findViewById(R.id.layout_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        layoutAvatarClick = findViewById(R.id.layout_avatar_click);
        layoutUsernameClick = findViewById(R.id.layout_username_click);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("更多设置");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupProfile(long userId) {
        // Avatar click → avatar picker dialog
        layoutAvatarClick.setOnClickListener(v -> showAvatarPickerDialog(userId));

        // Username click → edit dialog
        layoutUsernameClick.setOnClickListener(v -> showUsernameEditDialog(userId));

        if (userId == -1) {
            tvUsername.setText("未登录");
            tvPhone.setText("");
            return;
        }
        viewModel.getUserById(userId).observe(this, user -> {
            currentUser = user;
            if (user != null) {
                tvUsername.setText(user.getUsername() != null ? user.getUsername() : "未设置");
                tvPhone.setText(user.getPhone() != null ? user.getPhone() : "未设置");
                // Apply saved avatar
                applyAvatar(user.getAvatar());
            } else {
                tvUsername.setText("未设置");
                tvPhone.setText("");
                applyAvatar(null);
            }
        });
    }

    /** Set the avatar ImageView to the selected drawable resource. */
    private void applyAvatar(String avatarIndex) {
        int index = 0; // default to first avatar
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

    /** Show a dialog with 6 avatar options. */
    private void showAvatarPickerDialog(long userId) {
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_avatar_picker, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set up each avatar option with a click handler
        int[] avatarIds = {
            R.id.iv_avatar_1, R.id.iv_avatar_2, R.id.iv_avatar_3,
            R.id.iv_avatar_4, R.id.iv_avatar_5, R.id.iv_avatar_6
        };

        for (int i = 0; i < avatarIds.length; i++) {
            ImageView iv = dialogView.findViewById(avatarIds[i]);
            final String avatarIndex = String.valueOf(i + 1);
            iv.setOnClickListener(v -> {
                // Update database
                viewModel.updateUserAvatar(userId, avatarIndex);
                // Also save to SharedPreferences so the SettingsFragment tab picks it up
                appPreferences.saveAvatar(avatarIndex);
                // Apply to UI immediately
                applyAvatar(avatarIndex);
                dialog.dismiss();
            });
        }
    }

    /** Show a dialog to edit the display name. */
    private void showUsernameEditDialog(long userId) {
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改用户名");

        EditText input = new EditText(this);
        input.setPadding(48, 24, 48, 24);
        input.setText(tvUsername.getText().toString());
        input.setSelection(input.getText().length());
        input.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        builder.setView(input);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newName.length() > 20) {
                Toast.makeText(SettingsActivity.this, "用户名不能超过20个字符", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.updateUsername(userId, newName);
            // Also save to SharedPreferences so the SettingsFragment tab picks it up
            appPreferences.saveUsername(newName);
            // Optimistically update UI
            tvUsername.setText(newName);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void setupThemeToggle() {
        String currentTheme = prefsManager.getThemeMode();
        switch (currentTheme) {
            case "light":
                rgTheme.check(R.id.rb_light);
                break;
            case "dark":
                rgTheme.check(R.id.rb_dark);
                break;
            default:
                rgTheme.check(R.id.rb_system);
                break;
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_light) {
                prefsManager.setThemeMode("light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.rb_dark) {
                prefsManager.setThemeMode("dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                prefsManager.setThemeMode("system");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            // Signal the main activity to refresh
            MainActivity.sThemeChanged = true;
            // Restart this activity with zero transition to avoid flicker
            restartNoAnimation();
        });
    }

    /** Finish and restart this activity with a smooth crossfade transition to avoid flicker. */
    private void restartNoAnimation() {
        Intent intent = getIntent();
        // Start the new activity FIRST (it picks up the new theme via setDefaultNightMode),
        // then crossfade: this activity fades out while the new one fades in — no blank frames.
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void setupLoginSettings() {
        boolean autoLogin = prefsManager.isAutoLogin();
        switchAutoLogin.setChecked(autoLogin);

        switchAutoLogin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.setAutoLogin(isChecked);
        });
    }

    private void setupDataManagement() {
        layoutExport.setOnClickListener(v -> {
            long userId = prefsManager.getUserId();
            viewModel.exportData(userId);
        });

        layoutImport.setOnClickListener(v -> {
            filePickerLauncher.launch("application/json");
        });

        layoutClear.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("清除数据")
                    .setMessage("确定要清除所有数据吗？此操作不可恢复！")
                    .setPositiveButton("清除", (dialog, which) -> {
                        long userId = prefsManager.getUserId();
                        viewModel.clearAllData(userId);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void setupFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        viewModel.importData(uri);
                    }
                }
        );
    }

    private void setupAccountSettings() {
        layoutManageAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.bill_prj.ui.account.AccountManageActivity.class);
            startActivity(intent);
        });

        layoutManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryManageActivity.class);
            startActivity(intent);
        });
    }

    private void setupSecurity() {
        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void setupAbout() {
        // Version info is set in layout directly
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("退出登录")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("退出", (dialog, which) -> {
                        viewModel.logout();
                        Toast.makeText(SettingsActivity.this, "已退出登录", Toast.LENGTH_SHORT).show();
                        finishAffinity();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        builder.setTitle("修改密码");

        EditText etOldPassword = dialogView.findViewById(R.id.et_old_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String oldPwd = etOldPassword.getText().toString().trim();
            String newPwd = etNewPassword.getText().toString().trim();
            String confirmPwd = etConfirmPassword.getText().toString().trim();

            if (oldPwd.isEmpty() || newPwd.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                Toast.makeText(SettingsActivity.this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPwd.length() < 6) {
                Toast.makeText(SettingsActivity.this, "新密码长度不能少于6位", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = prefsManager.getUserId();
            viewModel.changePassword(userId, oldPwd, newPwd);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void observeMessages() {
        viewModel.getMessage().observe(this, message -> {
            if (message == null) return;

            // Handle internal update signals silently
            if ("avatar_updated".equals(message)) {
                Toast.makeText(SettingsActivity.this, "头像已更新", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("username_updated".equals(message)) {
                Toast.makeText(SettingsActivity.this, "用户名已更新", Toast.LENGTH_SHORT).show();
                return;
            }

            if (message.startsWith("/") || message.contains(":")) {
                File file = new File(message);
                if (file.exists()) {
                    Toast.makeText(SettingsActivity.this, "数据已导出", Toast.LENGTH_SHORT).show();
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/json");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(Intent.createChooser(shareIntent, "分享备份文件"));
                }
            } else {
                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
