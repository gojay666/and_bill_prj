package com.bill_prj.ui.statistics;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class StatisticsViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public StatisticsViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StatisticsViewModel.class)) {
            return (T) new StatisticsViewModel((android.app.Application) context.getApplicationContext());
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
