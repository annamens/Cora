package com.adaptivebiotech.cora.ui.order;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.ui.CoraPage;

public class CompareBilling  extends CoraPage {

    public void isCorrectPage () {
        String css = ".modal-title";
        waitForElementVisible (css);
        assertEquals (getText (css), "Compare and Select Billing");
    }

    public int countNumDifferentFields () {
        String css = ".compare-value.different";
        List <WebElement> differentFields = waitForElementsVisible (css);
        return differentFields.size ();
    }

    public void clickCancel () {
        String css = ".modal-footer .btn.btn-link"; // ick
        assertTrue (click (css));
        waitForAjaxCalls ();
    }

}
