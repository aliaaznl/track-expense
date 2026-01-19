package com.tracker.expensetracker.repository;

import com.tracker.expensetracker.entity.Export;
import com.tracker.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExportRepository extends JpaRepository<Export, Long> {
    List<Export> findByUserOrderByExportedAtDesc(User user);
    void deleteByUser(User user);
}
