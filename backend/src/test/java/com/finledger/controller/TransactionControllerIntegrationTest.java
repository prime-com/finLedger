package com.finledger.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.dto.TransactionRequestDTO;
import com.finledger.entity.Transaction;
import com.finledger.entity.TransactionType;
import com.finledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/transactions with valid payload should return 201 Created")
    void testCreateTransaction_ValidPayload_Returns201() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "Salary March",
                new BigDecimal("85000.00"),
                "Employment",
                TransactionType.INCOME,
                LocalDate.of(2026, 3, 1),
                "Monthly salary"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Salary March"))
                .andExpect(jsonPath("$.amount").value(85000.00))
                .andExpect(jsonPath("$.category").value("Employment"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.transactionDate").value("2026-03-01"))
                .andExpect(jsonPath("$.notes").value("Monthly salary"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions with invalid amount should return 400 Bad Request")
    void testCreateTransaction_InvalidAmount_Returns400() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "Gym Membership",
                new BigDecimal("-500.00"), // Invalid negative amount
                "Fitness",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 2),
                "Monthly gym fee"
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.amount").exists());
    }

    @Test
    @DisplayName("POST /api/v1/transactions with blank title should return 400 Bad Request")
    void testCreateTransaction_BlankTitle_Returns400() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "   ",
                new BigDecimal("1200.00"),
                "Utilities",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 3),
                null
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("GET /api/v1/transactions should return list of transactions")
    void testGetAllTransactions() throws Exception {
        Transaction tx1 = new Transaction();
        tx1.setTitle("Freelance");
        tx1.setAmount(new BigDecimal("20000.00"));
        tx1.setCategory("Side Gig");
        tx1.setType(TransactionType.INCOME);
        tx1.setTransactionDate(LocalDate.of(2026, 3, 5));
        transactionRepository.save(tx1);

        Transaction tx2 = new Transaction();
        tx2.setTitle("Dinner");
        tx2.setAmount(new BigDecimal("1500.00"));
        tx2.setCategory("Food");
        tx2.setType(TransactionType.EXPENSE);
        tx2.setTransactionDate(LocalDate.of(2026, 3, 6));
        transactionRepository.save(tx2);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", anyOf(is("Freelance"), is("Dinner"))));
    }

    @Test
    @DisplayName("GET /api/v1/transactions?type=EXPENSE filters properly")
    void testGetFilteredTransactions_ByType() throws Exception {
        Transaction tx1 = new Transaction();
        tx1.setTitle("Freelance");
        tx1.setAmount(new BigDecimal("20000.00"));
        tx1.setCategory("Side Gig");
        tx1.setType(TransactionType.INCOME);
        tx1.setTransactionDate(LocalDate.of(2026, 3, 5));
        transactionRepository.save(tx1);

        Transaction tx2 = new Transaction();
        tx2.setTitle("Dinner");
        tx2.setAmount(new BigDecimal("1500.00"));
        tx2.setCategory("Food");
        tx2.setType(TransactionType.EXPENSE);
        tx2.setTransactionDate(LocalDate.of(2026, 3, 6));
        transactionRepository.save(tx2);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Dinner"));
    }

    @Test
    @DisplayName("GET /api/v1/transactions/{id} returns 200 when exists, 404 when absent")
    void testGetTransactionById() throws Exception {
        Transaction tx = new Transaction();
        tx.setTitle("Internet Bill");
        tx.setAmount(new BigDecimal("999.00"));
        tx.setCategory("Utilities");
        tx.setType(TransactionType.EXPENSE);
        tx.setTransactionDate(LocalDate.of(2026, 3, 7));
        Transaction saved = transactionRepository.save(tx);

        mockMvc.perform(get("/api/v1/transactions/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Internet Bill"));

        mockMvc.perform(get("/api/v1/transactions/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /api/v1/transactions/{id} updates transaction")
    void testUpdateTransaction() throws Exception {
        Transaction tx = new Transaction();
        tx.setTitle("Old Title");
        tx.setAmount(new BigDecimal("1000.00"));
        tx.setCategory("Shopping");
        tx.setType(TransactionType.EXPENSE);
        tx.setTransactionDate(LocalDate.of(2026, 3, 8));
        Transaction saved = transactionRepository.save(tx);

        TransactionRequestDTO updateRequest = new TransactionRequestDTO(
                "Updated Shopping Title",
                new BigDecimal("1450.00"),
                "Shopping",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 8),
                "Updated note"
        );

        mockMvc.perform(put("/api/v1/transactions/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Shopping Title"))
                .andExpect(jsonPath("$.amount").value(1450.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/transactions/{id} deletes and returns 204")
    void testDeleteTransaction() throws Exception {
        Transaction tx = new Transaction();
        tx.setTitle("To Delete");
        tx.setAmount(new BigDecimal("500.00"));
        tx.setCategory("Miscellaneous");
        tx.setType(TransactionType.EXPENSE);
        tx.setTransactionDate(LocalDate.of(2026, 3, 9));
        Transaction saved = transactionRepository.save(tx);

        mockMvc.perform(delete("/api/v1/transactions/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/transactions/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/transactions/summary computes financial metrics")
    void testGetSummary() throws Exception {
        Transaction income = new Transaction();
        income.setTitle("Stipend");
        income.setAmount(new BigDecimal("30000.00"));
        income.setCategory("Education");
        income.setType(TransactionType.INCOME);
        income.setTransactionDate(LocalDate.of(2026, 3, 1));
        transactionRepository.save(income);

        Transaction expense = new Transaction();
        expense.setTitle("Books");
        expense.setAmount(new BigDecimal("4500.00"));
        expense.setCategory("Education");
        expense.setType(TransactionType.EXPENSE);
        expense.setTransactionDate(LocalDate.of(2026, 3, 2));
        transactionRepository.save(expense);

        mockMvc.perform(get("/api/v1/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(30000.00))
                .andExpect(jsonPath("$.totalExpense").value(4500.00))
                .andExpect(jsonPath("$.netBalance").value(25500.00))
                .andExpect(jsonPath("$.totalTransactions").value(2))
                .andExpect(jsonPath("$.incomeCount").value(1))
                .andExpect(jsonPath("$.expenseCount").value(1));
    }
}
