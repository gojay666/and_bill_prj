package com.bill_prj.data;

import com.bill_prj.R;
import com.bill_prj.data.entity.CategoryEntity;
import com.bill_prj.utils.CategoryIconUtils;

import java.util.ArrayList;
import java.util.List;

public enum BillType {
    // Income types - 6 types
    SALARY("工资", true, "#4CAF50", R.color.category_salary),
    BONUS("奖金", true, "#66BB6A", R.color.category_bonus),
    INVESTMENT("投资", true, "#81C784", R.color.category_investment),
    PART_TIME("兼职", true, "#A5D6A7", R.color.category_part_time),
    RED_PACKET("红包", true, "#E53935", R.color.category_red_packet),
    OTHER_INCOME("其他收入", true, "#9E9E9E", R.color.category_other),

    // Expense types - 12 types
    FOOD("餐饮", false, "#FF5722", R.color.category_food),
    TRANSPORT("交通", false, "#2196F3", R.color.category_transport),
    SHOPPING("购物", false, "#E91E63", R.color.category_shopping),
    ENTERTAINMENT("娱乐", false, "#9C27B0", R.color.category_entertainment),
    MEDICAL("医疗", false, "#F44336", R.color.category_medical),
    EDUCATION("教育", false, "#3F51B5", R.color.category_education),
    HOUSING("住房", false, "#795548", R.color.category_housing),
    UTILITIES("水电", false, "#607D8B", R.color.category_utilities),
    COMMUNICATION("通讯", false, "#00BCD4", R.color.category_communication),
    CLOTHING("服饰", false, "#FF4081", R.color.category_clothing),
    TRAVEL("旅行", false, "#FF9800", R.color.category_travel),
    SOCIAL("社交", false, "#E040FB", R.color.category_social),
    OTHER_EXPENSE("其他支出", false, "#9E9E9E", R.color.category_other);

    private final String displayName;
    private final boolean isIncome;
    private final String colorHex;
    private final int colorResId;

    BillType(String displayName, boolean isIncome, String colorHex, int colorResId) {
        this.displayName = displayName;
        this.isIncome = isIncome;
        this.colorHex = colorHex;
        this.colorResId = colorResId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getName() {
        return displayName;
    }

    public String getDisplayLetter() {
        if (displayName != null && !displayName.isEmpty()) {
            return String.valueOf(displayName.charAt(0));
        }
        return "?";
    }

    public int getColorResId() {
        return colorResId;
    }

    public static BillType fromDisplayName(String name) {
        if (name == null) return OTHER_EXPENSE;
        for (BillType type : BillType.values()) {
            if (type.displayName.equals(name)) {
                return type;
            }
        }
        return OTHER_EXPENSE;
    }

    public static List<BillType> getIncomeTypes() {
        List<BillType> types = new ArrayList<>();
        for (BillType type : BillType.values()) {
            if (type.isIncome) {
                types.add(type);
            }
        }
        return types;
    }

    public static List<BillType> getExpenseTypes() {
        List<BillType> types = new ArrayList<>();
        for (BillType type : BillType.values()) {
            if (!type.isIncome) {
                types.add(type);
            }
        }
        return types;
    }

    public static List<BillType> getAllTypes() {
        List<BillType> types = new ArrayList<>();
        for (BillType type : BillType.values()) {
            types.add(type);
        }
        return types;
    }

    /** 18 types total: 6 income + 12 expense */
    public static int getTotalCount() {
        return values().length;
    }

    /**
     * Create default CategoryEntity list from BillType enum.
     * Used for pre-populating the categories table.
     */
    public static List<CategoryEntity> getDefaultCategories() {
        List<CategoryEntity> list = new ArrayList<>();
        for (BillType type : BillType.values()) {
            CategoryEntity cat = new CategoryEntity();
            cat.setUserId(0); // global default
            cat.setName(type.getDisplayName());
            cat.setType(type.isIncome() ? "income" : "expense");
            cat.setEmoji(CategoryIconUtils.getIcon(type.getDisplayName()));
            cat.setColor(type.getColorHex());
            cat.setDefault(true);
            cat.setCreatedAt(System.currentTimeMillis());
            list.add(cat);
        }
        return list;
    }
}
