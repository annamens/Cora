package com.adaptivebiotech.cora.dto;

public enum Discrepancy {
    
    ShippingConditions("Shipping Conditions"), SpecimenType("Specimen Type"); // lots of these
    
    public final String text;
    
    private Discrepancy(String name) {
        this.text = name;
    }
}
