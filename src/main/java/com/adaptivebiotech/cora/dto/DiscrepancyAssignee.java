package com.adaptivebiotech.cora.dto;

public enum DiscrepancyAssignee {

    CLINICAL_SERVICES("Clinical Services"), CLINICAL_TRIALS("Clinical Trials");
    
    public final String text;
    
    private DiscrepancyAssignee(String name) {
        this.text = name;
    }
    
}
