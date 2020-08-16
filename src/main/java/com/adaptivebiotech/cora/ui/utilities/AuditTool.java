package com.adaptivebiotech.cora.ui.utilities;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.ui.cora.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class AuditTool extends CoraPage {

    public AuditTool () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (".header-title", "Audit Tool"));
    }
}
