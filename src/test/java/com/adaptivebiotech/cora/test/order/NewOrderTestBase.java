/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order;

import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import java.io.IOException;
import java.util.List;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

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

    protected String getTextFromPDF (String downloadDir, String url, int pageNumber) {
        String pdfFileLocation = join ("/", downloadDir, substringBefore (substringAfterLast (url, "/"), "?"));
        info ("PDF File Location: " + pdfFileLocation);

        // get file from URL and save it
        coraApi.get (url, pdfFileLocation);

        // read PDF and extract text
        PdfReader reader = null;
        String fileContent = null;
        try {
            reader = new PdfReader (pdfFileLocation);
            fileContent = PdfTextExtractor.getTextFromPage (reader, pageNumber).replace ("\n", " ");
        } catch (IOException e) {
            throw new RuntimeException (e);
        } finally {
            reader.close ();
        }
        return fileContent;
    }

}
