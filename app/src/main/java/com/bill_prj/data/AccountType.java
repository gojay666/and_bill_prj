package com.bill_prj.data;

import java.util.ArrayList;
import java.util.List;

public enum AccountType {
    CASH("现金"),
    BANK_CARD("银行卡"),
    WECHAT("微信"),
    ALIPAY("支付宝"),
    CREDIT_CARD("信用卡"),
    OTHER("其他");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AccountType fromDisplayName(String name) {
        for (AccountType type : AccountType.values()) {
            if (type.displayName.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + name);
    }

    public static List<AccountType> getAllTypes() {
        List<AccountType> types = new ArrayList<>();
        for (AccountType type : AccountType.values()) {
            types.add(type);
        }
        return types;
    }
}
