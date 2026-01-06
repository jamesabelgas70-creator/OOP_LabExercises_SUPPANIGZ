package com.aidsync.service;

import com.aidsync.model.ActivityLog;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing activity logs
 */
public class ActivityLogService {
    private static final List<ActivityLog> logs = new ArrayList<>();
    private static int nextId = 1;

    /**
     * Log an activity
     */
    public static void logActivity(String username, String action, String details) {
        ActivityLog log = new ActivityLog(username, action, details);
        log.setId(nextId++);
        logs.add(0, log); // Add to beginning for newest first
    }

    /**
     * Get all activity logs
     */
    public List<ActivityLog> getAllLogs() {
        return new ArrayList<>(logs);
    }

    /**
     * Get logs by username
     */
    public List<ActivityLog> getLogsByUsername(String username) {
        return logs.stream()
            .filter(log -> log.getUsername().equals(username))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Get logs by date range
     */
    public List<ActivityLog> getLogsByDateRange(LocalDateTime from, LocalDateTime to) {
        return logs.stream()
            .filter(log -> log.getTimestamp().isAfter(from) && log.getTimestamp().isBefore(to))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Clear all logs (admin only)
     */
    public void clearLogs() {
        logs.clear();
        nextId = 1;
    }
}