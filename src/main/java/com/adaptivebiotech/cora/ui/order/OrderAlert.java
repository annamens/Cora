package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.WebElement;
import com.seleniumfy.test.utils.BasePage;

public class OrderAlert extends BasePage {

    private final String alertModal       = ".modal-content";
    private final String alertTitle       = ".modal-title";
    private final String alertType        = "[name='select-alert-type'] select";
    private final String closeBtn         = "//button[text()='Close']";
    private final String activeTab        = "//span[contains(text(), 'ACTIVE')]";
    private final String resolveTab       = "//span[contains(text(), 'RESOLVED')]";
    private final String activeAlerts     = ".panel.active-alerts";
    private final String resolvedAlerts   = ".resolved-alerts";
    private final String resolvedBtn      = ".unresolve-alert-button";
    private final String accordionOpen    = "accordion-group[isopen='true']";
    private final String alertDescription = ".alert-type-description";
    private final String saveBtn          = "//*[text()='Save']";
    private final String cancelBtn        = "//*[text()='Cancel']";

    public void isCorrectPage () {
        assertTrue (waitUntilVisible (alertModal));
        assertTrue (isTextInElement (alertModal + " " + alertTitle, "Alerts for Order"));
    }

    public void isCorrectPage (String orderNo) {
        isCorrectPage ();
        assertTrue (isTextInElement (alertModal + " " + alertTitle, "Alerts for Order #" + orderNo));
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
        assertTrue (isTextInElement (accordionOpen + " " + alertDescription, alertName));
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

    public Map <String, String> getResolvedAlerts () {
        assertTrue (waitUntilVisible (resolvedAlerts));

        Map <String, String> resolvedAlertsMap = new HashMap <> ();
        for (WebElement element : waitForElements (resolvedAlerts)) {
            resolvedAlertsMap.put (getText (element, alertDescription), getText (element, resolvedBtn));
        }
        return resolvedAlertsMap;
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

}
