/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.patient.clonoseq;

import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PickPatientModule;
import com.adaptivebiotech.test.utils.TestHelper;

@Test (groups = "regression")
public class CreateNewPatientTestSuite extends CoraBaseBrowser {

    private Login             login            = new Login ();
    private OrdersList        ordersList       = new OrdersList ();
    private NewOrderClonoSeq  newOrderClonoSeq = new NewOrderClonoSeq ();
    private Patient           patient          = newPatient ();
    private PickPatientModule createNewPatient = new PickPatientModule ();
    private PatientDetail     patientDetail    = new PatientDetail ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc.requirements SR-9302
     */
    @Test (groups = "entlebucher")
    public void verifyPatientBirthdateValidation1 () {
        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.clickPickPatient ();

        // check birthdate can't be in future
        patient.dateOfBirth = genDate (2);
        createNewPatient.clickCreateNewPatient ();
        createNewPatient.fillPatientInfo (patient);
        assertEquals (createNewPatient.getBirthDateErrorMessage (), "Invalid date! Cannot be in future!");
        testLog ("Patient with dob in future can't be created");

        // check birthdate can't be earlier than 01/01/1900
        patient.dateOfBirth = "12/31/1899";
        createNewPatient.fillPatientInfo (patient);
        assertEquals (createNewPatient.getBirthDateErrorMessage (), "Invalid date! Out of range!");
        testLog ("Patient with dob earlier then 01/01/1900 can't be created");

        // check no error message for birthdate 01/01/1900
        patient.dateOfBirth = "01/01/1900";
        createNewPatient.fillPatientInfo (patient);
        assertFalse (createNewPatient.isBirthDateErrorVisible ());
        testLog ("Patient with dob starting from 01/01/1900 can be created");

        // check birthdate today's date
        patient.dateOfBirth = genDate (0);
        createNewPatient.fillPatientInfo (patient);
        assertFalse (createNewPatient.isBirthDateErrorVisible ());
        testLog ("Patient with dob today can be created");

        createNewPatient.clickSave ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getPatientName (), patient.fullname);
        assertEquals (newOrderClonoSeq.getPatientDOB (), patient.dateOfBirth);
        testLog ("Patient was created");
    }

    /**
     * @sdlc.requirements SR-12902,SR-12907
     */
    public void characterLimitEmailAndorderNotes () {

        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.selectPhysician (coraApi.getPhysician (clonoSEQ_selfpay));
        newOrderClonoSeq.clickPickPatient ();
        createNewPatient.clickCreateNewPatient ();
        createNewPatient.fillPatientInfo (patient);
        createNewPatient.clickSave ();
        newOrderClonoSeq.isCorrectPage ();

        int maxlength = 1000;
        String notes = TestHelper.randomString (maxlength + 1);
        newOrderClonoSeq.enterOrderNotes (notes);
        assertTrue (newOrderClonoSeq.isOrderNotesErrorPresent ());
        notes = notes.substring (0, 1000); // remove after test env changes
        newOrderClonoSeq.enterOrderNotes (notes);
        testLog ("Order notes character limit was removed");

        newOrderClonoSeq.billing.selectBilling (ChargeType.PatientSelfPay);
        String email = TestHelper.randomString (64) + "@" + TestHelper.randomString (64);
        newOrderClonoSeq.billing.enterPatientEmail (email);
        newOrderClonoSeq.clickSave ();
        testLog ("Length of string: " + email.length ());
        assertEquals (newOrderClonoSeq.getToastError (), "Please fix errors in the form");
        email = email.substring (0, 128);
        newOrderClonoSeq.billing.enterPatientEmail (email);
        newOrderClonoSeq.clickSave ();
        assertEquals (email.length (), 128);
        assertFalse (newOrderClonoSeq.isToastErrorPresent ());

        newOrderClonoSeq.clickPatientCode ();
        patientDetail.clickEditPatientShippingAddress ();
        String patientEmail = TestHelper.randomString (12) + "@" + TestHelper.randomString (24);
        patientDetail.enterEmail (patientEmail);
        patientDetail.clickSavePatientInsurance ();
        int maxLength = patientDetail.getEmailEntered ().length ();
        testLog ("Entered email is: " + patientDetail.getEmailEntered ());
        assertEquals (maxLength, 32);

        patientDetail.clickEditPatientBillingAddress ();
        patientDetail.enterEmail (patientEmail);
        patientDetail.clickSavePatientInsurance ();
        int length = patientDetail.getEmailEntered ().length ();
        testLog ("Entered email is: " + patientDetail.getEmailEntered ());
        assertEquals (length, 32);
    }

}
