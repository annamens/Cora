package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt2;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import static org.testng.util.Strings.isNotNullAndNotEmpty;
import com.adaptivebiotech.cora.dto.Insurance.PatientRelationship;
import com.adaptivebiotech.cora.dto.Insurance.PatientStatus;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.utils.PageHelper.AbnStatus;

/**
 * @author jpatel
 *
 */
public class BillingNewOrderTDetect extends BillingNewOrder {

    private final String abnStatus                    = "#abn-status-type";
    private final String insuranceProvider            = "[formcontrolname='insuranceProvider']";
    private final String groupNumber                  = "[formcontrolname='groupNumber']";
    private final String policyNumber                 = "[formcontrolname='policyNumber']";
    private final String insuredRelationship          = "#primary-bill-relation-type";
    private final String policyholder                 = "[formcontrolname='policyholder']";
    private final String hospitalizationStatus        = "#hospitalization-status";
    private final String institution                  = "[formcontrolname='institution']";
    private final String dischargeDate                = "[formcontrolname='dischargeDate']";
    private final String hasSecondaryInsurance        = "[formcontrolname='hasSecondaryInsurance'][ng-reflect-value='true']";
    private final String secondaryInsuranceProvider   = "#secondaryInsuranceProvider";
    private final String secondaryGroupNumber         = "#secondaryGroupNumber";
    private final String secondaryPolicyNumber        = "#secondaryPolicyNumber";
    private final String secondaryInsuredRelationship = "#second-bill-relation-type";
    private final String secondaryPolicyholder        = "#secondaryPolicyholder";
    private final String hasTertiaryInsurance         = "[formcontrolname='hasTertiaryInsurance'][ng-reflect-value='true']";
    private final String tertiaryInsuranceProvider    = "#tertiaryInsuranceProvider";
    private final String tertiaryGroupNumber          = "#tertiaryGroupNumber";
    private final String tertiaryPolicyNumber         = "#tertiaryPolicyNumber";
    private final String tertiaryInsuredRelationship  = "#tertiary-bill-relation-type";
    private final String tertiaryPolicyholder         = "#tertiaryPolicyholder";
    private final String patientAddress1              = "[formcontrolname='address1']";
    private final String patientAddress2              = "[formcontrolname='address2']";
    private final String patientPhone                 = "[formcontrolname='phone']";
    private final String patientEmail                 = "[formcontrolname='email']";
    private final String patientCity                  = "[formcontrolname='locality']";
    private final String patientState                 = "[formcontrolname='region']";
    private final String patientZipcode               = "[formcontrolname='postCode']";

    public boolean isBillingQuestionsVisible () {
        return isElementPresent ("[ng-if='ctrl.hasInsuranceQuestions() && ctrl.isTdx()']");
    }

    public void enterABNstatus (AbnStatus status) {
        assertTrue (clickAndSelectValue (abnStatus, status.name ()));
    }

    protected AbnStatus getAbnStatus () {
        return getEnum (AbnStatus.class, getFirstSelectedValue (abnStatus));
    }

    public void enterInsurance1Provider (String provider) {
        assertTrue (setText (insuranceProvider, provider));
    }

    public String getInsurance1Provider () {
        return readInput (insuranceProvider);
    }

    public void enterInsurance1GroupNumber (String group) {
        assertTrue (setText (groupNumber, group));
    }

    public String getInsurance1GroupNumber () {
        return readInput (groupNumber);
    }

    public void enterInsurance1Policy (String policy) {
        assertTrue (setText (policyNumber, policy));
    }

    public String getInsurance1Policy () {
        return readInput (policyNumber);
    }

