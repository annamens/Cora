package com.adaptivebiotech.cora.ui.workflow;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * feature-flags page (end-point: /cora/debug/feature-flags) displays the status of all feature flags
 * @author Jaydeepkumar Patel
 *         <a href="mailto:jpatelm@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
public class FeatureFlags extends CoraPage {

    private String pageHeader = ".container h1";
    private String tableRows  = ".table tbody tr";

    public FeatureFlags () {
        staticNavBarHeight = 200;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (pageHeader, "Feature Flags"));
        assertTrue (waitUntilVisible (tableRows));
    }

    public void navigateToFeatureFlagsPage () {
        assertTrue (navigateTo (coraTestUrl + "/cora/debug/feature-flags"));
        isCorrectPage ();
    }

    public Map <String, String> getFeatureFlags () {
        Map <String, String> featureFlags = new HashMap <> ();
        waitForElements (tableRows).forEach (tr -> {
            featureFlags.put (getText (tr, "td:nth-child(1)"), getText (tr, "td:nth-child(2)"));
        });
        return featureFlags;
    }

}
