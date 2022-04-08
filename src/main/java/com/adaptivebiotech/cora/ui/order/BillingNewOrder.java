package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.cora.dto.Insurance.PatientRelationship.Self;
import static com.adaptivebiotech.cora.dto.Insurance.PatientStatus.NonHospital;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.WebDriver;
import com.adaptivebiotech.cora.dto.Insurance.PatientRelationship;
import com.adaptivebiotech.cora.dto.Insurance.PatientStatus;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Orders.NoChargeReason;
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
    private final String reason                 = "#no-charge-reason-type";

    public void waitForBillingMismatchWarningVisible () {
        assertTrue (waitUntilVisible (billingMismatchWarning));
    }

    public String getBillingMismatchWarningText () {
        return getText (billingMismatchWarning);
    }

    public void selectBilling (ChargeType type) {
        assertTrue (clickAndSelectText (billing, type.label));
    }

    public boolean isReasonVisible () {
        return isElementVisible (reason);
    }

    public void selectReason (NoChargeReason reasonType) {
        assertTrue (clickAndSelectText (reason, reasonType.label));
    }

    public void waitForBilling () {
        assertTrue (waitUntil (millisDuration, millisPoll, new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                return getDropdownOptions (billing).size () > 1;
            }
        }));
    }

    public ChargeType getBilling () {
        return getEnum (ChargeType.class, getFirstSelectedValue (billing).replace ("string:", ""));
    }

    public List <String> getBillingDropDownOptions () {
        return getDropdownOptions (billing);
    }

    public boolean isBillingAddressVisible () {
        return isElementPresent ("//*[contains (text(),'Patient Billing Address')]");
    }

    public abstract void enterABNstatus (AbnStatus status);

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

    public abstract void enterPatientAddress1 (String address1);

    public abstract String getPatientAddress1 ();

    public abstract void enterPatientAddress2 (String address2);

    public abstract String getPatientAddress2 ();

    public abstract void enterPatientPhone (String phone);

    public abstract String getPatientPhone ();

    public abstract void enterPatientEmail (String email);

    public abstract String getPatientEmail ();

    public abstract void enterPatientCity (String city);

    public abstract String getPatientCity ();

    public abstract void enterPatientState (String state);

    public abstract String getPatientState ();

    public abstract void enterPatientZipcode (String zipcode);

    public abstract String getPatientZipcode ();

    public void enterPatientAddress (Patient patient) {
        enterPatientAddress1 (patient.address);
        enterPatientAddress2 (patient.address2);
        enterPatientPhone (patient.phone);
        enterPatientEmail (patient.email);
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

    public Patient getPatientBillingAddress () {
        Patient patient = new Patient ();
        patient.address = getPatientAddress1 ();
        patient.address2 = getPatientAddress2 ();
        patient.locality = getPatientCity ();
        patient.region = getPatientState ();
        patient.postCode = getPatientZipcode ();
        patient.phone = getPatientPhone ();
        patient.email = getPatientEmail ();
        return patient;
    }
}
