package com.finledger.dto;

import java.math.BigDecimal;

public class FinancialSummaryDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private long totalTransactions;
    private long incomeCount;
    private long expenseCount;

    public FinancialSummaryDTO() {
    }

    public FinancialSummaryDTO(BigDecimal totalIncome, BigDecimal totalExpense,
                               BigDecimal netBalance, long totalTransactions,
                               long incomeCount, long expenseCount) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.totalTransactions = totalTransactions;
        this.incomeCount = incomeCount;
        this.expenseCount = expenseCount;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getIncomeCount() {
        return incomeCount;
    }

    public void setIncomeCount(long incomeCount) {
        this.incomeCount = incomeCount;
    }

    public long getExpenseCount() {
        return expenseCount;
    }

    public void setExpenseCount(long expenseCount) {
        this.expenseCount = expenseCount;
    }
}
