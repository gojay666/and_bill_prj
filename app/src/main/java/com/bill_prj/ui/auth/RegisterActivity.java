package com.bill_prj.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bill_prj.data.entity.User;
import com.bill_prj.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        observeViewModel();
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("注册");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnRegister.setOnClickListener(v -> performRegister());
    }

    private void performRegister() {
        if (isLoading) return;
        isLoading = true;
        setLoadingState(true);

        String username = binding.tilRegUsername.getEditText().getText().toString().trim();
        String phone = binding.tilRegPhone.getEditText().getText().toString().trim();
        String password = binding.tilRegPassword.getEditText().getText().toString().trim();
        String confirmPassword = binding.tilConfirmPassword.getEditText().getText().toString().trim();

        boolean hasError = false;

        if (TextUtils.isEmpty(username)) {
            binding.tilRegUsername.setError("请输入用户名");
            hasError = true;
        } else {
            binding.tilRegUsername.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            binding.tilRegPhone.setError("请输入手机号");
            hasError = true;
        } else if (!phone.matches("1\\d{10}")) {
            binding.tilRegPhone.setError("请输入正确的11位手机号");
            hasError = true;
        } else {
            binding.tilRegPhone.setError(null);
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilRegPassword.setError("密码至少6位");
            hasError = true;
        } else {
            binding.tilRegPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("两次密码输入不一致");
            hasError = true;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        if (hasError) {
            setLoadingState(false);
            isLoading = false;
            return;
        }

        User user = new User(username, password, phone);
        authViewModel.register(user);
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            binding.btnRegister.setText("注册中...");
            binding.btnRegister.setEnabled(false);
            binding.progressBar.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.btnRegister.setText("注  册");
            binding.btnRegister.setEnabled(true);
            binding.progressBar.setVisibility(android.view.View.GONE);
        }
    }

    private void observeViewModel() {
        authViewModel.getRegisterResult().observe(this, result -> {
            setLoadingState(false);
            isLoading = false;
            if (result == null) return;

            switch (result.getStatus()) {
                case SUCCESS:
                    Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("registered_username",
                            result.getData() != null ? result.getData().getUsername() : "");
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case ERROR:
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }
}
