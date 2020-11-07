package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;

import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Discrepancy;
import com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee;

public class DiscrepancyPage extends CoraPage {

    public void addDiscrepancy (Discrepancy discrepancy, String notes,
                                DiscrepancyAssignee assignee) {
        String cssAdd = "#dropdownDiscrepancy";
        assertTrue (click (cssAdd));

//        String cssMenuItem = "//*[contains(text(), '" + discrepancy.text + "')]";

        String menuItemFmtString = "//*[@class='discrepancies-options']/ul/li[text()='%s']";
        String menuItem = String.format (menuItemFmtString, discrepancy.text);
        
        assertTrue (click (menuItem));
        String cssTextArea = "[ng-repeat='discrepancy in ctrl.discrepancies'] textarea";
        assertTrue (setText (cssTextArea, notes));
        String cssAssignee = "[ng-repeat='discrepancy in ctrl.discrepancies'] select";
        assertTrue (clickAndSelectText (cssAssignee, assignee.text));
    }

    public void clickSave () {
        String cssSave = "[ng-click='ctrl.save()'";
        assertTrue (click (cssSave));
    }

}
