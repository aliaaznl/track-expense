package com.tracker.expensetracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
@Data
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount")
    private Double amount; // Budget limit

    @Column(name = "category")
    private String category; // Category name

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "period_start")
    private LocalDate periodStart; // Budget period start

    @Column(name = "period_end")
    private LocalDate periodEnd; // Budget period end (optional, for monthly/yearly budgets)
}
