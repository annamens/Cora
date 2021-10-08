package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;

public class TDetectSpecimen extends Specimen {
    
    @Override
    public void enterCollectionDate (String date) {
        assertTrue (setText ("[formcontrolname='collectionDate']", date));
    }
}
