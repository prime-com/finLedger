package com.finledger.service;

import com.finledger.dto.FinancialSummaryDTO;
import com.finledger.dto.TransactionRequestDTO;
import com.finledger.dto.TransactionResponseDTO;
import com.finledger.entity.Transaction;
import com.finledger.entity.TransactionType;
import com.finledger.exception.ResourceNotFoundException;
import com.finledger.repository.TransactionRepository;
import com.finledger.repository.TransactionSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        Transaction transaction = new Transaction();
        transaction.setTitle(request.getTitle().trim());
        transaction.setAmount(request.getAmount());
        transaction.setCategory(request.getCategory().trim());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return TransactionResponseDTO.fromEntity(savedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAllTransactions(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate) {

        Specification<Transaction> spec = TransactionSpecification.withFilters(type, category, startDate, endDate);
        Sort sort = Sort.by(Sort.Direction.DESC, "transactionDate", "id");
        List<Transaction> transactions = transactionRepository.findAll(spec, sort);

        return transactions.stream()
                .map(TransactionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        return TransactionResponseDTO.fromEntity(transaction);
    }

    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO request) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));

        existing.setTitle(request.getTitle().trim());
        existing.setAmount(request.getAmount());
        existing.setCategory(request.getCategory().trim());
        existing.setType(request.getType());
        existing.setTransactionDate(request.getTransactionDate());
        existing.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        Transaction updated = transactionRepository.save(existing);
        return TransactionResponseDTO.fromEntity(updated);
    }

    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", "id", id);
        }
        transactionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public FinancialSummaryDTO getFinancialSummary(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions;
        if (startDate == null && endDate == null) {
            transactions = transactionRepository.findAll();
        } else {
            Specification<Transaction> spec = TransactionSpecification.withFilters(null, null, startDate, endDate);
            transactions = transactionRepository.findAll(spec);
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        long incomeCount = 0;
        long expenseCount = 0;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
                incomeCount++;
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(t.getAmount());
                expenseCount++;
            }
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        long totalTransactions = transactions.size();

        return new FinancialSummaryDTO(
                totalIncome,
                totalExpense,
                netBalance,
                totalTransactions,
                incomeCount,
                expenseCount
        );
    }
}
