package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.testng.Assert.assertEquals;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import com.adaptivebiotech.cora.dto.OrderInfo;
import com.adaptivebiotech.test.utils.PageHelper.ReportType;

public class PDFVerificationHelper extends PDFUtil {

    public void verifyHeader (String pdfFilePath, ReportType type, OrderInfo orderInformation) {

        try {

            String title = "B-CELL " + ( (type == ReportType.clonality) ? "CLONALITY (ID)" : "TRACKING (MRD)") + " REPORT";
            int pgNum = getPageCount (pdfFilePath);
            String texts = getText (pdfFilePath);

            testLog ("Verify report title ");
            assertEquals (countMatches (texts, title), pgNum);

            testLog ("Verify Report Date");
            assertEquals (countMatches (texts, orderInformation.orderDate_ISO_DATE), pgNum);

            testLog ("Verify Report #");
            assertEquals (countMatches (texts, orderInformation.reportNum), pgNum);

            testLog ("Verify Patient ID");
            assertEquals (countMatches (texts, orderInformation.externalSubjectId1), pgNum);

            testLog ("Verify Specimen Type/ Specimen Source");
            assertEquals (countMatches (texts, title), pgNum);

            testLog ("Verify Collection Date");
            assertEquals (countMatches (texts, title), pgNum);

            PDDocument pdf = PDDocument.load (new File (pdfFilePath));
            PDAcroForm form = pdf.getDocumentCatalog ().getAcroForm ();

            PDField patientName = form.getField ("patientName");
            assertEquals (patientName.isReadOnly (), false);
            assertEquals (patientName.getValueAsString ().equals (""), true);

            PDField dob = form.getField ("dob");
            assertEquals (dob.isReadOnly (), false);
            assertEquals (dob.getValueAsString ().equals (""), true);

            PDField mrn = form.getField ("mrn");
            assertEquals (mrn.isReadOnly (), false);
            assertEquals (mrn.getValueAsString ().equals (""), true);

            PDField gender = form.getField ("gender");
            assertEquals (gender.isReadOnly (), false);
            assertEquals (gender.getValueAsString ().equals (""), true);

            PDField diagnosisCode = form.getField ("diagnosisCode");
            assertEquals (diagnosisCode.isReadOnly (), false);
            assertEquals (diagnosisCode.getValueAsString ().equals (""), true);

            PDField dateReceived = form.getField ("dateReceived");
            assertEquals (dateReceived.isReadOnly (), false);
            assertEquals (dateReceived.getValueAsString ().equals (""), true);

            PDField orderingPhysician = form.getField ("orderingPhysician");
            assertEquals (orderingPhysician.isReadOnly (), false);
            assertEquals (orderingPhysician.getValueAsString ().equals (""), true);

            PDField institution = form.getField ("institution");
            assertEquals (institution.isReadOnly (), false);
            assertEquals (institution.getValueAsString ().equals (""), true);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }

    }

}
