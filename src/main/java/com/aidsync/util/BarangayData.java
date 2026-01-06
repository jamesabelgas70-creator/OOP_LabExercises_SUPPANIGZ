package com.aidsync.util;

import java.sql.*;
import java.util.*;

/**
 * Manages barangay and purok data
 */
public class BarangayData {
    private static final Map<String, List<String>> BARANGAY_PUROK_MAP = new LinkedHashMap<>();
    
    static {
        initializeMap();
    }
    
    private static void initializeMap() {
        BARANGAY_PUROK_MAP.put("BADAS", Arrays.asList(
            "Purok 1 - Riverside", "Purok 2 - Centro", "Purok 3 - Hillside",
            "Purok 4 - Seaside", "Purok 5 - Proper", "Purok 6 - San Roque",
            "Purok 7 - Upper Badas"
        ));
        
        BARANGAY_PUROK_MAP.put("BOBON", Arrays.asList(
            "Purok 1 - Baybayon", "Purok 2 - Kalubihan", "Purok 3 - Tabing-Dagat",
            "Purok 4 - Centro", "Purok 5 - Riverside"
        ));
        
        BARANGAY_PUROK_MAP.put("BUSO", Arrays.asList(
            "Purok 1 - Crossing", "Purok 2 - Lower Buso", "Purok 3 - Upper Buso",
            "Purok 4 - Proper"
        ));
        
        BARANGAY_PUROK_MAP.put("CABUAYA", Arrays.asList(
            "Purok 1 - Seaside", "Purok 2 - Riverside", "Purok 3 - Kabagian",
            "Purok 4 - Centro", "Purok 5 - Hillside"
        ));
        
        BARANGAY_PUROK_MAP.put("CENTRAL (POBLACION)", Arrays.asList(
            "Purok A - City Proper", "Purok B - Market Area", "Purok C - Plaza",
            "Purok D - Commercial Zone", "Purok E - Capitol Area", "Purok F - Seaside",
            "Purok G - Upper Poblacion"
        ));
        
        BARANGAY_PUROK_MAP.put("CULIAN", Arrays.asList(
            "Purok 1 - Crossing", "Purok 2 - Proper", "Purok 3 - Riverside",
            "Purok 4 - San Isidro"
        ));
        
        BARANGAY_PUROK_MAP.put("DAHICAN", Arrays.asList(
            "Purok 1 - Beachside", "Purok 2 - Highway", "Purok 3 - Tourism Zone",
            "Purok 4 - Uptown", "Purok 5 - Proper"
        ));
        
        BARANGAY_PUROK_MAP.put("DANAO", Arrays.asList(
            "Purok 1 - Centro", "Purok 2 - Riverside", "Purok 3 - Mabini",
            "Purok 4 - Magsaysay"
        ));
        
        BARANGAY_PUROK_MAP.put("DAWAN", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Seaside", "Purok 3 - Crossing",
            "Purok 4 - Upper Dawan"
        ));
        
