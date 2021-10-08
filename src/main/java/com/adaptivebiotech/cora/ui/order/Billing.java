package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.PageHelper.PatientStatus.NonHospital;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Patient.Address;
import com.adaptivebiotech.test.utils.PageHelper.AbnStatus;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Billing extends Diagnostic {

    private final String billingMismatchWarning = "[ng-if=\"ctrl.showBillingMismatchWarning()\"]";
    private final String billing                = "#billing-type";

    public void waitForBillingMismatchWarningVisible () {
        assertTrue (waitUntilVisible (billingMismatchWarning));
    }

    public String getBillingMismatchWarningText () {
        return getText (billingMismatchWarning);
    }

    public void selectBilling (ChargeType type) {
        assertTrue (clickAndSelectText (billing, type.label));
    }

    public ChargeType getBilling () {
        String type = getFirstSelectedValue (billing);
        return type != null && !type.equals ("?") ? ChargeType.valueOf (type.replace ("string:", "")) : null;
    }

    public List <String> getBillingDropDownOptions () {
        return getDropdownOptions (billing);
    }

    public void enterABNstatus (AbnStatus status) {
        assertTrue (clickAndSelectValue ("[name='abnStatusType']", "string:" + status));
    }

    public void enterInsurance1Provider (String provider) {
        assertTrue (setText ("[name='insuranceProvider']", provider));
    }

    public void enterInsurance1GroupNumber (String group) {
        assertTrue (setText ("[name='groupNumber']", group));
    }

    public void enterInsurance1Policy (String policy) {
        assertTrue (setText ("[name='policyNumber']", policy));
    }

    public void enterInsurance1Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue ("[name='insuredRelationship']", "string:" + relationship));
    }

    public void enterInsurance1PolicyHolder (String name) {
        assertTrue (setText ("[name='policyholder']", name));
    }

    public void enterInsurance1PatientStatus (PatientStatus status) {
        assertTrue (clickAndSelectValue ("[name='hospitalizationStatus']", "string:" + status));
    }

    public void enterInsurance1Hospital (String hospital) {
        assertTrue (setText ("[name='billingInstitution']", hospital));
    }

    public void enterInsurance1Discharge (String date) {
        assertTrue (setText ("[name='dischargeDate']", date));
    }

    public void addSecondaryInsurance () {
        assertTrue (click ("[name='isSecondaryInsurance'][value='true']"));
    }

    public void enterInsurance2Provider (String provider) {
        assertTrue (setText ("[name='secondaryInsuranceProvider']", provider));
    }

    public void enterInsurance2GroupNumber (String group) {
        assertTrue (setText ("[name='secondaryGroupNumber']", group));
    }

    public void enterInsurance2Policy (String policy) {
        assertTrue (setText ("[name='secondaryPolicyNumber']", policy));
    }

    public void enterInsurance2Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue ("[name='secondaryInsuredRelationship']", "string:" + relationship));
    }

    public void enterInsurance2PolicyHolder (String name) {
        assertTrue (setText ("[name='secondaryPolicyholder']", name));
    }

    public void enterPatientAddress1 (String address1) {
        assertTrue (setText ("//*[text()='Address 1']/..//input", address1));
    }

    public void enterPatientPhone (String phone) {
        assertTrue (setText ("//*[text()='Phone']/..//input", phone));
    }

    public void enterPatientCity (String city) {
        assertTrue (setText ("//*[text()='City']/..//input", city));
    }

    public void enterPatientState (String state) {
        assertTrue (clickAndSelectText ("//*[text()='State']/..//select",
                                        state == null ? "" : state));
    }

    public void enterPatientZipcode (String zipcode) {
        assertTrue (setText ("//*[text()='Zip Code']/..//input", zipcode));
    }

    public void enterPatientAddress (Address address) {
        enterPatientAddress1 (address.line1);
        enterPatientPhone (address.phone);
        enterPatientCity (address.city);
        enterPatientState (address.state);
        enterPatientZipcode (address.postalCode);
    }

    public void enterMedicareInfo (Patient patient) {
        selectBilling (patient.billingType);
        enterInsurance1Provider (patient.insurance1.provider);
        enterInsurance1Policy (patient.insurance1.policyNumber);
        enterInsurance1Relationship (patient.insurance1.insuredRelationship);
        if (!patient.insurance1.insuredRelationship.equals (PatientRelationship.Self))
            enterInsurance1PolicyHolder (patient.insurance1.policyholder);
        enterInsurance1PatientStatus (patient.insurance1.hospitalizationStatus);
        enterInsurance1Hospital (patient.insurance1.billingInstitution);
        enterInsurance1Discharge (patient.insurance1.dischargeDate);
        addSecondaryInsurance ();
        enterInsurance2Provider (patient.insurance2.provider);
        enterInsurance2GroupNumber (patient.insurance2.groupNumber);
        enterInsurance2Policy (patient.insurance2.policyNumber);
        enterInsurance2Relationship (patient.insurance2.insuredRelationship);
        if (!patient.insurance2.insuredRelationship.equals (PatientRelationship.Self))
            enterInsurance2PolicyHolder (patient.insurance2.policyholder);
    }

    public void enterInsuranceInfo (Patient patient) {
        selectBilling (patient.billingType);
        enterInsurance1Provider (patient.insurance1.provider);
        enterInsurance1GroupNumber (patient.insurance1.groupNumber);
        enterInsurance1Policy (patient.insurance1.policyNumber);
        enterInsurance1Relationship (patient.insurance1.insuredRelationship);
        if (!patient.insurance1.insuredRelationship.equals (PatientRelationship.Self))
            enterInsurance1PolicyHolder (patient.insurance1.policyholder);
        enterInsurance1PatientStatus (patient.insurance1.hospitalizationStatus);
        enterInsurance1Hospital (patient.insurance1.billingInstitution);
        enterInsurance1Discharge (patient.insurance1.dischargeDate);
        addSecondaryInsurance ();
        enterInsurance2Provider (patient.insurance2.provider);
        enterInsurance2GroupNumber (patient.insurance2.groupNumber);
        enterInsurance2Policy (patient.insurance2.policyNumber);
        enterInsurance2Relationship (patient.insurance2.insuredRelationship);
        if (!patient.insurance2.insuredRelationship.equals (PatientRelationship.Self))
            enterInsurance2PolicyHolder (patient.insurance2.policyholder);
    }

    public void enterBill (Patient patient) {
        selectBilling (patient.billingType);
        enterInsurance1PatientStatus (patient.insurance1.hospitalizationStatus);
        if (!NonHospital.equals (patient.insurance1.hospitalizationStatus)) {
            enterInsurance1Hospital (patient.insurance1.billingInstitution);
            enterInsurance1Discharge (patient.insurance1.dischargeDate);
        }
    }

    public void clickCompareAndSelectBilling () {
        String css = "[ng-click=\"ctrl.showCompareBillingModal()\"";
        assertTrue (click (css));
    }
}
