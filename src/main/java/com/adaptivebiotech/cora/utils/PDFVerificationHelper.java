package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import com.adaptivebiotech.cora.dto.KitOrder;
import com.adaptivebiotech.test.utils.PageHelper.ReportType;
import com.jcraft.jsch.SftpException;
import com.testautomationguru.utility.PDFUtil;

public class PDFVerificationHelper  {
    private PDFUtil pdfutil;

    public void verifyHeader (String pdfFilePath, ReportType type, KitOrder orderInformation) {

        try {

            String title = "B-CELL " + ( (type == ReportType.clonality) ? "CLONALITY (ID)" : "TRACKING (MRD)") + " REPORT";
            int pgNum = pdfutil.getPageCount (pdfFilePath);
            String texts = pdfutil.getText (pdfFilePath);

            assertEquals (countMatches (texts, title), pgNum);
            testLog ("The report title was verified");

            assertEquals (countMatches (texts, orderInformation.orderDate_ISO_DATE), pgNum);
            testLog ("Report Date was verified");

            assertEquals (countMatches (texts, orderInformation.reportNum), pgNum);
            testLog ("Report number was verified");
            
            assertEquals (countMatches (texts, orderInformation.externalSubjectId), pgNum);
            testLog ("Patient ID was verified");

            assertEquals (countMatches (texts, orderInformation.sampleSource), pgNum);
            testLog ("Specimen Source was verified");
            
            assertEquals (countMatches (texts, orderInformation.sampleType), pgNum);
            testLog ("Specimen Type was verified");

            assertEquals (countMatches (texts,orderInformation.collectionDate), pgNum);
            testLog ("Collection Date was verified"); 

            PDDocument pdf = PDDocument.load (new File (pdfFilePath));
            PDAcroForm form = pdf.getDocumentCatalog ().getAcroForm ();

            PDField patientName = form.getField ("patientName");
            assertFalse (patientName.isReadOnly ());
            assertEquals (patientName.getValueAsString (), "");

            PDField dob = form.getField ("dob");
            assertFalse (dob.isReadOnly ());
            assertEquals (dob.getValueAsString (),"");

            PDField mrn = form.getField ("mrn");
            assertFalse (mrn.isReadOnly ());
            assertEquals (mrn.getValueAsString (), "");

            PDField gender = form.getField ("gender");
            assertFalse (gender.isReadOnly ());
            assertEquals (gender.getValueAsString (), "");

            PDField diagnosisCode = form.getField ("diagnosisCode");
            assertFalse (diagnosisCode.isReadOnly ());
            assertEquals (diagnosisCode.getValueAsString (), "");

            PDField dateReceived = form.getField ("dateReceived");
            assertFalse (dateReceived.isReadOnly ());
            assertEquals (dateReceived.getValueAsString (), "");

            PDField orderingPhysician = form.getField ("orderingPhysician");
            assertFalse (orderingPhysician.isReadOnly ());
            assertEquals (orderingPhysician.getValueAsString (), "");

            PDField institution = form.getField ("institution");
            assertFalse (institution.isReadOnly ());
            assertEquals (institution.getValueAsString (), "");
        } catch (Exception e) {
            throw new RuntimeException (e);
        }

    }
    
    public void verifyCorrectDataInReportTrackingPDF (SftpServerHelper serverHelper, String path, ReportType type, KitOrder orderInformation) {
        try {
            String reportTempFile = "target/report.pdf";
            serverHelper.getChannel ().get (path, reportTempFile);
            verifyHeader (reportTempFile, type, orderInformation);
            Files.deleteIfExists (Paths.get (reportTempFile));
        } catch (SftpException e) {
            throw new RuntimeException (e);
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

}
