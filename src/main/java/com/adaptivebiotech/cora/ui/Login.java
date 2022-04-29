/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
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

    private final String loginUrl = coraTestUrl + "/cora/login";

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

    public void clickSignOut () {
        assertTrue (click ("//*[text()='sign out']"));
        assertTrue (waitUntilVisible (".cora-mascot"));
    }

    public void doLogin () {
        // sometimes login is stuck, give it a retry
        Timeout timer = new Timeout (millisDuration * 4, millisPoll * 5);
        while (!timer.Timedout ()) {
            doLogin (coraTestUser, coraTestPass);
            if (getCurrentUrl ().equals (loginUrl)) {
                timer.Wait ();
                refresh ();
            } else
                break;
        }
    }

    public void doLogin (String user, String pass) {
        info ("logging-in to Cora");
        openBrowser (loginUrl);
        isCorrectPage ();
        enterUsername (user);
        enterPassword (pass);
        clickSignIn ();
        assertTrue (hasPageLoaded ());
    }

    public String getLoginError () {
        return getText (".form-signin .alert-danger");
    }
}
