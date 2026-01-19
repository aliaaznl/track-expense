package com.tracker.expensetracker.repository;

import com.tracker.expensetracker.entity.Category;
import com.tracker.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository <Category, Long> {
    Category findByName(String name);
    List<Category> findByUser(User user);
    List<Category> findByIsSystemTrue(); // Get all system categories
    Optional<Category> findByNameAndUser(String name, User user);
    Optional<Category> findByNameAndIsSystemTrue(String name); // Check if system category exists
    void deleteByUser(User user);
}
