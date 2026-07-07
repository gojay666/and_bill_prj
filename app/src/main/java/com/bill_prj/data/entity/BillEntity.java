package com.bill_prj.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "bills",
        indices = {
            @Index(value = {"userId", "date"}),
            @Index(value = {"userId", "category"})
        })
public class BillEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "userId")
    private long userId;

    @ColumnInfo(name = "type")
    private boolean type; // true = income, false = expense

    @ColumnInfo(name = "category")
    private String category; // BillType name

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "accountId")
    private long accountId;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(name = "tags")
    private String tags; // comma-separated

    @ColumnInfo(name = "date")
    private long date; // timestamp

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public BillEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // === Type Constants ===
    public static final String TYPE_INCOME = "income";
    public static final String TYPE_EXPENSE = "expense";

    /**
     * Get the display name for the category.
     */
    public String getCategoryName() {
        return category != null ? category : "";
    }

    /**
     * Get the type as a string ("income" or "expense").
     */
    public String getType() {
        return type ? TYPE_INCOME : TYPE_EXPENSE;
    }
}
