package com.aidsync.dao;

import com.aidsync.model.Beneficiary;
import com.aidsync.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Beneficiary operations
 */
public class BeneficiaryDAO {
    
    /**
     * Create new beneficiary
     */
    public boolean create(Beneficiary beneficiary) {
        String sql = "INSERT INTO beneficiaries (" +
            "beneficiary_id, full_name, birth_date, gender, contact_number, " +
            "barangay, purok, street_address, family_size, is_household_head, " +
            "is_pwd, is_senior_citizen, is_pregnant, is_solo_parent, status" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, beneficiary.getBeneficiaryId());
            pstmt.setString(2, beneficiary.getFullName());
            
            if (beneficiary.getBirthDate() != null) {
                pstmt.setDate(3, Date.valueOf(beneficiary.getBirthDate()));
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            
            pstmt.setString(4, beneficiary.getGender());
            pstmt.setString(5, beneficiary.getContactNumber());
            pstmt.setString(6, beneficiary.getBarangay());
            pstmt.setString(7, beneficiary.getPurok());
            pstmt.setString(8, beneficiary.getStreetAddress());
            pstmt.setInt(9, beneficiary.getFamilySize());
            pstmt.setInt(10, beneficiary.isHouseholdHead() ? 1 : 0);
            pstmt.setInt(11, beneficiary.isPwd() ? 1 : 0);
            pstmt.setInt(12, beneficiary.isSeniorCitizen() ? 1 : 0);
            pstmt.setInt(13, beneficiary.isPregnant() ? 1 : 0);
            pstmt.setInt(14, beneficiary.isSoloParent() ? 1 : 0);
            pstmt.setString(15, beneficiary.getStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating beneficiary: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update beneficiary
     */
    public boolean update(Beneficiary beneficiary) {
        String sql = "UPDATE beneficiaries SET " +
            "full_name = ?, birth_date = ?, gender = ?, contact_number = ?, " +
            "barangay = ?, purok = ?, street_address = ?, family_size = ?, " +
            "is_household_head = ?, is_pwd = ?, is_senior_citizen = ?, " +
            "is_pregnant = ?, is_solo_parent = ?, status = ?, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, beneficiary.getFullName());
            
            if (beneficiary.getBirthDate() != null) {
                pstmt.setDate(2, Date.valueOf(beneficiary.getBirthDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            
            pstmt.setString(3, beneficiary.getGender());
            pstmt.setString(4, beneficiary.getContactNumber());
            pstmt.setString(5, beneficiary.getBarangay());
            pstmt.setString(6, beneficiary.getPurok());
            pstmt.setString(7, beneficiary.getStreetAddress());
            pstmt.setInt(8, beneficiary.getFamilySize());
            pstmt.setInt(9, beneficiary.isHouseholdHead() ? 1 : 0);
            pstmt.setInt(10, beneficiary.isPwd() ? 1 : 0);
            pstmt.setInt(11, beneficiary.isSeniorCitizen() ? 1 : 0);
            pstmt.setInt(12, beneficiary.isPregnant() ? 1 : 0);
            pstmt.setInt(13, beneficiary.isSoloParent() ? 1 : 0);
            pstmt.setString(14, beneficiary.getStatus());
            pstmt.setInt(15, beneficiary.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating beneficiary: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Soft delete beneficiary (move to recycle bin)
     */
    public boolean delete(int id) {
        String sql = "UPDATE beneficiaries SET " +
            "deleted = 1, deleted_at = CURRENT_TIMESTAMP, status = 'Inactive' " +
            "WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting beneficiary: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get beneficiary by ID
     */
    public Beneficiary getById(int id) {
        String sql = "SELECT * FROM beneficiaries WHERE id = ? AND deleted = 0";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBeneficiary(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting beneficiary: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get beneficiary by beneficiary ID
     */
    public Beneficiary getByBeneficiaryId(String beneficiaryId) {
        String sql = "SELECT * FROM beneficiaries WHERE beneficiary_id = ? AND deleted = 0";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, beneficiaryId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBeneficiary(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting beneficiary by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Search beneficiaries by name (case-insensitive)
     */
    public List<Beneficiary> searchByName(String searchTerm) {
        String sql = "SELECT * FROM beneficiaries WHERE full_name LIKE ? AND deleted = 0 ORDER BY full_name";
        List<Beneficiary> beneficiaries = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                beneficiaries.add(mapResultSetToBeneficiary(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching beneficiaries: " + e.getMessage());
            e.printStackTrace();
        }
        
        return beneficiaries;
    }
    
    /**
     * Get all beneficiaries
     */
    public List<Beneficiary> getAll() {
        String sql = "SELECT * FROM beneficiaries WHERE deleted = 0 ORDER BY full_name";
        List<Beneficiary> beneficiaries = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                beneficiaries.add(mapResultSetToBeneficiary(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all beneficiaries: " + e.getMessage());
            e.printStackTrace();
        }
        
        return beneficiaries;
    }
    
    /**
     * Check for duplicate (name + barangay + purok)
     */
    public boolean isDuplicate(String fullName, String barangay, String purok, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM beneficiaries WHERE full_name = ? AND barangay = ? AND purok = ? AND deleted = 0";
        
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fullName);
            pstmt.setString(2, barangay);
            pstmt.setString(3, purok);
            
            if (excludeId != null) {
                pstmt.setInt(4, excludeId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicate: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get next beneficiary ID (5-digit format)
     */
    public String getNextBeneficiaryId() {
        String sql = "SELECT beneficiary_id FROM beneficiaries ORDER BY beneficiary_id DESC LIMIT 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString("beneficiary_id");
                int nextNum = Integer.parseInt(lastId) + 1;
                return String.format("%05d", nextNum);
            } else {
                return "00001";
            }
        } catch (SQLException e) {
            System.err.println("Error getting next beneficiary ID: " + e.getMessage());
            e.printStackTrace();
            return "00001";
        } catch (NumberFormatException e) {
            return "00001";
        }
    }
    
    /**
     * Map ResultSet to Beneficiary object
     */
    private Beneficiary mapResultSetToBeneficiary(ResultSet rs) throws SQLException {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(rs.getInt("id"));
        beneficiary.setBeneficiaryId(rs.getString("beneficiary_id"));
        beneficiary.setFullName(rs.getString("full_name"));
        
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            beneficiary.setBirthDate(birthDate.toLocalDate());
        }
        
        beneficiary.setGender(rs.getString("gender"));
        beneficiary.setContactNumber(rs.getString("contact_number"));
        beneficiary.setBarangay(rs.getString("barangay"));
        beneficiary.setPurok(rs.getString("purok"));
        beneficiary.setStreetAddress(rs.getString("street_address"));
        beneficiary.setFamilySize(rs.getInt("family_size"));
        beneficiary.setHouseholdHead(rs.getInt("is_household_head") == 1);
        beneficiary.setPwd(rs.getInt("is_pwd") == 1);
        beneficiary.setSeniorCitizen(rs.getInt("is_senior_citizen") == 1);
        beneficiary.setPregnant(rs.getInt("is_pregnant") == 1);
        beneficiary.setSoloParent(rs.getInt("is_solo_parent") == 1);
        beneficiary.setStatus(rs.getString("status"));
        beneficiary.setDeleted(rs.getInt("deleted") == 1);
        
        Timestamp dateRegistered = rs.getTimestamp("date_registered");
        if (dateRegistered != null) {
            beneficiary.setDateRegistered(dateRegistered.toLocalDateTime());
        }
        
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        if (deletedAt != null) {
            beneficiary.setDeletedAt(deletedAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            beneficiary.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            beneficiary.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return beneficiary;
    }
}

