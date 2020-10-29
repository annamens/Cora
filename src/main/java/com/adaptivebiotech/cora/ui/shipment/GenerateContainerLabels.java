package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

public class GenerateContainerLabels extends CoraPage {

    public void selectPrinter (String printer) {
        String css = "[ng-change='ctrl.printerChange()']";
        waitForElementVisible (css);
        doWait (5000); // something funny here
        assertTrue (clickAndSelectText (css, printer));

    }

    public void clickPrint () {
        String css = "[ng-click='ctrl.printLabel()']";
        assertTrue (click (css));

    }

    public void clickClose () {
        String css = "[ng-click='ctrl.close();']";
        assertTrue (click (css));
        moduleLoading ();
    }
}
