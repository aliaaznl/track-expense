package com.tracker.expensetracker.service;

import com.tracker.expensetracker.dto.TransactionRequest;
import com.tracker.expensetracker.entity.Transaction;
import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.repository.TransactionRepository;
import com.tracker.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Get all transactions for a specific user (using email from JWT)
    public List<Transaction> getAllTransactions(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return transactionRepository.findByUser(user);
    }

    // 2. Add a new transaction (using email from JWT)
    @Transactional
    public Transaction addTransaction(TransactionRequest request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        
        // Create the main transaction
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setReceiptImage(request.getReceiptImage());
        transaction.setCategory(request.getCategory());
        transaction.setType(request.getType());
        transaction.setUser(user);
        
        // Handle recurring transactions
        if (request.getRecurring() != null && request.getRecurring().getFrequency() != null) {
            transaction.setIsRecurring(true);
            transaction.setRecurringFrequency(request.getRecurring().getFrequency());
            transaction.setRecurringEndDate(request.getRecurring().getEndDate());
            
            // Save the parent transaction first
            Transaction savedTransaction = transactionRepository.save(transaction);
            Long parentId = savedTransaction.getId();
            savedTransaction.setParentRecurringId(parentId);
            savedTransaction = transactionRepository.save(savedTransaction);
            
            // Create future recurring transactions
            List<Transaction> recurringTransactions = createRecurringTransactions(
                savedTransaction, 
                request.getRecurring().getFrequency(), 
                request.getRecurring().getEndDate()
            );
            
            // Save all recurring transactions
            transactionRepository.saveAll(recurringTransactions);
            
            return savedTransaction;
        } else {
            transaction.setIsRecurring(false);
            return transactionRepository.save(transaction);
        }
    }

    // Helper method to create recurring transactions
    private List<Transaction> createRecurringTransactions(Transaction parent, String frequency, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        LocalDate currentDate = parent.getDate();
        
        // Move to first future occurrence
        currentDate = getNextOccurrenceDate(currentDate, frequency);
        
        // Limit to 100 recurring transactions to prevent infinite loops
        int maxOccurrences = 100;
        int count = 0;
        
        while (count < maxOccurrences) {
            if (endDate != null && currentDate.isAfter(endDate)) {
                break;
            }
            
            Transaction recurringTxn = new Transaction();
            recurringTxn.setAmount(parent.getAmount());
            recurringTxn.setDescription(parent.getDescription());
            recurringTxn.setDate(currentDate);
            recurringTxn.setReceiptImage(parent.getReceiptImage());
            recurringTxn.setCategory(parent.getCategory());
            recurringTxn.setType(parent.getType());
            recurringTxn.setUser(parent.getUser());
            recurringTxn.setIsRecurring(true);
            recurringTxn.setRecurringFrequency(frequency);
            recurringTxn.setRecurringEndDate(endDate);
            recurringTxn.setParentRecurringId(parent.getId());
            
            transactions.add(recurringTxn);
            
            // Move to next occurrence
            currentDate = getNextOccurrenceDate(currentDate, frequency);
            count++;
        }
        
        return transactions;
    }

    // Helper method to calculate next occurrence date
    private LocalDate getNextOccurrenceDate(LocalDate currentDate, String frequency) {
        return switch (frequency.toUpperCase()) {
            case "DAILY" -> currentDate.plusDays(1);
            case "WEEKLY" -> currentDate.plusWeeks(1);
            case "MONTHLY" -> currentDate.plusMonths(1);
            case "YEARLY" -> currentDate.plusYears(1);
            default -> currentDate.plusDays(1);
        };
    }

    // 3. Update a transaction
    @Transactional
    public Transaction updateTransaction(Long id, TransactionRequest request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // Verify the transaction belongs to the user
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Transaction does not belong to user");
        }
        
        // Update transaction fields
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setReceiptImage(request.getReceiptImage());
        transaction.setCategory(request.getCategory());
        transaction.setType(request.getType());
        
        // Update recurring fields if provided
        if (request.getRecurring() != null) {
            transaction.setIsRecurring(request.getRecurring().getFrequency() != null);
            if (request.getRecurring().getFrequency() != null) {
                transaction.setRecurringFrequency(request.getRecurring().getFrequency());
                transaction.setRecurringEndDate(request.getRecurring().getEndDate());
            } else {
                transaction.setIsRecurring(false);
            }
        }
        
        return transactionRepository.save(transaction);
    }

    // 4. Get a single transaction by ID
    public Transaction getTransactionById(Long id, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // Verify the transaction belongs to the user
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Transaction does not belong to user");
        }
        
        return transaction;
    }

    // 5. Delete a transaction
    public void deleteTransaction(Long id, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // Verify the transaction belongs to the user
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Transaction does not belong to user");
        }
        
        transactionRepository.deleteById(id);
    }

    // 6. Bulk delete transactions
    @Transactional
    public void deleteTransactions(List<Long> ids, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        
        // Verify all transactions belong to the user before deleting
        for (Long id : ids) {
            Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
            
            if (!transaction.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized: Transaction does not belong to user: " + id);
            }
        }
        
        transactionRepository.deleteAllById(ids);
    }
}