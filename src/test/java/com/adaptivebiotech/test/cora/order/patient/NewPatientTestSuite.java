package com.adaptivebiotech.test.cora.order.patient;

import static com.adaptivebiotech.utils.PageHelper.PatientRelationship.Child;
import static com.adaptivebiotech.utils.PageHelper.PatientRelationship.Spouse;
import static com.adaptivebiotech.utils.PageHelper.PatientStatus.Inpatient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.test.cora.order.OrderTestBase;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.ui.cora.order.Billing;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

@Test (groups = { "cora-seeding" })
public class NewPatientTestSuite extends OrderTestBase {

    @BeforeMethod
    public void beforeMethod () {
        new CoraPage ().clickNewDiagnosticOrder ();
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
    }

    public void medicare () {
        Billing billing = new Billing ();
        billing.createNewPatient (patientMedicare);
        billing.selectBilling (patientMedicare.billingType);
        billing.enterInsurance1Provider (patientMedicare.insurance1.provider);
        billing.enterInsurance1Policy (patientMedicare.insurance1.policyNumber);
        billing.enterInsurance1Relationship (Child);
        billing.enterInsurance1PolicyHolder ("Moana");
        billing.enterInsurance1PatientStatus (Inpatient);
        billing.enterInsurance1Hospital ("Swedish Hospital");
        billing.enterInsurance1Discharge (patientMedicare.insurance1.dischargeDate);
        billing.addSecondaryInsurance ();
        billing.enterInsurance2Provider (patientMedicare.insurance2.provider);
        billing.enterInsurance2GroupNumber (patientMedicare.insurance2.groupNumber);
        billing.enterInsurance2Policy (patientMedicare.insurance2.policyNumber);
        billing.enterInsurance2Relationship (Spouse);
        billing.enterInsurance2PolicyHolder ("Fauna");
        billing.enterPatientAddress1 (patientMedicare.address.address1);
        billing.enterPatientPhone (patientMedicare.address.phone);
        billing.enterPatientCity (patientMedicare.address.city);
        billing.enterPatientState (patientMedicare.address.state);
        billing.enterPatientZipcode (patientMedicare.address.postCode);
        billing.clickSave ();
    }
}
