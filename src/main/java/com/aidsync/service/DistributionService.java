package com.aidsync.service;

import com.aidsync.dao.DistributionDAO;
import com.aidsync.dao.InventoryDAO;
import com.aidsync.model.Distribution;
import com.aidsync.model.DistributionItem;
import com.aidsync.model.InventoryItem;

import java.util.List;

/**
 * Service layer for Distribution operations
 */
public class DistributionService {
    private final DistributionDAO distributionDAO;
    private final InventoryDAO inventoryDAO;
    private final InventoryService inventoryService;
    
    public DistributionService() {
        this.distributionDAO = new DistributionDAO();
        this.inventoryDAO = new InventoryDAO();
        this.inventoryService = new InventoryService();
    }
    
    /**
     * Create new distribution
     */
    public boolean createDistribution(Distribution distribution) {
        if (distribution.getBeneficiaryId() <= 0) {
            throw new IllegalArgumentException("Beneficiary is required");
        }
        
        if (distribution.getItems() == null || distribution.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item must be distributed");
        }
        
        // Validate and update inventory
        for (DistributionItem item : distribution.getItems()) {
            InventoryItem inventoryItem = inventoryDAO.getById(item.getInventoryId());
            if (inventoryItem == null) {
                throw new IllegalArgumentException("Inventory item not found: " + item.getInventoryId());
            }
            
            if (inventoryItem.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for " + inventoryItem.getItemName() + 
                    ". Available: " + inventoryItem.getQuantity() + ", Requested: " + item.getQuantity());
            }
        }
        
        // Create distribution
        boolean success = distributionDAO.create(distribution);
        
        if (success) {
            // Update inventory quantities and log transactions
            for (DistributionItem item : distribution.getItems()) {
                InventoryItem invItem = inventoryDAO.getById(item.getInventoryId());
                if (invItem != null) {
                    int quantityBefore = invItem.getQuantity();
                    inventoryDAO.updateQuantity(item.getInventoryId(), -item.getQuantity());
                    int quantityAfter = quantityBefore - item.getQuantity();
                    
                    // Log transaction
                    inventoryService.logTransaction(
                        item.getInventoryId(),
                        "Distribution",
                        -item.getQuantity(),
                        quantityBefore,
                        quantityAfter,
                        distribution.getDistributedBy(),
                        "Distribution to beneficiary ID: " + distribution.getBeneficiaryId(),
                        distribution.getId(),
                        "Distribution"
                    );
                }
            }
        }
        
        return success;
    }
    
    /**
     * Get distribution by ID
     */
    public Distribution getDistributionById(int id) {
        return distributionDAO.getById(id);
    }
    
    /**
     * Get all distributions
     */
    public List<Distribution> getAllDistributions() {
        return distributionDAO.getAll();
    }
    
    /**
     * Get distributions by beneficiary ID
     */
    public List<Distribution> getDistributionsByBeneficiary(int beneficiaryId) {
        return distributionDAO.getByBeneficiaryId(beneficiaryId);
    }
    
    /**
     * Get distribution statistics for a beneficiary
     */
    public DistributionDAO.DistributionStats getDistributionStats(int beneficiaryId) {
        return distributionDAO.getDistributionStats(beneficiaryId);
    }
    
    /**
     * Void (delete) a distribution and restore inventory
     */
    public boolean voidDistribution(int distributionId) {
        Distribution distribution = distributionDAO.getById(distributionId);
        if (distribution == null) {
            throw new IllegalArgumentException("Distribution not found");
        }
        
        // Get distribution items before deletion
        List<DistributionItem> items = distributionDAO.deleteDistribution(distributionId);
        
        if (items != null && !items.isEmpty()) {
            // Restore inventory quantities and log transactions
            for (DistributionItem item : items) {
                InventoryItem invItem = inventoryDAO.getById(item.getInventoryId());
                if (invItem != null) {
                    int quantityBefore = invItem.getQuantity();
                    inventoryDAO.updateQuantity(item.getInventoryId(), item.getQuantity());
                    int quantityAfter = quantityBefore + item.getQuantity();
                    
                    // Log transaction
                    inventoryService.logTransaction(
                        item.getInventoryId(),
                        "Void Distribution",
                        item.getQuantity(),
                        quantityBefore,
                        quantityAfter,
                        null, // User ID not available in void operation
                        "Distribution voided - ID: " + distributionId,
                        distributionId,
                        "Distribution"
                    );
                }
            }
            return true;
        }
        
        return false;
    }
}

