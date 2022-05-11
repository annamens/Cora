/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.LYME_DX;
import static com.adaptivebiotech.cora.dto.Orders.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.cora.dto.Orders.OrderCategory.Diagnostic;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt2;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSeq2_WorkflowNanny;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.adaptivebiotech.cora.dto.Diagnostic.Order;
import com.adaptivebiotech.cora.dto.Diagnostic.Panel;
import com.adaptivebiotech.cora.dto.Diagnostic.Task;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.dto.Research.TechTransfer;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.SpecimenProperties;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TestScenarioBuilder {

    public static final LocalDateTime collectionDate     = now ().minusDays (10);
    public static final LocalDateTime reconciliationDate = now ().minusDays (10);
    public static final LocalDateTime arrivalDate        = now ().minusDays (15);

    public static Order order (OrderProperties properties, CoraTest... tests) {
        Order order = new Order ();
        order.name = "Selenium Test Order";
        order.properties = properties;
        order.tests = asList (tests);
        return order;
    }

    public static Stage stage (StageName name, StageStatus status) {
        Stage stage = new Stage ();
        stage.stageName = name;
        stage.stageStatus = status;
        stage.subStatusCode = "";
        stage.subStatusMessage = "";
        stage.drilldownUrl = "";
        stage.actor = "selenium test";
        return stage;
    }

    public static Specimen specimen () {
        Specimen specimen = new Specimen ();
        specimen.sampleType = Blood;
        specimen.collectionDate = dateToArrInt (collectionDate);
        specimen.reconciliationDate = dateToArrInt (reconciliationDate);
        specimen.properties = new SpecimenProperties (formatDt2.format (arrivalDate));
        return specimen;
    }

    public static Shipment shipment () {
        Shipment shipment = new Shipment ();
        shipment.category = Diagnostic;
        shipment.status = "IntakeComplete";
        shipment.arrivalDate = dateToArrInt (arrivalDate);
        shipment.carrier = "UPS";
        shipment.trackingNumber = "";
        shipment.condition = Ambient;
        shipment.expectedRecordType = "Order";

        Container container = new Container ();
        container.containerType = Tube;
        container.contentsLocked = true;
        container.integrity = "Pass";
        shipment.containers = asList (container);
        return shipment;
    }

    public static Task workflowNanny () {
        Task task = new Task ();
        task.name = "Clonoseq Report Scenario Test Helper";
        task.description = "Moves ClonoSeq workflows through the correct stages for tests.";
        task.status = Active;
        task.stageName = ClonoSeq2_WorkflowNanny;
        task.stageStatus = Ready;
        task.configId = "04d48793-c0c7-4b76-ab2d-53e2ef65891e";
        task.configName = "ReportScenarioTestHelper";
        return task;
    }

    public static Diagnostic diagnosticOrder (Physician physician,
                                              Patient patient,
                                              Specimen specimen,
                                              Shipment shipment) {
        return diagnosticOrder (physician.account, physician, patient, specimen, shipment);
    }

    public static Diagnostic diagnosticOrder (Account account,
                                              Physician physician,
                                              Patient patient,
                                              Specimen specimen,
                                              Shipment shipment) {
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.account = account;
        diagnostic.provider = physician;
        diagnostic.patient = patient;
        diagnostic.specimen = specimen;
        diagnostic.shipment = shipment;
        diagnostic.task = workflowNanny ();
        diagnostic.waitForResults = true;
        return diagnostic;
    }

    public static Research researchOrder (Specimen... specimens) {
        TechTransfer techTransfer = new TechTransfer ();
        techTransfer.workspace = "Adaptive-Testing";
        techTransfer.flowcellId = "selenium-staging";
        techTransfer.specimens = asList (specimens);
        return new Research (techTransfer);
    }

    public static Integer[] dateToArrInt (LocalDateTime dateTime) {
        DateTimeFormatter fmt = ofPattern ("uuuu-MM-dd-HH-mm-ss-SSS");
        return asList (dateTime.format (fmt).split ("-")).stream ().map (Integer::valueOf).toArray (Integer[]::new);
    }

    public static Diagnostic buildDiagnosticOrder (Physician physician,
                                                   Patient patient,
                                                   Stage stage,
                                                   CoraTest... tests) {
        Diagnostic diagnostic = diagnosticOrder (physician, patient, specimen (), shipment ());
        diagnostic.order = order (new OrderProperties (patient.billingType, CustomerShipment, "C91.00"), tests);
        diagnostic.order.mrn = patient.mrn;
        diagnostic.specimen.collectionDate = dateToArrInt (collectionDate);
        diagnostic.specimen.reconciliationDate = dateToArrInt (reconciliationDate);
        diagnostic.specimen.properties = new Specimen.SpecimenProperties ("2019-03-20");
        diagnostic.shipment.arrivalDate = dateToArrInt (arrivalDate);
        diagnostic.fastForwardStatus = stage;
        return diagnostic;
    }

    public static Diagnostic buildTdetectOrder (Physician physician,
                                                Patient patient,
                                                Stage stage,
                                                CoraTest test,
                                                Assay assay) {
        String covidPanel = "132d9440-8f75-46b8-b084-efe06346dfd4";
        String lymePanel = "21c3d625-4c5a-4e0b-896b-0d4f3e817d23";
        Diagnostic diagnostic = diagnosticOrder (physician, patient, null, shipment ());
        diagnostic.order = order (null, test);
        diagnostic.order.orderType = TDx;
        diagnostic.order.mrn = patient.mrn;
        diagnostic.order.billingType = patient.billingType;
        diagnostic.order.specimenDeliveryType = CustomerShipment;
        diagnostic.order.specimenDto = specimen ();
        diagnostic.order.specimenDto.name = test.workflowProperties.sampleName;
        diagnostic.order.specimenDto.properties = null;

        if (assay.equals (LYME_DX)) {
            diagnostic.order.panels = asList (new Panel (lymePanel));
        } else if (assay.equals (COVID19_DX_IVD)) {
            diagnostic.order.panels = asList (new Panel (covidPanel));
        }

        diagnostic.fastForwardStatus = stage;
        diagnostic.task = null;
        return diagnostic;
    }
}
