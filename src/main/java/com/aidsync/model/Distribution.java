package com.aidsync.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Distribution model representing relief distribution to beneficiaries
 */
public class Distribution {
    private int id;
    private int beneficiaryId;
    private Beneficiary beneficiary;
    private LocalDateTime distributionDate;
    private int distributedBy;
    private User distributedByUser;
    private Integer calamityId;
    private Calamity calamity;
    private String notes;
    private LocalDateTime createdAt;
    private List<DistributionItem> items;

    public Distribution() {
        this.items = new ArrayList<>();
        this.distributionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(int beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
        if (beneficiary != null) {
            this.beneficiaryId = beneficiary.getId();
        }
    }

    public LocalDateTime getDistributionDate() {
        return distributionDate;
    }

    public void setDistributionDate(LocalDateTime distributionDate) {
        this.distributionDate = distributionDate;
    }

    public int getDistributedBy() {
        return distributedBy;
    }

    public void setDistributedBy(int distributedBy) {
        this.distributedBy = distributedBy;
    }

    public User getDistributedByUser() {
        return distributedByUser;
    }

    public void setDistributedByUser(User distributedByUser) {
        this.distributedByUser = distributedByUser;
        if (distributedByUser != null) {
            this.distributedBy = distributedByUser.getId();
        }
    }

    public Integer getCalamityId() {
        return calamityId;
    }

    public void setCalamityId(Integer calamityId) {
        this.calamityId = calamityId;
    }

    public Calamity getCalamity() {
        return calamity;
    }

    public void setCalamity(Calamity calamity) {
        this.calamity = calamity;
        if (calamity != null) {
            this.calamityId = calamity.getId();
        } else {
            this.calamityId = null;
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DistributionItem> getItems() {
        return items;
    }

    public void setItems(List<DistributionItem> items) {
        this.items = items;
    }

    public void addItem(DistributionItem item) {
        this.items.add(item);
    }
}

