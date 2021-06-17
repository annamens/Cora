package com.adaptivebiotech.cora.dto;

public class PoolDetails {

    private String sampleName;
    private String holdingContainerName;
    private String containerName;
    private String pool;
    private double cellCount;
    private String notes;
    private String parentASID;
    private String specimenType;
    private String resolution;

    public String getSampleName () {
        return sampleName;
    }

    public void setSampleName (String sampleName) {
        this.sampleName = sampleName;
    }

    public String getHoldingContainerName () {
        return holdingContainerName;
    }

    public void setHoldingContainerName (String holdingContainerName) {
        this.holdingContainerName = holdingContainerName;
    }

    public String getContainerName () {
        return containerName;
    }

    public void setContainerName (String containerName) {
        this.containerName = containerName;
    }

    public String getPool () {
        return pool;
    }

    public void setPool (String pool) {
        this.pool = pool;
    }

    public double getCellCount () {
        return cellCount;
    }

    public void setCellCount (double cellCount) {
        this.cellCount = cellCount;
    }

    public String getNotes () {
        return notes;
    }

    public void setNotes (String notes) {
        this.notes = notes;
    }

    public String getParentASID () {
        return parentASID;
    }

    public void setParentASID (String parentASID) {
        this.parentASID = parentASID;
    }

    public String getSpecimenType () {
        return specimenType;
    }

    public void setSpecimenType (String specimenType) {
        this.specimenType = specimenType;
    }

    public String getResolution () {
        return resolution;
    }

    public void setResolution (String resolution) {
        this.resolution = resolution;
    }

}
