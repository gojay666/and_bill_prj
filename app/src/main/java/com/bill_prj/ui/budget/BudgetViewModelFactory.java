package com.bill_prj.ui.budget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BudgetViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public BudgetViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BudgetViewModel.class)) {
            return (T) new BudgetViewModel((android.app.Application) context.getApplicationContext());
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
