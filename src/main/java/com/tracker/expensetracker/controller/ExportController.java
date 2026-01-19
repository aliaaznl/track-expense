package com.tracker.expensetracker.controller;

import com.tracker.expensetracker.entity.Export;
import com.tracker.expensetracker.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @PostMapping("/save")
    public ResponseEntity<?> saveExport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename,
            @RequestParam("format") String format,
            @RequestParam("transactionCount") Integer transactionCount,
            @RequestParam("filters") String filtersJson) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            byte[] fileData = file.getBytes();
            Export export = exportService.saveExport(email, filename, format, fileData, 
                    transactionCount, filtersJson);

            Map<String, Object> response = new HashMap<>();
            response.put("id", export.getId());
            response.put("filename", export.getFilename());
            response.put("format", export.getFormat());
            response.put("exportedAt", export.getExportedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save export: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserExports() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            List<Export> exports = exportService.getUserExports(email);
            List<Map<String, Object>> exportList = exports.stream().map(exp -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", exp.getId());
                map.put("filename", exp.getFilename());
                map.put("format", exp.getFormat());
                map.put("transactionCount", exp.getTransactionCount());
                map.put("exportedAt", exp.getExportedAt());
                map.put("filters", exp.getFilters());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(exportList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadExport(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            Export export = exportService.getExportById(id, email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(export.getFormat().equalsIgnoreCase("PDF") 
                    ? MediaType.APPLICATION_PDF 
                    : MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDispositionFormData("attachment", export.getFilename());
            headers.setContentLength(export.getFileSize());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(export.getFileData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExport(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            exportService.deleteExport(id, email);
            return ResponseEntity.ok(Map.of("message", "Export deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
