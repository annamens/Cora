/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.DateHelper.pstZoneId;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.adaptivebiotech.cora.dto.Orders.OrderStatus;
import com.adaptivebiotech.cora.dto.UploadFile;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;

/**
 * @author jpatel
 *
 */
public class NewOrderTestBase extends CoraBaseBrowser {

    protected final List <String> headers                = asList ("Customer Instructions",
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
    protected final List <String> previewFiles           = Arrays.asList ("gifBelow15MB.gif",
                                                                          "jpgBelow15MB.jpg",
                                                                          "pdfBelow15MB.pdf",
                                                                          "pngBelow15MB.png");
    protected final List <String> uploadPreviewFiles     = previewFiles.stream ().map (e -> "uploadFiles/" + e)
                                                                       .collect (Collectors.toList ());
    protected final String        orderDetailsTab        = "ORDER DETAILS";
    protected final String        orderStatusTab         = "ORDER STATUS";
    protected final String        shipmentTab            = "SHIPMENT";
    protected final String        accessionTab           = "ACCESSION";
    protected final String        discrepancyTab         = "DISCREPANCY RESOLUTIONS";
    protected final String        validateToastErrorMsg  = "Please fix errors in the form";
    protected final String        collectionDateErrorMsg = "Please enter a valid date";
    protected final String        validdateSuccessMsg    = "order saved";
    protected final String        trackingNumber         = "12345678";

    protected final String[]      icdCodes               = { "C90.00" };

    protected final List <String> orderDiscrepTabList    = asList (orderDetailsTab, accessionTab, discrepancyTab);
    protected final List <String> orderDetailsTabList    = asList (orderDetailsTab, accessionTab);
    protected final List <String> discrepancyTabList     = asList (shipmentTab, accessionTab, discrepancyTab);
    protected final List <String> accessionTabList       = asList (shipmentTab, accessionTab);

    protected void validateAttachments (List <UploadFile> actualAttachments, List <String> expFiles,
                                        OrderStatus status) {
        assertEquals (actualAttachments.size (), expFiles.size ());
        actualAttachments.forEach (actual -> {
            assertTrue (expFiles.contains (actual.fileName));
            if (status.equals (Active)) {
                assertNull (actual.fileNameTitle);
            } else {
                assertTrue (expFiles.contains (actual.fileNameTitle));
            }
            assertNotNull (actual.fileUrl);
            assertTrue (actual.canFilePreview);
            assertEquals (actual.createdBy, coraTestUser);
            assertEquals (actual.createdDateTime.format (formatDt1), genDate (0, formatDt1, pstZoneId));
        });
    }
}
