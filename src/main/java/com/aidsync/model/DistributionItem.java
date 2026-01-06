package com.aidsync.model;

/**
 * Distribution item model (many-to-many relationship)
 */
public class DistributionItem {
    private int id;
    private int distributionId;
    private int inventoryId;
    private InventoryItem inventoryItem;
    private int quantity;

    public DistributionItem() {
    }

    public DistributionItem(int inventoryId, int quantity) {
        this.inventoryId = inventoryId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(int distributionId) {
        this.distributionId = distributionId;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
        if (inventoryItem != null) {
            this.inventoryId = inventoryItem.getId();
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

