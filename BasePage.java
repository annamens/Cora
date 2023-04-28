package com.seleniumfy.utils;

import static com.seleniumfy.utils.Log.debug;
import static com.seleniumfy.utils.Log.error;
import static com.seleniumfy.utils.Log.info;
import static com.seleniumfy.utils.Log.warn;
import static java.lang.String.format;
import static java.time.Duration.ofMillis;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.xpath;
import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeContains;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfElementsToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.numberOfWindowsToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfNestedElementsLocatedBy;
import static org.testng.Assert.assertTrue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import br.com.starcode.parccser.Parser;

/**
 * @author Annameni Srinivas
 *         <a href="mailto:sannameni@gmail.com">sannameni@gmail.com</a>
 */
public class BasePage extends BaseBrowser {

    protected int staticNavBarHeight = 140;

    public boolean navigateTo (String url) {
        driver.navigate ().to (url);
        return hasPageLoaded ();
    }

    public boolean navigateBack () {
        driver.navigate ().back ();
        return hasPageLoaded ();
    }

    public boolean navigateForward () {
        driver.navigate ().forward ();
        return hasPageLoaded ();
    }

    /**
     * Refresh the current page and wait for it to finished loading
     * 
     * @return Boolean status returning false on timedout
     */
    public boolean refresh () {
        driver.navigate ().refresh ();
        return hasPageLoaded ();
    }

    /**
     * Check to see if the page has finished loading
     * 
     * @return True if the page has finished loading, false if otherwise
     */
    public boolean hasPageLoaded () {
        TimeOut timer = new TimeOut (millisDuration, millisPoll);
        boolean isComplete = false;
        while (!timer.Timedout () && ! (isComplete = executeJScript ("return document.readyState").equals ("complete")))
            timer.Wait ();
        if (!isComplete)
            error ("page failed to load");
        return isComplete;
    }

    /**
     * A workaround for an open issue: https://github.com/SeleniumHQ/selenium/issues/1841
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if everything works, false if otherwise
     */
    public boolean clear (String target) {
        return clear (waitForElement (target));
    }
    public void clickOnBlankSpace() {   
    	driver.findElement(By.xpath("//html")).click(); 
    }

    /**
     * A workaround for an open issue: https://github.com/SeleniumHQ/selenium/issues/1841
     * 
     * @param el
     *            {@link WebElement}
     * @return True if everything works, false if otherwise
     */
    public boolean clear (WebElement el) {
        try {
            executeJScript ("arguments[0].value='';", scrollTo (el));
            return true;
        } catch (Exception e) {
            e.printStackTrace ();
            return false;
        }
    }

    /**
     * Wait for HTML DOM element to be present
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return {@link WebElement}
     */
    public WebElement waitForElement (String target) {
        return waitForElement (locateBy (target));
    }

