package com.bill_prj;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bill_prj.ui.auth.AuthViewModel;
import com.bill_prj.ui.auth.Result;
import com.bill_prj.ui.bill.MainActivity;
import com.bill_prj.data.entity.User;
import com.bill_prj.data.preferences.AppPreferences;
import com.bill_prj.databinding.ActivityLoginBinding;

public class TestActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private AppPreferences appPreferences;
    private boolean isPasswordVisible = false;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Throwable t) {
            showCrashError("super.onCreate: " + t.getClass().getName() + ": " + t.getMessage());
            return;
        }
        try {
            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
        } catch (Throwable t) {
            showCrashError("inflate/setContentView: " + t.getClass().getName() + ": " + t.getMessage());
            return;
        }
        try {
            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        } catch (Throwable t) {
            showCrashError("ViewModel: " + t.getClass().getName() + ": " + t.getMessage());
            return;
        }
        try {
            appPreferences = new AppPreferences(this);
        } catch (Throwable t) {
            showCrashError("AppPrefs: " + t.getClass().getName() + ": " + t.getMessage());
            return;
        }
        try {
            initViews();
        } catch (Throwable t) {
            showCrashError("initViews: " + t.getClass().getName() + ": " + t.getMessage());
            return;
        }
        try {
            observeViewModel();
        } catch (Throwable t) {
            showCrashError("observeVM: " + t.getClass().getName() + ": " + t.getMessage());
            return;
        }
        try {
            checkSavedLoginState();
        } catch (Throwable t) {
            showCrashError("checkLogin: " + t.getClass().getName() + ": " + t.getMessage());
        }
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
        } catch (Throwable t) {
            showCrashError("onStart: " + t.getClass().getName() + ": " + t.getMessage());
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
        } catch (Throwable t) {
            showCrashError("onResume: " + t.getClass().getName() + ": " + t.getMessage());
        }
    }

    private void showCrashError(String message) {
        android.util.Log.e("TEST_CRASH", message);
        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(message);
        tv.setTextColor(0xFFFF0000);
        tv.setTextSize(14);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setPadding(32, 32, 32, 32);
        sv.addView(tv);
        try {
            setContentView(sv);
        } catch (Exception e) {
            android.util.Log.e("TEST_CRASH", "showCrashError failed: " + e.getMessage());
        }
    }

    private void initViews() {
        binding.ivPasswordToggle.setOnClickListener(v -> togglePasswordVisibility());
        binding.btnLogin.setOnClickListener(v -> performLogin());

        boolean rememberPwd = appPreferences.isRememberPasswordEnabled();
        binding.cbRememberPassword.setChecked(rememberPwd);

        binding.tvRegister.setOnClickListener(v -> {
            startActivityForResult(new Intent(TestActivity.this, com.bill_prj.ui.auth.RegisterActivity.class), 100);
        });
        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(TestActivity.this, com.bill_prj.ui.auth.ForgotPasswordActivity.class));
        });

        if (appPreferences.isRememberPasswordEnabled()) {
            String savedUsername = appPreferences.getUsername();
            String savedPassword = appPreferences.getSavedPassword();
            if (!TextUtils.isEmpty(savedUsername)) {
                binding.tilUsername.getEditText().setText(savedUsername);
            }
            if (!TextUtils.isEmpty(savedPassword)) {
                binding.tilPassword.getEditText().setText(savedPassword);
            }
        }
    }

    private void checkSavedLoginState() {
        if (appPreferences.isAutoLoginEnabled() && appPreferences.isLoggedIn()) {
            startActivity(new Intent(TestActivity.this, MainActivity.class));
            finish();
        }
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        int inputType = isPasswordVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        binding.tilPassword.getEditText().setInputType(inputType);
        binding.tilPassword.getEditText().setSelection(
                binding.tilPassword.getEditText().getText().length());
        binding.ivPasswordToggle.setImageResource(isPasswordVisible
                ? android.R.drawable.ic_menu_view
                : android.R.drawable.ic_lock_lock);
    }

    private void performLogin() {
        if (isLoading) return;
        isLoading = true;
        setLoadingState(true);
        loginWithPassword();
    }

    private void loginWithPassword() {
        String username = binding.tilUsername.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError("请输入用户名");
            setLoadingState(false);
            isLoading = false;
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("请输入密码");
            setLoadingState(false);
            isLoading = false;
            return;
        }

        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);

        authViewModel.login(username, password);
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            binding.btnLogin.setText("登录中...");
            binding.btnLogin.setEnabled(false);
            binding.progressBar.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.btnLogin.setText("登录");
            binding.btnLogin.setEnabled(true);
            binding.progressBar.setVisibility(android.view.View.GONE);
        }
    }

    private void observeViewModel() {
        authViewModel.getLoginResult().observe(this, result -> {
            setLoadingState(false);
            isLoading = false;
            if (result == null) return;

            switch (result.getStatus()) {
                case SUCCESS:
                    handleLoginSuccess(result.getData());
                    break;
                case ERROR:
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void handleLoginSuccess(User user) {
        boolean rememberPwd = binding.cbRememberPassword.isChecked();
        appPreferences.saveRememberPassword(rememberPwd, binding.tilPassword.getEditText().getText().toString().trim());
        appPreferences.saveLoginState(user.getId(), user.getUsername());
        appPreferences.saveAutoLogin(true);

        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(TestActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String username = data.getStringExtra("registered_username");
            if (!TextUtils.isEmpty(username)) {
                binding.tilUsername.getEditText().setText(username);
                binding.tilPassword.getEditText().requestFocus();
            }
        }
    }
}
