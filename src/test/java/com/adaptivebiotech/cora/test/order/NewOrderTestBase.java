/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order;

import static java.util.Arrays.asList;
import java.util.List;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;

/**
 * @author jpatel
 *
 */
public class NewOrderTestBase extends CoraBaseBrowser {

    protected final List <String> headers             = asList ("Customer Instructions",
                                                                "Order Notes",
                                                                "Ordering Physician",
                                                                "Patient",
                                                                "Specimen",
                                                                "Order Test",
                                                                "Billing",
                                                                "Order Authorization",
                                                                "Attachments",
                                                                "Messages",
                                                                "History");
    protected final String        orderDetailsTab     = "ORDER DETAILS";
    protected final String        orderStatusTab      = "ORDER STATUS";
    protected final String        shipmentTab         = "SHIPMENT";
    protected final String        accessionTab        = "ACCESSION";
    protected final String        discrepancyTab      = "DISCREPANCY RESOLUTIONS";

    protected final List <String> orderDiscrepTabList = asList (orderDetailsTab, accessionTab, discrepancyTab);
    protected final List <String> orderDetailsTabList = asList (orderDetailsTab, accessionTab);
    protected final List <String> discrepancyTabList  = asList (shipmentTab, accessionTab, discrepancyTab);
    protected final List <String> accessionTabList    = asList (shipmentTab, accessionTab);
}
