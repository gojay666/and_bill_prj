package com.bill_prj.ui.auth;

import androidx.annotation.Nullable;

public class Result<T> {
    private final Status status;
    private final String message;
    private final T data;

    private Result(Status status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Nullable
    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(Status.SUCCESS, null, data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(Status.SUCCESS, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(Status.ERROR, message, null);
    }

    public static <T> Result<T> loading() {
        return new Result<>(Status.LOADING, null, null);
    }

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}
