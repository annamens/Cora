package com.adaptivebiotech.cora.ui.order;

import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt2;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;

/**
 * @author jpatel
 *
 */
public class BillingNewOrderClonoSeq extends BillingNewOrder {

    private final String insuranceProvider            = "[name='insuranceProvider']";
    private final String groupNumber                  = "[name='groupNumber']";
    private final String policyNumber                 = "[name='policyNumber']";
    private final String insuredRelationship          = "[name='insuredRelationship']";
    private final String policyholder                 = "[name='policyholder']";
    private final String hospitalizationStatus        = "[name='hospitalizationStatus']";
    private final String institution                  = "[name='billingInstitution']";
    private final String dischargeDate                = "[name='dischargeDate']";
    private final String hasSecondaryInsurance        = "[name='isSecondaryInsurance'][value='true']";
    private final String secondaryInsuranceProvider   = "[name='secondaryInsuranceProvider']";
    private final String secondaryGroupNumber         = "[name='secondaryGroupNumber']";
    private final String secondaryPolicyNumber        = "[name='secondaryPolicyNumber']";
    private final String secondaryInsuredRelationship = "[name='secondaryInsuredRelationship']";
    private final String secondaryPolicyholder        = "[name='secondaryPolicyholder']";
    private final String hasTertiaryInsurance         = "[name='isTertiaryInsurance'][value='true']";
    private final String tertiaryInsuranceProvider    = "[name='tertiaryInsuranceProvider']";
    private final String tertiaryGroupNumber          = "[name='tertiaryGroupNumber']";
    private final String tertiaryPolicyNumber         = "[name='tertiaryPolicyNumber']";
    private final String tertiaryInsuredRelationship  = "[name='tertiaryInsuredRelationship']";
    private final String tertiaryPolicyholder         = "[name='tertiaryPolicyholder']";

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
        assertTrue (clickAndSelectValue (insuredRelationship, "string:" + relationship));
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
        assertTrue (clickAndSelectValue (hospitalizationStatus, "string:" + status));
    }

    public PatientStatus getInsurance1PatientStatus () {
        return getEnum (PatientStatus.class, getFirstSelectedText (hospitalizationStatus));
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
        assertTrue (clickAndSelectValue (secondaryInsuredRelationship, "string:" + relationship));
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
        assertTrue (clickAndSelectValue (tertiaryInsuredRelationship, "string:" + relationship));
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

    public String getPatientAddress1 () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.address1']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    protected String getPatientPhone () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.phone']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    protected String getPatientCity () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.locality']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    protected String getPatientState () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.region']";
        return isElementPresent (css) ? getFirstSelectedText (css) : null;
    }

    protected String getPatientZipcode () {
        String css = "[ng-model*='ctrl.orderEntry.orderBilling.guarantor.postCode']";
        return isElementPresent (css) ? readInput (css) : null;
    }
}
