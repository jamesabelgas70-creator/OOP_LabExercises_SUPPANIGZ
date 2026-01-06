package com.aidsync.model;

import java.time.LocalDate;

/**
 * Filter criteria for beneficiary searches
 */
public class FilterCriteria {
    private String barangay;
    private String status;
    private Boolean isPwd;
    private Boolean isSeniorCitizen;
    private Boolean isPregnant;
    private Boolean isSoloParent;
    private Integer minFamilySize;
    private Integer maxFamilySize;
    private String gender;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public FilterCriteria() {}

    // Getters and Setters
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsPwd() { return isPwd; }
    public void setIsPwd(Boolean isPwd) { this.isPwd = isPwd; }

    public Boolean getIsSeniorCitizen() { return isSeniorCitizen; }
    public void setIsSeniorCitizen(Boolean isSeniorCitizen) { this.isSeniorCitizen = isSeniorCitizen; }

    public Boolean getIsPregnant() { return isPregnant; }
    public void setIsPregnant(Boolean isPregnant) { this.isPregnant = isPregnant; }

    public Boolean getIsSoloParent() { return isSoloParent; }
    public void setIsSoloParent(Boolean isSoloParent) { this.isSoloParent = isSoloParent; }

    public Integer getMinFamilySize() { return minFamilySize; }
    public void setMinFamilySize(Integer minFamilySize) { this.minFamilySize = minFamilySize; }

    public Integer getMaxFamilySize() { return maxFamilySize; }
    public void setMaxFamilySize(Integer maxFamilySize) { this.maxFamilySize = maxFamilySize; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }

    public boolean isEmpty() {
        return barangay == null && status == null && isPwd == null && 
               isSeniorCitizen == null && isPregnant == null && isSoloParent == null &&
               minFamilySize == null && maxFamilySize == null && gender == null &&
               dateFrom == null && dateTo == null;
    }
}