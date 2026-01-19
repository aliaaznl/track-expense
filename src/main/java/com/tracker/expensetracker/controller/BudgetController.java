package com.tracker.expensetracker.controller;

import com.tracker.expensetracker.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetService.BudgetDTO>> getAllBudgets() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(budgetService.getAllBudgets(email));
    }

    @PostMapping
    public ResponseEntity<BudgetService.BudgetDTO> createBudget(@RequestBody Map<String, Object> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        String category = (String) request.get("category");
        Double amount = Double.parseDouble(request.get("amount").toString());
        
        budgetService.createBudget(email, category, amount);
        
        // Return the updated list and find the created budget
        List<BudgetService.BudgetDTO> budgets = budgetService.getAllBudgets(email);
        BudgetService.BudgetDTO createdBudget = budgets.stream()
            .filter(b -> b.getCategory().equals(category))
            .findFirst()
            .orElse(null);
        
        return ResponseEntity.ok(createdBudget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetService.BudgetDTO> updateBudget(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Double amount = Double.parseDouble(request.get("amount").toString());
        budgetService.updateBudget(id, email, amount);
        
        // Return the updated budget
        List<BudgetService.BudgetDTO> budgets = budgetService.getAllBudgets(email);
        BudgetService.BudgetDTO updatedBudget = budgets.stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .orElse(null);
        
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok("Deleted");
    }
}
