package com.adaptivebiotech.cora.ui.workflow;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
        isCorrectPage ();
    }

    public void clickClone () {
        assertTrue (click (clone));
    }

    public boolean isCloneVisible () {
        return waitUntilVisible (clone);
    }

    public List <String> getOverlayMessages () {
        List <String> overlayMsg = waitForElements (overlayMessage).stream ().map (ele -> ele.getText ())
                                                                   .collect (Collectors.toList ());
        waitForElementInvisible (overlayMessage);
        return overlayMsg;
    }

    public List <String> getAttachedAccounts () {
        return getTextList (attachedAccounts);
    }

    public void deleteAttachedAccounts (String... accounts) {
        for (WebElement element : waitForElements (attachedAccounts)) {
            if (Arrays.asList (accounts).contains (getText (element).trim ())) {
                element.findElement (By.xpath ("./..//button")).click ();
            }
        }
    }
}
