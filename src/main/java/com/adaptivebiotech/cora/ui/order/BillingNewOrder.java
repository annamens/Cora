package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Insurance.PatientRelationship.Self;
import static com.adaptivebiotech.cora.dto.Insurance.PatientStatus.NonHospital;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Insurance.PatientRelationship;
import com.adaptivebiotech.cora.dto.Insurance.PatientStatus;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public abstract class BillingNewOrder extends CoraPage {

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
        return getEnum (ChargeType.class, getFirstSelectedValue (billing).replace ("string:", ""));
    }

    public List <String> getBillingDropDownOptions () {
        return getDropdownOptions (billing);
    }

    public void enterABNstatus (AbnStatus status) {
        assertTrue (clickAndSelectValue ("[name='abnStatusType']", "string:" + status));
    }

    protected AbnStatus getAbnStatus () {
        String css = "[ng-model^='ctrl.orderEntry.order.abnStatusType']";
        return AbnStatus.getAbnStatus (getFirstSelectedText (css));
    }

    public abstract void enterInsurance1Provider (String provider);

    public abstract void enterInsurance1GroupNumber (String group);

    public abstract void enterInsurance1Policy (String policy);

    public abstract void enterInsurance1Relationship (PatientRelationship relationship);

    public abstract void enterInsurance1PolicyHolder (String name);

    public abstract void enterInsurance1PatientStatus (PatientStatus status);

    public abstract void enterInsurance1Hospital (String hospital);

    public abstract void enterInsurance1Discharge (String date);

    public abstract void addSecondaryInsurance ();

    public abstract void enterInsurance2Provider (String provider);

    public abstract void enterInsurance2GroupNumber (String group);

    public abstract void enterInsurance2Policy (String policy);

    public abstract void enterInsurance2Relationship (PatientRelationship relationship);

    public abstract void enterInsurance2PolicyHolder (String name);

    public abstract void addTertiaryInsurance ();

    public abstract void enterInsurance3Provider (String provider);

    public abstract void enterInsurance3GroupNumber (String group);

    public abstract void enterInsurance3Policy (String policy);

    public abstract void enterInsurance3Relationship (PatientRelationship relationship);

    public abstract void enterInsurance3PolicyHolder (String name);

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

    public void enterPatientAddress (Patient patient) {
        enterPatientAddress1 (patient.address);
        enterPatientPhone (patient.phone);
        enterPatientCity (patient.locality);
        enterPatientState (patient.region);
        enterPatientZipcode (patient.postCode);
    }

    public void enterMedicareInfo (Patient patient) {
        selectBilling (patient.billingType);
        if (patient.abnStatusType != null)
            enterABNstatus (patient.abnStatusType);
        enterInsurance1Provider (patient.insurance1.provider);
        enterInsurance1Policy (patient.insurance1.policyNumber);
        enterInsurance1Relationship (patient.insurance1.insuredRelationship);
        if (!patient.insurance1.insuredRelationship.equals (Self))
            enterInsurance1PolicyHolder (patient.insurance1.policyholder);
        enterInsurance1PatientStatus (patient.insurance1.hospitalizationStatus);
        enterInsurance1Hospital (patient.insurance1.billingInstitution);
        enterInsurance1Discharge (patient.insurance1.dischargeDate);

        if (patient.insurance2 != null) {
            addSecondaryInsurance ();
            enterInsurance2Provider (patient.insurance2.provider);
            enterInsurance2GroupNumber (patient.insurance2.groupNumber);
            enterInsurance2Policy (patient.insurance2.policyNumber);
            enterInsurance2Relationship (patient.insurance2.insuredRelationship);
            if (!patient.insurance2.insuredRelationship.equals (Self))
                enterInsurance2PolicyHolder (patient.insurance2.policyholder);
        }

        if (patient.insurance3 != null) {
            addTertiaryInsurance ();
            enterInsurance3Provider (patient.insurance3.provider);
            enterInsurance3GroupNumber (patient.insurance3.groupNumber);
            enterInsurance3Policy (patient.insurance3.policyNumber);
            enterInsurance3Relationship (patient.insurance3.insuredRelationship);
            if (!patient.insurance3.insuredRelationship.equals (Self))
                enterInsurance3PolicyHolder (patient.insurance3.policyholder);
        }
    }

    public void enterInsuranceInfo (Patient patient) {
        selectBilling (patient.billingType);
        enterInsurance1Provider (patient.insurance1.provider);
        enterInsurance1GroupNumber (patient.insurance1.groupNumber);
        enterInsurance1Policy (patient.insurance1.policyNumber);
        enterInsurance1Relationship (patient.insurance1.insuredRelationship);
        if (!patient.insurance1.insuredRelationship.equals (Self))
            enterInsurance1PolicyHolder (patient.insurance1.policyholder);
        enterInsurance1PatientStatus (patient.insurance1.hospitalizationStatus);
        enterInsurance1Hospital (patient.insurance1.billingInstitution);
        enterInsurance1Discharge (patient.insurance1.dischargeDate);

        if (patient.insurance2 != null) {
            addSecondaryInsurance ();
            enterInsurance2Provider (patient.insurance2.provider);
            enterInsurance2GroupNumber (patient.insurance2.groupNumber);
            enterInsurance2Policy (patient.insurance2.policyNumber);
            enterInsurance2Relationship (patient.insurance2.insuredRelationship);
            if (!patient.insurance2.insuredRelationship.equals (Self))
                enterInsurance2PolicyHolder (patient.insurance2.policyholder);
        }

        if (patient.insurance3 != null) {
            addTertiaryInsurance ();
            enterInsurance3Provider (patient.insurance3.provider);
            enterInsurance3GroupNumber (patient.insurance3.groupNumber);
            enterInsurance3Policy (patient.insurance3.policyNumber);
            enterInsurance3Relationship (patient.insurance3.insuredRelationship);
            if (!patient.insurance3.insuredRelationship.equals (Self))
                enterInsurance3PolicyHolder (patient.insurance3.policyholder);
        }
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
