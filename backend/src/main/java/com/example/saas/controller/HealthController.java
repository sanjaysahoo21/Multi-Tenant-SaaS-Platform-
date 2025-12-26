package com.example.saas.controller;

import com.example.saas.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");

        // Check database connection
        try {
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection()) {
                    if (conn.isValid(2)) {
                        response.put("database", "connected");
                    } else {
                        response.put("database", "disconnected");
                    }
                }
            } else {
                response.put("database", "unknown");
            }
        } catch (Exception e) {
            response.put("database", "disconnected");
            response.put("databaseError", e.getMessage());
        }

        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
