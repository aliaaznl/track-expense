package com.tracker.expensetracker.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    private Double amount;
    private String description;
    private LocalDate date;
    private String receiptImage;
    private String category;
    private String type;
    private RecurringInfo recurring;

    @Data
    public static class RecurringInfo {
        private String frequency; // DAILY, WEEKLY, MONTHLY, YEARLY
        private LocalDate endDate;
    }
}
