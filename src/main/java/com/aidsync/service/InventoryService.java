package com.aidsync.service;

import com.aidsync.dao.InventoryDAO;
import com.aidsync.dao.InventoryTransactionDAO;
import com.aidsync.model.InventoryItem;
import com.aidsync.model.InventoryTransaction;

import java.util.List;

/**
 * Service layer for Inventory operations
 */
public class InventoryService {
    private final InventoryDAO inventoryDAO;
    private final InventoryTransactionDAO transactionDAO;
    
    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
        this.transactionDAO = new InventoryTransactionDAO();
    }
    
    /**
     * Create new inventory item
     */
    public boolean createInventoryItem(InventoryItem item) {
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }
        
        if (item.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        return inventoryDAO.create(item);
    }
    
    /**
     * Update inventory item
     */
    public boolean updateInventoryItem(InventoryItem item) {
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name is required");
        }
        
        if (item.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        return inventoryDAO.update(item);
    }
    
    /**
     * Get all inventory items
     */
    public List<InventoryItem> getAllInventoryItems() {
        return inventoryDAO.getAll();
    }
    
    /**
     * Get inventory item by ID
     */
    public InventoryItem getInventoryItemById(int id) {
        return inventoryDAO.getById(id);
    }
    
    /**
     * Get low stock items
     */
    public List<InventoryItem> getLowStockItems() {
        return inventoryDAO.getLowStockItems();
    }
    
    /**
     * Restock inventory item (add quantity)
     */
    public boolean restockInventoryItem(int inventoryId, int quantityToAdd, Integer userId, String notes) {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than 0");
        }
        
        InventoryItem item = inventoryDAO.getById(inventoryId);
        if (item == null) {
            throw new IllegalArgumentException("Inventory item not found");
        }
        
        int quantityBefore = item.getQuantity();
        int quantityAfter = quantityBefore + quantityToAdd;
        
        // Update quantity
        boolean success = inventoryDAO.updateQuantity(inventoryId, quantityToAdd);
        
        if (success) {
            // Log transaction
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setInventoryId(inventoryId);
            transaction.setUserId(userId);
            transaction.setTransactionType("Restock");
            transaction.setQuantityChange(quantityToAdd);
            transaction.setQuantityBefore(quantityBefore);
            transaction.setQuantityAfter(quantityAfter);
            transaction.setNotes(notes);
            transactionDAO.create(transaction);
        }
        
        return success;
    }
    
    /**
     * Set inventory quantity (direct set, not add)
     */
    public boolean setInventoryQuantity(int inventoryId, int newQuantity, Integer userId, String notes) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        InventoryItem item = inventoryDAO.getById(inventoryId);
        if (item == null) {
            throw new IllegalArgumentException("Inventory item not found");
        }
        
        int quantityBefore = item.getQuantity();
        int quantityChange = newQuantity - quantityBefore;
        
        // Update quantity directly
        item.setQuantity(newQuantity);
        boolean success = inventoryDAO.update(item);
        
        if (success && quantityChange != 0) {
            // Log transaction only if quantity changed
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setInventoryId(inventoryId);
            transaction.setUserId(userId);
            transaction.setTransactionType("Set Quantity");
            transaction.setQuantityChange(quantityChange);
            transaction.setQuantityBefore(quantityBefore);
            transaction.setQuantityAfter(newQuantity);
            transaction.setNotes(notes);
            transactionDAO.create(transaction);
        }
        
        return success;
    }
    
    /**
     * Log inventory transaction (for distributions, void distributions, etc.)
     */
    public void logTransaction(int inventoryId, String transactionType, int quantityChange, 
                              int quantityBefore, int quantityAfter, Integer userId, 
                              String notes, Integer referenceId, String referenceType) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setInventoryId(inventoryId);
        transaction.setUserId(userId);
        transaction.setTransactionType(transactionType);
        transaction.setQuantityChange(quantityChange);
        transaction.setQuantityBefore(quantityBefore);
        transaction.setQuantityAfter(quantityAfter);
        transaction.setNotes(notes);
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        transactionDAO.create(transaction);
    }
    
    /**
     * Get transactions for an inventory item
     */
    public List<InventoryTransaction> getTransactionsByInventoryId(int inventoryId) {
        return transactionDAO.getByInventoryId(inventoryId);
    }
    
    /**
     * Get all transactions
     */
    public List<InventoryTransaction> getAllTransactions() {
        return transactionDAO.getAll();
    }
}

