package com.finledger.dto;

import com.finledger.entity.Transaction;
import com.finledger.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponseDTO {

    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private TransactionType type;
    private LocalDate transactionDate;
    private String notes;
    private LocalDateTime createdAt;

    public TransactionResponseDTO() {
    }

    public TransactionResponseDTO(Long id, String title, BigDecimal amount, String category,
                                  TransactionType type, LocalDate transactionDate, String notes,
                                  LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.transactionDate = transactionDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static TransactionResponseDTO fromEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getTitle(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getType(),
                transaction.getTransactionDate(),
                transaction.getNotes(),
                transaction.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
