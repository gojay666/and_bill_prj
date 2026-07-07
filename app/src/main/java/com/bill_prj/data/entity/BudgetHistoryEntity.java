package com.bill_prj.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budget_history",
        indices = {@Index(value = "budgetId")})
public class BudgetHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "budgetId")
    private long budgetId;

    @ColumnInfo(name = "previousAmount")
    private double previousAmount;

    @ColumnInfo(name = "newAmount")
    private double newAmount;

    @ColumnInfo(name = "changeTime")
    private long changeTime;

    @ColumnInfo(name = "reason")
    private String reason;

    public BudgetHistoryEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(long budgetId) {
        this.budgetId = budgetId;
    }

    public double getPreviousAmount() {
        return previousAmount;
    }

    public void setPreviousAmount(double previousAmount) {
        this.previousAmount = previousAmount;
    }

    public double getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(double newAmount) {
        this.newAmount = newAmount;
    }

    public long getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(long changeTime) {
        this.changeTime = changeTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
