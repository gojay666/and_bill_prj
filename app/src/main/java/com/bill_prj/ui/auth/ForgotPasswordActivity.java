package com.bill_prj.ui.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bill_prj.databinding.ActivityForgotPasswordBinding;

/*
 * Layout reference: activity_forgot_password.xml
 * Contains:
 *   - Step indicator (Step 1/3, Step 2/3, Step 3/3)
 *   - Step 1: phone number input + send verification code button + verify button
 *   - Step 2: new password + confirm password + submit button
 *   - Step 3: success message + back to login button
 */

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthViewModel authViewModel;
    private CountDownTimer countDownTimer;

    private int currentStep = 1;
    private String verifiedPhone = "";
    private boolean isLoading = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        initViews();
        observeViewModel();
        showStep(1);
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("重置密码");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Step 1: Send verification code
        binding.btnSendCode.setOnClickListener(v -> sendVerificationCode());

        // Step 1: Verify code
        binding.btnVerifyCode.setOnClickListener(v -> verifyCode());

        // Step 2: New password visibility toggle
        binding.ivNewPasswordToggle.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            int inputType = isNewPasswordVisible
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            binding.tilNewPassword.getEditText().setInputType(inputType);
            binding.tilNewPassword.getEditText().setSelection(
                    binding.tilNewPassword.getEditText().getText().length());
            binding.ivNewPasswordToggle.setImageResource(isNewPasswordVisible
                    ? android.R.drawable.ic_menu_view
                    : android.R.drawable.ic_lock_lock);
        });

        // Step 2: Confirm password visibility toggle
        binding.ivConfirmPasswordToggle.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            int inputType = isConfirmPasswordVisible
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            binding.tilConfirmPassword.getEditText().setInputType(inputType);
            binding.tilConfirmPassword.getEditText().setSelection(
                    binding.tilConfirmPassword.getEditText().getText().length());
            binding.ivConfirmPasswordToggle.setImageResource(isConfirmPasswordVisible
                    ? android.R.drawable.ic_menu_view
                    : android.R.drawable.ic_lock_lock);
        });

        // Step 2: Submit reset
        binding.btnSubmitReset.setOnClickListener(v -> performResetPassword());

        // Step 3: Back to login
        binding.btnBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void showStep(int step) {
        currentStep = step;
        binding.step1Indicator.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.step2Indicator.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.step3Indicator.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        binding.step1Content.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.step2Content.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.step3Content.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        switch (step) {
            case 1:
                binding.tvStepTitle.setText("步骤 1/3: 验证手机号");
                break;
            case 2:
                binding.tvStepTitle.setText("步骤 2/3: 设置新密码");
                break;
            case 3:
                binding.tvStepTitle.setText("步骤 3/3: 完成");
                break;
        }
    }

    private void sendVerificationCode() {
        String phone = binding.tilPhone.getEditText().getText().toString().trim();
        if (TextUtils.isEmpty(phone) || phone.length() != 11 || !phone.startsWith("1")) {
            binding.tilPhone.setError("请输入正确的手机号");
            return;
        }
        binding.tilPhone.setError(null);

        // Check if phone exists in DB first
        authViewModel.checkPhoneExists(phone);
    }

    private void onPhoneExistsChecked(boolean exists, String phone) {
        if (!exists) {
            Toast.makeText(this, "该手机号未注册", Toast.LENGTH_SHORT).show();
            return;
        }

        this.verifiedPhone = phone;
        binding.btnSendCode.setEnabled(false);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.btnSendCode.setText(millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                binding.btnSendCode.setText("发送验证码");
                binding.btnSendCode.setEnabled(true);
            }
        }.start();

        authViewModel.sendVerificationCode(phone);
    }

    private void verifyCode() {
        String code = binding.etCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            binding.etCode.setError("请输入验证码");
            return;
        }
        binding.etCode.setError(null);

        // Move to step 2 after code verification
        showStep(2);
    }

    private void performResetPassword() {
        if (isLoading) return;
        isLoading = true;
        setLoadingState(true);

        String newPassword = binding.tilNewPassword.getEditText().getText().toString().trim();
        String confirmPassword = binding.tilConfirmPassword.getEditText().getText().toString().trim();

        boolean hasError = false;

        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            binding.tilNewPassword.setError("密码至少6位");
            hasError = true;
        } else {
            binding.tilNewPassword.setError(null);
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("两次密码输入不一致");
            hasError = true;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        if (TextUtils.isEmpty(verifiedPhone)) {
            Toast.makeText(this, "手机号验证异常，请返回重试", Toast.LENGTH_SHORT).show();
            setLoadingState(false);
            isLoading = false;
            showStep(1);
            return;
        }

        if (hasError) {
            setLoadingState(false);
            isLoading = false;
            return;
        }

        authViewModel.resetPassword(verifiedPhone, newPassword);
    }

    private void setLoadingState(boolean loading) {
        if (loading) {
            binding.btnSubmitReset.setText("提交中...");
            binding.btnSubmitReset.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.btnSubmitReset.setText("确认重置");
            binding.btnSubmitReset.setEnabled(true);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void observeViewModel() {
        authViewModel.getPhoneCheckResult().observe(this, result -> {
            if (result == null) return;
            switch (result.getStatus()) {
                case SUCCESS:
                    if (result.getData() != null) {
                        String phone = binding.tilPhone.getEditText().getText().toString().trim();
                        onPhoneExistsChecked(result.getData(), phone);
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });

        authViewModel.getSendCodeResult().observe(this, result -> {
            if (result != null && result.getStatus() == Result.Status.ERROR) {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    binding.btnSendCode.setText("发送验证码");
                    binding.btnSendCode.setEnabled(true);
                }
            }
        });

        authViewModel.getResetPasswordResult().observe(this, result -> {
            setLoadingState(false);
            isLoading = false;
            if (result == null) return;
            switch (result.getStatus()) {
                case SUCCESS:
                    showStep(3);
                    break;
                case ERROR:
                    Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
