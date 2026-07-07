package com.bill_prj.utils;

/**
 * 应用常量类
 * 集中管理所有 APP 常量
 */
public class Constants {

    // ==================== 数据库相关 ====================

    /** 数据库名称 */
    public static final String DB_NAME = "bill_database";

    /** 数据库版本号 */
    public static final int DB_VERSION = 1;

    // ==================== SharedPreferences 相关 ====================

    /** SharedPreferences 文件名 */
    public static final String PREF_NAME = "bill_prefs";

    /** 登录状态键 */
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    /** 用户ID键 */
    public static final String KEY_USER_ID = "user_id";

    /** 用户名键 */
    public static final String KEY_USERNAME = "username";

    /** 记住密码键 */
    public static final String KEY_REMEMBER_PASSWORD = "remember_password";

    /** 已保存密码键 */
    public static final String KEY_SAVED_PASSWORD = "saved_password";

    /** 主题模式键 */
    public static final String KEY_THEME_MODE = "theme_mode";

    /** 首次启动键 */
    public static final String KEY_FIRST_LAUNCH = "first_launch";

    /** 语言设置键 */
    public static final String KEY_LANGUAGE = "language";

    /** 通知开关键 */
    public static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";

    /** 默认货币键 */
    public static final String KEY_DEFAULT_CURRENCY = "default_currency";

    /** 生物识别登录键 */
    public static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    // ==================== 主题模式值 ====================

    /** 浅色主题 */
    public static final int THEME_LIGHT = 0;

    /** 深色主题 */
    public static final int THEME_DARK = 1;

    /** 跟随系统主题 */
    public static final int THEME_SYSTEM = 2;

    // ==================== 预算类型 ====================

    /** 每日预算 */
    public static final int BUDGET_TYPE_DAILY = 0;

    /** 每周预算 */
    public static final int BUDGET_TYPE_WEEKLY = 1;

    /** 每月预算 */
    public static final int BUDGET_TYPE_MONTHLY = 2;

    /** 每年预算 */
    public static final int BUDGET_TYPE_YEARLY = 3;

    /** 自定义预算 */
    public static final int BUDGET_TYPE_CUSTOM = 4;

    // ==================== 账单类型 ====================

    /** 支出 */
    public static final int BILL_TYPE_EXPENSE = 0;

    /** 收入 */
    public static final int BILL_TYPE_INCOME = 1;

    // ==================== 分页相关 ====================

    /** 默认每页数据条数 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    // ==================== 请求码 ====================

    /** 添加账单请求码 */
    public static final int REQUEST_CODE_ADD_BILL = 1001;

    /** 编辑账单请求码 */
    public static final int REQUEST_CODE_EDIT_BILL = 1002;

    /** 查看账单详情请求码 */
    public static final int REQUEST_CODE_VIEW_BILL = 1003;

    /** 选择分类请求码 */
    public static final int REQUEST_CODE_SELECT_CATEGORY = 1004;

    /** 选择日期请求码 */
    public static final int REQUEST_CODE_SELECT_DATE = 1005;

    /** 拍照请求码 */
    public static final int REQUEST_CODE_TAKE_PHOTO = 1006;

    /** 选择图片请求码 */
    public static final int REQUEST_CODE_SELECT_IMAGE = 1007;

    /** 文件选择请求码 */
    public static final int REQUEST_CODE_SELECT_FILE = 1008;

    /** 备份文件请求码 */
    public static final int REQUEST_CODE_BACKUP = 1009;

    /** 恢复文件请求码 */
    public static final int REQUEST_CODE_RESTORE = 1010;

    /** 导出CSV请求码 */
    public static final int REQUEST_CODE_EXPORT_CSV = 1011;

    /** 扫码请求码 */
    public static final int REQUEST_CODE_SCAN = 1012;

    /** 设置密码请求码 */
    public static final int REQUEST_CODE_SET_PASSWORD = 1013;

    /** 生物识别认证请求码 */
    public static final int REQUEST_CODE_BIOMETRIC_AUTH = 1014;

    /** 权限请求码 - 相机 */
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 2001;

    /** 权限请求码 - 存储 */
    public static final int REQUEST_CODE_PERMISSION_STORAGE = 2002;

    /** 权限请求码 - 定位 */
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 2003;

    // ==================== 通知相关 ====================

    /** 通知渠道ID - 账单提醒 */
    public static final String NOTIFICATION_CHANNEL_BILL_REMINDER = "bill_reminder";

