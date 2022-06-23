/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.debug;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Stuck;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.util.Strings.isNullOrEmpty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class OrcaHistory extends CoraPage {

    private final long millisDuration = 3000000l; // 50mins
    private final long millisPoll     = 60000l;   // 60sec

    public OrcaHistory () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".content .container h1", "Debugging: ORCA Workflow"));
        assertTrue (waitUntilVisible ("table.genoTable"));
    }

    public void gotoOrderDebug (String id) {
        assertTrue (navigateTo (coraTestUrl + "/cora/debug/orcaHistory?workflowId=" + id));
        isCorrectPage ();
    }

    public String getCreated () {
        return getText ("//*[th='Created:']/td");
    }

    public String getFinished () {
        return getText ("//*[th='Finished:']/td");
    }

    public String getWorkflowId () {
        return readInput ("#claimDiv [name='workflowId']");
    }

    public String getFileLocation (String filename) {
        return getAttribute (format ("//a[text()='%s']", filename), "href");
    }

    public String downloadFile (String filename) {
        assertTrue (click (format ("//a[text()='%s']", filename)));

        String report = getDownloadsDir () + filename;
        Timeout timer = new Timeout (millisDuration, millisPoll);
        while (!timer.Timedout () && !navigateTo (format ("file://%s", report)))
            timer.Wait ();
        getDriver ().navigate ().back ();
        return report;
    }

    public String getFileUrl (String fileName) {
        assertTrue (isElementVisible (format ("//a[text()='%s']", fileName)));
        return getAttribute (format ("//a[text()='%s']", fileName), "href");
    }

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus, String message) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s, Message: %s";
        String xpath = "//table[@class='genoTable']//td[text()='%s']/../td[text()='%s']/../td[contains (text(),'%s')]/..//span[text()='%s']";
        String check = format (xpath, stage, status, substatus == null ? "" : substatus, message);
        Timeout timer = new Timeout (millisDuration, millisPoll);
        boolean found = false;
        String orcaHistoryUrl = getCurrentUrl ();
        while (!timer.Timedout () && ! (found = isElementPresent (check))) {
            checkForStuck (status);
            doForceClaim (orcaHistoryUrl);
            timer.Wait ();
            refresh ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus, message));
    }

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s";
        Timeout timer = new Timeout (millisDuration, millisPoll);
        boolean found = false;
        String orcaHistoryUrl = getCurrentUrl ();
        while (!timer.Timedout () && ! (found = isStagePresent (stage, status, substatus))) {
            checkForStuck (status);
            doForceClaim (orcaHistoryUrl);
            timer.Wait ();
            refresh ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus));
    }

    public void waitFor (StageName stage, StageStatus status) {
        String fail = "unable to locate Stage: %s, Status: %s";
        Timeout timer = new Timeout (millisDuration, millisPoll);
        boolean found = false;
        String orcaHistoryUrl = getCurrentUrl ();
        while (!timer.Timedout () && ! (found = isStagePresent (stage, status))) {
            checkForStuck (status);
            doForceClaim (orcaHistoryUrl);
            timer.Wait ();
            refresh ();
        }
        if (!found)
            fail (format (fail, stage, status));
    }

    public boolean isStagePresent (StageName stage, StageStatus status, StageSubstatus substatus) {
        String xpath = "//table[@class='genoTable']//td[text()='%s']/../td[text()='%s']/../td[contains (text(),'%s')]";
        return isElementPresent (format (xpath, stage.name (), status.name (), substatus.name ()));
    }

    public boolean isStagePresent (StageName stage, StageStatus status) {
        String xpath = "//table[@class='genoTable']//td[text()='%s']/../td[text()='%s']";
        return isElementPresent (format (xpath, stage.name (), status.name ()));
    }

    public void checkForStuck (StageStatus status) {
        if (!Stuck.equals (status)) {
            if (isElementPresent (format ("//table[@class='genoTable']//td[text()='%s']", Stuck)))
                fail (format ("the workflow is '%s'", Stuck));
        }
    }

    public void doForceClaim (String orcaHistoryUrl) {
        String lastClaimed = "//a[text()='Last Claimed']", claim = "#claimDiv";
        try {
            assertTrue (click (lastClaimed));

            // sometimes the click failed
            if (isElementVisible (claim))
                assertTrue (click (claim + " [name='submit']"));
            else {
                assertTrue (click (lastClaimed));
                if (isElementVisible (claim))
                    assertTrue (click (claim + " [name='submit']"));
            }
        } catch (Exception e) {
            Logging.error ("doForceClaim exception: " + e);
            navigateTo (orcaHistoryUrl);
            isCorrectPage ();
        }

    }

    public void waitForTopLevel (StageName stage, StageStatus status, StageSubstatus substatus) {
        String fail = "unable to locate top level Stage: %s, Status: %s, Substatus: %s";
        Timeout timer = new Timeout (millisDuration, millisPoll);
        boolean found = false;
        String orcaHistoryUrl = getCurrentUrl ();
        while (!timer.Timedout () && ! (found = isTopLevelStagePresent (stage, status, substatus))) {
            checkForStuck (status);
            doForceClaim (orcaHistoryUrl);
            timer.Wait ();
            refresh ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus));
    }

    public boolean isTopLevelStagePresent (StageName stage, StageStatus status, StageSubstatus substatus) {
        String xpath = "//table[@class='genoTable']//tr[td[text()='%s']][1]/td[text()='%s']/../td[contains (text(),'%s')]";
        return isElementPresent (format (xpath, stage.name (), status.name (), substatus.name ()));
    }

    public String getOrderId () {
        return substringAfterLast (getAttribute ("a[href*='/cora/order/auto?id=']", "href"), "id=");
    }

    public String getOrderTestId () {
        return substringAfterLast (getAttribute ("a[href*='/cora/order/status']", "href"), "ordertestid=");
    }

    public void clickOrder () {
        assertTrue (navigateTo (getAttribute ("a[href*='/cora/order/auto']", "href")));
        pageLoading ();
    }

    public void clickOrderTest () {
        assertTrue (navigateTo (getAttribute ("a[href*='/cora/order/status']", "href")));
        pageLoading ();
    }

    public void setWorkflowProperty (WorkflowProperty property, String value) {

        String propXpath = "//th[text()='%s:']/../td[contains(.,'%s')]";

        enterWorkflowPropertyName (property);
        enterWorkflowPropertyValue (value);
        clickForceWorkflowProperty ();

        assertTrue (waitUntilVisible (format (propXpath, property.name (), value)));

        refresh (); // need to do this otherwise if you do a setWorkflowProperty next it doesn't
                    // enter the text
    }

    public void setWorkflowProperties (Map <WorkflowProperty, String> properties) {
        properties.entrySet ().forEach (wp -> {
            setWorkflowProperty (wp.getKey (), wp.getValue ());
        });
    }

    public void forceStatusUpdate (StageName stageName, StageStatus stageStatus) {
        String orcaHistoryUrl = getCurrentUrl ();
        String stageNameSelect = "select[name='stageName']";
        String stageStatusSelect = "select[name='stageStatus']";
        assertTrue (clickAndSelectValue (stageNameSelect, stageName.name ()));
        assertTrue (clickAndSelectValue (stageStatusSelect, stageStatus.name ()));
        assertTrue (click ("form[action*='forceWorkflowStatus'] input[type='submit']"));
        assertTrue (hasPageLoaded ());
        if (getCurrentUrl ().endsWith ("forceWorkflowStatus")) {
            navigateTo (orcaHistoryUrl);
            isCorrectPage ();
            if (!isStagePresent (stageName, stageStatus)) {
                forceStatusUpdate (stageName, stageStatus);
            }
        }
        waitFor (stageName, stageStatus);
    }

    public Map <String, String> getWorkflowProperties () {
        Map <String, String> props = new HashMap <> ();
        waitForElements ("//h3[text()='Properties']/following-sibling::table[1]//tr").forEach (tr -> {
            props.put (getText (tr, "th").replace (":", ""), getText (tr, "td"));
        });
        return props;
    }

    public Map <String, String> getWorkflowFiles () {
        Map <String, String> files = new HashMap <> ();
        waitForElements ("//h3[text()='Files']/following-sibling::ul[1]//a").forEach (a -> {
            files.put (getText (a), getAttribute (a, "href"));
        });
        return files;
    }

    public List <StageName> getWorkflowStages () {
        List <StageName> stages = new ArrayList <> ();
        for (String stage : getText ("//*[*[text()='Stages:']]/td").split ("\\s*,\\s*"))
            stages.add (StageName.valueOf (stage));
        return stages;
    }

    public List <Stage> parseStatusHistory () {
        List <Stage> histories = new ArrayList <> ();
        for (WebElement tr : waitForElements (".genoTable tr")) {
            if (isElementPresent (tr, "td")) {
                Stage stage = new Stage ();
                stage.stageName = StageName.valueOf (getText (tr, "td:nth-child(1)"));
                stage.stageStatus = StageStatus.valueOf (getText (tr, "td:nth-child(2)"));
                String subs = getText (tr, "td:nth-child(3)");
                stage.stageSubstatus = isNullOrEmpty (subs) ? null : StageSubstatus.valueOf (subs.replace ("-", "_"));
                stage.subStatusMessage = getText (tr, ".ssm");
                stage.timestamp = getText (tr, "td:nth-child(5)");
                stage.actor = getText (tr, "td:nth-child(6)");

                String drilldown = "td:nth-child(7) a";
                if (isElementPresent (tr, drilldown))
                    stage.drilldownUrl = getAttribute (tr, drilldown, "href");
                histories.add (stage);
            }
        }
        return histories;
    }

    public void uploadFile (String path, String filename) {
        String chooseFile = "//form[@action='/cora/debug/uploadFile']//input[@name='uploadFile']";
        String input = "//form[@action='/cora/debug/uploadFile']//input[@name='fileName']";
        String submit = "//form[@action='/cora/debug/uploadFile']//input[@name='submit']";

        waitForElement (chooseFile).sendKeys (path);
        String fileUrl = String.format ("//a[text()='%s']", filename);

        assertTrue (setText (input, filename));
        assertTrue (click (submit));
        assertTrue (waitUntilVisible (fileUrl));

    }

    private void enterWorkflowPropertyName (WorkflowProperty property) {
        String propertyNameInput = "[name='propertyName']";
        assertTrue (setText (propertyNameInput, property.name ()));
    }

    private void enterWorkflowPropertyValue (String value) {
        String propertyValueInput = "[name='propertyValue']";
        assertTrue (setText (propertyValueInput, value));
    }

    private void clickForceWorkflowProperty () {
        assertTrue (click ("form[action*='forceWorkflowProperty'] input[type='submit']"));
    }

    public boolean isFilePresent (String fileName) {
        String fileLocator = "//h3[text()='Files']/following-sibling::ul//a[text()='%s']";
        return isElementPresent (format (fileLocator, fileName));
    }

    public boolean waitForFilePresent (String fileName) {
        Function <WebDriver, Boolean> func = new Function <WebDriver, Boolean> () {
            public Boolean apply (WebDriver driver) {
                if (isFilePresent (fileName))
                    return true;
                else {
                    refresh ();
                    return false;
                }
            }
        };
        return waitUntil (millisDuration, millisPoll / 6, func);
    }
}
