package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;

import com.adaptivebiotech.cora.dto.Discrepancy;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.dto.DiscrepancyAssignee;

public class DiscrepancyPage extends CoraPage {

    public void addDiscrepancy (Discrepancy discrepancy, String notes,
                                DiscrepancyAssignee assignee) {
        String cssAdd = "#dropdownDiscrepancy";
        assertTrue (click (cssAdd));

        String cssMenuItem = "//*[contains(text(), '" + discrepancy.text + "')]";

        assertTrue (click (cssMenuItem));
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
