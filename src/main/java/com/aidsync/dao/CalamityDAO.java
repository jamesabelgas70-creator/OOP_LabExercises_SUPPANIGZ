package com.aidsync.dao;

import com.aidsync.model.Calamity;
import com.aidsync.model.CalamityItem;
import com.aidsync.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Calamity operations
 */
public class CalamityDAO {
    
    /**
     * Create new calamity
     */
    public boolean createCalamity(Calamity calamity) {
        String sql = "INSERT INTO calamities (name, description, status) VALUES (?, ?, ?)";
        
        System.out.println("Creating calamity: " + calamity.getName());
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, calamity.getName());
            pstmt.setString(2, calamity.getDescription());
            pstmt.setString(3, calamity.getStatus());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                // Get generated ID
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    calamity.setId(rs.getInt(1));
                    System.out.println("Generated calamity ID: " + calamity.getId());
                    // Insert calamity items
                    insertCalamityItems(calamity);
                    System.out.println("Calamity created successfully");
                    return true;
                }
            }
            System.out.println("Failed to create calamity - no rows affected");
            return false;
        } catch (SQLException e) {
            System.err.println("Error creating calamity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get calamity by ID
     */
    public Calamity getCalamityById(int id) {
        String sql = "SELECT * FROM calamities WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Calamity calamity = mapResultSetToCalamity(rs);
                // Load items
                calamity.setItems(getCalamityItems(calamity.getId()));
                return calamity;
            }
        } catch (SQLException e) {
            System.err.println("Error getting calamity: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all calamities
     */
    public List<Calamity> getAllCalamities() {
        String sql = "SELECT * FROM calamities ORDER BY name";
        List<Calamity> calamities = new ArrayList<>();
        
        System.out.println("Loading all calamities from database...");
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Calamity calamity = mapResultSetToCalamity(rs);
                calamities.add(calamity);
                System.out.println("Loaded calamity: " + calamity.getName() + " (ID: " + calamity.getId() + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error getting all calamities: " + e.getMessage());
        }
        
        // Load items for each calamity separately to avoid connection conflicts
        for (Calamity calamity : calamities) {
            calamity.setItems(getCalamityItems(calamity.getId()));
        }
        
        System.out.println("Total calamities loaded: " + calamities.size());
        return calamities;
    }
    
    /**
     * Get active calamities only
     */
    public List<Calamity> getActiveCalamities() {
        // First, let's see all calamities in the database
        String debugSql = "SELECT id, name, status FROM calamities ORDER BY name";
        System.out.println("DEBUG: All calamities in database:");
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(debugSql)) {
            
            while (rs.next()) {
                System.out.println("DEBUG: - ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Status: '" + rs.getString("status") + "'");
            }
        } catch (SQLException e) {
            System.err.println("Error in debug query: " + e.getMessage());
        }
        
        String sql = "SELECT * FROM calamities WHERE status = 'Active' ORDER BY name";
        List<Calamity> calamities = new ArrayList<>();
        
        System.out.println("DEBUG: Querying active calamities with SQL: " + sql);
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Calamity calamity = mapResultSetToCalamity(rs);
                // Don't load items for dropdown - they're not needed and may cause issues
                calamities.add(calamity);
                System.out.println("DEBUG: Found active calamity: " + calamity.getName() + " (ID: " + calamity.getId() + ", Status: " + calamity.getStatus() + ")");
            }
        } catch (SQLException e) {
            System.err.println("Error getting active calamities: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("DEBUG: Total active calamities found: " + calamities.size());
        return calamities;
    }
    
    /**
     * Update calamity
     */
    public boolean updateCalamity(Calamity calamity) {
        String sql = "UPDATE calamities SET name = ?, description = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, calamity.getName());
            pstmt.setString(2, calamity.getDescription());
            pstmt.setString(3, calamity.getStatus());
            pstmt.setInt(4, calamity.getId());
            
            if (pstmt.executeUpdate() > 0) {
                // Update items: delete old and insert new
                deleteCalamityItems(calamity.getId());
                insertCalamityItems(calamity);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating calamity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete calamity (only if no distributions reference it)
     */
    public boolean deleteCalamity(int id) {
        // Check if calamity is used in distributions
        String checkSql = "SELECT COUNT(*) FROM distributions WHERE calamity_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Calamity is in use, cannot delete
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking calamity usage: " + e.getMessage());
            return false;
        }
        
        // Delete calamity items first
        deleteCalamityItems(id);
        
        // Delete calamity
        String sql = "DELETE FROM calamities WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting calamity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if calamity name exists
     */
    public boolean calamityNameExists(String name) {
        return calamityNameExists(name, null);
    }
    
    /**
     * Check if calamity name exists (excluding a specific calamity ID)
     */
    public boolean calamityNameExists(String name, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM calamities WHERE name = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking calamity name: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Get calamity items for a calamity
     */
    private List<CalamityItem> getCalamityItems(int calamityId) {
        String sql = "SELECT * FROM calamity_items WHERE calamity_id = ?";
        List<CalamityItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, calamityId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CalamityItem item = new CalamityItem();
                    item.setId(rs.getInt("id"));
                    item.setCalamityId(rs.getInt("calamity_id"));
                    item.setInventoryId(rs.getInt("inventory_id"));
                    item.setStandardQuantity(rs.getInt("standard_quantity"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting calamity items: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Insert calamity items
     */
    private void insertCalamityItems(Calamity calamity) throws SQLException {
        if (calamity.getItems() == null || calamity.getItems().isEmpty()) {
            return;
        }
        
        String sql = "INSERT INTO calamity_items (calamity_id, inventory_id, standard_quantity) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (CalamityItem item : calamity.getItems()) {
                pstmt.setInt(1, calamity.getId());
                pstmt.setInt(2, item.getInventoryId());
                pstmt.setInt(3, item.getStandardQuantity());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        }
    }
    
    /**
     * Delete calamity items
     */
    private void deleteCalamityItems(int calamityId) {
        String sql = "DELETE FROM calamity_items WHERE calamity_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, calamityId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting calamity items: " + e.getMessage());
        }
    }
    
    /**
     * Map ResultSet to Calamity object
     */
    private Calamity mapResultSetToCalamity(ResultSet rs) throws SQLException {
        Calamity calamity = new Calamity();
        calamity.setId(rs.getInt("id"));
        calamity.setName(rs.getString("name"));
        calamity.setDescription(rs.getString("description"));
        calamity.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            calamity.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return calamity;
    }
}

