package com.bill_prj.utils;

import java.text.DecimalFormat;

public class CurrencyUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    public static String format(double amount) {
        return DECIMAL_FORMAT.format(amount);
    }

    public static String formatWithSymbol(double amount) {
        return "¥" + DECIMAL_FORMAT.format(amount);
    }

    public static String formatIncome(double amount) {
        return "+¥" + DECIMAL_FORMAT.format(amount);
    }

    public static String formatExpense(double amount) {
        return "-¥" + DECIMAL_FORMAT.format(amount);
    }

    public static String formatAmount(double amount, boolean isIncome) {
        if (isIncome) {
            return "+¥" + DECIMAL_FORMAT.format(amount);
        } else {
            return "-¥" + DECIMAL_FORMAT.format(amount);
        }
    }

    public static String formatPercentage(double value) {
        DecimalFormat percentFormat = new DecimalFormat("#0.0");
        return percentFormat.format(value) + "%";
    }
}
