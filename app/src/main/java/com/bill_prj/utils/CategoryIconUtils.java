package com.bill_prj.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 分类图标工具类
 * 将分类名称映射为直观的图标（使用 emoji 字符）
 */
public class CategoryIconUtils {

    private static final Map<String, String> ICON_MAP = new HashMap<>();

    static {
        // 支出分类
        ICON_MAP.put("餐饮", "\uD83C\uDF5C");    // 🍜
        ICON_MAP.put("交通", "\uD83D\uDE97");    // 🚗
        ICON_MAP.put("购物", "\uD83D\uDED2");    // 🛒
        ICON_MAP.put("娱乐", "\uD83C\uDFAE");    // 🎮
        ICON_MAP.put("住房", "\uD83C\uDFE0");    // 🏠
        ICON_MAP.put("水电", "\uD83D\uDCA1");    // 💡
        ICON_MAP.put("通讯", "\uD83D\uDCF1");    // 📱
        ICON_MAP.put("医疗", "\uD83C\uDFE5");    // 🏥
        ICON_MAP.put("教育", "\uD83D\uDCDA");    // 📚
        ICON_MAP.put("服饰", "\uD83D\uDC57");    // 👗
        ICON_MAP.put("美妆", "\uD83D\uDC84");    // 💄
        ICON_MAP.put("运动", "\u26BD");          // ⚽
        ICON_MAP.put("礼物", "\uD83C\uDF81");    // 🎁

        // 收入分类
        ICON_MAP.put("工资", "\uD83D\uDCB0");    // 💰
        ICON_MAP.put("奖金", "\uD83C\uDFC6");    // 🏆
        ICON_MAP.put("兼职", "\uD83D\uDCBC");    // 💼
        ICON_MAP.put("投资", "\uD83D\uDCC8");    // 📈
        ICON_MAP.put("红包", "\uD83E\uDDE7");    // 🧧
        ICON_MAP.put("退款", "\u21A9\uFE0F");    // ↩️

        // 通用
        ICON_MAP.put("其他收入", "\uD83D\uDCB0");    // 💰
        ICON_MAP.put("其他支出", "\uD83D\uDCC3");    // 📃
        ICON_MAP.put("旅行", "\u2708\uFE0F");        // ✈️
        ICON_MAP.put("社交", "\uD83D\uDC65");        // 👥
        ICON_MAP.put("其他", "\uD83D\uDCCC");        // 📌
    }

    /**
     * 获取分类对应的图标字符（emoji）
     *
     * @param category 分类名称
     * @return 图标字符，未找到映射时返回第一个汉字或 "?"
     */
    public static String getIcon(String category) {
        if (category == null || category.isEmpty()) {
            return "?";
        }
        String icon = ICON_MAP.get(category);
        if (icon != null) {
            return icon;
        }
        // 未找到映射时使用第一个字符作为后备
        return category.substring(0, 1);
    }

    /**
     * 获取分类对应的颜色资源 ID（与 colors.xml 保持一致）
     */
    public static int getColorResId(String category) {
        if (category == null) return 0xFF94A3B8;
        switch (category) {
            case "餐饮": return 0xFFF97316;
            case "交通": return 0xFF3B82F6;
            case "购物": return 0xFFEC4899;
            case "娱乐": return 0xFF8B5CF6;
            case "住房": return 0xFF78716C;
            case "水电": return 0xFF64748B;
            case "通讯": return 0xFF06B6D4;
            case "医疗": return 0xFFEF4444;
            case "教育": return 0xFF6366F1;
            case "服饰": return 0xFFE11D48;
            case "美妆": return 0xFFEC4899;
            case "运动": return 0xFF22C55E;
            case "礼物": return 0xFFEC4899;
            case "工资": return 0xFF22C55E;
            case "奖金": return 0xFFA3E635;
            case "兼职": return 0xFF34D399;
            case "投资": return 0xFF14B8A6;
            case "红包": return 0xFFEF4444;
            case "退款": return 0xFF22C55E;
            case "其他收入": return 0xFF22C55E;
            case "其他支出": return 0xFF94A3B8;
            case "旅行": return 0xFF3B82F6;
            case "社交": return 0xFFEC4899;
            case "其他": return 0xFF94A3B8;
            default: return 0xFF94A3B8;
        }
    }
}
