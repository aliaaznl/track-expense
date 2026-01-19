package com.tracker.expensetracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String description;

    private LocalDate date;

    private String receiptImage;

    // Many transactions belong to One User.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "category")
    private String category;

    @Column(name = "type")
    private String type; // Will store "EXPENSE" or "INCOME"

    // Recurring transaction fields
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "recurring_frequency")
    private String recurringFrequency; // DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(name = "recurring_end_date")
    private LocalDate recurringEndDate;

    @Column(name = "parent_recurring_id")
    private Long parentRecurringId; // Links recurring transactions together
}