    /** 通知渠道ID - 预算超支 */
    public static final String NOTIFICATION_CHANNEL_BUDGET_OVER = "budget_over";

    /** 通知渠道名称 - 账单提醒 */
    public static final String NOTIFICATION_CHANNEL_NAME_BILL_REMINDER = "账单提醒";

    /** 通知渠道名称 - 预算超支 */
    public static final String NOTIFICATION_CHANNEL_NAME_BUDGET_OVER = "预算超支";

    /** 通知ID - 账单提醒 */
    public static final int NOTIFICATION_ID_BILL_REMINDER = 3001;

    /** 通知ID - 预算超支 */
    public static final int NOTIFICATION_ID_BUDGET_OVER = 3002;

    // ==================== 分类相关 ====================

    /** 默认支出分类 */
    public static final String[] DEFAULT_EXPENSE_CATEGORIES = {
            "餐饮", "交通", "购物", "娱乐", "住房", "水电", "通讯",
            "医疗", "教育", "服饰", "美妆", "运动", "礼物", "其他"
    };

    /** 默认收入分类 */
    public static final String[] DEFAULT_INCOME_CATEGORIES = {
            "工资", "奖金", "兼职", "投资", "红包", "退款", "其他"
    };

    // ==================== 图表相关 ====================

    /** 图表动画时长（毫秒） */
    public static final int CHART_ANIMATION_DURATION = 800;

    /** 饼图最小百分比（小于此值不显示标签） */
    public static final float CHART_PIE_MIN_PERCENTAGE = 3.0f;

    // ==================== 日期相关 ====================

    /** 默认查看天数范围 */
    public static final int DEFAULT_VIEW_DAYS = 30;

    /** 日期选择器显示的最大未来天数 */
    public static final int MAX_FUTURE_DAYS = 365;

    // ==================== 导出相关 ====================

    /** CSV 文件分隔符 */
    public static final String CSV_DELIMITER = ",";

    /** CSV 文件名前缀 */
    public static final String CSV_FILE_PREFIX = "bill_export_";

    /** 备份文件名前缀 */
    public static final String BACKUP_FILE_PREFIX = "bill_backup_";

    /** 备份文件扩展名 */
    public static final String BACKUP_FILE_EXTENSION = ".json";

    /** CSV 文件扩展名 */
    public static final String CSV_FILE_EXTENSION = ".csv";

    // ==================== 日期格式 ====================

    /** 默认日期格式 */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /** 默认时间格式 */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /** 默认日期时间格式 */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // ==================== 金额限制 ====================

    /** 单笔金额最小值 */
    public static final double MIN_BILL_AMOUNT = 0.01;

    /** 单笔金额最大值 */
    public static final double MAX_BILL_AMOUNT = 99999999.99;

    /** 金额小数位数 */
    public static final int AMOUNT_DECIMAL_PLACES = 2;

    // ==================== 其他 ====================

    /** 应用 TAG */
    public static final String APP_TAG = "BillPrj";

    /** 空字符串 */
    public static final String EMPTY_STRING = "";

    /** 默认货币符号 */
    public static final String DEFAULT_CURRENCY_SYMBOL = "¥";

    /** 默认货币代码 */
    public static final String DEFAULT_CURRENCY_CODE = "CNY";

    // ==================== Fragment Tags ====================
    public static final String TAG_HOME = "home";
    public static final String TAG_BILLS = "bills";
    public static final String TAG_STATISTICS = "statistics";
    public static final String TAG_SETTINGS = "settings";

    // ==================== Intent Extras ====================
    public static final String EXTRA_BILL_ID = "extra_bill_id";

    // ==================== Date Range Constants ====================
    public static final int RANGE_TODAY = 0;
    public static final int RANGE_WEEK = 1;
    public static final int RANGE_MONTH = 2;

    // ==================== Request Codes (keep both style for compatibility) ====================
    public static final int REQUEST_ADD_BILL = 1001;
    public static final int REQUEST_EDIT_BILL = 1002;

    // ==================== Theme Mode (String versions for SharedPreferences) ====================
    public static final String THEME_MODE_LIGHT = "light";
    public static final String THEME_MODE_DARK = "dark";
    public static final String THEME_MODE_SYSTEM = "system";

    /**
     * 私有构造函数，防止实例化
     */
    private Constants() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
}
