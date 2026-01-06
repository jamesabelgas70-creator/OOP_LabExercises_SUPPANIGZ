package com.aidsync.service;

import com.aidsync.dao.BeneficiaryDAO;
import com.aidsync.model.Beneficiary;
import com.aidsync.model.FilterCriteria;
import com.aidsync.util.BarangayData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Beneficiary operations
 */
public class BeneficiaryService {
    private final BeneficiaryDAO beneficiaryDAO;
    
    public BeneficiaryService() {
        this.beneficiaryDAO = new BeneficiaryDAO();
    }
    
    /**
     * Create new beneficiary
     */
    public boolean createBeneficiary(Beneficiary beneficiary) {
        return createBeneficiary(beneficiary, "System");
    }
    
    /**
     * Create new beneficiary with user tracking
     */
    public boolean createBeneficiary(Beneficiary beneficiary, String username) {
        validateBeneficiary(beneficiary);
        
        // Check for duplicates
        if (beneficiaryDAO.isDuplicate(beneficiary.getFullName(), 
                beneficiary.getBarangay(), beneficiary.getPurok(), null)) {
            throw new IllegalArgumentException("A beneficiary with the same name, barangay, and purok already exists");
        }
        
        // Generate beneficiary ID
        beneficiary.setBeneficiaryId(beneficiaryDAO.getNextBeneficiaryId());
        beneficiary.setDateRegistered(LocalDateTime.now());
        beneficiary.setCreatedAt(LocalDateTime.now());
        beneficiary.setUpdatedAt(LocalDateTime.now());
        
        boolean success = beneficiaryDAO.create(beneficiary);
        if (success) {
            ActivityLogService.logActivity(username, "CREATE_BENEFICIARY", 
                "Created beneficiary: " + beneficiary.getFullName() + " (ID: " + beneficiary.getBeneficiaryId() + ")");
        }
        return success;
    }
    
    /**
     * Update beneficiary
     */
    public boolean updateBeneficiary(Beneficiary beneficiary) {
        return updateBeneficiary(beneficiary, "System");
    }
    
    /**
     * Update beneficiary with user tracking
     */
    public boolean updateBeneficiary(Beneficiary beneficiary, String username) {
        validateBeneficiary(beneficiary);
        
        // Check for duplicates (excluding current beneficiary)
        if (beneficiaryDAO.isDuplicate(beneficiary.getFullName(), 
                beneficiary.getBarangay(), beneficiary.getPurok(), beneficiary.getId())) {
            throw new IllegalArgumentException("A beneficiary with the same name, barangay, and purok already exists");
        }
        
        beneficiary.setUpdatedAt(LocalDateTime.now());
        boolean success = beneficiaryDAO.update(beneficiary);
        if (success) {
            ActivityLogService.logActivity(username, "UPDATE_BENEFICIARY", 
                "Updated beneficiary: " + beneficiary.getFullName() + " (ID: " + beneficiary.getBeneficiaryId() + ")");
        }
        return success;
    }
    
    /**
     * Delete beneficiary (soft delete)
     */
    public boolean deleteBeneficiary(int id) {
        return deleteBeneficiary(id, "System");
    }
    
    /**
     * Delete beneficiary with user tracking
     */
    public boolean deleteBeneficiary(int id, String username) {
        Beneficiary beneficiary = beneficiaryDAO.getById(id);
        boolean success = beneficiaryDAO.delete(id);
        if (success && beneficiary != null) {
            ActivityLogService.logActivity(username, "DELETE_BENEFICIARY", 
                "Deleted beneficiary: " + beneficiary.getFullName() + " (ID: " + beneficiary.getBeneficiaryId() + ")");
        }
        return success;
    }
    
    /**
     * Get beneficiary by ID
     */
    public Beneficiary getBeneficiaryById(int id) {
        return beneficiaryDAO.getById(id);
    }
    
    /**
     * Get beneficiary by beneficiary ID
     */
    public Beneficiary getBeneficiaryByBeneficiaryId(String beneficiaryId) {
        return beneficiaryDAO.getByBeneficiaryId(beneficiaryId);
    }
    
