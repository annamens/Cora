package com.adaptivebiotech.cora.ui.workflow;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
public class EmrConfig extends CoraPage {

    private final String createEmrConfig = "//button[text()='Create EMR Config']";
    private final String backToDebugging = "//button[text()='Back to Debugging']";
    private final String headerRow       = "table thead tr";
    private final String rows            = "table tbody tr";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (popupTitle, "List of EMR Configs"));
        assertTrue (waitUntilVisible (createEmrConfig));
        assertTrue (waitUntilVisible (backToDebugging));
    }

    public void gotoEmrConfigPage () {
        assertTrue (navigateTo (coraTestUrl + "/cora/debug/emr-config"));
        isCorrectPage ();
    }

    public void clickCreateEmrConfig () {
        assertTrue (click (createEmrConfig));
    }

    public boolean isCreateEmrConfigPresent () {
        return isElementPresent (createEmrConfig);
    }

    public void clickBackToDebugging () {
        assertTrue (click (backToDebugging));
    }

    public List <Map <String, String>> getEmrConfigTable () {
        List <Map <String, String>> tableData = new ArrayList <> ();
        List <String> headers = getTextList (waitForElement (headerRow), "th");
        waitForElements (rows).forEach (row -> {
            Map <String, String> map = new HashMap <> ();
            List <String> rowData = getTextList (row, "td");

            for (int i = 0; i < headers.size (); i++) {
                map.put (headers.get (i), rowData.get (i));
            }
            tableData.add (map);
        });
        return tableData;
    }

}
