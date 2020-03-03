package com.adaptivebiotech.cora.ui.workflow;

import static com.adaptivebiotech.cora.test.CoraEnvironment.reportPrefix;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static org.testng.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.ui.cora.workflow.History;
import com.seleniumfy.test.utils.Timeout;

public class Debug extends History {

    public Debug () {

    }

    public boolean isFileinFileList (String fileName, int retry, int wait) {

        Timeout timer = new Timeout (retry, wait);
        String xPath = String.format ("//*[text()='%s']", fileName);
        boolean result = false;
        do {
            timer.Wait ();
            result = waitUntilVisible (xPath);
        } while (!timer.Timedout () && !result);

        return result;
    }

    public void gotoOrderDebug (String id, int retry, int wait) {

        Timeout timer = new Timeout (retry, wait);
        boolean result = false;
        do {
            timer.Wait ();
            navigateTo (coraTestUrl + "/cora/debug/orcaHistory?workflowId=" + id);
            result = waitUntilVisible (".navbar") && waitUntilVisible ("table.genoTable") && isTextInElement (".content .container",
                                                                                                              "Debugging: ORCA Workflow") && waitUntilVisible (".content .container");
        } while (!timer.Timedout () && !result);
        assertTrue (result);
        isCorrectPage ();
    }

    public void verifyReportDatajsonFile (int retry, int wait) {
        assertTrue (isFileinFileList ("reportData.json", retry, wait));
    }

    // it is a pdf file, the formatt like : ClinicalReport-A120324019.pdf
    public String getClinicalReportFileName (int retry, int wait) {

        Timeout timer = new Timeout (retry, wait);
        String fileName = null;
        boolean result = false;
        do {
            List <WebElement> myList = waitForElements (".content > .container > ul > li");
            timer.Wait ();
            for (WebElement element : myList) {
                String file = element.getText ();
                if (result = file.contains ("ClinicalReport-" + reportPrefix)) {
                    fileName = file;
                    break;
                }
            } // for loop
        } while (!timer.Timedout () && !result);

        assertTrue (result);
        return fileName;
    }

    public void verifyClinicalReportPdfFile (int retry, int wait) {
        String actualFileName = getClinicalReportFileName (retry, wait);
        LocalDateTime now = LocalDateTime.now ();

        // get current year of last two digit
        String currYear = Integer.toString (now.getYear ()).substring (2);
        String expectedFilePre = String.format ("ClinicalReport-%s%s", reportPrefix, currYear);
        String pattern = "(^0{4}[2-9][5-9]|^0{3}[1-9]\\d{2}|^0{2}[1-9]\\d{3}|^0[1-9]\\d{4}|[1-9]\\d{5})";
        assertTrue (actualFileName.replaceAll (expectedFilePre + "(\\d{6})\\.pdf", "$1").matches (pattern));
    }

    // For xth1, "1" is for table title

    public String getStatusHistoryTableStringValue (String xth1, String xth2) {

        return getText (String.format (".genoTable > tbody > tr:nth-child(%s) > td:nth-child(%s)>.ssm", xth1, xth2));

    }

    public String getPropertiesItem (String name, int retry, int wait) {

        Timeout timer = new Timeout (retry, wait);
        boolean result = false;
        String str = null;
        do {
            timer.Wait ();
            refresh ();
            str = getText (String.format ("//*[text()='%s:']/parent::*/td", name));
            result = (str != null);

        } while (!timer.Timedout () && !result);
        assertTrue (result);
        return str;
    }

}
