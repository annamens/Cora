package com.seleniumfy.utils;

import static com.seleniumfy.utils.Log.error;
import static com.seleniumfy.utils.Log.info;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * @author Annameni Srinivas
 *         <a href="mailto:sannameni@gmail.com">sannameni@gmail.com</a>
 */

@Listeners ({ ConfigureFailurePolicyListener.class, Reporting.class })
public class BaseBrowser {

    public static WebDriver                      driver;

    static ExtentReports                         report;
    static ExtentTest                            test;

    protected long                               sleepInMillis  = 500;
    protected int                                webdriverWait  = 10;
    protected long                               millisDuration = 30000l;                        // 30sec
    protected long                               millisPoll     = 1000l;                         // 1
                                                                                                 // sec
    protected boolean                            checkAjaxCalls = true;
    private static final ThreadLocal <WebDriver> webDriver      = new ThreadLocal <> ();
    private static Properties                    prop;

    protected String                             HrSoftUsername = getProperty ("username");
    protected String                             HrSoftPassword = getProperty ("password");
    protected String                             HrSoftTestUrl  = getProperty ("url");
    protected String                             adminUser      = getProperty ("adminUser");

    protected String                             custId         = getProperty ("custId");
    protected String                             compPlanId     = getProperty ("compPlanId");
    protected String                             compPlanName   = getProperty ("compPlanName");
    protected String                             topPlannerName = getProperty ("topPlannerName");
    
    protected String							clientName = getProperty("clientName");

    public String getProperty (String property) {
        prop = new Properties ();
        FileReader fr;
        try {
            fr = new FileReader (System.getProperty ("user.dir") + "/config/config.properties");
            try {
                prop.load (fr);
            } catch (IOException e) {
                e.printStackTrace ();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
        }
        return prop.getProperty (property);

    }

    @BeforeMethod (alwaysRun = true)
    public final void beforeMethodBase () {
        if (hasQuit ())
            initDriver ();
    }

    @BeforeSuite (alwaysRun = true)
    public void initDriver () {
        info ("Opening browser, and initializing webdriver");
        String browser = getProperty ("browser");
        if (browser.equals ("chrome")) {
            ChromeOptions options = new ChromeOptions();
           // options.addArguments("--start-maximized", "--headless", "--window-size=2560,1440","--ignore-certificate-errors","--disable-extensions","--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");
            WebDriverManager.chromedriver ().setup ();
            driver = new ChromeDriver (options);
        }
        if (browser.equals ("firefox")) {
            WebDriverManager.firefoxdriver ().setup ();
            driver = new FirefoxDriver ();
        }
        if (browser.equals ("edge")) {
            WebDriverManager.edgedriver ().setup ();
            driver = new EdgeDriver ();
        }
        driver.manage ().timeouts ().implicitlyWait (Duration.ofSeconds (10));

        driver.manage ().window ().maximize ();
    }

    /**
     * Open a new web browser
     * 
     * @param url
     *            A url string
     */
    public void openBrowser (String url) {
        if (hasQuit ())
            initDriver ();

        if (url != null)
            driver.get (url);
    }

    /**
     * To check if we have closed the current session web browser
     * 
     * @return True if the session has been closed, false if otherwise
     */
    public boolean hasQuit () {
        return driver == null;
    }

    /**
     * Send browser to the location provided in the url and wait for the page to finished loading
     * 
     * @param url
     *            A url string
     * @return Boolean status returning false on timedout
     */
    public boolean navigateTo (String url) {
        driver.navigate ().to (url);
        return true;
    }

    public void doWait (long milliSeconds) {
        try {
            Thread.sleep (milliSeconds);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    static void setDriver (WebDriver driver) {
        info ("setting webDriver ...");
        webDriver.set (driver);
    }

    /**
     * Get the current session webdriver
     * 
     * @return {@link WebDriver}
     */
    public WebDriver getDriver () {
        return webDriver.get ();
    }

    /**
     * close the current browser
     */
    public void closeBrowser () {
        driver.close ();
    }

    @AfterMethod (alwaysRun = true)
    public void deleteCookies () {
        driver.manage ().deleteAllCookies ();
    }

    /**
     * Close the current session web browser
     */
      @AfterSuite (alwaysRun = true)
      public void tearDown () {
      try {
      doWait (millisPoll);
      if (null != driver) {
      info ("closing browser, and nullify webdriver");
      driver.close ();
      driver.quit ();
      }
      } catch (Exception e) {
      error ("failed to execute driver.quit()", e);
      }
      }
     
    
    /**
     * navigates to new tab or window
     */
    public void navigateToTab (int tabIndex) {
        driver.switchTo ().window (new ArrayList <> (driver.getWindowHandles ()).get (tabIndex));
    }

    /**
     * opens a new empty tab and switches to it
     */
    public void openNewEmptyTab () {
        driver.switchTo ().newWindow (WindowType.TAB);
    }

    /**
     * opens a new empty window and switches to it
     */
    public void openNewEmptyWindow () {
        driver.switchTo ().newWindow (WindowType.WINDOW);
    }

}
