package com.adaptivebiotech.cora.utils;

import static java.util.EnumSet.allOf;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    
    public enum MiraLab {
        AntigenMapProduction ("Antigen Map Production"),
        AntigenMapRD ("Antigen Map R&D"),
        TCRDiscovery ("TCR Discovery");

        public final String text;

        private MiraLab (String name) {
            this.text = name;
        }
    }

    public enum MiraType {
        MIRA ("MIRA"), NotAMIRA ("Not a MIRA"), NAMBAM ("NAMBAM");

        public final String text;

        private MiraType (String name) {
            this.text = name;
        }
    }

    public enum MiraExpansionMethod {
        AntiCD3 ("anti-CD3"),
        DCExpansionCD4 ("DC Expansion - CD4"),
        DCExpansionCD8 ("DC Expansion - CD8"),
        DCExpansionPanT ("DC Expansion - Pan T"),
        ImmunocultCD3CD28CD2 ("Immunocult CD3/CD28/CD2");

        public final String text;

        private MiraExpansionMethod (String name) {
            this.text = name;
        }
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

    public enum Ethnicity {

        @JsonProperty ("Hispanic or Latino")
        HISPANIC("Hispanic or Latino"),
        @JsonProperty ("Non Hispanic or Latino")
        NON_HISPANIC("Non Hispanic or Latino"),
        @JsonProperty ("Unknown")
        UNKNOWN("Unknown"),
        @JsonProperty ("Asked, but unknown")
        ASKED("Asked, but unknown");

        public final String text;

        public static Ethnicity getEthnicity (String text) {
            return allOf (Ethnicity.class).parallelStream ().filter (e -> e.text.equals (text)).findAny ().get ();
        }

        private Ethnicity (String text) {
            this.text = text;
        }
    }

    public enum Race {

        @JsonProperty ("American Indian or Alaska Native")
        AMERICAN_INDIAN("American Indian or Alaska Native"),
        @JsonProperty ("Asian")
        ASIAN("Asian"),
        @JsonProperty ("Black or African American")
        BLACK("Black or African American"),
        @JsonProperty ("Native Hawaiian or Other Pacific Islander")
        NATIVE_HAWAIIAN("Native Hawaiian or Other Pacific Islander"),
        @JsonProperty ("White")
        WHITE("White"),
        @JsonProperty ("Unknown")
        UNKNOWN("Unknown"),
        @JsonProperty ("Asked, but unknown")
        ASKED("Asked, but unknown");

        public final String text;

        public static Race getRace (String text) {
            return allOf (Race.class).parallelStream ().filter (r -> r.text.equals (text)).findAny ().get ();
        }

        private Race (String text) {
            this.text = text;
        }
    }
}
