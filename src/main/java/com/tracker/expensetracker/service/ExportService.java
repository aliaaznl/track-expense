package com.tracker.expensetracker.service;

import com.tracker.expensetracker.entity.Export;
import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.repository.ExportRepository;
import com.tracker.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExportService {

    @Autowired
    private ExportRepository exportRepository;

    @Autowired
    private UserRepository userRepository;

    public Export saveExport(String email, String filename, String format, byte[] fileData, 
                            Integer transactionCount, String filtersJson) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Export export = new Export();
        export.setFilename(filename);
        export.setFormat(format);
        export.setFileData(fileData);
        export.setFileSize((long) fileData.length);
        export.setTransactionCount(transactionCount);
        export.setFilters(filtersJson);
        export.setExportedAt(LocalDateTime.now());
        export.setUser(user);

        return exportRepository.save(export);
    }

    public List<Export> getUserExports(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return exportRepository.findByUserOrderByExportedAtDesc(user);
    }

    public Export getExportById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Export> export = exportRepository.findById(id);
        if (export.isPresent() && export.get().getUser().getId().equals(user.getId())) {
            return export.get();
        }
        throw new RuntimeException("Export not found or access denied");
    }

    @Transactional
    public void deleteExport(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Export> export = exportRepository.findById(id);
        if (export.isPresent() && export.get().getUser().getId().equals(user.getId())) {
            exportRepository.delete(export.get());
        } else {
            throw new RuntimeException("Export not found or access denied");
        }
    }
}
