package com.adaptivebiotech.cora.ui.patient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class PatientDetail extends CoraPage {

    public PatientDetail () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement ("[role='tablist'] .active a", "PATIENT DETAILS"));
    }

    public String getFirstName () {
        return getText ("[label='First Name']").replace ("First Name", "").trim ();
    }

    public String getLastName () {
        return getText ("[label='Last Name']").replace ("Last Name", "").trim ();
    }

    public void clickEditPatientDemographics() {
        String xpathForEditButton = "//*[@class='demographics-details']/..//button";
        assertTrue(click(xpathForEditButton));
        String expectedTitle = "Edit Patient Demographics";
        String title = waitForElementVisible(".modal-title").getText ();
        assertEquals(title, expectedTitle);
    }
    
    public void clickEditPatientInsurance() {
        String xpath = "//*[label='Billing Type']/../../../..//button";
        assertTrue(click(xpath));
        String expectedTitle = "Edit Patient Insurance";
        String title = waitForElementVisible (".modal-title").getText ();
        assertEquals(title, expectedTitle);
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

    public void clickClose () {
        String cssForCloseButton = "go-back";
        assertTrue (click (cssForCloseButton));
        moduleLoading ();
    }
    
    public String getPrimaryInsuranceProvider () {
        String css = "[label=\"Insurance Provider\"] > div > div:nth-child(2)";
        return getText (waitForElementVisible (css));
    }

    public String getPrimaryInsuranceGroupNumber() {
        String css = "[label=\"Group Number\"] > div > div:nth-child(2)";
        return getText(waitForElementVisible(css));
    }
    
    public String getPrimaryInsurancePolicyNumber() {
        String css = "[label=\"Policy Number\"] > div > div:nth-child(2)";
        return getText(waitForElementVisible(css));
    }
    
    public String getPrimaryInsurancePatientRelationship() {
        String css = "[label=\"Patient Relationship to Policyholder\"] > div > div:nth-child(2)";
        return getText(waitForElementVisible(css));
    }
    
    public String getPrimaryInsurancePolicyholderName() {
        String css = "[label=\"Policyholder name\"] > div > div:nth-child(2)";
        return getText(waitForElementVisible(css));
    }
    
    
    // edit patient insurance popup

    
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
    

}
