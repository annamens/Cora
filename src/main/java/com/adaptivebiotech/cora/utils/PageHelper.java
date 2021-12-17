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

        public static MiraLab getMiraLab (String text) {
            return allOf (MiraLab.class).parallelStream ().filter (r -> r.text.equals (text)).findAny ().get ();
        }
    }

    public enum MiraType {
        MIRA ("MIRA"), NotAMIRA ("Not a MIRA"), NAMBAM ("NAMBAM");

        public final String text;

        private MiraType (String name) {
            this.text = name;
        }

        public static MiraType getMiraType (String text) {
            return allOf (MiraType.class).parallelStream ().filter (r -> r.text.equals (text)).findAny ().get ();
        }
    }

    public enum MiraExpansionMethod {
        AntiCD3 ("anti-CD3"),
        DCExpansionCD4 ("DC Expansion - CD4"),
        DCExpansionCD8 ("DC Expansion - CD8"),
        DCExpansionPanT ("DC Expansion - Pan T"),
        ImmunocultCD3CD28CD2 ("Immunocult CD3/CD28/CD2"),
        AntigenSpecificPeptide ("Antigen Specific- Peptide"),
        AntigenSpecificTransgene ("Antigen Specific- Transgene"),
        NoExpansion ("No Expansion"),
        Polyclonal ("Polyclonal");

        public final String text;

        private MiraExpansionMethod (String name) {
            this.text = name;
        }

        public static MiraExpansionMethod getMiraExpansionMethod (String text) {
            return allOf (MiraExpansionMethod.class).parallelStream ().filter (r -> r.text.equals (text)).findAny ()
                                                    .get ();
        }
    }

    public enum MiraStage {
        MIRAPrep, MIRAShip, PoolExtraction, immunoSEQ, MIRAAnalysis, MIRAQC, MIRAAgate, Publishing;
    }

    public enum MiraStatus {
        Ready, Processing, Awaiting, Stuck, Failed, Finished, Cancelled;
    }

    public enum MiraQCStatus {
        ACCEPTED, FAILED;
    }

    public enum MiraCostCenter {
        COV ("COV"), CRI ("CRI"), AMDL ("AMDL"), CRI_NAM ("CRI-NAM"), CRI_PERSONAL ("CRI-PERSONAL");

        public final String text;

        private MiraCostCenter (String text) {
            this.text = text;
        }

        public static MiraCostCenter getMiraCostCenter (String text) {
            return allOf (MiraCostCenter.class).parallelStream ().filter (r -> r.text.equals (text)).findAny ().get ();
        }
    }

    public enum MiraSortType {
        CD4 ("CD4"), CD8 ("CD8"), CD8andCD4 ("CD8 and CD4");

        public final String text;

        private MiraSortType (String text) {
            this.text = text;
        }

        public static MiraSortType getMiraSortType (String text) {
            return allOf (MiraSortType.class).parallelStream ().filter (r -> r.text.equals (text)).findAny ().get ();
        }
    }

    public enum MiraInputCellType {
        NaiveCD4 ("Naive CD4 T cells"),
        NaiveCD8 ("Naive CD8 T cells"),
        NaiveTcells ("Naive T cells (CD8 and CD4)"),
        PBMC ("PBMC"),
        Tcells ("T cells (pan/memory)");

        public final String text;

        private MiraInputCellType (String text) {
            this.text = text;
        }

        public static MiraInputCellType getMiraInputCellType (String text) {
            return allOf (MiraInputCellType.class).parallelStream ().filter (r -> r.text.equals (text)).findAny ()
                                                  .get ();
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
            return allOf (Ethnicity.class).parallelStream ().filter (e -> e.text.equals (text)).findFirst ()
                                          .orElse (null);
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
            return allOf (Race.class).parallelStream ().filter (r -> r.text.equals (text)).findFirst ().orElse (null);
        }

        private Race (String text) {
            this.text = text;
        }
    }

    public enum CorrectionType {
        Updated, Amended;
    }

    public enum FriendlyOrderStatus {
        All ("All"),
        PendingOrder ("Pending Order"),
        SpecimenReceived ("Specimen Received"),
        SpecimenCoordination ("Specimen Coordination"),
        SpecimenProcessing ("Specimen Processing"),
        ResultsAvailable ("Results Available");

        public String label;

        private FriendlyOrderStatus (String label) {
            this.label = label;
        }

        public static FriendlyOrderStatus getFriendlyOrderStatus (String label) {
            return allOf (FriendlyOrderStatus.class).parallelStream ().filter (a -> a.label.equals (label)).findFirst ()
                                                    .get ();
        }
    }
}