        BARANGAY_PUROK_MAP.put("DON ENRIQUE LOPEZ", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Hillside", "Purok 3 - Riverside",
            "Purok 4 - Lopez Extension"
        ));
        
        BARANGAY_PUROK_MAP.put("DON MARTIN MARUNDAN", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Mabuhay", "Purok 3 - Mabini",
            "Purok 4 - Sto. Niño", "Purok 5 - Marundan Hills"
        ));
        
        BARANGAY_PUROK_MAP.put("DON SALVADOR LOPEZ SR.", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Upper Lopez", "Purok 3 - Riverside",
            "Purok 4 - Lopez Extension"
        ));
        
        BARANGAY_PUROK_MAP.put("LANGKA", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Riverside", "Purok 3 - Hillside",
            "Purok 4 - Langka Center"
        ));
        
        BARANGAY_PUROK_MAP.put("LAWIGAN", Arrays.asList(
            "Purok 1 - Seaside", "Purok 2 - Proper", "Purok 3 - Lawigan Beach",
            "Purok 4 - Upper Lawigan"
        ));
        
        BARANGAY_PUROK_MAP.put("LIBUDON", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Crossing", "Purok 3 - Hillside",
            "Purok 4 - San Roque"
        ));
        
        BARANGAY_PUROK_MAP.put("LUBAN", Arrays.asList(
            "Purok 1 - Centro", "Purok 2 - Riverside", "Purok 3 - Luban Proper",
            "Purok 4 - Luban Extension"
        ));
        
        BARANGAY_PUROK_MAP.put("MACAMBOL", Arrays.asList(
            "Purok 1 - Seaside", "Purok 2 - Proper", "Purok 3 - Kabukiran",
            "Purok 4 - Forestline", "Purok 5 - Macambol Hills"
        ));
        
        BARANGAY_PUROK_MAP.put("MAMALI", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - San Miguel", "Purok 3 - Crossing",
            "Purok 4 - Upper Mamali"
        ));
        
        BARANGAY_PUROK_MAP.put("MATIAO", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Commercial Area", "Purok 3 - Barangay Hall Zone",
            "Purok 4 - Highway", "Purok 5 - Sitio Corner"
        ));
        
        BARANGAY_PUROK_MAP.put("MAYO", Arrays.asList(
            "Purok 1 - Seaside", "Purok 2 - Market Area", "Purok 3 - Proper",
            "Purok 4 - Riverside", "Purok 5 - Upper Mayo"
        ));
        
        BARANGAY_PUROK_MAP.put("SAINZ", Arrays.asList(
            "Purok 1 - Crossing", "Purok 2 - Proper", "Purok 3 - Hillside",
            "Purok 4 - Sainz Extension"
        ));
        
        BARANGAY_PUROK_MAP.put("SANGHAY", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Riverside", "Purok 3 - Kabatian",
            "Purok 4 - San Pedro"
        ));
        
        BARANGAY_PUROK_MAP.put("TAGABAKID", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Tabing-Dagat", "Purok 3 - Crossing",
            "Purok 4 - Upper Tagabakid", "Purok 5 - Riverside"
        ));
        
        BARANGAY_PUROK_MAP.put("TAGBINONGA", Arrays.asList(
            "Purok 1 - Proper", "Purok 2 - Seaside", "Purok 3 - Marquez",
            "Purok 4 - Mangga", "Purok 5 - Riverside"
        ));
        
        BARANGAY_PUROK_MAP.put("TAGUIBO", Arrays.asList(
            "Purok 1 - Centro", "Purok 2 - Proper", "Purok 3 - Riverside",
            "Purok 4 - Taguibo Hills"
        ));
        
        BARANGAY_PUROK_MAP.put("TAMISAN", Arrays.asList(
            "Purok 1 - Beachside", "Purok 2 - Proper", "Purok 3 - Riverside",
            "Purok 4 - Upper Tamisan"
        ));
    }
    
    /**
     * Initialize barangay data in database
     */
    public static void initializeBarangays(Connection conn) throws SQLException {
        // This method can be used to store barangay data in database if needed
        // For now, we use the in-memory map
    }
    
    /**
     * Get list of all barangays
     */
    public static List<String> getAllBarangays() {
        return new ArrayList<>(BARANGAY_PUROK_MAP.keySet());
    }
    
    /**
     * Get puroks for a specific barangay
     */
    public static List<String> getPuroksForBarangay(String barangay) {
        return BARANGAY_PUROK_MAP.getOrDefault(barangay, new ArrayList<>());
    }
    
    /**
     * Check if barangay exists
     */
    public static boolean isValidBarangay(String barangay) {
        return BARANGAY_PUROK_MAP.containsKey(barangay);
    }
    
    /**
     * Check if purok exists for barangay
     */
    public static boolean isValidPurok(String barangay, String purok) {
        List<String> puroks = getPuroksForBarangay(barangay);
        return puroks.contains(purok);
    }
}

