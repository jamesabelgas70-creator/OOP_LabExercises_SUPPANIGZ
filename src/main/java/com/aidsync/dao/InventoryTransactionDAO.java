package com.aidsync.dao;

import com.aidsync.model.InventoryTransaction;
import com.aidsync.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Inventory Transaction operations
 */
public class InventoryTransactionDAO {
    
    /**
     * Create a new inventory transaction
     */
    public boolean create(InventoryTransaction transaction) {
        String sql = "INSERT INTO inventory_transactions " +
            "(inventory_id, user_id, transaction_type, quantity_change, quantity_before, quantity_after, notes, reference_id, reference_type) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, transaction.getInventoryId());
            if (transaction.getUserId() != null) {
                pstmt.setInt(2, transaction.getUserId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, transaction.getTransactionType());
            pstmt.setInt(4, transaction.getQuantityChange());
            pstmt.setInt(5, transaction.getQuantityBefore());
            pstmt.setInt(6, transaction.getQuantityAfter());
            pstmt.setString(7, transaction.getNotes());
            if (transaction.getReferenceId() != null) {
                pstmt.setInt(8, transaction.getReferenceId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            pstmt.setString(9, transaction.getReferenceType());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating inventory transaction: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all transactions for an inventory item
     */
    public List<InventoryTransaction> getByInventoryId(int inventoryId) {
        String sql = "SELECT t.*, i.item_name, " +
            "COALESCE(NULLIF(u.full_name, ''), u.username) as user_display_name " +
            "FROM inventory_transactions t " +
            "LEFT JOIN inventory i ON t.inventory_id = i.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "WHERE t.inventory_id = ? " +
            "ORDER BY t.created_at DESC";
        
        List<InventoryTransaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, inventoryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting inventory transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    /**
     * Get all transactions
     */
    public List<InventoryTransaction> getAll() {
        String sql = "SELECT t.*, i.item_name, " +
            "COALESCE(NULLIF(u.full_name, ''), u.username) as user_display_name " +
            "FROM inventory_transactions t " +
            "LEFT JOIN inventory i ON t.inventory_id = i.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "ORDER BY t.created_at DESC";
        
        List<InventoryTransaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all inventory transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    /**
     * Map ResultSet to InventoryTransaction object
     */
    private InventoryTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setId(rs.getInt("id"));
        transaction.setInventoryId(rs.getInt("inventory_id"));
        
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            transaction.setUserId(userId);
        }
        
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setQuantityChange(rs.getInt("quantity_change"));
        transaction.setQuantityBefore(rs.getInt("quantity_before"));
        transaction.setQuantityAfter(rs.getInt("quantity_after"));
        transaction.setNotes(rs.getString("notes"));
        
        int referenceId = rs.getInt("reference_id");
        if (!rs.wasNull()) {
            transaction.setReferenceId(referenceId);
        }
        
        transaction.setReferenceType(rs.getString("reference_type"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Display fields
        transaction.setInventoryItemName(rs.getString("item_name"));
        String userDisplayName = rs.getString("user_display_name");
        transaction.setUserName(userDisplayName); // Will be null if user_id is null or user doesn't exist
        
        return transaction;
    }
}

