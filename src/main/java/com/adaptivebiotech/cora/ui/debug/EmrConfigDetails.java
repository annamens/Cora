package com.adaptivebiotech.cora.ui.debug;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.seleniumfy.test.utils.Environment.webdriverWait;
import static com.seleniumfy.test.utils.Logging.info;
import static com.seleniumfy.test.utils.Logging.warn;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfElementsToBe;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
public class EmrConfigDetails extends CreateEmrConfig {

    private final String clone            = "//button[text()='Clone']";
    private final String overlayMessage   = "#toast-container .toast-success";
    private final String attachedAccounts = "//*[text()='Attached Accounts']/..//span[not(contains(@class, 'glyphicon'))]";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (popupTitle, "EMR Config Details"));
    }

    public void gotoEmrConfigDetailsPage (String emrConfigId) {
        assertTrue (navigateTo (coraTestUrl + "/cora/debug/emr-config-details?id=" + emrConfigId));
        pageLoading ();
        isCorrectPage ();
    }

    public void clickClone () {
        assertTrue (click (clone));
    }

    public boolean isCloneVisible () {
        return waitUntilVisible (clone);
    }

    public List <String> getOverlayMessages (int numOfMessages) {
        List <String> overlayMsg = waitForNumberOfElementsToBe (locateBy (overlayMessage), numOfMessages).stream ()
                                                                                                         .map (el -> el.getText ())
                                                                                                         .collect (toList ());
        waitForElementInvisible (overlayMessage);
        return overlayMsg;
    }

    public List <String> getAttachedAccounts () {
        return getTextList (attachedAccounts);
    }

    public void deleteAttachedAccounts (String... accounts) {
        for (WebElement element : waitForElements (attachedAccounts)) {
            if (Arrays.asList (accounts).contains (getText (element).trim ())) {
                click (element, "./..//button");
            }
        }
    }

    private List <WebElement> waitForNumberOfElementsToBe (By by, int numOfElements) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (getDriver (), webdriverWait,
                    sleepInMillis).until (numberOfElementsToBe (by, numOfElements));
        } catch (Exception e) {
            warn (String.valueOf (e));
            info ("let's give waitFornumberOfElementsToBe another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (getDriver (), webdriverWait, sleepInMillis) {
                {
                    info ("ok, it's done ...");
                }
            }.until (numberOfElementsToBe (by, numOfElements));
        }
    }
}
