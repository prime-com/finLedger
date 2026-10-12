package com.finledger.service;

import com.finledger.dto.FinancialSummaryDTO;
import com.finledger.dto.TransactionRequestDTO;
import com.finledger.dto.TransactionResponseDTO;
import com.finledger.entity.Transaction;
import com.finledger.entity.TransactionType;
import com.finledger.exception.ResourceNotFoundException;
import com.finledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction sampleIncome;
    private Transaction sampleExpense;

    @BeforeEach
    void setUp() {
        sampleIncome = new Transaction(
                1L,
                "Freelance Project",
                new BigDecimal("45000.00"),
                "Salary",
                TransactionType.INCOME,
                LocalDate.of(2026, 3, 1),
                "Consulting gig",
                LocalDateTime.now()
        );

        sampleExpense = new Transaction(
                2L,
                "Groceries",
                new BigDecimal("5000.00"),
                "Food",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 2),
                "Weekly shopping",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("createTransaction should save and return TransactionResponseDTO")
    void testCreateTransaction() {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "Freelance Project",
                new BigDecimal("45000.00"),
                "Salary",
                TransactionType.INCOME,
                LocalDate.of(2026, 3, 1),
                "Consulting gig"
        );

        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleIncome);

        TransactionResponseDTO response = transactionService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Freelance Project");
        assertThat(response.getAmount()).isEqualByComparingTo("45000.00");
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("getAllTransactions without filters returns all records sorted")
    void testGetAllTransactions_NoFilters() {
        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(sampleIncome, sampleExpense));

        List<TransactionResponseDTO> list = transactionService.getAllTransactions(null, null, null, null);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getTitle()).isEqualTo("Freelance Project");
        assertThat(list.get(1).getTitle()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("getAllTransactions with filters uses Specification")
    void testGetAllTransactions_WithFilters() {
        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(sampleIncome));

        List<TransactionResponseDTO> list = transactionService.getAllTransactions(
                TransactionType.INCOME, "Salary", null, null);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getTitle()).isEqualTo("Freelance Project");
    }

    @Test
    @DisplayName("getTransactionById returns transaction if present")
    void testGetTransactionById_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));

        TransactionResponseDTO response = transactionService.getTransactionById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getTransactionById throws ResourceNotFoundException if id does not exist")
    void testGetTransactionById_NotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: '99'");
    }

    @Test
    @DisplayName("updateTransaction updates fields and saves updated transaction")
    void testUpdateTransaction_Success() {
        TransactionRequestDTO updateRequest = new TransactionRequestDTO(
                "Updated Gig",
                new BigDecimal("50000.00"),
                "Bonus",
                TransactionType.INCOME,
                LocalDate.of(2026, 3, 5),
                "Updated notes"
        );

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponseDTO result = transactionService.updateTransaction(1L, updateRequest);

        assertThat(result.getTitle()).isEqualTo("Updated Gig");
        assertThat(result.getAmount()).isEqualByComparingTo("50000.00");
        assertThat(result.getCategory()).isEqualTo("Bonus");
    }

    @Test
    @DisplayName("deleteTransaction deletes record when exists")
    void testDeleteTransaction_Success() {
        when(transactionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(1L);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTransaction throws ResourceNotFoundException when record does not exist")
    void testDeleteTransaction_NotFound() {
        when(transactionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("getFinancialSummary computes totalIncome, totalExpense, and netBalance accurately")
    void testGetFinancialSummary() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(sampleIncome, sampleExpense));

        FinancialSummaryDTO summary = transactionService.getFinancialSummary(null, null);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalIncome()).isEqualByComparingTo("45000.00");
        assertThat(summary.getTotalExpense()).isEqualByComparingTo("5000.00");
        assertThat(summary.getNetBalance()).isEqualByComparingTo("40000.00");
        assertThat(summary.getTotalTransactions()).isEqualTo(2);
        assertThat(summary.getIncomeCount()).isEqualTo(1);
        assertThat(summary.getExpenseCount()).isEqualTo(1);
    }
}
