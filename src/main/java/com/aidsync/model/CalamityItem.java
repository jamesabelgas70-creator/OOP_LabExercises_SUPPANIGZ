package com.aidsync.model;

/**
 * CalamityItem model representing an item assigned to a calamity with standard quantity
 */
public class CalamityItem {
    private int id;
    private int calamityId;
    private int inventoryId;
    private int standardQuantity; // Standard quantity for this item in this calamity
    private InventoryItem inventoryItem; // Reference to actual inventory item

    public CalamityItem() {
    }

    public CalamityItem(int calamityId, int inventoryId, int standardQuantity) {
        this.calamityId = calamityId;
        this.inventoryId = inventoryId;
        this.standardQuantity = standardQuantity;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCalamityId() {
        return calamityId;
    }

    public void setCalamityId(int calamityId) {
        this.calamityId = calamityId;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getStandardQuantity() {
        return standardQuantity;
    }

    public void setStandardQuantity(int standardQuantity) {
        this.standardQuantity = standardQuantity;
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
}

