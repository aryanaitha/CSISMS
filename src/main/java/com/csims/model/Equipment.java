package com.csims.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Heavy machinery and equipment asset.
 * Statuses: Available, Assigned, In Use, Under Maintenance, Out of Service.
 */
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int equipmentId;
    private String equipmentCode;
    private String name;
    private String category;
    private String status = "Available"; // Available, Assigned, In Use, Under Maintenance, Out of Service
    private Integer projectId;
    private LocalDate purchaseDate;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDue;

    // Joined fields
    private String projectName;

    public Equipment() {}

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getLastServiceDate() {
        return lastServiceDate;
    }

    public void setLastServiceDate(LocalDate lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }

    public LocalDate getNextServiceDue() {
        return nextServiceDue;
    }

    public void setNextServiceDue(LocalDate nextServiceDue) {
        this.nextServiceDue = nextServiceDue;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean isServiceOverdue() {
        if (nextServiceDue == null) {
            return false;
        }
        return nextServiceDue.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return name + " (" + equipmentCode + ")";
    }
}
