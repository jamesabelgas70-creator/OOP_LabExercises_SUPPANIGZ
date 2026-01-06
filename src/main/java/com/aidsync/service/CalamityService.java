package com.aidsync.service;

import com.aidsync.dao.CalamityDAO;
import com.aidsync.model.Calamity;

import java.util.List;

/**
 * Service layer for Calamity operations
 */
public class CalamityService {
    private final CalamityDAO calamityDAO;
    
    public CalamityService() {
        this.calamityDAO = new CalamityDAO();
    }
    
    /**
     * Create new calamity
     */
    public boolean createCalamity(Calamity calamity) {
        if (calamity.getName() == null || calamity.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Calamity name is required");
        }
        
        if (calamityDAO.calamityNameExists(calamity.getName())) {
            throw new IllegalArgumentException("Calamity name already exists");
        }
        
        if (calamity.getStatus() == null) {
            calamity.setStatus("Active");
        }
        
        return calamityDAO.createCalamity(calamity);
    }
    
    /**
     * Get calamity by ID
     */
    public Calamity getCalamityById(int id) {
        return calamityDAO.getCalamityById(id);
    }
    
    /**
     * Get all calamities
     */
    public List<Calamity> getAllCalamities() {
        return calamityDAO.getAllCalamities();
    }
    
    /**
     * Get active calamities only
     */
    public List<Calamity> getActiveCalamities() {
        return calamityDAO.getActiveCalamities();
    }
    
    /**
     * Update calamity
     */
    public boolean updateCalamity(Calamity calamity) {
        if (calamity.getName() == null || calamity.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Calamity name is required");
        }
        
        if (calamityDAO.calamityNameExists(calamity.getName(), calamity.getId())) {
            throw new IllegalArgumentException("Calamity name already exists");
        }
        
        if (calamity.getStatus() == null) {
            calamity.setStatus("Active");
        }
        
        return calamityDAO.updateCalamity(calamity);
    }
    
    /**
     * Delete calamity
     */
    public boolean deleteCalamity(int id) {
        return calamityDAO.deleteCalamity(id);
    }
}

