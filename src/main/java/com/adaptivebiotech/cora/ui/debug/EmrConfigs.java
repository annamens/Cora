package com.adaptivebiotech.cora.ui.debug;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import com.adaptivebiotech.cora.dto.emr.EmrConfig;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
public class EmrConfigs extends CoraPage {

    private final String createEmrConfig = "//button[text()='Create EMR Config']";
    private final String backToDebugging = "//button[text()='Back to Debugging']";
    private final String headerRow       = "table thead tr";

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

    public List <String> getTableHeaders () {
        return getTextList (waitForElement (headerRow), "th");
    }

    public EmrConfig getEmrConfig (String configId) {
        String row = format ("//*[td='%s']", configId) + "/td[%s]";
        EmrConfig config = new EmrConfig ();
        config.emrConfigId = getText (format (row, 1));
        config.emrType = getText (format (row, 2));
        config.displayName = getText (format (row, 3));
        config.ISS = getText (format (row, 4));
        config.trustEmail = Boolean.valueOf (format (row, 5));
        config.properties = mapper.readValue (getText (format (row, 6)), Map.class);
        return config;
    }
}
