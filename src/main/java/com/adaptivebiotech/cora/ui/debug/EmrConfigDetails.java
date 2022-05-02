/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.debug;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
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
}
