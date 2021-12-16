package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Patient.Address;
import com.adaptivebiotech.test.utils.PageHelper.PatientRelationship;
import com.adaptivebiotech.test.utils.PageHelper.PatientStatus;

/**
 * @author jpatel
 *
 */
public class BillingNewOrderTDetect extends BillingNewOrder {

    public void enterInsurance1Provider (String provider) {
        assertTrue (setText ("[formcontrolname='insuranceProvider']", provider));
    }

    public void enterInsurance1GroupNumber (String group) {
        assertTrue (setText ("[formcontrolname='groupNumber']", group));
    }

    public void enterInsurance1Policy (String policy) {
        assertTrue (setText ("[formcontrolname='policyNumber']", policy));
    }

    public void enterInsurance1Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue ("[formcontrolname='insuredRelationship']", relationship.name ()));
    }

    public void enterInsurance1PolicyHolder (String name) {
        assertTrue (setText ("[formcontrolname='policyholder']", name));
    }

    public void enterInsurance1PatientStatus (PatientStatus status) {
        assertTrue (clickAndSelectValue ("[formcontrolname='hospitalizationStatus']", status.name ()));
    }

    public void enterInsurance1Hospital (String hospital) {
        assertTrue (setText ("[formcontrolname='billingInstitution']", hospital));
    }

    public void enterInsurance1Discharge (String date) {
        assertTrue (setText ("[formcontrolname='dischargeDate']", date));
    }

    public void addSecondaryInsurance () {
        assertTrue (click ("[formcontrolname='hasSecondaryInsurance'][ng-reflect-value='true']"));
    }

    public void enterInsurance2Provider (String provider) {
        assertTrue (setText ("[formcontrolname='secondaryInsuranceProvider']", provider));
    }

    public void enterInsurance2GroupNumber (String group) {
        assertTrue (setText ("[formcontrolname='secondaryGroupNumber']", group));
    }

    public void enterInsurance2Policy (String policy) {
        assertTrue (setText ("[formcontrolname='secondaryPolicyNumber']", policy));
    }

    public void enterInsurance2Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue ("[formcontrolname='secondaryInsuredRelationship']", "string:" + relationship));
    }

    public void enterInsurance2PolicyHolder (String name) {
        assertTrue (setText ("[formcontrolname='secondaryPolicyholder']", name));
    }

    public void addTertiaryInsurance () {
        assertTrue (click ("[formcontrolname='hasTertiaryInsurance'][ng-reflect-value='true']"));
    }

    public void enterInsurance3Provider (String provider) {
        assertTrue (setText ("[formcontrolname='tertiaryInsuranceProvider']", provider));
    }

    public void enterInsurance3GroupNumber (String group) {
        assertTrue (setText ("[formcontrolname='tertiaryGroupNumber']", group));
    }

    public void enterInsurance3Policy (String policy) {
        assertTrue (setText ("[formcontrolname='tertiaryPolicyNumber']", policy));
    }

    public void enterInsurance3Relationship (PatientRelationship relationship) {
        assertTrue (clickAndSelectValue ("[formcontrolname='tertiaryInsuredRelationship']", "string:" + relationship));
    }

    public void enterInsurance3PolicyHolder (String name) {
        assertTrue (setText ("[formcontrolname='tertiaryPolicyholder']", name));
    }

    public String getPatientAddress1 () {
        String css = "[formcontrolname='address1']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientAddress2 () {
        String css = "[formcontrolname='address2']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientCity () {
        String css = "[formcontrolname='locality']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientState () {
        String css = "[formcontrolname='region']";
        return isElementPresent (css) ? getFirstSelectedText (css) : null;
    }

    public String getPatientZipcode () {
        String css = "[formcontrolname='postCode']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientPhone () {
        String css = "[formcontrolname='phone']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public String getPatientEmail () {
        String css = "[formcontrolname='email']";
        return isElementPresent (css) ? readInput (css) : null;
    }

    public Address getPatientBillingAddress () {
        Address address = new Address ();
        address.line1 = getPatientAddress1 ();
        address.line2 = getPatientAddress2 ();
        address.city = getPatientCity ();
        address.state = getPatientState ();
        address.postalCode = getPatientZipcode ();
        address.phone = getPatientPhone ();
        address.email = getPatientEmail ();
        return address;
    }

}
