/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.shipment;

import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.ui.CoraPage;

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
        return getText (".shipment-entry-header [data-ng-bind='ctrl.entry.shipment.shipmentNumber']");
    }

    public String getShipmentStatus () {
        return getText (shipmentStatus);
    }

    public void clickShipmentTab () {
        assertTrue (click ("#shipment-tab-link"));
        pageLoading ();
    }

    public void clickAccessionTab () {
        assertTrue (click ("#shipment-accession-tab-link"));
        pageLoading ();
    }

    public void clickDiscrepancyResolutionsTab () {
        assertTrue (click ("#shipment-discrepancy-tab-link"));
        pageLoading ();
    }

}
