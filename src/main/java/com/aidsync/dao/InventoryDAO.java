package com.aidsync.dao;

import com.aidsync.model.InventoryItem;
import com.aidsync.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Inventory operations
 */
public class InventoryDAO {
    
    /**
     * Create new inventory item
     */
    public boolean create(InventoryItem item) {
        String sql = "INSERT INTO inventory (item_name, category, quantity, unit, low_stock_threshold) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getUnit());
            pstmt.setInt(5, item.getLowStockThreshold());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating inventory item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update inventory item
     */
    public boolean update(InventoryItem item) {
        String sql = "UPDATE inventory SET item_name = ?, category = ?, quantity = ?, unit = ?, low_stock_threshold = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getCategory());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setString(4, item.getUnit());
            pstmt.setInt(5, item.getLowStockThreshold());
            pstmt.setInt(6, item.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating inventory item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all inventory items
     */
    public List<InventoryItem> getAll() {
        String sql = "SELECT * FROM inventory ORDER BY item_name";
        List<InventoryItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all inventory items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Get inventory item by ID
     */
    public InventoryItem getById(int id) {
        String sql = "SELECT * FROM inventory WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToInventoryItem(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting inventory item: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Update inventory quantity (for distribution)
     */
    public boolean updateQuantity(int id, int quantityChange) {
        String sql = "UPDATE inventory SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, quantityChange);
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating inventory quantity: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get low stock items
     */
    public List<InventoryItem> getLowStockItems() {
        String sql = "SELECT * FROM inventory WHERE quantity <= low_stock_threshold ORDER BY quantity ASC";
        List<InventoryItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting low stock items: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Map ResultSet to InventoryItem object
     */
    private InventoryItem mapResultSetToInventoryItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setId(rs.getInt("id"));
        item.setItemName(rs.getString("item_name"));
        item.setCategory(rs.getString("category"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnit(rs.getString("unit"));
        item.setLowStockThreshold(rs.getInt("low_stock_threshold"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            item.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return item;
    }
}

