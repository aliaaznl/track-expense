package com.tracker.expensetracker.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String colour; 
    private String icon;
    
    // Many categories belong to One User (nullable for system categories)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    // Flag to indicate if this is a system/pre-defined category
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;
}
