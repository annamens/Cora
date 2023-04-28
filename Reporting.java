package com.seleniumfy.utils;

import static java.lang.String.join;
import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.annotations.BeforeTest;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/**
 * @author Annameni Srinivas
 *         <a href="mailto:sannameni@gmail.com">sannameni@gmail.com</a>
 */
public class Reporting extends TestListenerAdapter {
    public static ExtentSparkReporter htmlReporter;
    public static ExtentReports       extent;
    public static ExtentTest          logger;

    BaseBrowser                       bb = new BaseBrowser ();

    @BeforeTest
    @Override
    public void onStart (ITestContext testContext) {
        super.onStart (testContext);
        String timeStamp = new SimpleDateFormat ("dd.MM.yyyy.HH.mm.ss").format (new Date ());// timestamp
        String repName = "Test-Report-" + timeStamp + ".html";

        htmlReporter = new ExtentSparkReporter (System.getProperty ("user.dir") + "/Results/" + repName);
        Capabilities capabilities = ((RemoteWebDriver) BaseBrowser.driver).getCapabilities ();
        extent = new ExtentReports ();
        extent.attachReporter (htmlReporter);
        extent.setSystemInfo ("Host Name", "HRSoft");
        extent.setSystemInfo ("Environment", "SQA");
        extent.setSystemInfo ("OS", System.getProperty ("os.name"));
        extent.setSystemInfo ("Java version", System.getProperty ("java.version"));
        extent.setSystemInfo ("Browser ", capabilities.getBrowserName ());
        extent.setSystemInfo ("user", bb.getProperty ("username"));

        
        htmlReporter.config ().setDocumentTitle ("HRSoft Test Project"); // Tile of report
        htmlReporter.config ().setReportName ("HRSoft Extent Reports");
        // htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP); //location of the
        // chart
        htmlReporter.config ().setTheme (Theme.STANDARD);
    }

    @Override
    public void onTestStart (ITestResult result) {
        super.onTestStart (result);
        logger = extent.createTest (getTestName (result)); // create new entry in the report
        printTestStatus (result, "test start");

    }

    @Override
    public void onTestSuccess (ITestResult tr) {
        super.onTestSuccess (tr);
        printTestStatus (tr, "test passed");
        logger.log (Status.PASS, MarkupHelper.createLabel (getTestName (tr), ExtentColor.GREEN));

    }

    @Override
    public void onTestFailure (ITestResult tr) {
        super.onTestFailure (tr);
        printTestStatus (tr, "FAILED");
        logger = extent.createTest (getTestName (tr)); // create new entry in the report
        logger.log (Status.FAIL, MarkupHelper.createLabel (getTestName (tr), ExtentColor.RED));
        logger.log (Status.FAIL, tr.getThrowable ());
        String screenshotPath = System.getProperty ("user.dir") + "/Screenshots/" + tr.getName () + ".png";
        File src = ((TakesScreenshot) BaseBrowser.driver).getScreenshotAs (OutputType.FILE);
        File trg = new File (screenshotPath);

        try {
            FileUtils.copyFile (src, trg);
        } catch (IOException e) {
            e.printStackTrace ();
        }

        if (trg.exists ()) {
            logger.fail ("Screenshot is below:" + logger.addScreenCaptureFromPath (screenshotPath));
        }

    }

    @Override
    public void onTestSkipped (ITestResult tr) {
        super.onTestSkipped (tr);
        printTestStatus (tr, "test skipped");
        logger.log (Status.SKIP, MarkupHelper.createLabel (getTestName (tr), ExtentColor.ORANGE));
        logger.log (Status.SKIP, tr.getThrowable ());
    }

    @Override
    public void onFinish (ITestContext testContext) {
        extent.flush ();
    }

    private void printTestStatus (ITestResult result, String status) {
        System.out.println (status + ": " + getTestName (result) + "()");
    }

    private String getTestName (ITestResult result) {
        Object[] params = result.getParameters ();
        String[] temp = result.getTestClass ().getName ().split ("\\.");
        String name = join (".", temp[temp.length - 2], temp[temp.length - 1], result.getMethod ().getMethodName ());
        if (params != null && params.length > 0)
            name = join (".", name, stream (params).map (p -> valueOf (p)).collect (joining (".")));
        return name;
    }
    
    public static void log(Status status, String message, boolean captureScreenshot ) {
    	System.out.println(status+":"+message);
    	if(logger == null)
    		return;
    	logger.log(status, message);
    	
    	if(captureScreenshot) {
            String screenshotPath = System.getProperty ("user.dir") + "/Screenshots/" + System.currentTimeMillis() + ".png";
            File src = ((TakesScreenshot) BaseBrowser.driver).getScreenshotAs (OutputType.FILE);
            File trg = new File (screenshotPath);

            try {
                FileUtils.copyFile (src, trg);
            } catch (IOException e) {
                e.printStackTrace ();
            }

            if (trg.exists ()) {
            	logger.addScreenCaptureFromPath (screenshotPath);
            }

    	}
    }
    
    public static void flush() {
    	if(extent == null)
    		return;
    	extent.flush();
    }
    
}
