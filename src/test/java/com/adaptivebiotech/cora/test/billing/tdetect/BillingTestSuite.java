/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.billing.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Client;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.NoCharge;
import static com.adaptivebiotech.cora.dto.Orders.NoChargeReason.IncompleteDocumentation;
import static com.adaptivebiotech.cora.dto.Orders.NoChargeReason.getAllReasons;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_all_payments;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_insurance;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_trial;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newInsurancePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newMedicarePatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.billing.BillingTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * Note:
 * - ICD codes and patient billing address are not required
 */
@Test (groups = "regression")
public class BillingTestSuite extends BillingTestBase {

    private final String    log        = "created an order with billing: %s";
    private Login           login      = new Login ();
    private OrdersList      ordersList = new OrdersList ();
    private NewOrderTDetect diagnostic = new NewOrderTDetect ();
    private Specimen        specimen   = bloodSpecimen ();
    private NewShipment     shipment   = new NewShipment ();
    private Accession       accession  = new Accession ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc.requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void insurance () {
        Patient patient = newInsurancePatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_insurance),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * Note:
     * - ABN Status is "Not Required" by default
     * 
     * @sdlc.requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void medicare () {
        Patient patient = newMedicarePatient ();
        patient.abnStatusType = null;
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_medicare),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc.requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void patient_self_pay () {
        Patient patient = newSelfPayPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc.requirements SR-7907:R1
     */
    @Test (groups = "corgi")
    public void client_bill () {
        Patient patient = newClientPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc.requirements SR-7907:R20
     */
    @Test (groups = "corgi")
    public void bill_per_study_protocol () {
        Patient patient = newTrialProtocolPatient ();
        diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_trial),
                                       patient,
                                       null,
                                       specimen.collectionDate.toString (),
                                       COVID19_DX_IVD,
                                       Active,
                                       Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc.requirements SR-7593
     */
    @Test (groups = "entlebucher")
    public void verifyNoChargeReasonIsRequired () {
        Order order = diagnostic.createTDetectOrder (coraApi.getPhysician (TDetect_all_payments),
                                                     newTrialProtocolPatient (),
                                                     null,
                                                     specimen.collectionDate.toString (),
                                                     COVID19_DX_IVD);
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (order.orderNumber);
        shipment.selectDiagnosticSpecimenContainerType (Vacutainer);
        shipment.clickSave ();
        shipment.clickAccessionTab ();
        accession.completeAccession ();
        diagnostic.isCorrectPage ();
        diagnostic.waitForSpecimenDelivery ();
        diagnostic.billing.selectBilling (Client);
        assertFalse (diagnostic.billing.isReasonVisible ());
        testLog ("Reason drop down is not visible when anything but No Charge is picked as billing option");

        diagnostic.billing.selectBilling (NoCharge);
        testLog ("No Charge is available as billing option for T Detect");

        assertTrue (diagnostic.billing.isReasonVisible ());
        testLog ("Reason drop down is visible when No Charge is picked as billing option");

        assertEquals (diagnostic.billing.getAllNoChargeReasons (), getAllReasons ());
        testLog ("No Charge Reason list contains all required values");

        diagnostic.clickSaveAndActivate ();
        assertTrue (diagnostic.billing.isErrorForNoChargeReasonVisible ());
        testLog ("Reason is required when No Charge is picked as billing option");

        diagnostic.billing.selectReason (IncompleteDocumentation);
        diagnostic.activateOrder ();
        testLog ("Order activated");

        List <Map <String, Object>> queryResults = coraDb.executeSelect (noChargeReasonQuery + "'" + order.orderNumber + "'");
        assertEquals (queryResults.size (), 1);
        Map <String, Object> queryEmrData = queryResults.get (0);
        assertEquals (queryEmrData.get ("no_charge_reason").toString (), IncompleteDocumentation.label);
        testLog ("No charge reason is saved in DB");
    }
}
