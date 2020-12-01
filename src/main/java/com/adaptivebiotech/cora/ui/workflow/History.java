package com.adaptivebiotech.cora.ui.workflow;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.HashMap;
import java.util.Map;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class History extends CoraPage {

    private final long millisRetry = 3000000l; // 50mins
    private final long waitRetry   = 20000l;   // 20sec

    public History () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".content .container", "Debugging: ORCA Workflow"));
        assertTrue (waitUntilVisible ("table.genoTable"));
    }

    public void gotoOrderDebug (String id) {
        assertTrue (navigateTo (coraTestUrl + "/cora/debug/orcaHistory?workflowId=" + id));
        isCorrectPage ();
    }

    public void cancelOrder () {
        assertTrue (clickAndSelectValue ("[name='stageStatus']", "Cancelled"));
        assertTrue (click ("[action*='/cora/debug/forceWorkflowStatus'] [name='submit']"));
        assertTrue (isTextInElement ("table.genoTable", "Cancelled"));
    }

    public String getWorkflowId () {
        return readInput ("#claimDiv [name='workflowId']");
    }

    public String getFileLocation (String filename) {
        return getAttribute (format ("//a[text()='%s']", filename), "href");
    }

    public void downloadFile (String filename) {
        assertTrue (click (format ("//a[text()='%s']", filename)));

        String report = getDownloadsDir () + filename;
        Timeout timer = new Timeout (300000l, waitRetry);
        while (!timer.Timedout () && !navigateTo (format ("file://%s", report)))
            timer.Wait ();
        getDriver ().navigate ().back ();
    }

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus, String message) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s, Message: %s";
        String xpath = "//table[@class='genoTable']//td[text()='%s']/../td[text()='%s']/../td[contains (text(),'%s')]/..//span[text()='%s']";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        while (!timer.Timedout () && ! (found = isElementPresent (format (xpath, stage, status, substatus, message)))) {
            doForceClaim ();
            timer.Wait ();
            refresh ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus, message));
    }

    public void waitFor (StageName stage, StageStatus status, StageSubstatus substatus) {
        String fail = "unable to locate Stage: %s, Status: %s, Substatus: %s";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        while (!timer.Timedout () && ! (found = isStagePresent (stage, status, substatus))) {
            doForceClaim ();
            timer.Wait ();
            refresh ();
        }
        if (!found)
            fail (format (fail, stage, status, substatus));
    }

    public void waitFor (StageName stage, StageStatus status) {
        String fail = "unable to locate Stage: %s, Status: %s";
        Timeout timer = new Timeout (millisRetry, waitRetry);
        boolean found = false;
        while (!timer.Timedout () && ! (found = isStagePresent (stage, status))) {
            doForceClaim ();
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

    public void doForceClaim () {
        String lastClaimed = "//a[text()='Last Claimed']", claim = "#claimDiv";
        assertTrue (click (lastClaimed));

        // sometimes the click failed
        if (isElementVisible (claim))
            assertTrue (click (claim + " [name='submit']"));
        else {
            assertTrue (click (lastClaimed));
            if (isElementVisible (claim))
                assertTrue (click (claim + " [name='submit']"));
        }
    }

    public void clickOrderTest () {
        String url = getAttribute ("a[href*='/cora/order/status']", "href");
        assertTrue (navigateTo (url));
        pageLoading ();
    }

    public void setWorkflowProperty (WorkflowProperty property, String value) {

        String propXpath = "//th[text()='%s:']/../td[contains(text(),'%s')]";

        enterWorkflowPropertyName (property);
        enterWorkflowPropertyValue (value);
        clickForceWorkflowProperty ();

        assertTrue (hasPageLoaded ());
        assertTrue (waitUntilVisible (format (propXpath, property.name (), value)));

        refresh (); // need to do this otherwise if you do a setWorkflowProperty next it doesn't
                    // enter the text
    }

    public void setWorkflowProperties (Map <WorkflowProperty, String> properties) {
        properties.entrySet ().forEach (wp -> {
            setWorkflowProperty (wp.getKey (), wp.getValue ());
        });
    }

    public void setWorkflowPropertyWhichDisplaysAsLink (WorkflowProperty property, String value) {

        String propXpath = "//th[text()='%s:']/../td/a[contains(text(),'%s')]";

        enterWorkflowPropertyName (property);
        enterWorkflowPropertyValue (value);
        clickForceWorkflowProperty ();

        assertTrue (hasPageLoaded ());
        assertTrue (waitUntilVisible (format (propXpath, property.name (), value)));

        refresh ();
    }

    public void forceStatusUpdate (StageName stageName, StageStatus stageStatus) {
        String stageNameSelect = "select[name='stageName']";
        String stageStatusSelect = "select[name='stageStatus']";

        if (stageName != null) {
            assertTrue (clickAndSelectValue (stageNameSelect, stageName.name ()));
        }
        waitForAttrContains (waitForElementVisible (stageNameSelect), "value", stageName.name ());
        assertTrue (clickAndSelectValue (stageStatusSelect, stageStatus.name ()));
        waitForAttrContains (waitForElementVisible (stageStatusSelect), "value", stageStatus.name ());
        assertTrue (click ("form[action*='forceWorkflowStatus'] input[type='submit']"));
        pageLoading ();
        assertTrue (hasPageLoaded ());

        waitFor (stageName, stageStatus);
    }

    public Map <String, String> getWorkflowProperties () {
        Map <String, String> props = new HashMap <> ();
        waitForElements ("//h3[text()='Properties']/following-sibling::table[1]//tr").forEach (tr -> {
            props.put (getText (tr, "th").replace (":", ""), getText (tr, "td"));
        });
        return props;
    }
    
    private void enterWorkflowPropertyName (WorkflowProperty property) {
        String propertyNameInput = "[name='propertyName']";
        assertTrue (setText (propertyNameInput, property.name ()));
        assertTrue (waitForAttrContains (waitForElementVisible (propertyNameInput), "value", property.name ()));
    }

    private void enterWorkflowPropertyValue (String value) {
        String propertyValueInput = "[name='propertyValue']";
        assertTrue (setText (propertyValueInput, value));
        assertTrue (waitForAttrContains (waitForElementVisible (propertyValueInput), "value", value));
    }

    private void clickForceWorkflowProperty () {
        assertTrue (click ("form[action*='forceWorkflowProperty'] input[type='submit']"));
    }
}
