/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.patient.clonoseq;

import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PickPatientModule;

@Test (groups = "regression")
public class CreateNewPatientTestSuite extends CoraBaseBrowser {

    private Login             login            = new Login ();
    private OrdersList        ordersList       = new OrdersList ();
    private NewOrderClonoSeq  newOrderClonoSeq = new NewOrderClonoSeq ();
    private Patient           patient          = newPatient ();
    private PickPatientModule createNewPatient = new PickPatientModule ();

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
}
