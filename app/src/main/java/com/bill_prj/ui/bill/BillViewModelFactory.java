package com.bill_prj.ui.bill;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BillViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public BillViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BillViewModel.class)) {
            return (T) new BillViewModel((android.app.Application) context.getApplicationContext());
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
