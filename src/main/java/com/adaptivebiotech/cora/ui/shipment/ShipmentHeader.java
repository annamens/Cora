package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.utils.PageHelper.Discrepancy;
import com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ShipmentHeader extends CoraPage {

    protected final String shipmentStatus = "[ng-bind='ctrl.entry | shipmentEntryStatus']";

    public String getHeaderShipmentNumber () {
        return getText (".shipment").replace ("\n", " ");
    }

    public String getShipmentNumber () {
        return getText ("[ng-bind='ctrl.entry.shipment.shipmentNumber']");
    }

    public String getShipmentStatus () {
        return getText (shipmentStatus);
    }

    public void gotoShipment () {
        assertTrue (click ("#shipment-tab-link"));
    }

    public void gotoAccession () {
        assertTrue (click ("#shipment-accession-tab-link"));
    }

    public void gotoDiscrepancyResolutions () {
        assertTrue (click ("#shipment-discrepancy-tab-link"));
    }

    // discrepancy pop up methods
    public void addDiscrepancy (Discrepancy discrepancy,
                                String notes,
                                DiscrepancyAssignee assignee) {
        String cssAdd = "#dropdownDiscrepancy";
        assertTrue (click (cssAdd));

        String menuItemFmtString = "//*[@class='discrepancies-options']/ul/li[text()='%s']";
        String menuItem = String.format (menuItemFmtString, discrepancy.text);

        assertTrue (click (menuItem));
        String cssTextArea = "[ng-repeat='discrepancy in ctrl.discrepancies'] textarea";
        assertTrue (setText (cssTextArea, notes));
        String cssAssignee = "[ng-repeat='discrepancy in ctrl.discrepancies'] select";
        assertTrue (clickAndSelectText (cssAssignee, assignee.text));
    }

    public void clickDiscrepancySave () {
        String cssSave = "[ng-click='ctrl.save()'";
        assertTrue (click (cssSave));
    }
}