    public void enterInsurance1Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue (insuredRelationship, relationship.name ()));
    }

    public PatientRelationship getInsurance1Relationship () {
        return getEnum (PatientRelationship.class, getFirstSelectedText (insuredRelationship));
    }

    public void enterInsurance1PolicyHolder (String name) {
        assertTrue (setText (policyholder, name));
    }

    public String getInsurance1PolicyHolder () {
        return readInput (policyholder);
    }

    public void enterInsurance1PatientStatus (PatientStatus status) {
        assertTrue (clickAndSelectValue (hospitalizationStatus, status.name ()));
    }

    public PatientStatus getInsurance1PatientStatus () {
        return getEnum (PatientStatus.class, getFirstSelectedValue (hospitalizationStatus));
    }

    public void enterInsurance1Hospital (String hospital) {
        assertTrue (setText (institution, hospital));
    }

    public String getInsurance1Hospital () {
        return readInput (institution);
    }

    public void enterInsurance1Discharge (String date) {
        assertTrue (setText (dischargeDate, date));
    }

    public String getInsurance1DischargeDate () {
        String dt = readInput (dischargeDate);
        return dt != null ? formatDt1.format (formatDt2.parse (dt)) : dt;
    }

    public void addSecondaryInsurance () {
        assertTrue (click (hasSecondaryInsurance));
    }

    public boolean hasSecondaryInsurance () {
        return findElement (hasSecondaryInsurance).isSelected ();
    }

    public void enterInsurance2Provider (String provider) {
        assertTrue (setText (secondaryInsuranceProvider, provider));
    }

    public String getInsurance2Provider () {
        return readInput (secondaryInsuranceProvider);
    }

    public void enterInsurance2GroupNumber (String group) {
        assertTrue (setText (secondaryGroupNumber, group));
    }

    public String getInsurance2GroupNumber () {
        return readInput (secondaryGroupNumber);
    }

    public void enterInsurance2Policy (String policy) {
        assertTrue (setText (secondaryPolicyNumber, policy));
    }

    public String getInsurance2Policy () {
        return readInput (secondaryPolicyNumber);
    }

    public void enterInsurance2Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue (secondaryInsuredRelationship, relationship.name ()));
    }

    public PatientRelationship getInsurance2Relationship () {
        return getEnum (PatientRelationship.class, getFirstSelectedText (secondaryInsuredRelationship));
    }

    public void enterInsurance2PolicyHolder (String name) {
        assertTrue (setText (secondaryPolicyholder, name));
    }

    public String getInsurance2PolicyHolder () {
        return readInput (secondaryPolicyholder);
    }

    public void addTertiaryInsurance () {
        assertTrue (click (hasTertiaryInsurance));
    }

    public boolean hasTertiaryInsurance () {
        return findElement (hasTertiaryInsurance).isSelected ();
    }

    public void enterInsurance3Provider (String provider) {
        assertTrue (setText (tertiaryInsuranceProvider, provider));
    }

    public String getInsurance3Provider () {
        return readInput (tertiaryInsuranceProvider);
    }

    public void enterInsurance3GroupNumber (String group) {
        assertTrue (setText (tertiaryGroupNumber, group));
    }

    public String getInsurance3GroupNumber () {
        return readInput (tertiaryGroupNumber);
    }

    public void enterInsurance3Policy (String policy) {
        assertTrue (setText (tertiaryPolicyNumber, policy));
    }

    public String getInsurance3Policy () {
        return readInput (tertiaryPolicyNumber);
    }

    public void enterInsurance3Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue (tertiaryInsuredRelationship, relationship.name ()));
    }

    public PatientRelationship getInsurance3Relationship () {
        return getEnum (PatientRelationship.class, getFirstSelectedText (tertiaryInsuredRelationship));
    }

    public void enterInsurance3PolicyHolder (String name) {
        assertTrue (setText (tertiaryPolicyholder, name));
    }

    public String getInsurance3PolicyHolder () {
        return readInput (tertiaryPolicyholder);
    }

    public void enterPatientAddress1 (String address1) {
        assertTrue (setText (patientAddress1, address1));
    }

    public String getPatientAddress1 () {
        return readInput (patientAddress1);
    }

    // address2 is not required
    public void enterPatientAddress2 (String address2) {
        if (isNotNullAndNotEmpty (address2))
            assertTrue (setText (patientAddress2, address2));
    }

    public String getPatientAddress2 () {
        return readInput (patientAddress2);
    }

    public void enterPatientPhone (String phone) {
        assertTrue (setText (patientPhone, phone));
    }

    public String getPatientPhone () {
        return readInput (patientPhone);
    }

    // email is not required
    public void enterPatientEmail (String email) {
        if (isNotNullAndNotEmpty (email))
            assertTrue (setText (patientEmail, email));
    }

    public String getPatientEmail () {
        return readInput (patientEmail);
    }

    public void enterPatientCity (String city) {
        assertTrue (setText (patientCity, city));
    }

    public String getPatientCity () {
        return readInput (patientCity);
    }

    public void enterPatientState (String state) {
        assertTrue (clickAndSelectText (patientState, state));
    }

    public String getPatientState () {
        return getFirstSelectedText (patientState);
    }

    public void enterPatientZipcode (String zipcode) {
        assertTrue (setText (patientZipcode, zipcode));
    }

    public String getPatientZipcode () {
        return readInput (patientZipcode);
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
