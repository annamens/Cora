package com.adaptivebiotech.cora.ui.patient;

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

    public void clickEditPatientDemographics () {
        String xpathForEditButton = "/html/body/div[4]/div/div/ui-view-ng-upgrade/ui-view/patient/div/div[2]/patient-details-tab/div/patient-demographics/div/div/div/div/div[2]/div/div[2]/button";

        assertTrue (click (xpathForEditButton));
        pageLoading ();

    }

    public void clickEditPatientInsurance () {
        String xpath = "/html/body/div[4]/div/div/ui-view-ng-upgrade/ui-view/patient/div/div[2]/patient-details-tab/div/patient-billing/div/div/div/div[2]/div[2]/div[2]/button";
        assertTrue (click (xpath));
        waitForElementVisible ("edit-patient-insurance-details");
    }

    public void enterPatientNotes (String notes) {
        String cssForTextField = ".patient-notes-section textarea";
        assertTrue (setText (cssForTextField, notes));
    }

    public void clickSave () {
        String cssForSaveButton = ".patient-notes-section .btn-primary";
        assertTrue (click (cssForSaveButton));
        waitForAjaxCalls ();
        pageLoading ();
    }

    public void clickClose () {
        String cssForCloseButton = "go-back";
        waitForElementClickable (cssForCloseButton);
        assertTrue (click (cssForCloseButton));
        waitForAjaxCalls ();
        moduleLoading ();
    }

    public String getPrimaryInsuranceProvider () {
        String css = "[label=\"Insurance Provider\"] > div > div:nth-child(2)";
        return getText (waitForElementVisible (css));
    }

    public String getPrimaryInsuranceGroupNumber () {
        String xpath = "/html/body/div[4]/div/div/ui-view-ng-upgrade/ui-view/patient/div/div[2]/patient-details-tab/div/patient-billing/div/div/div/div[2]/div[3]/div/div[1]/patient-insurance-details/div/labeled-value[2]/div/div[2]";
        return getText (waitForElementVisible (xpath));
    }

    public String getPrimaryInsurancePolicyNumber () {
          String xpath = "/html/body/div[4]/div/div/ui-view-ng-upgrade/ui-view/patient/div/div[2]/patient-details-tab/div/patient-billing/div/div/div/div[2]/div[3]/div/div[1]/patient-insurance-details/div/labeled-value[3]/div/div[2]";
        return getText (waitForElementVisible (xpath));
    }

    public String getPrimaryInsurancePatientRelationship () {
        String xpath = "/html/body/div[4]/div/div/ui-view-ng-upgrade/ui-view/patient/div/div[2]/patient-details-tab/div/patient-billing/div/div/div/div[2]/div[3]/div/div[1]/patient-insurance-details/div/labeled-value[4]/div/div[2]";
        return getText (waitForElementVisible (xpath));
    }

    public String getPrimaryInsurancePolicyholderName () {
        String xpath = "/html/body/div[4]/div/div/ui-view-ng-upgrade/ui-view/patient/div/div[2]/patient-details-tab/div/patient-billing/div/div/div/div[2]/div[3]/div/div[1]/patient-insurance-details/div/labeled-value[5]/div/div[2]";
        return getText (waitForElementVisible (xpath));
    }

}
