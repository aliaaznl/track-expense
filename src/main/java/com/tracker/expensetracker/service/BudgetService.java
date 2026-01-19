package com.tracker.expensetracker.service;

import com.tracker.expensetracker.entity.Budget;
import com.tracker.expensetracker.entity.Transaction;
import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.repository.BudgetRepository;
import com.tracker.expensetracker.repository.TransactionRepository;
import com.tracker.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Budget createBudget(String email, String category, Double amount) {
        User user = userRepository.findByEmail(email).orElseThrow();
        
        // Check if budget already exists for this category
        Optional<Budget> existingBudget = budgetRepository.findByUserAndCategory(user, category);
        if (existingBudget.isPresent()) {
            // Update existing budget
            Budget budget = existingBudget.get();
            budget.setAmount(amount);
            budget.setCreatedDate(LocalDate.now());
            return budgetRepository.save(budget);
        }
        
        // Create new budget
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(category);
        budget.setAmount(amount);
        budget.setCreatedDate(LocalDate.now());
        budget.setPeriodStart(LocalDate.now());
        budget.setPeriodEnd(LocalDate.now().plusMonths(1)); // Default to monthly
        
        return budgetRepository.save(budget);
    }

    public List<BudgetDTO> getAllBudgets(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Budget> budgets = budgetRepository.findByUser(user);
        List<Transaction> expenses = transactionRepository.findByUserAndType(user, "EXPENSE");
        
        return budgets.stream().map(budget -> {
            BudgetDTO dto = new BudgetDTO();
            dto.setId(budget.getId());
            dto.setCategory(budget.getCategory());
            dto.setAmount(budget.getAmount());
            dto.setCreatedDate(budget.getCreatedDate());
            
            // Calculate spending for this category
            double spent = expenses.stream()
                .filter(t -> t.getCategory().equals(budget.getCategory()))
                .filter(t -> t.getDate().isAfter(budget.getPeriodStart().minusDays(1)) && 
                            (budget.getPeriodEnd() == null || t.getDate().isBefore(budget.getPeriodEnd().plusDays(1))))
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            dto.setSpent(spent);
            dto.setRemaining(budget.getAmount() - spent);
            dto.setItemCount((int) expenses.stream()
                .filter(t -> t.getCategory().equals(budget.getCategory()))
                .filter(t -> t.getDate().isAfter(budget.getPeriodStart().minusDays(1)) && 
                            (budget.getPeriodEnd() == null || t.getDate().isBefore(budget.getPeriodEnd().plusDays(1))))
                .count());
            
            return dto;
        }).collect(Collectors.toList());
    }

    public Budget updateBudget(Long id, String email, Double amount) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        // Verify the budget belongs to the user
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Budget does not belong to user");
        }
        
        budget.setAmount(amount);
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }

    // DTO for budget with calculated spending
    public static class BudgetDTO {
        private Long id;
        private String category;
        private Double amount;
        private Double spent;
        private Double remaining;
        private Integer itemCount;
        private LocalDate createdDate;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public Double getSpent() { return spent; }
        public void setSpent(Double spent) { this.spent = spent; }
        
        public Double getRemaining() { return remaining; }
        public void setRemaining(Double remaining) { this.remaining = remaining; }
        
        public Integer getItemCount() { return itemCount; }
        public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
        
        public LocalDate getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    }
}
