package com.aidsync.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Calamity/Event model representing different types of disasters or events
 */
public class Calamity {
    private int id;
    private String name;
    private String description;
    private String status; // "Active" or "Inactive"
    private LocalDateTime createdAt;
    private List<CalamityItem> items;

    public Calamity() {
        this.items = new ArrayList<>();
        this.status = "Active";
    }

    public Calamity(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<CalamityItem> getItems() {
        return items;
    }

    public void setItems(List<CalamityItem> items) {
        this.items = items;
    }

    public void addItem(CalamityItem item) {
        this.items.add(item);
    }

    public boolean isActive() {
        return "Active".equals(status);
    }
}

