package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;

/**
 * @author jpatel
 *
 */
public class BillingNewOrderClonoSeq extends BillingNewOrder {

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

    public void addTertiaryInsurance () {
        assertTrue (click ("[name='isTertiaryInsurance'][value='true']"));
    }

    public void enterInsurance3Provider (String provider) {
        assertTrue (setText ("[name='tertiaryInsuranceProvider']", provider));
    }

    public void enterInsurance3GroupNumber (String group) {
        assertTrue (setText ("[name='tertiaryGroupNumber']", group));
    }

    public void enterInsurance3Policy (String policy) {
        assertTrue (setText ("[name='tertiaryPolicyNumber']", policy));
    }

    public void enterInsurance3Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue ("[name='tertiaryInsuredRelationship']", "string:" + relationship));
    }

    public void enterInsurance3PolicyHolder (String name) {
        assertTrue (setText ("[name='tertiaryPolicyholder']", name));
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
