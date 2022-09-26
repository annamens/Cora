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
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
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
    private PatientsList      patientsList     = new PatientsList ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc.requirements SR-9302
     */
    @Test (groups = "entlebucher")
    public void verifyPatientBirthdateValidation () {
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
     * @sdlc.requirements SR-8370 ,SR-8369
     */
    public void characterLimitEmailAndOrderNotes () {
        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.selectPhysician (coraApi.getPhysician (clonoSEQ_selfpay));
        newOrderClonoSeq.clickPickPatient ();
        createNewPatient.clickCreateNewPatient ();
        createNewPatient.fillPatientInfo (patient);
        createNewPatient.clickSave ();
        newOrderClonoSeq.isCorrectPage ();

        String notes = TestHelper.randomString (50000);
        newOrderClonoSeq.enterOrderNotes (notes);
        testLog ("Order notes character length is: " + notes.length ());

        newOrderClonoSeq.billing.selectBilling (ChargeType.PatientSelfPay);
        String x = TestHelper.randomString (63);
        String email = TestHelper.randomString (64) + "@" + x + "." + x + "." + TestHelper.randomString (62);
        newOrderClonoSeq.billing.enterPatientEmail (email);
        newOrderClonoSeq.clickSave ();
        testLog ("Length of string: " + email.length ());
        assertEquals (newOrderClonoSeq.getToastError (), "Please fix errors in the form");
        String trimmedEmail = email.substring (0, 254);
        newOrderClonoSeq.billing.enterPatientEmail (trimmedEmail);
        newOrderClonoSeq.clickSave ();
        assertEquals (trimmedEmail.length (), 254);
        assertFalse (newOrderClonoSeq.isToastErrorPresent ());

        newOrderClonoSeq.clickPatientCode ();
        patientDetail.clickEditPatientShippingAddress ();
        patientDetail.enterEmail (email);
        patientDetail.clickSavePatientInsurance ();
        int maxLength = patientDetail.getShippingEmailEntered ().length ();
        testLog ("Entered email length is: " + maxLength);
        assertEquals (maxLength, 254);

        patientDetail.clickEditPatientBillingAddress ();
        patientDetail.enterEmail (email);
        patientDetail.clickSavePatientInsurance ();
        int length = patientDetail.getBillingEmailEntered ().length ();
        testLog ("Entered email length is: " + length);
        assertEquals (length, 254);
    }

    /**
     * @sdlc.requirements SR-4934
     */
    public void patientLastNameSearch () {
        newOrderClonoSeq.selectNewClonoSEQDiagnosticOrder ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.selectPhysician (coraApi.getPhysician (clonoSEQ_selfpay));

        newOrderClonoSeq.clickPickPatient ();
        patient.lastName = RandomStringUtils.randomNumeric (6);
        createNewPatient.searchOrCreatePatient (patient);
        createNewPatient.clickRemovePatient ();
        newOrderClonoSeq.clickPickPatient ();
        createNewPatient.searchPatientWithLastName (patient);
        assertTrue (createNewPatient.isPickPatientRowPresent ());
        assertFalse (createNewPatient.isNoPatientsFound ());
        String patientDetails = createNewPatient.getFirstRowPatient ();
        testLog ("Patient details searched with last name are: " + patientDetails);
        createNewPatient.clickSelectPatient ();
        newOrderClonoSeq.clickSave ();

        createNewPatient.clickPatients ();
        patientsList.searchPatient (patient.lastName);
        assertTrue (patientsList.isPatientListPresent ());
        assertFalse (patientsList.isNoResultsFoundPresent ());
        testLog ("Patient search with numeric lastname is working");
    }

}
