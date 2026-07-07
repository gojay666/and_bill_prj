package com.bill_prj.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * 数字格式化工具类
 * 提供金额、百分比等数字格式化功能
 */
public class NumberUtils {

    /** 金额格式：带千分位分隔符，保留两位小数 */
    private static final String AMOUNT_PATTERN = "#,##0.00";

    /** 整数金额格式：带千分位分隔符，无小数 */
    private static final String AMOUNT_INTEGER_PATTERN = "#,##0";

    /** 百分比格式：保留一位小数 */
    private static final String PERCENTAGE_PATTERN = "#0.0";

    /** 纯数字格式：保留两位小数，无千分位 */
    private static final String DECIMAL_PATTERN = "0.00";

    /**
     * 格式化金额，带千分位分隔符，保留两位小数
     * 例如：1234.56 -> "1,234.56"
     *
     * @param amount 金额
     * @return 格式化后的金额字符串
     */
    public static String formatAmount(double amount) {
        DecimalFormat df = new DecimalFormat(AMOUNT_PATTERN);
        return df.format(amount);
    }

    /**
     * 格式化金额，带千分位分隔符，无小数位
     * 例如：1234.56 -> "1,235"
     */
    public static String formatAmountInteger(double amount) {
        DecimalFormat df = new DecimalFormat(AMOUNT_INTEGER_PATTERN);
        return df.format(Math.round(amount));
    }

    /**
     * 格式化金额，不带千分位，保留两位小数
     * 例如：1234.56 -> "1234.56"
     */
    public static String formatAmountPlain(double amount) {
        DecimalFormat df = new DecimalFormat(DECIMAL_PATTERN);
        return df.format(amount);
    }

    /**
     * 格式化金额并添加收入/支出符号和货币符号
     * 收入：+¥1,234.56
     * 支出：-¥1,234.56
     *
     * @param amount   金额（正数）
     * @param isIncome true 表示收入，false 表示支出
     * @return 格式化后的金额字符串
     */
    public static String formatAmountWithSymbol(double amount, boolean isIncome) {
        String prefix = isIncome ? "+" : "-";
        return prefix + Constants.DEFAULT_CURRENCY_SYMBOL + formatAmount(Math.abs(amount));
    }

    /**
     * 格式化金额并添加货币符号，无正负号
     * 例如：1234.56 -> "¥1,234.56"
     */
    public static String formatAmountWithCurrency(double amount) {
        return Constants.DEFAULT_CURRENCY_SYMBOL + formatAmount(amount);
    }

    /**
     * 格式化金额并添加正负号和货币符号
     * 正数：+¥1,234.56
     * 负数：-¥1,234.56
     * 零：¥0.00
     */
    public static String formatAmountSigned(double amount) {
        if (amount == 0) {
            return Constants.DEFAULT_CURRENCY_SYMBOL + "0.00";
        }
        String sign = amount > 0 ? "+" : "-";
        return sign + Constants.DEFAULT_CURRENCY_SYMBOL + formatAmount(Math.abs(amount));
    }

    /**
     * 格式化金额（使用系统默认的 NumberFormat）
     */
    public static String formatAmountLocale(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(true);
        return nf.format(amount);
    }

    /**
     * 解析金额字符串为 double
     * 支持 "1,234.56"、"-1,234.56"、"+1,234.56" 等格式
     *
     * @param amountStr 金额字符串
     * @return 解析后的 double 值，解析失败返回 0.0
     */
    public static double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }
        try {
            // 去除货币符号和空格
            String cleaned = amountStr.replaceAll("[¥$€£￥+,]", "")
                    .replaceAll("\\s+", "")
                    .trim();
            // 处理正负号
            if (cleaned.startsWith("+")) {
                cleaned = cleaned.substring(1);
            }
            DecimalFormat df = new DecimalFormat(AMOUNT_PATTERN);
            df.setParseBigDecimal(false);
            Number number = df.parse(cleaned);
            return number != null ? number.doubleValue() : 0.0;
        } catch (ParseException e) {
            try {
                return Double.parseDouble(amountStr.replaceAll("[^\\d.\\-]", ""));
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
    }

    /**
     * 格式化百分比
     * 例如：45.2 -> "45.2%"
     *
     * @param percentage 百分比数值（如 45.2 表示 45.2%）
     * @return 格式化后的百分比字符串
     */
    public static String formatPercentage(double percentage) {
        DecimalFormat df = new DecimalFormat(PERCENTAGE_PATTERN);
        return df.format(percentage) + "%";
    }

    /**
     * 格式化小数比例为百分比
     * 例如：0.452 -> "45.2%"
     *
     * @param ratio 比例值（0~1 之间）
     * @return 格式化后的百分比字符串
     */
    public static String formatRatioAsPercentage(double ratio) {
        return formatPercentage(ratio * 100);
    }

    /**
     * 格式化百分比，保留指定小数位数
     *
     * @param percentage 百分比数值
     * @param decimals   小数位数
     * @return 格式化后的百分比字符串
     */
    public static String formatPercentage(double percentage, int decimals) {
        StringBuilder pattern = new StringBuilder("#0.");
        for (int i = 0; i < decimals; i++) {
            pattern.append('0');
        }
        DecimalFormat df = new DecimalFormat(pattern.toString());
        return df.format(percentage) + "%";
    }

    /**
     * 保留两位小数
     */
    public static double roundTo2Decimals(double value) {
        DecimalFormat df = new DecimalFormat(DECIMAL_PATTERN);
        try {
            Number number = df.parse(df.format(value));
            return number != null ? number.doubleValue() : 0.0;
        } catch (ParseException e) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    /**
     * 保留指定位数小数
     */
    public static double roundToDecimals(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    /**
     * 判断金额是否为有效值
     */
    public static boolean isValidAmount(double amount) {
        return amount >= Constants.MIN_BILL_AMOUNT
                && amount <= Constants.MAX_BILL_AMOUNT
                && !Double.isNaN(amount)
                && !Double.isInfinite(amount);
    }

    /**
     * 格式化金额为简洁形式
     * 大于等于10000时显示为"x.x万"
     * 大于等于100000000时显示为"x.x亿"
     */
    public static String formatAmountCompact(double amount) {
        double abs = Math.abs(amount);
        String sign = amount < 0 ? "-" : "";
        if (abs >= 100000000) {
            return sign + formatAmountPlain(abs / 100000000) + "亿";
        } else if (abs >= 10000) {
            return sign + formatAmountPlain(abs / 10000) + "万";
        } else {
            return sign + formatAmountPlain(abs);
        }
    }

    /**
     * 隐藏金额中间部分，用于隐私保护
     * 例如：1234.56 -> "****.56"
     */
    public static String maskAmount(double amount) {
        String formatted = formatAmountPlain(amount);
        int dotIndex = formatted.indexOf('.');
        if (dotIndex > 0) {
            return "****" + formatted.substring(dotIndex);
        }
        return "****";
    }

    /**
     * 格式化数字为带单位的字符串
     * 例如：1000 -> "1,000"
     */
    public static String formatNumber(long number) {
        DecimalFormat df = new DecimalFormat(AMOUNT_INTEGER_PATTERN);
        return df.format(number);
    }

    /**
     * Alias for formatAmount - formats currency with thousands separator and 2 decimal places.
     */
    public static String formatCurrency(double amount) {
        return formatAmount(amount);
    }

    /**
     * Format decimal with thousands separator and 2 decimal places.
     */
    public static String formatDecimal(double value) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(value);
    }
}
