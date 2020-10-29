package com.adaptivebiotech.cora.dto;

public enum Carrier {
 UPS("UPS"), FEDEX("Fedex"), COURIER("Courier"), OTHER("Other");
    
    public final String text; 
    
    private Carrier(String name) {
        this.text = name;
    }
}
