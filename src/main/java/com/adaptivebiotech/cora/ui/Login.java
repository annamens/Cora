package com.adaptivebiotech.cora.ui;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.info;
import static org.testng.Assert.assertTrue;
import com.seleniumfy.test.utils.BasePage;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class Login extends BasePage {

    public Login () {
        staticNavBarHeight = 35;
    }

    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".cora-mascot"));
    }

    public void enterUsername (String email) {
        assertTrue (setText ("#userName", email));
    }

    public void enterPassword (String pass) {
        assertTrue (setText ("#password", pass));
    }

    public void clickSignIn () {
        assertTrue (click ("button[type='submit']"));
    }

    public void doLogin () {
        doLogin (coraTestUser, coraTestPass);
    }

    public void doLogin (String user, String pass) {
        String url = coraTestUrl + "/cora/login";
        info ("logging-in to Cora");
        openBrowser (url);
        isCorrectPage ();
        enterUsername (user);
        enterPassword (pass);

        // sometimes login is stuck, give it a retry
        Timeout timer = new Timeout (millisRetry * 4, waitRetry * 5);
        while (!timer.Timedout () && getCurrentUrl ().equals (url)) {
            clickSignIn ();
            assertTrue (hasPageLoaded ());
            timer.Wait ();
        }
    }

    public String getLoginError () {
        return getText (".form-signin .alert-danger");
    }
}