    /**
     * Wait for HTML DOM element to be a
     * 
     * @param by
     *            {@link By}
     * @return {@link WebElement}
     */
    public WebElement waitForElement (By by) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (30), Duration.ofMillis (500))
                                                                                               .until (presenceOfElementLocated (by));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForElement another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (presenceOfElementLocated (by));
        }
    }

    /**
     * Wait for ajax calls to finish
     * 
     * @return True if all calls are finished, false if otherwise
     */
    public boolean waitForAjaxCalls () {
        String jsScript = "if (typeof $ != 'undefined') ";
        jsScript += "{$.fx.off=true; return ($.active == 0 && $(':animated').length == 0);} else return true;";
        if (!checkAjaxCalls)
            return true;
        boolean done = false;
        TimeOut timer = new TimeOut (millisDuration, millisPoll);
        while (! (done = Boolean.valueOf (executeJScript (jsScript)))) {
            timer.Wait ();
            if (!done) {
                error ("timedout");
            }
        }
        return done;
    }

    /**
     * Wait for a file download to complete
     * 
     * @param file
     *            The file location
     * @return True if the has been downloaded, false if otherwise
     */
    public Boolean isFileDownloaded (String file) {
        // String path ="C:\\Users\\srinivas.annameni\\Downloads\\";
        assertTrue (navigateTo ("file:///C:/Users/srinivas.annameni/Downloads/"));
        String locator = format ("//a[contains(text()='%s')]", file);
        TimeOut timer = new TimeOut (millisDuration, millisPoll);
        boolean found = isElementPresent (locator);
        while (!timer.Timedout () && !found) {
            timer.Wait ();
            refresh ();
            found = isElementPresent (locator);
        }

        if (found)
            navigateBack ();
        return found;
    }

    /**
     * Check to see if HTML DOM element exist on the page
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if element exists, false if otherwise
     */
    public boolean isElementPresent (String target) {
        return isElementPresent (locateBy (target));
    }

    /**
     * Check to see if HTML DOM element exist on the page. Not using wait here, we want it to
     * timeout right away, if element is not found
     * 
     * @param by
     *            {@link By}
     * @return True if element exists, false if otherwise
     */
    public boolean isElementPresent (By by) {
        waitForAjaxCalls ();
        try {
            findElement (by);
            return true;
        } catch (Exception e) {
            debug (e.toString ());
            return false;
        }
    }

    /**
     * Check to see if HTML DOM element exist on the page
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @return True if element exists, false if otherwise
     */
    public boolean isElementPresent (WebElement el, String target) {
        try {
            findElement (el, target);
            return true;
        } catch (Exception e) {
            debug (e.toString ());
            return false;
        }
    }

    /**
     * An alternative wait using {@link FluentWait}
     * 
     * @param millisDuration
     *            The duration of the wait in milliseconds
     * @param millisPoll
     *            The duration in milliseconds to sleep between polls
     * @param func
     *            {@link Function} that returns a boolean condition
     * @return the status of {@link Function} at the end of the wait loop
     */
    public boolean waitUntil (long millisDuration, long millisPoll, Function <WebDriver, Boolean> func) {
        return new FluentWait <> (driver).withTimeout (ofMillis (millisDuration)).pollingEvery (ofMillis (millisPoll))
                                         .until (func);
    }

    private ExpectedCondition <List <WebElement>> visibilityOfEls (String target) {
        return visibilityOfAllElementsLocatedBy (locateBy (target));
    }

    private ExpectedCondition <List <WebElement>> visibilityOfEls (WebElement el, String target) {
        return visibilityOfNestedElementsLocatedBy (el, locateBy (target));

    }

    /**
     * Decide what method to use to locate the element, either using xpath or css
     * 
     * @param target
     *            HTML DOM child elements (css or xpath)
     * @return {@link By}
     */
    public By locateBy (String target) {
        try {
            return isCssSelector (target) ? cssSelector (target) : xpath (target);
        } catch (Exception e) {
            error ("", e);
            return null;
        }
    }

    /**
     * Wait for HTML DOM element to be visible
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return {@link WebElement}
     */
    public WebElement waitForElementVisible (String target) {
        return waitForElementVisible (locateBy (target));
    }

    /**
     * Wait for HTML DOM element to be visible
     * 
     * @param by
     *            {@link By}
     * @return {@link WebElement}
     */
    public WebElement waitForElementVisible (By by) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (30), Duration.ofMillis (500))
                                                                                               .until (visibilityOfElementLocated (by));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForElementVisible another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (visibilityOfElementLocated (by));
        }
    }

    /**
     * Wait for HTML DOM element to be visible
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @return {@link WebElement}
     */
    public WebElement waitForElementVisible (WebElement el, String target) {
        return waitForElementsVisible (el, target).get (0);
    }

    /**
     * Wait for all HTML DOM elements to be visible
     * 
     * @param target
     *            HTML DOM elements (css or xpath)
     * @return A list of visible {@link WebElement}
     */
    public List <WebElement> waitForElementsVisible (String target) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (10),
                    Duration.ofMillis (500)).until (visibilityOfEls (target));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForElementsVisible another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (visibilityOfEls (target));
        }
    }

    /**
     * Wait for HTML DOM element to be visible
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @return {@link WebElement}
     */
    public List <WebElement> waitForElementsVisible (WebElement el, String target) {
        waitForAjaxCalls ();
        try {

            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500))
                                                                                               .until (visibilityOfEls (el,
                                                                                                                        target));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForElementsVisible another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (visibilityOfEls (el, target));
        }
    }

    /**
     * Wrapper for {@link WebElement}.isDisplayed()
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if the element is invisible, false if otherwise
     */
    public boolean waitForElementInvisible (String target) {
        return waitForElementInvisible (locateBy (target));
    }

    /**
     * Wrapper for {@link WebElement}.isDisplayed()
     * 
     * @param by
     *            {@link By}
     * @return True if the element is invisible, false if otherwise
     */
    public boolean waitForElementInvisible (By by) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (20), Duration.ofMillis (500))
                                                                                               .until (invisibilityOfElementLocated (by));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForElementInvisible another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (20), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (invisibilityOfElementLocated (by));
        }
    }

    /**
     * Wait for HTML DOM element to be clickable
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return {@link WebElement}
     */
    public WebElement waitForElementClickable (String target) {
        return waitForElementClickable (locateBy (target));
    }

    /**
     * Wait for HTML DOM element to be clickable
     * 
     * @param by
     *            {@link By}
     * @return {@link WebElement}
     */
    public WebElement waitForElementClickable (By by) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (30), Duration.ofMillis (500))
                                                                                               .until (elementToBeClickable (by));
        } catch (Exception e) {
             warn (e.toString ());
             info ("let's give waitForElementClickable another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                     info ("ok, it's done ...");
                }
            }.until (elementToBeClickable (by));
        }
    }

    /**
     * Wait for HTML DOM element to be clickable
     * 
     * @param el
     *            {@link WebElement}
     * @return {@link WebElement}
     */
    public WebElement waitForElementClickable (WebElement el) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (30),
                    Duration.ofMillis (500)).until (elementToBeClickable (el));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForElementClickable another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (elementToBeClickable (el));
        }
    }

    /**
     * Wait for HTML DOM element to be visible
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if the element is visible, false if otherwise
     */
    public boolean waitUntilVisible (String target) {
        try {
            return waitForElementVisible (target).isDisplayed ();
        } catch (Exception e) {
            warn (e.toString ());
            return false;
        }
    }

    /**
     * Execute a javascript command
     * 
     * @param javascript
     *            A string of javascript command
     * @return The string output
     */
    public String executeJScript (String javascript) {
        return String.valueOf ( ((JavascriptExecutor) driver).executeScript (javascript));
    }

    /**
     * Execute a javascript command on the given {@link WebElement}
     * 
     * @param javascript
     *            A string of javascript command
     * @param el
     *            {@link WebElement}
     * @return The string output
     */
    public String executeJScript (String javascript, WebElement el) {
        return String.valueOf ( ((JavascriptExecutor) driver).executeScript (javascript, el));
    }

    /**
     * Click on HTML DOM element
     * 
     * @param by
     *            {@link By}
     * @return True if everything works, false if otherwise
     */
    public boolean click (By by) {
        return click (waitForElementClickable (by));
    }

    /**
     * Click on HTML DOM element
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if everything works, false if otherwise
     */
    public boolean click (String target) {
        scrollToView (target);
        return click (waitForElementClickable (target));
    }

    /**
     * Click on HTML DOM element
     * 
     * @param el
     *            {@link WebElement}
     * @return True if everything works, false if otherwise
     */
    public boolean click (WebElement el) {
        try {
            scrollTo (el).click ();
            return true;
        } catch (Exception e1) {
            warn (String.valueOf (e1));
            info ("let's give click another try ...");
            doWait (sleepInMillis);
            try {
                executeJScript ("arguments[0].click();", el);
                info ("ok, it's done ...");
                return true;
            } catch (Exception e2) {
                error (String.valueOf (e2), e2);
                return false;
            }
        }
    }

    /**
     * Wrapper for webdriver findElement()
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return {@link WebElement}
     */
    public WebElement findElement (String target) {
        return driver.findElement (locateBy (target));
    }

    /**
     * Wrapper for webdriver findElement()
     * 
     * @param by
     *            {@link By}
     * @return {@link WebElement}
     */
    public WebElement findElement (By by) {
        return driver.findElement (by);
    }

    /**
     * Wrapper for webdriver findElement()
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM element (css or xpath)
     * @return {@link WebElement}
     */
    public WebElement findElement (WebElement el, String target) {
        return el.findElement (locateBy (target));
    }

    /**
     * Wrapper for webdriver findElements()
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return A list of {@link WebElement}
     */
    public List <WebElement> findElements (String target) {
        return driver.findElements (locateBy (target));
    }

    /**
     * Wrapper for webdriver findElements()
     * 
     * @param by
     *            {@link By}
     * @return A list of {@link WebElement}
     */
    public List <WebElement> findElements (By by) {
        return driver.findElements (by);
    }

    /**
     * Wrapper for webdriver findElements()
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM element (css or xpath)
     * @return A list of {@link WebElement}
     */
    public List <WebElement> findElements (WebElement el, String target) {
        return el.findElements (locateBy (target));
    }

    /**
     * Check to see if HTML DOM element is visible on the page. Not using wait here, we want it to
     * timeout right away, if element is invisible
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if element is visible, false if otherwise
     */
    public boolean isElementVisible (String target) {
        try {
            return isElementVisible (findElement (target));
        } catch (Exception e) {
            info (format ("unable to find elemet: %s, exception: %s", target, e.toString ()));
            return false;
        }
    }

    /**
     * Check to see if HTML DOM element is visible on the page
     * 
     * @param by
     *            {@link By}
     * @return True if element is visible, false if otherwise
     */
    public boolean isElementVisible (By by) {
        try {
            return isElementVisible (findElement (by));
        } catch (Exception e) {
            info (format ("unable to find elemet: %s, exception: %s", by, e.toString ()));
            return false;
        }
    }

    /**
     * Check to see if HTML DOM element is visible on the page
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @return True if element is visible, false if otherwise
     */
    public boolean isElementVisible (WebElement el, String target) {
        try {
            return isElementVisible (findElement (el, target));
        } catch (Exception e) {
            info (format ("unable to find elemet: %s, exception: %s", target, e.toString ()));
            return false;
        }
    }

    /**
     * Check to see if HTML DOM element is visible on the page
     * 
     * @param el
     *            {@link WebElement}
     * @return True if element is visible, false if otherwise
     */
    public boolean isElementVisible (WebElement el) {
        waitForAjaxCalls ();
        try {
            return el.isDisplayed ();
        } catch (Exception e) {
            warn (e.toString ());
            return false;
        }
    }

    /**
     * Get the HTML DOM element's attribute
     * 
     * @param target
     *            HTML DOM child element (css or xpath)
     * @param attribute
     *            The target attribute name
     * @return The string value of the target attribute
     */
    public String getAttribute (String target, String attribute) {
        return getAttribute (locateBy (target), attribute);
    }

    /**
     * Get the HTML DOM element's attribute with some flexibility on the method used to locate the
     * element (ie. css, xpath, id, tag, name, etc)
     * 
     * @param by
     *            {@link By}
     * @param attribute
     *            The target attribute name
     * @return The string value of the target attribute
     */
    public String getAttribute (By by, String attribute) {
        return getAttribute (waitForElement (by), attribute);
    }

    /**
     * Get the HTML DOM element's attribute
     * 
     * @param el
     *            {@link WebElement}
     * @param attribute
     *            The target attribute name
     * @return The string value of the target attribute
     */
    public String getAttribute (WebElement el, String attribute) {
        String attr = el.getAttribute (attribute);
        return attr != null && attr.length () >= 0 ? attr.trim () : null;
    }

    /**
     * Get the HTML DOM element's attribute
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @param attribute
     *            The target attribute name
     * @return The string value of the target attribute
     */
    public String getAttribute (WebElement el, String target, String attribute) {
        return getAttribute (findElement (el, target), attribute);
    }

    /**
     * Get multiple HTML DOM elements attribute all at once
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @param attribute
     *            The target attribute name
     * @return A list (string) of attribute value
     */
    public List <String> getAttributeList (String target, String attribute) {
        return getAttributeList (locateBy (target), attribute);
    }

    /**
     * Get multiple HTML DOM elements attribute all at once
     * 
     * @param by
     *            {@link By}
     * @param attribute
     *            The target attribute name
     * @return A list (string) of attribute value
     */
    public List <String> getAttributeList (By by, String attribute) {
        return waitForElements (by).stream ()
                                   .filter (e -> e.getAttribute (attribute) != null)
                                   .map (e -> e.getAttribute (attribute).trim ()).collect (toList ());
    }

    /**
     * Get multiple HTML DOM elements attribute all at once
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM element (css or xpath)
     * @param attribute
     *            The target attribute name
     * @return A list (string) of attribute value
     */
    public List<Object> getAttributeList (WebElement el, String target, String attribute) {
        return waitForElements (el, target).stream ()
                                           .filter (e -> e.getAttribute (attribute) != null)
                                           .map (e -> e.getAttribute (attribute).trim ()).collect (toList ());
    }

    /**
     * Look for the present of a text. Element is not always visible until you scroll down (ie.
     * flyover). So, first we wait for the element to be present and then scroll to than element.
     * Afterward, we look for the present of the text.
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @param text
     *            The string text to search
     * @return True if the text is present, false if otherwise
     */
    public boolean isTextInElement (String target, String text) {
        try {
            return isTextInElement (scrollTo (waitForElement (target)), text);
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give isTextInElement another try ...");
            doWait (sleepInMillis);
            return isTextInElement (scrollTo (waitForElement (target)), text);
        }
    }

    /**
     * Look for the present of a text
     * 
     * @param el
     *            {@link WebElement}
     * @param text
     *            The string text to search
     * @return True if the text is present, false if otherwise
     */
    public boolean isTextInElement (WebElement el, String text) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (10),
                    Duration.ofMillis (500)).until (textPresentInEl (el, text));
        } catch (Exception e) {
            error ("looking for=" + text + ", found text=" + el.getText ());
            error (e.getMessage (), e);
            return false;
        }
    }

    /**
     * Look for the present of a text
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @param text
     *            The string text to search
     * @return True if the text is present, false if otherwise
     */
    public boolean isTextInElement (WebElement el, String target, String text) {
        waitForAjaxCalls ();
        WebElement el2 = findElement (el, target);
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (10),
                    Duration.ofMillis (500)).until (textPresentInEl (el2, text));
        } catch (Exception e) {
            error ("looking for=" + text + ", found text=" + el2.getText ());
            error (e.getMessage (), e);
            return false;
        }
    }

    private ExpectedCondition <Boolean> textPresentInEl (WebElement el, String text) {
        return textToBePresentInElement (el, text);
    }

    /**
     * Verify the element is nolonger present (and it's not invisible)
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if the element has disappeared, false if otherwise
     */
    public boolean noSuchElementPresent (String target) {
        waitForAjaxCalls ();
        try {
            if (new WebDriverWait (driver, Duration.ofSeconds (10),
                    Duration.ofMillis (500)).until (invisibilityOfEl (target))) {
                TimeOut timer = new TimeOut (millisDuration, millisPoll);
                boolean isPresent = false;
                while (!timer.Timedout () && (isPresent = isElementPresent (target)))
                    timer.Wait ();
                if (isPresent)
                    warn ("element=" + target + ", still present after " + millisDuration + "msec");
                else
                    return true;
            }
            return false;
        } catch (Exception e) {
            error (e.getMessage (), e);
            return false;
        }
    }

    /**
     * Verify the element is hidden
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return True if the element is hidden, false if otherwise
     */
    public boolean isElementHidden (String target) {
        waitForAjaxCalls ();
        try {
            if (new WebDriverWait (driver, Duration.ofSeconds (10),
                    Duration.ofMillis (500)).until (invisibilityOfEl (target)))
                return isElementPresent (target);
            return false;
        } catch (Exception e) {
            debug (e.toString ());
            return false;
        }
    }

    private ExpectedCondition <Boolean> invisibilityOfEl (String target) {
        return invisibilityOfElementLocated (locateBy (target));
    }

    /**
     * Wrapper for {@link Select}.getOptions()
     * 
     * @param select
     *            &lt;select&gt; element (css or xpath)
     * @return drop down option values
     */
    public void scrollVertical()
    {
    	
    }
    public List <String> getDropdownOptions (String select) {
        try {
            Select dropdown = new Select (scrollTo (waitForElement (select)));
            return dropdown.getOptions ().stream ().map (el -> el.getText ()).collect (toList ());
        } catch (Exception e) {
            error (e.getMessage (), e);
            return null;
        }
    }

    /**
     * Wrapper for {@link Select}.selectByValue()
     * 
     * @param select
     *            &lt;select&gt; element (css or xpath)
     * @param value
     *            The string value to match against
     * @return True if the matching option has been selected, false if otherwise
     */
    public boolean clickAndSelectValue (String select, String value) {
        try {
            Select dropdown = new Select (scrollTo (waitForElementClickable (select)));
            dropdown.selectByValue (value);

            TimeOut timer = new TimeOut (millisDuration, millisPoll);
            String target = null;
            while (!timer.Timedout () && !value.equals (target = readInput (dropdown.getFirstSelectedOption ()))) {
                timer.Wait ();
                dropdown.selectByValue (value);
            }
            return value.equals (target);
        } catch (Exception e) {
            error (e.getMessage (), e);
            return false;
        }
    }

    /**
     * Wrapper for {@link Select}.selectByVisibleText()
     * 
     * @param select
     *            &lt;select&gt; element (css or xpath)
     * @param text
     *            The string text to match against
     * @return True if the matching option has been selected, false if otherwise
     */
    public boolean clickAndSelectText (String select, String text) {
        return clickAndSelectText (waitForElementClickable (select), text);
    }

    /**
     * Wrapper for {@link Select}.selectByVisibleText()
     * 
     * @param el
     *            {@link WebElement}
     * @param text
     *            The string text to match against
     * @return True if the matching option has been selected, false if otherwise
     */
    public boolean clickAndSelectText (WebElement el, String text) {
        try {
            Select dropdown = new Select (scrollTo (el));
            dropdown.selectByVisibleText (text);

            TimeOut timer = new TimeOut (millisDuration, millisPoll);
            String target = null;
            while (!timer.Timedout () && !text.equals (target = getText (dropdown.getFirstSelectedOption ()))) {
                timer.Wait ();
                dropdown.selectByVisibleText (text);
            }
            return text.equals (target);
        } catch (Exception e) {
            error (e.getMessage (), e);
            return false;
        }
    }

    /**
     * Wrapper for {@link Select}.selectByVisibleText()
     * 
     * @param el
     *            {@link WebElement}
     * @param select
     *            &lt;select&gt; child element (css or xpath)
     * @param text
     *            The string text to match against
     * @return True if the matching option has been selected, false if otherwise
     */
    public boolean clickAndSelectText (WebElement el, String select, String text) {
        return clickAndSelectText (findElement (el, select), text);
    }

    /**
     * Wrapper for {@link Select}.selectByIndex()
     * 
     * @param select
     *            &lt;select&gt; element (css or xpath)
     * @param index
     *            The option at this index will be selected
     * @return True if the option at the given index has been selected, false if otherwise
     */
    public String clickAndSelectIndex (String select, int index) {
        try {
            Select dropdown = new Select (scrollTo (waitForElementClickable (select)));
            dropdown.selectByIndex (index);
            return getText (dropdown.getFirstSelectedOption ());
        } catch (Exception e) {
            error (e.getMessage (), e);
            return null;
        }
    }

    /**
     * Wrapper for {@link Select}.getFirstSelectedOption()
     * 
     * @param select
     *            &lt;select&gt; element (css or xpath)
     * @return The first selected option's text
     */
    public String getFirstSelectedText (String select) {
        return getFirstSelectedText (waitForElementVisible (select));
    }

    /**
     * Wrapper for {@link Select}.getFirstSelectedOption()
     * 
     * @param el
     *            {@link WebElement}
     * @return The first selected option's text
     */
    public String getFirstSelectedText (WebElement el) {
        try {
            return getText (new Select (scrollTo (el)).getFirstSelectedOption ());
        } catch (Exception e) {
            error (e.getMessage (), e);
            return null;
        }
    }

    /**
     * Wrapper for {@link Select}.getFirstSelectedOption()
     * 
     * @param el
     *            {@link WebElement}
     * @param select
     *            &lt;select&gt; child element (css or xpath)
     * @return The first selected option's text
     */
    public String getFirstSelectedText (WebElement el, String select) {
        return getFirstSelectedText (findElement (el, select));
    }

    /**
     * Wrapper for {@link Select}.getFirstSelectedOption()
     * 
     * @param select
     *            &lt;select&gt; element (css or xpath)
     * @return The first selected option's value attribute
     */
    public String getFirstSelectedValue (String select) {
        return getFirstSelectedValue (waitForElementVisible (select));
    }

    /**
     * Wrapper for {@link Select}.getFirstSelectedOption()
     * 
     * @param el
     *            {@link WebElement}
     * @return The first selected option's value attribute
     */
    public String getFirstSelectedValue (WebElement el) {
        try {
            return readInput (new Select (scrollTo (el)).getFirstSelectedOption ());
        } catch (Exception e) {
            error (e.getMessage (), e);
            return null;
        }
    }

    /**
     * Press a keyboard key
     * 
     * @param key
     *            {@link Keys}
     * @return True if everything works, false if otherwise
     */
    public boolean pressKey (Keys key) {
        try {
            doWait (sleepInMillis);
            driver.switchTo ().activeElement ().sendKeys (key);
            return true;
        } catch (Exception e) {
            error (e.getMessage (), e);
            return false;
        }
    }

    /**
     * Simulate a mouse hover movement
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     */
    public void hover (String target) {
        hover (locateBy (target));
    }

    /**
     * Simulate a mouse hover movement
     * 
     * @param by
     *            {@link By}
     */
    public void hover (By by) {
        hover (waitForElement (by));
    }

    /**
     * Simulate a mouse hover movement
     * 
     * @param el
     *            {@link WebElement}
     */
    public void hover (WebElement el) {
        scrollTo (el);
        new Actions (driver).moveToElement (el).build ().perform ();
        waitForAjaxCalls ();
    }

    /**
     * Simulate a mouse hover movement
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM element (css or xpath)
     */
    public void hover (WebElement el, String target) {
        hover (findElement (el, target));
    }

    /**
     * Scroll to the given element on the page
     * 
     * @param el
     *            {@link String}
     */
    public void scrollTo (String target) {
        scrollTo (locateBy (target));
    }

    /**
     * Scroll to the given element on the page
     * 
     * @param el
     *            {@link By}
     */
    public void scrollTo (By by) {
        scrollTo (findElement (by));
    }

    /**
     * Scroll to the given element on the page
     * 
     * @param el
     *            {@link WebElement}
     * @return {@link WebElement}
     */
    public WebElement scrollTo (WebElement el) {
        Point location = el.getLocation ();
        Dimension size = el.getSize ();
        int y = location.getY () - (size.getHeight () / 2);
        executeJScript (format ("window.scrollTo (0, %s)",
                                y <= staticNavBarHeight ? staticNavBarHeight : y - staticNavBarHeight));
        return el;
    }
    
    public void scrollToView(By by) {
    	scrollToView(findElement(by));
    }
    public void scrollToView(String target) {
    	scrollToView(findElement(target));
    }
    public void scrollToView(WebElement el) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
    }

    /**
     * Get the displayed text of &lt;input&gt; element
     * 
     * @param target
     *            &lt;input&gt; element (css or xpath)
     * @return The displayed text string
     */
    public String readInput (String target) {
        return getAttribute (locateBy (target), "value");
    }

    /**
     * Get the displayed text of &lt;input&gt; element
     * 
     * @param by
     *            {@link By}
     * @return The displayed text string
     */
    public String readInput (By by) {
        return getAttribute (waitForElementVisible (by), "value");
    }

    /**
     * Get the displayed text of &lt;input&gt; element
     * 
     * @param el
     *            {@link WebElement}
     * @return The displayed text string
     */
    public String readInput (WebElement el) {
        return getAttribute (el, "value");
    }

    /**
     * Get the displayed text of &lt;input&gt; element
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            &lt;input&gt; child element (css or xpath)
     * @return The displayed text string
     */
    public String readInput (WebElement el, String target) {
        return getAttribute (el, target, "value");
    }

    /**
     * Get the displayed text of multiple &lt;input&gt; elements
     * 
     * @param target
     *            &lt;input&gt; element (css or xpath)
     * @return A list (string) of displayed texts
     */
    public List <String> readInputList (String target) {
        return getAttributeList (target, "value");
    }

    /**
     * Get the displayed text of multiple &lt;input&gt; elements
     * 
     * @param by
     *            {@link By}
     * @return A list (string) of displayed texts
     */
    public List <String> readInputList (By by) {
        return getAttributeList (by, "value");
    }

    /**
     * Get the displayed text of multiple &lt;input&gt; elements
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            &lt;input&gt; element (css or xpath)
     * @return A list (string) of displayed texts
     */
    public List<Object> readInputList (WebElement el, String target) {
        return getAttributeList (el, target, "value");
    }

    /**
     * Wait for all HTML DOM elements to be present
     * 
     * @param target
     *            HTML DOM elements (css or xpath)
     * @return A list of {@link WebElement}
     */
    public List <WebElement> waitForElements (String target) {
        return waitForElements (locateBy (target));
    }

    /**
     * Wait for all HTML DOM elements to be present
     * 
     * @param by
     *            {@link By}
     * @return A list of {@link WebElement}
     */
    public List <WebElement> waitForElements (By by) {
        waitForAjaxCalls ();
        long sleepInMillis = 500;
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (20),
                    Duration.ofSeconds (20)).until (presenceOfAllElementsLocatedBy (by));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForElements another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (presenceOfAllElementsLocatedBy (by));
        }
    }

    /**
     * Wait for all HTML DOM elements to be present
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child elements (css or xpath)
     * @return A list of {@link WebElement}
     */
    public List <WebElement> waitForElements (WebElement el, String target) {
        return findElements (el, target);
    }

    /**
     * Get the displayed text of multiple HTML DOM elements all at once
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return A list (string) of displayed texts
     */
    public List <String> getTextList (String target) {
        return getTextList (locateBy (target));
    }

    /**
     * Get the displayed text of multiple HTML DOM elements all at once
     * 
     * @param by
     *            {@link By}
     * @return A list (string) of displayed texts
     */
    public List <String> getTextList (By by) {
        return waitForElements (by).stream ().map (e -> getText (e)).collect (toList ());
    }

    /**
     * Get the displayed text of multiple HTML DOM elements all at once
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @return A list (string) of displayed texts
     */
    public List <String> getTextList (WebElement el, String target) {
        return waitForElements (el, target).stream ().map (e -> getText (e)).collect (toList ());
    }

    /**
     * Get the displayed text
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @return The displayed text string
     */
    public String getText (String target) {
        return getText (locateBy (target));
    }

    /**
     * Get the displayed text
     * 
     * @param by
     *            {@link By}
     * @return The displayed text string
     */
    public String getText (By by) {
        return getText (waitForElementVisible (by));
    }

    /**
     * Get the displayed text
     * 
     * @param el
     *            {@link WebElement}
     * @return The displayed text string
     */
    public String getText (WebElement el) {
        String text = el.getText ();
        return text != null && text.length () >= 0 ? text.trim () : null;
    }

    /**
     * Get the displayed text
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @return The displayed text string
     */
    public String getText (WebElement el, String target) {
        return getText (findElement (el, target));
    }

    /**
     * Simulate user's typing input
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @param text
     *            Text to enter
     * @return True if everything works, false if otherwise
     */
    public boolean setText (String target, String text) {
        return setText (locateBy (target), text);
    }

    /**
     * Simulate user's typing input
     * 
     * @param by
     *            {@link By}
     * @param text
     *            Text to enter
     * @return True if everything works, false if otherwise
     */
    public boolean setText (By by, String text) {
        return setText (waitForElementVisible (by), text);
    }

    /**
     * Simulate user's typing input
     * 
     * @param el
     *            {@link WebElement}
     * @param text
     *            Text to enter
     * @return True if everything works, false if otherwise
     */
    public boolean setText (WebElement el, String text) {
        try {
            scrollTo (el).sendKeys (text);
            return true;
        } catch (Exception e) {
            error (e.getMessage (), e);
            return false;
        }
    }

    /**
     * Simulate user's typing input
     * 
     * @param el
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @param text
     *            Text to enter
     * @return True if everything works, false if otherwise
     */
    public boolean setText (WebElement el, String target, String text) {
        return setText (waitForElementVisible (el, target), text);
    }

    /**
     * 
     * @param by
     *            {@link By}
     * @param propertyName
     *            css property name
     * @return css property value
     */
    public String getCssValue (By by, String propertyName) {
        return getCssValue (waitForElement (by), propertyName);
    }

    /**
     * Get CSS property value
     * 
     * @param element
     *            {@link WebElement}
     * @param propertyName
     *            css property name
     * @return css property value
     */
    public String getCssValue (WebElement element, String propertyName) {
        return element.getCssValue (propertyName);
    }

    /**
     * Get CSS property value
     * 
     * @param element
     *            {@link WebElement}
     * @param target
     *            HTML DOM child element (css or xpath)
     * @param propertyName
     *            css property name
     * @return css property value
     */
    public String getCssValue (WebElement element, String target, String propertyName) {
        return findElement (element, target).getCssValue (propertyName);
    }

    /**
     * Get CSS property value
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @param propertyName
     *            css property name
     * @return css property value
     */
    public String getCssValue (String target, String propertyName) {
        return getCssValue (locateBy (target), propertyName);
    }

    /**
     * Check if the locator an xpath one
     * 
     * @param locator
     *            A string locator path
     * @return True if the locator is a css selector, false if otherwise
     */
    public boolean isCssSelector (String locator) {
        try {
            Parser.parse (locator);
        } catch (Exception e) {
            debug (format ("locator path '%s' is not a css selector", locator));
            return false;
        }
        return true;
    }

    /**
     * Verify the element's class has specific value
     * 
     * @param target
     *            HTML DOM element (css or xpath)
     * @param value
     *            Expected class value
     * @return True if the class contained a specific value, false if otherwise
     */
    public boolean checkClassFor (String target, String value) {
        return waitForAttrContains (locateBy (target), "class", value);
    }

    /**
     * Verify the element's class has specific value
     * 
     * @param by
     *            {@link By}
     * @param value
     *            Expected class value
     * @return True if the class contained a specific value, false if otherwise
     */
    public boolean checkClassFor (By by, String value) {
        return waitForAttrContains (by, "class", value);
    }

    /**
     * Verify the element's class has specific value
     * 
     * @param el
     *            {@link WebElement}
     * @param value
     *            Expected class value
     * @return True if the class contained a specific value, false if otherwise
     */
    public boolean checkClassFor (WebElement el, String value) {
        return waitForAttrContains (el, "class", value);
    }

    /**
     * Wait for the total number of window to equal <b>numOfWindows</b>
     * 
     * @param numOfWindows
     *            The expected number of window to be
     * @return True if the total number of window is equal to <b>numOfWindows</b>, false if
     *         otherwise
     */
    public boolean waitForChildWindows (int numOfWindows) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (
                    driver, Duration.ofSeconds (10), Duration.ofMillis (500))
                                                                             .until (numberOfWindowsToBe (numOfWindows));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForChildWindows another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (numberOfWindowsToBe (numOfWindows));
        }
    }

    /**
     * Wait for an alert to show
     * 
     * @return {@link Alert}
     */
    public Alert waitForAlert () {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (driver, Duration.ofSeconds (10),
                    Duration.ofMillis (500)).until (alertIsPresent ());
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForAlert another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (alertIsPresent ());
        }
    }

    /**
     * wait for alert to Accept when no such element exception
     */
    public void acceptAlertWithoutException () {
        Wait <WebDriver> wait = new FluentWait <WebDriver> (driver).withTimeout (Duration.ofSeconds (10))
                                                                   .pollingEvery (Duration.ofSeconds (10))
                                                                   .ignoring (NoSuchElementException.class);
        Alert alert = wait.until (new Function <WebDriver, Alert> () {
            public Alert apply (WebDriver driver) {
                try {
                    return driver.switchTo ().alert ();
                } catch (NoAlertPresentException e) {
                    return null;
                }
            }
        });
        alert.accept ();
    }

    /**
     * Wait for an attribute in the element to contain a specific value
     * 
     * @param by
     *            {@link By}
     * @param attribute
     *            The target attribute name
     * @param value
     *            Expected attribute value
     * @return True if the attribute contained a specific value, false if otherwise
     */
    public boolean waitForAttrContains (By by, String attribute, String value) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (
                    driver, Duration.ofSeconds (10), Duration.ofMillis (500))
                                                                             .until (attributeContains (by,
                                                                                                        attribute,
                                                                                                        value));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForAttrContains another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (attributeContains (by, attribute, value));
        }
    }

    /**
     * Wait for an attribute in the element to contain a specific value
     * 
     * @param el
     *            {@link WebElement}
     * @param attribute
     *            The target attribute name
     * @param value
     *            Expected attribute value
     * @return True if the attribute contained a specific value, false if otherwise
     */
    public boolean waitForAttrContains (WebElement el, String attribute, String value) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (
                    driver, Duration.ofSeconds (10), Duration.ofMillis (500))
                                                                             .until (attributeContains (el,
                                                                                                        attribute,
                                                                                                        value));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitForAttrContains another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (attributeContains (el, attribute, value));
        }
    }

    /**
     * Wait for the total number of the target element to equal <b>numOfElements</b>
     * 
     * @param target
     *            HTML DOM elements (css or xpath)
     * @param numOfElements
     *            The expected number of element to be
     * @return A list of visible {@link WebElement}
     */
    public List <WebElement> waitForNumberOfElementsToBe (String target, int numOfElements) {
        return waitForNumberOfElementsToBe (locateBy (target), numOfElements);
    }

    /**
     * Wait for the total number of the target element to equal <b>numOfElements</b>
     * 
     * @param by
     *            {@link By}
     * @param numOfElements
     *            The expected number of element to be
     * @return A list of visible {@link WebElement}
     */
    public List <WebElement> waitForNumberOfElementsToBe (By by, int numOfElements) {
        waitForAjaxCalls ();
        try {
            return new WebDriverWait (
                    driver, Duration.ofSeconds (10), Duration.ofMillis (500))
                                                                             .until (numberOfElementsToBe (by,
                                                                                                           numOfElements));
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give waitFornumberOfElementsToBe another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done ...");
                }
            }.until (numberOfElementsToBe (by, numOfElements));
        }
    }

    /**
     * Wait for angular5 calls
     * 
     * @return True if all calls are stable
     */
    public boolean waitForAngularReady () {
        String jsScript = "return window.getAllAngularTestabilities().findIndex(x=>!x.isStable()) === -1";
        TimeOut timer = new TimeOut (millisDuration, millisPoll);
        boolean isStable = false;
        while (!timer.Timedout () && ! (isStable = Boolean.valueOf (executeJScript (jsScript))))
            timer.Wait ();
        if (!isStable)
            error ("timedout");
        return isStable;
    }

    /**
     * switches to the frame
     * 
     * @param by
     *            {@link String}
     */
    public void switchToFrame (String target) {
        switchToFrame (locateBy (target));
    }

    /**
     * switches to the frame
     * 
     * @param by
     *            {@link By}
     */
    public void switchToFrame (By by) {
        switchToFrame (findElement (by));
    }

    /**
     * switches to the default frame content
     * 
     */
    public void switchToDefaultContent () {
        driver.switchTo ().defaultContent ();
    }

    /**
     * switches to the frame
     * 
     * @param by
     *            {@link WebElement}
     * @return
     */
    public WebDriver switchToFrame (WebElement el) {
        try {
            return new WebDriverWait (
                    driver, Duration.ofSeconds (10), Duration.ofMillis (500))
                                                                             .until (frameToBeAvailableAndSwitchToIt (el));
        } catch (NoSuchFrameException e) {
            warn (e.toString ());
            info ("let's give waitFornumberOfElementsToBe another try ...");
            doWait (sleepInMillis);
            return new WebDriverWait (
                    driver, Duration.ofSeconds (10), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done...");
                }
            }.until (frameToBeAvailableAndSwitchToIt (el));

        }

    }

    /**
     * Accepts alert
     */
    public void alertAccept () {
        try {
            new WebDriverWait (
                    driver, Duration.ofSeconds (5), Duration.ofMillis (500))
                                                                             .until (alertIsPresent ());
            driver.switchTo ().alert ().accept ();
        } catch (Exception e) {
            warn (e.toString ());
            info ("let's give alertAccept another try ...");
            doWait (sleepInMillis);

            new WebDriverWait (
                    driver, Duration.ofSeconds (5), Duration.ofMillis (500)) {
                {
                    info ("ok, it's done...");
                }
            }
             .until (alertIsPresent ());
            driver.switchTo ().alert ().accept ();
        }
    }

    /**
     * checks if an alert is present or not
     * @return 
     * 
     * @returns true if alert is present
     * 
     */
    public boolean isAlertPresent () {
        try 
        { 
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds (2));
           wait.until(alertIsPresent()); 
           return true; 
        }   
        catch (NoAlertPresentException e) 
        { 
            warn (e.toString ());
            return false; 
        }  
       
    }

    /**
     * Dismisses alerts
     * 
     */
    public void alertDismiss () {
        isAlertPresent ();
        Alert al = driver.switchTo ().alert ();
        al.dismiss ();
    }

    /**
     * @return text from the alert pop up
     */
    public String alertGetText () {
        isAlertPresent ();
        Alert al = driver.switchTo ().alert ();
        return al.getText ();
    }

    /**
     * sends input message to alerts
     */
    public void alertSetText (String message) {
        isAlertPresent ();
        Alert al = driver.switchTo ().alert ();
        al.sendKeys (message);
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
