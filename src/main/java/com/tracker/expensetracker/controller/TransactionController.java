package com.tracker.expensetracker.controller;

import com.tracker.expensetracker.dto.TransactionRequest;
import com.tracker.expensetracker.entity.Transaction;
import com.tracker.expensetracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // GET all transactions (Auto-detects who is logged in!)
    @GetMapping
    public ResponseEntity<List<Transaction>> getAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return ResponseEntity.ok(transactionService.getAllTransactions(username));
    }

    // GET a single transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return ResponseEntity.ok(transactionService.getTransactionById(id, username));
    }

    // POST a new transaction - Updated to accept TransactionRequest with recurring support
    @PostMapping
    public ResponseEntity<Transaction> addTransaction(@RequestBody TransactionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return ResponseEntity.ok(transactionService.addTransaction(request, username));
    }

    // PUT update an existing transaction
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody TransactionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return ResponseEntity.ok(transactionService.updateTransaction(id, request, username));
    }

    // DELETE a single transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        transactionService.deleteTransaction(id, username);
        return ResponseEntity.ok("Deleted");
    }

    // DELETE multiple transactions (bulk delete)
    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteTransactions(@RequestBody Map<String, List<Long>> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        List<Long> ids = request.get("ids");
        transactionService.deleteTransactions(ids, username);
        return ResponseEntity.ok("Deleted " + ids.size() + " transactions");
    }
}