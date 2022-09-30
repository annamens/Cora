/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IUO_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import java.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PickPatientModule;

@Test (groups = "regression")
public class NewOrderTestSuite extends NewOrderTestBase {

    private Login             login            = new Login ();
    private OrdersList        ordersList       = new OrdersList ();
    private NewOrderClonoSeq  newOrderClonoSeq = new NewOrderClonoSeq ();
    private PickPatientModule pickpatient      = new PickPatientModule ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    public void sections_order () {
        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getSectionHeaders (), headers);
        testLog ("found the Order Authorization section below the Billing section of the clonoSEQ order form");
    }

    /**
     * @sdlc.requirements SR-8396:R2
     */
    @Test (groups = "dingo")
    public void billing_questions () {
        String log = "the qualifying questionnaires input fields are hidden for '%s'";

        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.billing.selectBilling (CommercialInsurance);
        assertFalse (newOrderClonoSeq.billing.isBillingQuestionsVisible ());
        testLog (format (log, CommercialInsurance.label));

        newOrderClonoSeq.billing.selectBilling (Medicare);
        assertFalse (newOrderClonoSeq.billing.isBillingQuestionsVisible ());
        testLog (format (log, Medicare.label));
    }

    /**
     * NOTE: SR-T4353
     * 
     * @sdlc.requirements SR-13087
     */
    @Test (groups = "irish-wolfhound")
    public void orderTestSelection () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genLocalDate (-3);
        Assay assayTest = ID_BCell2_IUO_CLIA;

        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.selectPhysician (coraApi.getPhysician (clonoSEQ_selfpay));
        newOrderClonoSeq.clickPickPatient ();
        pickpatient.searchOrCreatePatient (newTrialProtocolPatient ());
        newOrderClonoSeq.enterPatientICD_Codes ("C90.00");
        newOrderClonoSeq.clickEnterSpecimenDetails ();
        newOrderClonoSeq.enterSpecimenType (specimenDto.sampleType);
        newOrderClonoSeq.enterAntiCoagulant (specimenDto.anticoagulant);
        newOrderClonoSeq.enterCollectionDate ((LocalDate) specimenDto.collectionDate);
        newOrderClonoSeq.clickAssayTest (assayTest);
        newOrderClonoSeq.clickSave ();
        Assay testSelection = newOrderClonoSeq.getTestSelection ();

        assertEquals (testSelection, assayTest);
        testLog ("Test Selection set to: " + testSelection + ", with no additional order tests selected");
    }
}