    /**
     * Search beneficiaries by name
     */
    public List<Beneficiary> searchBeneficiaries(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return beneficiaryDAO.getAll();
        }
        return beneficiaryDAO.searchByName(searchTerm.trim());
    }
    
    /**
     * Get all beneficiaries
     */
    public List<Beneficiary> getAllBeneficiaries() {
        return beneficiaryDAO.getAll();
    }
    
    /**
     * Filter beneficiaries by criteria
     */
    public List<Beneficiary> filterBeneficiaries(FilterCriteria criteria, String searchTerm) {
        List<Beneficiary> beneficiaries = searchTerm != null && !searchTerm.trim().isEmpty() 
            ? beneficiaryDAO.searchByName(searchTerm.trim())
            : beneficiaryDAO.getAll();
        
        if (criteria == null || criteria.isEmpty()) {
            return beneficiaries;
        }
        
        return beneficiaries.stream()
            .filter(b -> matchesBarangay(b, criteria.getBarangay()))
            .filter(b -> matchesStatus(b, criteria.getStatus()))
            .filter(b -> matchesGender(b, criteria.getGender()))
            .filter(b -> matchesPwd(b, criteria.getIsPwd()))
            .filter(b -> matchesSeniorCitizen(b, criteria.getIsSeniorCitizen()))
            .filter(b -> matchesPregnant(b, criteria.getIsPregnant()))
            .filter(b -> matchesSoloParent(b, criteria.getIsSoloParent()))
            .filter(b -> matchesFamilySize(b, criteria.getMinFamilySize(), criteria.getMaxFamilySize()))
            .filter(b -> matchesDateRange(b, criteria.getDateFrom(), criteria.getDateTo()))
            .collect(Collectors.toList());
    }
    
    private boolean matchesBarangay(Beneficiary b, String barangay) {
        return barangay == null || barangay.equals(b.getBarangay());
    }
    
    private boolean matchesStatus(Beneficiary b, String status) {
        return status == null || status.equals(b.getStatus());
    }
    
    private boolean matchesGender(Beneficiary b, String gender) {
        return gender == null || gender.equals(b.getGender());
    }
    
    private boolean matchesPwd(Beneficiary b, Boolean isPwd) {
        return isPwd == null || isPwd.equals(b.isPwd());
    }
    
    private boolean matchesSeniorCitizen(Beneficiary b, Boolean isSeniorCitizen) {
        return isSeniorCitizen == null || isSeniorCitizen.equals(b.isSeniorCitizen());
    }
    
    private boolean matchesPregnant(Beneficiary b, Boolean isPregnant) {
        return isPregnant == null || isPregnant.equals(b.isPregnant());
    }
    
    private boolean matchesSoloParent(Beneficiary b, Boolean isSoloParent) {
        return isSoloParent == null || isSoloParent.equals(b.isSoloParent());
    }
    
    private boolean matchesFamilySize(Beneficiary b, Integer min, Integer max) {
        if (min != null && b.getFamilySize() < min) return false;
        return max == null || b.getFamilySize() <= max;
    }
    
    private boolean matchesDateRange(Beneficiary b, LocalDate from, LocalDate to) {
        if (b.getDateRegistered() == null) return true;
        LocalDate regDate = b.getDateRegistered().toLocalDate();
        if (from != null && regDate.isBefore(from)) return false;
        return to == null || !regDate.isAfter(to);
    }
    
    /**
     * Validate beneficiary data
     */
    private void validateBeneficiary(Beneficiary beneficiary) {
        if (beneficiary.getFullName() == null || beneficiary.getFullName().trim().length() < 2) {
            throw new IllegalArgumentException("Full name must be at least 2 characters");
        }
        
        if (beneficiary.getBarangay() == null || beneficiary.getBarangay().trim().isEmpty()) {
            throw new IllegalArgumentException("Barangay is required");
        }
        
        if (!BarangayData.isValidBarangay(beneficiary.getBarangay())) {
            throw new IllegalArgumentException("Invalid barangay selected");
        }
        
        if (beneficiary.getPurok() == null || beneficiary.getPurok().trim().isEmpty()) {
            throw new IllegalArgumentException("Purok is required");
        }
        
        if (!BarangayData.isValidPurok(beneficiary.getBarangay(), beneficiary.getPurok())) {
            throw new IllegalArgumentException("Invalid purok selected for the barangay");
        }
        
        if (beneficiary.getFamilySize() < 1 || beneficiary.getFamilySize() > 20) {
            throw new IllegalArgumentException("Family size must be between 1 and 20");
        }
        
        if (beneficiary.getBirthDate() != null && beneficiary.getBirthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
    }
}

