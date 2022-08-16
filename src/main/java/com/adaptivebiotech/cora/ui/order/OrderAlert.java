/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static java.lang.String.join;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import com.seleniumfy.test.utils.BasePage;

public class OrderAlert extends BasePage {

    private final String alertModal       = ".modal-content";
    private final String alertTitle       = ".modal-title";
    private final String alertType        = "[name='select-alert-type'] select";
    private final String closeBtn         = "//button[text()='Close']";
    private final String activeTab        = "//span[contains(text(), 'ACTIVE')]";
    private final String resolveTab       = "//span[contains(text(), 'RESOLVED')]";
    private final String activeAlerts     = ".panel.active-alerts";
    private final String resolvedAlerts   = ".panel.resolved-alerts";
    private final String accordionOpen    = "accordion-group[isopen='true']";
    private final String alertDescription = ".alert-type-description";
    private final String saveBtn          = "//*[text()='Save']";
    private final String cancelBtn        = "//*[text()='Cancel']";
    private final String panelOpen        = ".panel-open";
    private final String newAlert         = ".new-alert";
    private final String addAlert         = "//select[@class='form-control ng-untouched ng-pristine ng-valid']";
    private final String medicalNecOption = "//option[@value='2: Object']";
    private final String pathologyOption  = "//option[@value='4: Object']";
    private final String correctedOption  = "//option[@value='3: Object']";
    private final String clinicalOption   = "//option[@value='7: Object']";
    private final String saveNewAlert     = "//button[@class='btn btn-primary mar-right-10 mar-top-10']";
    private final String expandTopAlert   = "//span[@class='alert-expand glyphicon glyphicon-triangle-right']";
    private final String expandEmails     = "//span[@class='btn glyphicon alert-expand glyphicon-triangle-right']";
    private final String resolveTopAlert  = "//button[@class='btn btn-primary resolve-alert-button']";
    private final String boxChecked       = "//input[@ng-reflect-model='true']";
    private final String noBoxChecked     = "//input[@ng-reflect-model='false']";
    private final String closeExpandAlert = "//button[@class='btn btn-secondary pull-right']";
    private final String getAlertNote     = String.join (" ", panelOpen, ".alert-panel-content textarea");
    private final String addAlertNote     = String.join (" ", panelOpen, ".add-alert-type textarea");

    public void isCorrectPage () {
        assertTrue (waitUntilVisible (alertModal));
        assertTrue (isTextInElement (alertModal + " " + alertTitle, "Alerts for Order"));
    }

    public void isCorrectPage (String orderNo) {
        isCorrectPage ();
        assertTrue (isTextInElement (alertModal + " " + alertTitle, "Alerts for Order #" + orderNo));
    }

    public void clickNewAlert () {
        assertTrue (click (newAlert));
    }

    public void clickAddAlert () {
        assertTrue (click (addAlert));
    }

    public void addLetterOfMedicalNecessity () {
        clickNewAlert ();
        clickAddAlert ();
        assertTrue (click (medicalNecOption));
    }

    public void addPathologyReport () {
        clickNewAlert ();
        clickAddAlert ();
        assertTrue (click (pathologyOption));
    }

    public void addCorrectedReport () {
        clickNewAlert ();
        clickAddAlert ();
        assertTrue (click (correctedOption));
    }

    public void addClinicalConsultationOption () {
        clickNewAlert ();
        clickAddAlert ();
        assertTrue (click (clinicalOption));
    }

    public void clickSaveNewAlert () {
        assertTrue (click (saveNewAlert));
    }

    public void expandTopAlert () {
        assertTrue (click (expandTopAlert));
    }

    public void expandEmailsFromTopAlert () {
        assertTrue (click (expandEmails));
    }

    public void resolveTopAlert () {
        clickNewAlert ();
        assertTrue (click (resolveTopAlert));
        clickClose ();
    }

    public boolean noBoxesChecked () {
        while (!isElementPresent ("//input[@class='recipient-email ng-untouched ng-pristine ng-valid']")) {}
        assertTrue (!isElementPresent (boxChecked));
        return isElementPresent (noBoxChecked);
    }

    public void closeExpandedAlert () {
        assertTrue (click (closeExpandAlert));
    }

    public boolean isAlertModalPresent () {
        return isElementPresent (alertModal);
    }

    public void selectAlertType (String alertName) {
        assertTrue (clickAndSelectText (alertType, alertName));
    }

    public void clickClose () {
        assertTrue (click (closeBtn));
    }

    public void clickActiveTab () {
        assertTrue (click (activeTab));
    }

    public void clickResolveTab () {
        assertTrue (click (resolveTab));
    }

    public void clickSave (String alertName) {
        assertTrue (isTextInElement (join (" ", accordionOpen, ".alert-type-description-add"), alertName));
        assertTrue (click (saveBtn));
    }

    public void clickCancel () {
        assertTrue (click (cancelBtn));
    }

    public List <String> getActiveAlertsList () {
        if (isElementPresent (activeAlerts + " " + alertDescription)) {
            return getTextList (activeAlerts + " " + alertDescription);
        }
        return new ArrayList <> ();
    }

    public List <String> getResolvedAlertsList () {
        if (isElementPresent (resolvedAlerts + " " + alertDescription)) {
            return getTextList (resolvedAlerts + " " + alertDescription);
        }
        return new ArrayList <> ();
    }

    public void clickAlert (String alertName) {
        assertTrue (click (String.format ("//*[@class='alert-type-description' and text()='%s']", alertName)));
    }

    public void clickAlertResolveBtn (String alertName) {
        assertTrue (click (String.format ("//*[@class='alert-type-description' and text()='%s']/ancestor::accordion-group//button[text()='Resolve']",
                                          alertName)));
    }

    public boolean isEmailNotificationPresent (String alertName) {
        return isElementPresent (String.format ("//*[@class='alert-type-description' and text()='%s']/ancestor::accordion-group//*[@class='send-notification-label']",
                                                alertName));
    }

    public void setAlertNote (String alertNote) {
        assertTrue (setText (addAlertNote, alertNote));
    }

    public String getAlertNote () {
        return readInput (getAlertNote);
    }

}
