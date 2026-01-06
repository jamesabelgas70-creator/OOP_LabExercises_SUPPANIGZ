package com.aidsync.dao;

import com.aidsync.model.Distribution;
import com.aidsync.model.DistributionItem;
import com.aidsync.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Distribution operations
 */
public class DistributionDAO {
    
    /**
     * Create new distribution with items
     */
    public boolean create(Distribution distribution) {
        String sql = "INSERT INTO distributions (beneficiary_id, calamity_id, distribution_date, distributed_by, notes) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, distribution.getBeneficiaryId());
                
                if (distribution.getCalamityId() != null) {
                    pstmt.setInt(2, distribution.getCalamityId());
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                
                if (distribution.getDistributionDate() != null) {
                    pstmt.setTimestamp(3, Timestamp.valueOf(distribution.getDistributionDate()));
                } else {
                    pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                }
                
                pstmt.setInt(4, distribution.getDistributedBy());
                pstmt.setString(5, distribution.getNotes());
                
                pstmt.executeUpdate();
                
                // Get generated distribution ID
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int distributionId = rs.getInt(1);
                    distribution.setId(distributionId);
                    
                    // Insert distribution items
                    if (!distribution.getItems().isEmpty()) {
                        insertDistributionItems(conn, distributionId, distribution.getItems());
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error creating distribution: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Insert distribution items
     */
    private void insertDistributionItems(Connection conn, int distributionId, List<DistributionItem> items) throws SQLException {
        String sql = "INSERT INTO distribution_items (distribution_id, inventory_id, quantity) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (DistributionItem item : items) {
                pstmt.setInt(1, distributionId);
                pstmt.setInt(2, item.getInventoryId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    /**
     * Get distribution by ID
     */
    public Distribution getById(int id) {
        String sql = "SELECT d.*, b.beneficiary_id, b.full_name, u.username, c.name as calamity_name " +
            "FROM distributions d " +
            "LEFT JOIN beneficiaries b ON d.beneficiary_id = b.id " +
            "LEFT JOIN users u ON d.distributed_by = u.id " +
            "LEFT JOIN calamities c ON d.calamity_id = c.id " +
            "WHERE d.id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Distribution distribution = mapResultSetToDistribution(rs);
                
                // Load distribution items
                distribution.setItems(getDistributionItems(conn, id));
                
                return distribution;
            }
        } catch (SQLException e) {
            System.err.println("Error getting distribution: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all distributions
     */
    public List<Distribution> getAll() {
        String sql = "SELECT d.*, b.beneficiary_id, b.full_name, u.username, c.name as calamity_name " +
            "FROM distributions d " +
            "LEFT JOIN beneficiaries b ON d.beneficiary_id = b.id " +
            "LEFT JOIN users u ON d.distributed_by = u.id " +
            "LEFT JOIN calamities c ON d.calamity_id = c.id " +
            "ORDER BY d.distribution_date DESC";
        
        List<Distribution> distributions = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Distribution distribution = mapResultSetToDistribution(rs);
                distribution.setItems(getDistributionItems(conn, distribution.getId()));
                distributions.add(distribution);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all distributions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return distributions;
    }
    
    /**
     * Get distributions by beneficiary ID
     */
    public List<Distribution> getByBeneficiaryId(int beneficiaryId) {
        String sql = "SELECT d.*, b.beneficiary_id, b.full_name, u.username, c.name as calamity_name " +
            "FROM distributions d " +
            "LEFT JOIN beneficiaries b ON d.beneficiary_id = b.id " +
            "LEFT JOIN users u ON d.distributed_by = u.id " +
            "LEFT JOIN calamities c ON d.calamity_id = c.id " +
            "WHERE d.beneficiary_id = ? " +
            "ORDER BY d.distribution_date DESC";
        
        List<Distribution> distributions = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, beneficiaryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Distribution distribution = mapResultSetToDistribution(rs);
                distribution.setItems(getDistributionItems(conn, distribution.getId()));
                distributions.add(distribution);
            }
        } catch (SQLException e) {
            System.err.println("Error getting distributions by beneficiary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return distributions;
    }
    
    /**
     * Get distribution items for a distribution
     */
    private List<DistributionItem> getDistributionItems(Connection conn, int distributionId) throws SQLException {
        String sql = "SELECT di.*, i.item_name, i.unit " +
            "FROM distribution_items di " +
            "LEFT JOIN inventory i ON di.inventory_id = i.id " +
            "WHERE di.distribution_id = ?";
        
        List<DistributionItem> items = new ArrayList<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, distributionId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DistributionItem item = new DistributionItem();
                item.setId(rs.getInt("id"));
                item.setDistributionId(rs.getInt("distribution_id"));
                item.setInventoryId(rs.getInt("inventory_id"));
                item.setQuantity(rs.getInt("quantity"));
                items.add(item);
            }
        }
        
        return items;
    }
    
    /**
     * Delete (void) a distribution and return its items for inventory restoration
     */
    public List<DistributionItem> deleteDistribution(int distributionId) {
        List<DistributionItem> items = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Get distribution items before deletion
                items = getDistributionItems(conn, distributionId);
                
                // Delete distribution items
                String deleteItemsSql = "DELETE FROM distribution_items WHERE distribution_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteItemsSql)) {
                    pstmt.setInt(1, distributionId);
                    pstmt.executeUpdate();
                }
                
                // Delete distribution
                String deleteDistSql = "DELETE FROM distributions WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteDistSql)) {
                    pstmt.setInt(1, distributionId);
                    pstmt.executeUpdate();
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting distribution: " + e.getMessage());
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Get distribution statistics for a beneficiary
     * Returns: count, last distribution date, total items received
     */
    public DistributionStats getDistributionStats(int beneficiaryId) {
        String sql = "SELECT " +
            "COUNT(d.id) as dist_count, " +
            "MAX(d.distribution_date) as last_dist_date, " +
            "COALESCE(SUM(di.quantity), 0) as total_items " +
            "FROM distributions d " +
            "LEFT JOIN distribution_items di ON d.id = di.distribution_id " +
            "WHERE d.beneficiary_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, beneficiaryId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                DistributionStats stats = new DistributionStats();
                stats.setDistributionCount(rs.getInt("dist_count"));
                
                Timestamp lastDate = rs.getTimestamp("last_dist_date");
                if (lastDate != null) {
                    stats.setLastDistributionDate(lastDate.toLocalDateTime());
                }
                
                stats.setTotalItemsReceived(rs.getInt("total_items"));
                return stats;
            }
        } catch (SQLException e) {
            System.err.println("Error getting distribution stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return new DistributionStats();
    }
    
    /**
     * Distribution statistics helper class
     */
    public static class DistributionStats {
        private int distributionCount;
        private LocalDateTime lastDistributionDate;
        private int totalItemsReceived;
        
        public DistributionStats() {
            this.distributionCount = 0;
            this.totalItemsReceived = 0;
        }
        
        public int getDistributionCount() {
            return distributionCount;
        }
        
        public void setDistributionCount(int distributionCount) {
            this.distributionCount = distributionCount;
        }
        
        public LocalDateTime getLastDistributionDate() {
            return lastDistributionDate;
        }
        
        public void setLastDistributionDate(LocalDateTime lastDistributionDate) {
            this.lastDistributionDate = lastDistributionDate;
        }
        
        public int getTotalItemsReceived() {
            return totalItemsReceived;
        }
        
        public void setTotalItemsReceived(int totalItemsReceived) {
            this.totalItemsReceived = totalItemsReceived;
        }
    }
    
    /**
     * Map ResultSet to Distribution object
     */
    private Distribution mapResultSetToDistribution(ResultSet rs) throws SQLException {
        Distribution distribution = new Distribution();
        distribution.setId(rs.getInt("id"));
        distribution.setBeneficiaryId(rs.getInt("beneficiary_id"));
        distribution.setDistributedBy(rs.getInt("distributed_by"));
        distribution.setNotes(rs.getString("notes"));
        
        // Handle calamity_id (can be null)
        int calamityId = rs.getInt("calamity_id");
        if (!rs.wasNull()) {
            distribution.setCalamityId(calamityId);
        }
        
        Timestamp distributionDate = rs.getTimestamp("distribution_date");
        if (distributionDate != null) {
            distribution.setDistributionDate(distributionDate.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            distribution.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return distribution;
    }
}

