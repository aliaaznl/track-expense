package com.tracker.expensetracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "exports")
public class Export {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "format", nullable = false)
    private String format; // PDF or EXCEL

    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "transaction_count")
    private Integer transactionCount;

    @Column(name = "filters", columnDefinition = "TEXT")
    private String filters; // JSON string of filter information

    @Column(name = "exported_at", nullable = false)
    private LocalDateTime exportedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
