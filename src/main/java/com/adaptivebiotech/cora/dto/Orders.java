/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.cora.dto.Alerts.Alert;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Reminders.Reminder;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.utils.PageHelper.FriendlyOrderStatus;
import com.adaptivebiotech.cora.utils.PageHelper.OrderType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Orders {

    public List <Order> list = new ArrayList <> ();

    public Orders () {}

    public Orders (List <Order> list) {
        this.list = list;
    }

    public Order findOrderByNumber (String number) {
        return list.parallelStream ().filter (o -> o.orderNumber.equals (number)).findAny ().get ();
    }

    public List <Order> findTestOrders () {
        return list.parallelStream ()
                   .filter (o -> o.patient != null && o.patient.firstName != null && o.patient.lastName != null)
                   .collect (toList ());
    }

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class Order {

        @JsonAlias ("orderId")
        public String              id;
        public String              category_id;
        public String              name;
        @JsonAlias ("orderStatus")
        public OrderStatus         status;
        public FriendlyOrderStatus friendlyOrderStatus;
        public String              salesforceOrderId;
        public String              salesforceOrderNumber;
        public Object              mrn;
        public OrderProperties     properties;
        public String              version;
        public String              patient_snapshot;
        public String              manifest_file_name;
        public String              data_analysis_group;
        public String              trf_file_name;
        public String              calibrated_receptors;
        public String              date_signed;
        public String              project_id;
        public String              orderEntryType;
        public Boolean             isTrfAttached;
        public String              customerInstructions;
        public Physician           physician;
        public Patient             patient;
        public List <String>       icdcodes;
        public Specimen            specimenDto;
        public String              reportDate;
        public List <OrderTest>    tests = new ArrayList <> ();
        public UploadFile          trf;
        public List <UploadFile>   doraAttachments;
        public List <UploadFile>   orderAttachments;
        public List <UploadFile>   shipmentAttachments;
        public String              notes;
        public List <Alert>        alerts;
        public Reminder            reminder;
        public Workflow            workflow;
        public OrderType           orderType;
        public Boolean             postToImmunoSEQ;
        public ChargeType          billingType;
        public DeliveryType        specimenDeliveryType;
        public OrderAuthorization  documentedByType;
        public String              externalOrderCode;
        public Object              intakeCompletedDate;
        public ContainerType       specimenDisplayContainerType;
        public Integer             specimenDisplayContainerCount;
        public Object              specimenDisplayArrivalDate;

        // for /cora/api/v1/orders/search
        public String              category;
        public String              customerName;
        public String              displayOrderNumber;
        public String              orderNumber;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime       created;
        public String              createdBy;
        public Integer             numTests;
        public Double              percentComplete;
        public OrderType           diagnosticOrderType;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime       lastActivity;
        public List <Stage>        stages;
        public String              patient_code;
        public String              key;

        // for /cora/api/v2/patients/list/{patientId}/orders
        public String              providerFirstName;
        public String              providerLastName;
        public String              accountName;
        public String              orderTestStatusType;
        public String              collectionDate;
        public String              testName;
        public String              orderTestId;
        public String              orderTestResultDisplayValue;
        public String              workflowId;
        public LocalDateTime       workflowFinishedTimeStamp;
        public String              reportFileName;
        public Object              reportAdditionalComments;
        public Object              reportNote;
        public String              reportActor;
        public Object              correctedReportFileName;
        public String              reportRelativeUrl;
        public String              orderStatusDisplay;

        // for /cora/api/v1/attachments/orders/:orderNumberOrId
        public String              modifiedBy;
        public String              attachedToId;
        public String              attachedToTableName;
        public boolean             inactive;
        public String              fileName;
        public String              attachmentType;
        public String              url;
        public boolean             isPdf;

        @Override
        public String toString () {
            return toStringOverride (this);
        }

        public Physician getPhysician () {
            Physician physician = new Physician ();
            physician.lastName = providerLastName;
            physician.firstName = providerFirstName;
            physician.accountName = accountName;
            return physician;
        }
    }

    /*
     * Note: based on api: /cora/api/v1/orderTests/order/:orderId
     */
    public static final class OrderTest {

        public String          id;
        public Integer         version;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime   created;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime   modified;
        public String          createdBy;
        public String          modifiedBy;
        public OrderProperties properties;
        public CoraTest        test;
        public Specimen        specimen;
        public String          sampleName;
        public OrderStatus     status;
        public String          dueDate;
        public String          qcType;
        public String          tags;
        public String          sampleWellLocation;
        public String          currentQueueDate;
        public String          boxId;
        public String          boxLocation;
        public String          isRegulated;
        public String          doNotDilute;
        public String          flag;
        public String          preUniquifiedSampleName;
        public String          originalQcType;
        public String          directives;
        public String          isManualSelection;
        public String          preManifestExternalContainerId;
        public String          plateId;
        public Double          desiredDnaConcentration;
        public String          percentTCell;
        public String          gdnaConcentration;
        public String          gdnaRatio;
        public String          cdnaConcentration;
        public String          cdnaRatio;
        public String          cellCount;
        public String          cellSort;
        public String          actualResolution;
        public String          assayMapItem;
        public String          pipelineConfigOverride;
        public String          sampleSourceForManifest;
        public String          currentQueue;
        public String          containerType;
        public String          notes;
        public Boolean         tdx;
        public String          key;
        public String          categoryType;
        public String          orderName;

        // for /cora/api/v1/orderTests/search
        public String          orderId;
        public String          testCode;
        public String          testName;
        public String          workflowName;
        public String          workflowId;
        public OrderCategory   category;
        public String          customerName;
        public StageName       stage;
        public StageStatus     stageStatus;
        public StageSubstatus  subStatusCode;
        public String          subStatusMessage;
        public String          drilldownUrl;
        public String          specimenNumber;
        public String          patientCode;
        public String          regulationLevel;
        public String          lastActivity;
        public String          finished;
        public List <Stage>    stages;

        // for UI
        public Assay           assay;
        public Boolean         selected;

        public OrderTest () {}

        public OrderTest (Assay assay) {
            this.test = new CoraTest ();
            this.test.name = assay.test;
            this.assay = assay;
            this.selected = true;
        }

        @Override
        public String toString () {
            return toStringOverride (this);
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (OrderTest) o);
        }
    }

    public static final class OrderProperties {

        public ChargeType   BillingType;
        public DeliveryType SpecimenDeliveryType;
        public String       Icd10Codes;

        public OrderProperties () {}

        public OrderProperties (ChargeType BillingType, DeliveryType SpecimenDeliveryType) {
            this.BillingType = BillingType;
            this.SpecimenDeliveryType = SpecimenDeliveryType;
        }

        public OrderProperties (ChargeType BillingType, DeliveryType SpecimenDeliveryType, String Icd10Codes) {
            this (BillingType, SpecimenDeliveryType);
            this.Icd10Codes = Icd10Codes;
        }

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

    public enum ChargeType {
        Client ("Client Bill", "Bill my Institution"),
        TrialProtocol ("Bill per Study Protocol"),
        CommercialInsurance ("Insurance (Including Medicare Advantage)", "Insurance (including Medicare Advantage plans)"),
        Medicare ("Medicare"),
        PatientSelfPay ("Patient Self-pay", "Patient Self-Pay"),
        NoCharge ("No Charge"),
        InternalPharmaBilling ("Internal Pharma Billing");

        public String label;
        public String doraLabel;

        private ChargeType (String label) {
            this.label = label;
        }

        private ChargeType (String label, String dora) {
            this.label = label;
            this.doraLabel = dora;
        }

        public static ChargeType getChargeType (String label) {
            return allOf (ChargeType.class).parallelStream ().filter (ct -> ct.label.equals (label)).findAny ().get ();
        }
    }

    public enum NoChargeReason {
        NoReportIssued ("No report issued (i.e -Q/A, QC, validation, proficiency)"),
        IncompleteDocumentation ("Incomplete documentation"),
        TimelinessOfBilling ("Timeliness of billing"),
        CustomerService ("Customer service - management approved (document in notes)"),
        OperationalIssue ("Operational issue (document in notes)"),
        Other ("Other (document in notes)");

        public String label;

        private NoChargeReason (String label) {
            this.label = label;
        }

        public static NoChargeReason getNoChargeReason (String label) {
            return allOf (NoChargeReason.class).parallelStream ().filter (st -> st.label.equals (label)).findAny ()
                                               .get ();
        }

        public static List <String> getAllReasons () {
            return allOf (NoChargeReason.class).stream ().map (e -> e.label).collect (toList ());
        }
    }

    public enum DeliveryType {
        CustomerShipment ("Shipping Specimen to Adaptive"),
        PathRequest ("Adaptive Assists with Specimen Retrieval"),
        Reflex ("Use Specimen Stored at Adaptive (Reflex)"),
        BloodDrawLabCorp ("Adaptive Schedules Patient Blood Draw with LabCorp"),
        BloodDrawHome ("Adaptive Schedules Patient In-Home Blood Draw");

        public String label;

        private DeliveryType (String label) {
            this.label = label;
        }

        public static DeliveryType getDeliveryType (String label) {
            return allOf (DeliveryType.class).parallelStream ().filter (st -> st.label.equals (label)).findAny ()
                                             .get ();
        }
    }

    public enum OrderCategory {
        All, Diagnostic, Research
    }

    public enum OrderStatus {
        All, Pending, Active, Completed, Cancelled, Failed, FailedActivation, PendingActivation
    }

    public enum Assay {
        ID_BCell2_IVD ("B cell Clonality", "B-cell 2.0 Clonality (IVD)"),
        ID_BCell2_IUO ("B cell Clonality", "B-cell 2.0 Clonality (IUO CLIA-extract)"),
        ID_BCell2_CLIA ("B cell Clonality", "B-cell 2.0 Clonality (CLIA)"),
        ID_TCRB ("T cell Clonality", "TCRB Clonality (CLIA)"),
        ID_TCRB_IUO ("T cell Clonality", "TCRB Clonality (IUO CLIA-extract)"),
        ID_TCRG ("T cell Clonality", "TCRG Clonality (CLIA)"),
        ID_TCRG_IUO ("T cell Clonality", "TCRG Clonality (IUO CLIA-extract)"),
        MRD_BCell2_IVD ("Tracking", "B-cell 2.0 Tracking (IVD)"),
        MRD_BCell2_IUO ("Tracking", "B-cell 2.0 Tracking (IUO CLIA-extract)"),
        MRD_BCell2_CLIA ("Tracking", "B-cell 2.0 Tracking (CLIA)"),
        MRD_TCRB ("Tracking", "TCRB Tracking (CLIA)"),
        MRD_TCRB_IUO ("Tracking", "TCRB Tracking (IUO CLIA-extract)"),
        MRD_TCRG ("Tracking", "TCRG Tracking (CLIA)"),
        MRD_TCRG_IUO ("Tracking", "TCRG Tracking (IUO CLIA-extract)"),
        COVID19_DX_IVD ("Covid19", "T-Detect COVID"),
        LYME_DX ("Lyme", "T-Detect Lyme");

        public String type;
        public String test;

        private Assay (String type, String test) {
            this.type = type;
            this.test = test;
        }

        public static Assay getAssay (String test) {
            return allOf (Assay.class).parallelStream ().filter (a -> a.test.equals (test)).findAny ().get ();
        }
    }

    public enum OrderAuthorization {
        TrfWetSig ("Physician wet signature on TRF"),
        TrfESig ("Physician e-signature on TRF"),
        ExternalTrf ("Internal requisition form with handwritten or electronic physician signature authorizing clonoSEQ order"),
        SignatureBypass ("Electronically Authorized in Medical Record");

        public String coraLabel;

        private OrderAuthorization (String coraLabel) {
            this.coraLabel = coraLabel;
        }

        public static OrderAuthorization getOrderAuthorization (String coraLabel) {
            return allOf (OrderAuthorization.class).parallelStream ().filter (st -> st.coraLabel.equals (coraLabel))
                                                   .findAny ().orElse (null);
        }
    }

}
