package com.aidsync.model;

import java.time.LocalDateTime;

/**
 * Model representing inventory transaction/audit trail
 */
public class InventoryTransaction {
    private int id;
    private int inventoryId;
    private Integer userId;
    private String transactionType; // "Restock", "Set Quantity", "Distribution", "Void Distribution"
    private int quantityChange; // Positive for restock, negative for distribution
    private int quantityBefore;
    private int quantityAfter;
    private String notes;
    private Integer referenceId; // ID of related entity (e.g., distribution ID)
    private String referenceType; // Type of related entity (e.g., "Distribution")
    private LocalDateTime createdAt;
    
    // For display purposes
    private String inventoryItemName;
    private String userName;
    
    public InventoryTransaction() {}
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getInventoryId() {
        return inventoryId;
    }
    
    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public int getQuantityChange() {
        return quantityChange;
    }
    
    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }
    
    public int getQuantityBefore() {
        return quantityBefore;
    }
    
    public void setQuantityBefore(int quantityBefore) {
        this.quantityBefore = quantityBefore;
    }
    
    public int getQuantityAfter() {
        return quantityAfter;
    }
    
    public void setQuantityAfter(int quantityAfter) {
        this.quantityAfter = quantityAfter;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Integer getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }
    
    public String getReferenceType() {
        return referenceType;
    }
    
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getInventoryItemName() {
        return inventoryItemName;
    }
    
    public void setInventoryItemName(String inventoryItemName) {
        this.inventoryItemName = inventoryItemName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
}

