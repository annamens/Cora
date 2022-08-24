/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.patient;

import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Insurance.PatientRelationship;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class PatientDetail extends PatientHeader {

    public PatientDetail () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "PATIENT DETAILS"));
    }

    public String getPatientId () {
        return getDriver ().getCurrentUrl ().split ("patient/")[1];
    }

    public String getFirstName () {
        return getText ("[label='First Name']").replace ("First Name", "").trim ();
    }

    public String getLastName () {
        return getText ("[label='Last Name']").replace ("Last Name", "").trim ();
    }

    public String getDateOfBirth () {
        return getText ("[label='Birth Date']").replace ("Birth Date", "").trim ();
    }

    public String getPatientMRDStatus () {
        String css = ".patient-status";
        return getText (css);
    }

    public void clickEditPatientDemographics () {
        String xpathForEditButton = "//*[@class='demographics-details']/..//button//*[contains(@class,'glyphicon-pencil')]";
        assertTrue (click (xpathForEditButton));
        String expectedTitle = "Edit Patient Demographics";
        assertTrue (isTextInElement (popupTitle, expectedTitle));
    }

    public void clickEditPatientInsurance () {
        String xpath = "//*[label='Billing Type']//ancestor::div[@class='editable-section']//button";
        assertTrue (click (xpath));
        String expectedTitle = "Edit Patient Insurance";
        assertTrue (isTextInElement (popupTitle, expectedTitle));
    }

    public void clickEditPatientBillingAddress () {
        String xpath = "//*[div='Patient Billing Address']//ancestor::div[@class='editable-section']//button";
        assertTrue (click (xpath));
        String expectedTitle = "Edit Patient Contact Information";
        assertTrue (isTextInElement (popupTitle, expectedTitle));
    }

    public void enterPatientNotes (String notes) {
        String cssForTextField = ".patient-notes-section textarea";
        assertTrue (setText (cssForTextField, notes));
    }

    public void clickSave () {
        String cssForSaveButton = ".patient-notes-section .btn-primary";
        assertTrue (click (cssForSaveButton));
        pageLoading ();
    }

    public String getPrimaryInsuranceProvider () {
        String xpath = "//*[label=\"Insurance Provider\"]/following-sibling::div";
        return getText (xpath);
    }

    public String getPrimaryInsuranceGroupNumber () {
        String xpath = "//*[label=\"Group Number\"]/following-sibling::div";
        return getText (xpath);
    }

    public String getPrimaryInsurancePolicyNumber () {
        String xpath = "//*[label=\"Policy Number\"]/following-sibling::div";
        return getText (xpath);
    }

    public PatientRelationship getPrimaryInsurancePatientRelationship () {
        String xpath = "//*[label='Patient Relationship to Policyholder']/following-sibling::div";
        return getEnum (PatientRelationship.class, getText (xpath));
    }

    public String getPrimaryInsurancePolicyholderName () {
        String xpath = "//*[label=\"Policyholder name\"]/following-sibling::div";
        return getText (xpath);
    }

    public void setBillingType (String billingType) {
        String css = "#billingType";
        assertTrue (clickAndSelectText (css, billingType));
    }

    public void enterInsuranceProvider (String insuranceProvider) {
        assertTrue (setText ("#primaryInsuranceProvider", insuranceProvider));
    }

    public void enterGroupNumber (String groupNumber) {
        String css = "#primaryGroupNumber";
        assertTrue (setText (css, groupNumber));
    }

    public void enterPolicyNumber (String policyNumber) {
        String css = "#primaryPolicyNumber";
        assertTrue (setText (css, policyNumber));
    }

    public void setPrimaryInsuredRelationship (String relationship) {
        String css = "#primaryInsuredRelationship";
        assertTrue (clickAndSelectText (css, relationship));
    }

    public void enterPolicyholderName (String name) {
        String css = "#primaryPolicyholder";
        assertTrue (setText (css, name));
    }

    public void clickSavePatientInsurance () {
        assertTrue (click ("[type='submit']"));
        moduleLoading ();
    }

    public void enterAddressLine1 (String address) {
        String css = "#address";
        assertTrue (setText (css, address));
    }

    public void enterAddressLine2 (String address) {
        String css = "#address2";
        assertTrue (setText (css, address));
    }

    public void enterCity (String address) {
        String css = "#locality";
        assertTrue (setText (css, address));
    }

    public void enterState (String address) {
        String css = "#region";
        assertTrue (setText (css, address));
    }

    public void enterZip (String address) {
        String css = "#postCode";
        assertTrue (setText (css, address));
    }

    public void enterPhone (String address) {
        String css = "#phone";
        assertTrue (setText (css, address));
    }

    public void enterEmail (String address) {
        String css = "#email";
        assertTrue (setText (css, address));
    }

    public void linkPatient (String patientCode) {
        assertTrue (click (".glyphicon-link"));
        assertTrue (isTextInElement (".modal-content" + " " + ".modal-title", "Link ID Sequences"));
        assertTrue (setText ("#linkedPatientCode", patientCode));
        assertTrue (click ("//button[text()='Find']"));
        assertTrue (waitUntilVisible ("//button[text()='Remove']"));
        assertTrue (click ("//button[text()='Link Patients']"));
        assertTrue (isTextInElement (".modal-content" + " " + ".modal-title", "Link ID Sequences Confirmation"));
        assertTrue (click ("//button[text()='Yes, Link Patients']"));
    }
}
