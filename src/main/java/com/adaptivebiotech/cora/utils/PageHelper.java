package com.adaptivebiotech.cora.utils;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class PageHelper {

    public enum OrderType {
        CDx, TDx
    }

    public enum DiscrepancyType {
        Documentation, Specimen, General
    }

    public enum LinkShipment {
        Account, SalesforceOrder, Project
    }

    public enum MiraPanel {
        Aloha, Belmont, Cherry, Denny, Eastlake, Lakeview, Minor, Pike
    }
    
    public enum Carrier {
        UPS ("UPS"), FEDEX ("Fedex"), COURIER ("Courier"), OTHER ("Other");

        public final String text;

        private Carrier (String name) {
            this.text = name;
        }
    }
    
    public enum DiscrepancyAssignee {

        CLINICAL_SERVICES ("Clinical Services"), CLINICAL_TRIALS ("Clinical Trials");

        public final String text;

        private DiscrepancyAssignee (String name) {
            this.text = name;
        }

    }

    public enum Discrepancy {

        ShippingConditions ("Shipping Conditions"), SpecimenType ("Specimen Type"); // lots of these

        public final String text;

        private Discrepancy (String name) {
            this.text = name;
        }
    }
    
    
}
