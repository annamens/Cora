package com.adaptivebiotech.cora.ui.shipment;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.adaptivebiotech.cora.ui.CoraPage;

public class GenerateContainerLabels extends CoraPage {

    private final String copyToClipboard = "[ng-click='ctrl.copyToClipboard()']";
    private final String printers        = "[ng-change='ctrl.printerChange()']";
    private final String print           = "[ng-click='ctrl.printLabel()']";
    private final String close           = "[ng-click='ctrl.close();']";
    private final String table           = ".modal-content table.containers-label-list";
    private final String headerCell      = "thead tr th:nth-child(%s)";
    private final String rowCell         = "td:nth-child(%s)";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (popupTitle, "Generate Container Labels"));
    }

    public boolean isCopyToClipboardVisible () {
        return isElementVisible (copyToClipboard);
    }

    public void selectPrinter (String printer) {
        waitForElementVisible (printers);
        assertTrue (clickAndSelectText (printers, printer));
    }

    public boolean isPrintersVisible () {
        return isElementVisible (printers);
    }

    public void clickPrint () {
        assertTrue (click (print));
    }

    public boolean isPrintVisible () {
        return isElementVisible (print);
    }

    public void clickClose () {
        assertTrue (click (close));
        moduleLoading ();
    }

    public boolean isCloseVisible () {
        executeJScript ("arguments[0].scrollIntoView()", getDriver ().findElement (locateBy (close)));
        return isElementVisible (close);
    }

    public List <Map <String, String>> getGenerateContainerLabelDetails () {
        List <Map <String, String>> tableData = new ArrayList <> ();
        waitForElements (table + " tbody tr").forEach (row -> {
            Map <String, String> rowData = new HashMap <> ();
            for (int i = 2; i <= getTextList (row, "td").size (); i++) {
                rowData.put (getText (format (table + " " + headerCell, i)),
                             getText (format (table + " " + rowCell, i)));
            }
            tableData.add (rowData);
        });
        return tableData;
    }
}
