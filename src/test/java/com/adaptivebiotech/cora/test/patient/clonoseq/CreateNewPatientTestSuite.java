package com.adaptivebiotech.cora.test.patient.clonoseq;

import static com.adaptivebiotech.cora.utils.DateUtils.getPastFutureDate;
import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.CreateNewPatient;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = { "regression", "entlebucher" })
public class CreateNewPatientTestSuite extends CoraBaseBrowser {

    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private NewOrderTDetect  newOrderTDetect  = new NewOrderTDetect ();
    private Patient          patient          = newPatient ();
    private CreateNewPatient createNewPatient = new CreateNewPatient ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc.requirements SR-9302
     */
    public void VerifyPatientBirthdateValidation () {
        newOrderTDetect.selectNewTDetectDiagnosticOrder ();
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.clickPickPatient ();
        // check birthdate can't be in future
        patient.dateOfBirth = getPastFutureDate (2);
        createNewPatient.clickCreateNewPatient();
        createNewPatient.fillPatientInfo (patient);
        assertEquals(createNewPatient.getErrorMessage(), "Invalid date! Cannot be in future!");
        testLog ("Patient with dob in future can't be created");
        // check birthdate can't be earlier than 01/01/1900
        patient.dateOfBirth = "12/31/1899";
        createNewPatient.fillPatientInfo (patient);
        assertEquals(createNewPatient.getErrorMessage(), "Invalid date! Out of range");
        testLog ("Patient with dob earlier then 01/01/1900 can't be created");
        // check no error message for birthdate 01/01/1900
        patient.dateOfBirth = "01/01/1900";
        createNewPatient.fillPatientInfo (patient);
        assertEquals(createNewPatient.getErrorMessage(), "");
        testLog ("Patient with dob starting from 01/01/1900 can be created");
        // check birthdate today's date
        patient.dateOfBirth = getPastFutureDate (0);
        createNewPatient.fillPatientInfo (patient);
        assertEquals(createNewPatient.getErrorMessage(), "");
        testLog ("Patient with dob today can be created");
        createNewPatient.clickSave ();
        newOrderTDetect.isCorrectPage ();
        assertEquals (newOrderTDetect.getPatientName (), patient.fullname);
        assertEquals (newOrderTDetect.getPatientDOB (), patient.dateOfBirth);
        testLog ("Patient was created");
    }
}
