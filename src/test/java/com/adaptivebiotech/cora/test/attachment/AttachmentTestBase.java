/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.attachment;

import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.DateHelper.pstZoneId;
import static org.testng.Assert.assertEquals;
import static java.lang.ClassLoader.getSystemResource;
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
public class AttachmentTestBase extends CoraBaseBrowser {

    protected final List <String> previewPDFfile     = Arrays.asList ("PDFtypebelow15MB.pdf");
    protected final List <String> PDFfile            = Arrays.asList (getSystemResource ("uploadFiles/PDFtypebelow15MB.PDF").getPath ());

    protected final List <String> previewFiles       = Arrays.asList ("gifBelow15MB.gif",
                                                                      "jpgBelow15MB.jpg",
                                                                      "pdfBelow15MB.pdf",
                                                                      "pngBelow15MB.png");

    protected final List <String> uploadPreviewFiles = previewFiles.stream ()
                                                                   .map (e -> getSystemResource ("uploadFiles/" + e).getPath ())
                                                                   .collect (Collectors.toList ());

    protected final String[]      icdCodes           = { "A20.0" };

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
