/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.ui.CoraPage;

public class CompareBillingModule extends CoraPage {

    public void isCorrectPage () {
        assertTrue (isTextInElement (popupTitle, "Compare and Select Billing"));
    }

    public int countNumDifferentFields () {
        String css = ".compare-value.different";
        List <WebElement> differentFields = waitForElementsVisible (css);
        return differentFields.size ();
    }

    public void clickCancel () {
        String css = ".modal-footer .btn.btn-link"; // ick
        assertTrue (click (css));
    }
}
