package com.finledger.dto;

import com.finledger.entity.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionRequestDTO {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Amount cannot have more than 12 digits before and 2 digits after the decimal point")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    @Size(max = 60, message = "Category cannot exceed 60 characters")
    private String category;

    @NotNull(message = "Transaction type must be either INCOME or EXPENSE")
    private TransactionType type;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    public TransactionRequestDTO() {
    }

    public TransactionRequestDTO(String title, BigDecimal amount, String category,
                                 TransactionType type, LocalDate transactionDate, String notes) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.transactionDate = transactionDate;
        this.notes = notes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
