package com.tracker.expensetracker.service;

import com.tracker.expensetracker.entity.Category;
import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.repository.CategoryRepository;
import com.tracker.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<Category> getAllCategories(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get system categories (available to all users)
        List<Category> systemCategories = categoryRepository.findByIsSystemTrue();
        
        // Get user's custom categories
        List<Category> userCategories = categoryRepository.findByUser(user);
        
        // Combine both lists
        List<Category> allCategories = new java.util.ArrayList<>();
        allCategories.addAll(systemCategories);
        allCategories.addAll(userCategories);
        
        return allCategories;
    }

    public Category createCategory(Category category, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if a system category with the same name exists
        Optional<Category> systemCategory = categoryRepository.findByNameAndIsSystemTrue(category.getName());
        if (systemCategory.isPresent()) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists as a system category");
        }
        
        // Check if category with same name already exists for this user
        Optional<Category> existing = categoryRepository.findByNameAndUser(category.getName(), user);
        if (existing.isPresent()) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        
        // Validate required fields
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }
        
        // Set default values if not provided
        if (category.getIcon() == null || category.getIcon().trim().isEmpty()) {
            category.setIcon("fa-circle-question");
        }
        if (category.getColour() == null || category.getColour().trim().isEmpty()) {
            category.setColour("#52525b");
        }
        
        // Associate category with user and mark as non-system
        category.setUser(user);
        category.setIsSystem(false);
        
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category existing = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // System categories cannot be updated by users
        if (existing.getIsSystem() != null && existing.getIsSystem()) {
            throw new RuntimeException("System categories cannot be modified");
        }
        
        // Verify the category belongs to the user
        if (existing.getUser() == null || !existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Category does not belong to user");
        }
        
        // Check if a system category with the same name exists
        Optional<Category> systemCategory = categoryRepository.findByNameAndIsSystemTrue(category.getName());
        if (systemCategory.isPresent()) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists as a system category");
        }
        
        // Check if another category with the same name exists for this user (excluding current one)
        Optional<Category> duplicate = categoryRepository.findByNameAndUser(category.getName(), user);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        
        // Validate required fields
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }
        
        // Update fields
        existing.setName(category.getName());
        if (category.getIcon() != null && !category.getIcon().trim().isEmpty()) {
            existing.setIcon(category.getIcon());
        }
        if (category.getColour() != null && !category.getColour().trim().isEmpty()) {
            existing.setColour(category.getColour());
        }
        
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long id, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // System categories cannot be deleted by users
        if (category.getIsSystem() != null && category.getIsSystem()) {
            throw new RuntimeException("System categories cannot be deleted");
        }
        
        // Verify the category belongs to the user
        if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Category does not belong to user");
        }
        
        categoryRepository.deleteById(id);
    }

    public Category getCategoryById(Long id, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        // System categories are available to all users
        // User categories are only available to the owner
        if (category.getIsSystem() == null || !category.getIsSystem()) {
            // It's a user category, verify ownership
            if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized: Category does not belong to user");
            }
        }
        
        return category;
    }
}
