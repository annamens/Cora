/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.patient.tdetect;

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
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.patient.PickPatientModule;
import com.adaptivebiotech.test.utils.TestHelper;

@Test (groups = "regression")
public class CreateNewPatientTestSuite extends CoraBaseBrowser {

    private Login             login            = new Login ();
    private OrdersList        ordersList       = new OrdersList ();
    private NewOrderTDetect   newOrderTDetect  = new NewOrderTDetect ();
    private PatientsList      patientsList     = new PatientsList ();
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
    public void verifyPatientBirthdateValidation () {
        newOrderTDetect.selectNewTDetectDiagnosticOrder ();
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.clickPickPatient ();

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
        newOrderTDetect.isCorrectPage ();
        assertEquals (newOrderTDetect.getPatientName (), patient.fullname);
        assertEquals (newOrderTDetect.getPatientDOB (), patient.dateOfBirth);
        testLog ("Patient was created");
    }

    /**
     * @sdlc.requirements SR-8370 ,SR-8369
     */
    @Test (groups = "irish-wolfhound")
    public void characterLimitEmailAndOrderNotes () {
        newOrderTDetect.selectNewTDetectDiagnosticOrder ();
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.selectPhysician (coraApi.getPhysician (clonoSEQ_selfpay));
        newOrderTDetect.clickPickPatient ();
        createNewPatient.clickCreateNewPatient ();
        createNewPatient.fillPatientInfo (patient);
        createNewPatient.clickSave ();
        newOrderTDetect.isCorrectPage ();

        String notes = TestHelper.randomString (50000);
        newOrderTDetect.enterOrderNotes (notes);
        testLog ("Order notes character length is: " + notes.length ());

        newOrderTDetect.billing.selectBilling (ChargeType.PatientSelfPay);
        String char63 = TestHelper.randomString (63);
        String email = TestHelper.randomString (64) + "@" + char63 + "." + char63 + "." + TestHelper.randomString (62);
        newOrderTDetect.billing.enterPatientEmail (email);
        newOrderTDetect.clickSave ();
        testLog ("Length of string: " + email.length ());
        assertEquals (newOrderTDetect.getToastError (), "Please fix errors in the form");
        String trimmedEmail = email.substring (0, 254);
        newOrderTDetect.billing.enterPatientEmail (trimmedEmail);
        newOrderTDetect.clickSave ();
        assertEquals (trimmedEmail.length (), 254);
        assertFalse (newOrderTDetect.isToastErrorPresent ());

        newOrderTDetect.clickPatientCode ();
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
    @Test (groups = "irish-wolfhound")
    public void patientLastNameSearch () {
        Patient patientDigit = newPatient ();
        patientDigit.firstName = "T-Detect";
        patient.lastName = "493402";
        newOrderTDetect.selectNewTDetectDiagnosticOrder ();
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.selectPhysician (coraApi.getPhysician (clonoSEQ_selfpay));

        newOrderTDetect.clickPickPatient ();
        createNewPatient.searchOrCreatePatient (patientDigit);
        createNewPatient.clickRemovePatient ();
        newOrderTDetect.clickPickPatient ();
        createNewPatient.searchPatientWithLastName (patientDigit);
        assertTrue (createNewPatient.isPickPatientRowPresent ());
        assertFalse (createNewPatient.isNoPatientsFound ());
        String patientDetails = createNewPatient.getFirstRowPatient ();
        assertEquals (patientDetails.split ("\n")[0], patientDigit.firstName);
        assertEquals (patientDetails.split ("\n")[1], patientDigit.lastName);
        assertEquals (patientDetails.split ("\n")[2], patientDigit.dateOfBirth);
        testLog ("Patient details searched with last name are: " + patientDetails);
        testLog ("Patient search with numeric lastname is working");

        createNewPatient.clickCancel ();
        newOrderTDetect.clickPatients ();
        patientsList.ignoredUnsavedChanges ();
        patientsList.waitForNewPatientToAppear (patientDetails.split ("\n")[3]);
        testLog ("numeric lastname patient is searchable on Patient List page");

        assertEquals (patientDetail.getFirstName (), patientDigit.firstName);
        assertEquals (patientDetail.getLastName (), patientDigit.lastName);
        assertEquals (patientDetail.getDateOfBirth (), patientDigit.dateOfBirth);
        testLog ("validate numeric lastname patient on Patient Details page");
    }

}
