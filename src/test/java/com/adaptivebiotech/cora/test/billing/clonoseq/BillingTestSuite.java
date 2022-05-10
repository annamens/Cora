/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.billing.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Client;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.NoCharge;
import static com.adaptivebiotech.cora.dto.Orders.NoChargeReason.TimelinessOfBilling;
import static com.adaptivebiotech.cora.dto.Orders.NoChargeReason.getAllReasons;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_all_payments;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_insurance;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static com.adaptivebiotech.cora.utils.PageHelper.AbnStatus.NotRequired;
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
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

@Test (groups = "regression")
public class BillingTestSuite extends BillingTestBase {

    private final String        log         = "created an order with billing: %s";
    private final String[]      icdCodes    = { "V95.43XA" };
    private Login               login       = new Login ();
    private OrdersList          ordersList  = new OrdersList ();
    private NewOrderClonoSeq    diagnostic  = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq orderDetail = new OrderDetailClonoSeq ();
    private Specimen            specimen    = bloodSpecimen ();
    private NewShipment         shipment    = new NewShipment ();
    private Accession           accession   = new Accession ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    public void insurance () {
        Patient patient = newInsurancePatient ();
        patient.race = null;
        patient.ethnicity = null;

        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_insurance),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc.requirements 173.Medicare.required, SR-1516
     */
    public void medicare_abn_required () {
        Patient patient = newMedicarePatient ();
        patient.race = null;
        patient.ethnicity = null;

        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_medicare),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));

        assertEquals (orderDetail.billing.getAbnStatus (), patient.abnStatusType);
        testLog ("ABN Status dropdown was visible and was able to make a selection");
    }

    /**
     * @sdlc.requirements 173.Medicare.required, SR-1516
     */
    public void medicare_abn_not_required () {
        Patient patient = newMedicarePatient ();
        patient.race = null;
        patient.ethnicity = null;
        patient.abnStatusType = null;

        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_medicare),
                                        patient,
                                        new String[] { icdCodes[0], "C91.10" },
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));

        assertEquals (orderDetail.billing.getAbnStatus (), NotRequired);
        testLog ("ABN Status field value was 'Not required' and was not editable");
    }

    public void patient_self_pay () {
        Patient patient = newSelfPayPatient ();
        patient.race = null;
        patient.ethnicity = null;

        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_selfpay),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }

    public void client_bill () {
        Patient patient = newClientPatient ();
        patient.race = null;
        patient.ethnicity = null;

        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }

    public void bill_per_study_protocol () {
        Patient patient = newTrialProtocolPatient ();
        patient.race = null;
        patient.ethnicity = null;

        diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                        patient,
                                        icdCodes,
                                        ID_BCell2_CLIA,
                                        specimen,
                                        Active,
                                        Tube);
        testLog (format (log, patient.billingType.label));
    }

    /**
     * @sdlc.requirements SR-7593
     */
    @Test (groups = "entlebucher")
    public void verifyNoChargeReasonIsRequired () {
        Order order = diagnostic.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_all_payments),
                                                      newTrialProtocolPatient (),
                                                      icdCodes,
                                                      ID_BCell2_CLIA,
                                                      specimen);
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

        diagnostic.billing.selectReason (TimelinessOfBilling);
        diagnostic.activateOrder ();
        testLog ("Order activated");

        List <Map <String, Object>> queryResults = coraDb.executeSelect (noChargeReasonQuery + "'" + order.orderNumber + "'");
        assertEquals (queryResults.size (), 1);
        Map <String, Object> queryEmrData = queryResults.get (0);
        assertEquals (queryEmrData.get ("no_charge_reason").toString (), TimelinessOfBilling.label);
        testLog ("No charge reason is saved in DB");
    }
}
