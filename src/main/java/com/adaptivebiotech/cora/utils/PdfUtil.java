/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.utils;

import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.ClassLoader.getSystemResource;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.testng.Assert.fail;
import com.testautomationguru.utility.PDFUtil;
import de.redsix.pdfcompare.CompareResult;
import de.redsix.pdfcompare.Exclusion;
import de.redsix.pdfcompare.PdfComparator;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class PdfUtil {

    private static final PDFUtil pdfUtil = new PDFUtil ();

    /**
     * Open a pdf file and return the content as text
     * 
     * @param pdfFileLocation
     *            The location of pdf file
     * @return The content of pdf file
     */
    public static String getTextFromPDF (String pdfFileLocation) {
        try {
            String fileContent = pdfUtil.getText (pdfFileLocation);
            info ("File Content:\n" + fileContent);
            return fileContent;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * Open a pdf file and return the content of <pageNumber> as text
     * 
     * @param pdfFileLocation
     *            The location of pdf file
     * @param pageNumber
     *            The pdf page number
     * @return The content of pdf file
     */
    public static String getTextFromPDF (String pdfFileLocation, int pageNumber) {
        try {
            String fileContent = pdfUtil.getText (pdfFileLocation, pageNumber);
            info ("File Content:\n" + fileContent);
            return fileContent;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * Open a pdf file, read pageNumber, and return extracted text from beginText and endText
     * 
     * @param pdfFileLocation
     *            The location of pdf file
     * @param pageNumber
     *            The pdf page number
     * @param beginText
     *            The begin text search
     * @param endText
     *            The end text search
     * @return The result of the text search
     */
    public static String getTextFromPDF (String pdfFileLocation, int pageNumber, String beginText, String endText) {
        String extractedText = getTextFromPDF (pdfFileLocation, pageNumber);
        return substringBetween (extractedText, beginText, endText);
    }

    public static void compareTrfFiles (String actualTrfLoc, String expectedTrfLoc, Exclusion... exclusions) {
        try {
            String actual = getFile (actualTrfLoc).getPath ();
            String expected = getSystemResource (expectedTrfLoc).getPath ();
            PdfComparator <CompareResult> pdfComparator = new PdfComparator <> (expected, actual);
            asList (exclusions).forEach (e -> pdfComparator.with (e));
            final CompareResult result = pdfComparator.compare ();
            if (result.isNotEqual ()) {
                result.writeTo (actual.replace ("pdf", "diff"));
                fail ("Differences found: " + actual);
            }
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
