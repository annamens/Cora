package com.adaptivebiotech.cora.ui.workflow;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.test.utils.Logging;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
public class CreateEmrConfig extends CoraPage {

    private final String save                   = "//button[contains(text(),'Save')]";
    private final String cancel                 = "//button[text()='Cancel']";

    private final String configId               = "[name='id']";
    private final String emrType                = "[name='emrType']";
    private final String displayName            = "[name='displayName']";
    private final String clientId               = "[name='clientId']";
    private final String trustEmail             = "[name='trustEmail']";
    private final String version                = "[name='version']";
    private final String iss                    = "[name='iss']";
    private final String clientSecret           = "[name='clientSecret']";

    private final String userKey                = "//*[text()='Users Emails']/..//*[@ng-reflect-name='%s']//*[@placeholder='Key']";
    private final String userEmail              = "[ng-reflect-name='%s'] [placeholder='Email']";
    private final String deleteUser             = "//*[@ng-reflect-name='%s']//button[text()='Delete User']";
    private final String addUser                = "//button[contains(text(),'Add User')]";
    private final String userEmailRows          = "[formarrayname='usersFormDataArr'] [placeholder='Key']";

    private final String propertyKey            = "//*[text()='Properties']/..//*[@ng-reflect-name='%s']//*[@placeholder='Key']";
    private final String propertyValue          = "[ng-reflect-name='%s'] [placeholder='Value']";
    private final String deleteProperty         = "//*[@ng-reflect-name='%s']//button[text()='Delete Property']";
    private final String addProperty            = "//button[contains(text(),'Add Property')]";
    private final String propertyRows           = "[formarrayname='propertiesFormDataArr'] [placeholder='Key']";

    private final String transforms             = "[name='transforms']";
    private final String showTransformsAsJson   = "//button[text()='Show Transforms as JSON']";

    private final String selectAccountsBtn      = "//*[text()='Select Accounts']";
    private final String selectAccountsDropDown = ".dropdown-menu.multi-select-popup";
    private final String accountSearch          = "[placeholder='Search...']";
    private final String accountSearchResults   = "a.dropdown-item";
    private final String newAccounts            = "//*[text()='New Accounts']/..//span[not(contains(@class, 'glyphicon'))]";

    private final String overlayMessage         = "#toast-container .toast-message";
    private final String fieldError             = ".alert.alert-danger";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (popupTitle, "Create new EMR Config"));
    }

    public void gotoEmrConfigPage () {
        assertTrue (navigateTo (coraTestUrl + "/cora/debug/new-emr-config"));
        isCorrectPage ();
    }

    public void clickSave () {
        assertTrue (click (save));
    }

    public boolean isSaveEnabled () {
        return waitForElement (save).isEnabled ();
    }

    public void clickCancel () {
        assertTrue (click (cancel));
    }

    public void enterEmrConfigId (String id) {
        assertTrue (setText (configId, id));
    }

    public void clearEmrConfigId () {
        assertTrue (clear (configId));
    }

    public String getEmrConfigId () {
        return getAttribute (configId, "value");
    }

    public void enterEmrType (String emrTypeStr) {
        assertTrue (setText (emrType, emrTypeStr));
    }

    public String getEmrType () {
        return getAttribute (emrType, "value");
    }

    public void enterDisplayName (String displayNameStr) {
        assertTrue (setText (displayName, displayNameStr));
    }

    public void clearDisplayName () {
        assertTrue (clear (displayName));
    }

    public String getDisplayName () {
        return getAttribute (displayName, "value");
    }

    public void enterClientId (String id) {
        assertTrue (setText (clientId, id));
    }

    public String getClientId () {
        return getAttribute (clientId, "value");
    }

    public void checkTrustEmail () {
        assertTrue (click (trustEmail));
    }

    public boolean isTrustEmailChecked () {
        return waitForElementVisible (trustEmail).isSelected ();
    }

    public void enterVersion (String versionStr) {
        assertTrue (setText (version, versionStr));
    }

    public void clearVersion () {
        assertTrue (clear (version));
    }

    public String getVersion () {
        return getAttribute (version, "value");
    }

    public void enterIss (String issStr) {
        assertTrue (setText (iss, issStr));
    }

    public String getIss () {
        return getAttribute (iss, "value");
    }

    public void enterClientSecret (String clientSecretStr) {
        assertTrue (setText (clientSecret, clientSecretStr));
    }

    public String getClientSecret () {
        return getAttribute (clientSecret, "value");
    }

    public void enterUserEmail (int index, String key, String email) {
        assertTrue (setText (String.format (userKey, index), key));
        assertTrue (setText (String.format (userEmail, index), email));
    }

    public List <Map <String, String>> getUserEmails () {
        List <Map <String, String>> userEmails = new ArrayList <> ();
        List <WebElement> userEmailElements = waitForElements (userEmailRows);
        for (int i = 0; i < userEmailElements.size (); i++) {
            Map <String, String> map = new HashMap <> ();
            map.put ("key", getAttribute (String.format (userKey, i), "value"));
            map.put ("email", getAttribute (String.format (userEmail, i), "value"));
            userEmails.add (map);
        }
        return userEmails;
    }

    public void clickDeleteUser (int index) {
        assertTrue (click (String.format (deleteUser, index)));
    }

    public void clickAddUser () {
        assertTrue (click (addUser));
    }

    public void enterProperty (int index, String key, String value) {
        assertTrue (setText (String.format (propertyKey, index), key));
        assertTrue (setText (String.format (propertyValue, index), value));
    }

    public List <Map <String, String>> getProperties () {
        List <Map <String, String>> properties = new ArrayList <> ();
        List <WebElement> propertyElements = waitForElements (propertyRows);
        for (int i = 0; i < propertyElements.size (); i++) {
            Map <String, String> map = new HashMap <> ();
            map.put ("key", getAttribute (String.format (propertyKey, i), "value"));
            map.put ("value", getAttribute (String.format (propertyValue, i), "value"));
            properties.add (map);
        }
        return properties;
    }

    public void clickDeleteProperty (int index) {
        assertTrue (click (String.format (deleteProperty, index)));
    }

    public void clickAddProperty () {
        assertTrue (click (addProperty));
    }

    public void enterTransforms (String transformsStr) {
        assertTrue (setText (transforms, transformsStr));
    }

    public void clearTransforms () {
        assertTrue (clear (transforms));
    }

    public String getTransforms () {
        return getAttribute (transforms, "value");
    }

    public void clickShowTransformsAsJson () {
        assertTrue (click (showTransformsAsJson));
    }

    public void selectAccounts (String... accounts) {
        assertTrue (click (selectAccountsBtn));
        assertEquals (getCssProperty (selectAccountsDropDown, "display"), "block");

        String previousFirstResult = null;
        for (int i = 0; i < accounts.length; i++) {
            assertTrue (clear (accountSearch));
            assertTrue (setText (accountSearch, accounts[i]));

            if (i != 0) {
                for (int j = 0; j < 10; j++) {
                    doWait (1000);
                    String currentFirstResult = getText (waitForElements (accountSearchResults).get (0), "label");
                    Logging.info ("waiting for dropdown results to change from previousFirstResult to currentFirstResult");
                    Logging.info ("previousFirstResult: " + previousFirstResult + ", currentFirstResult: " + currentFirstResult);
                    if (!currentFirstResult.equals (previousFirstResult)) {
                        break;
                    }
                    if (j == 0) {
                        throw new RuntimeException (
                                "previousFirstResult and currentFirstResult are the same after 10 attempts");
                    }
                }
            }

            for (WebElement element : waitForElements (accountSearchResults)) {
                previousFirstResult = getText (waitForElements (accountSearchResults).get (0), "label");
                if (getText (element, "label").trim ().equals (accounts[i])) {
                    click (element, "input");
                    break;
                }
            }
        }
        assertTrue (click (selectAccountsBtn));
        assertEquals (getCssProperty (selectAccountsDropDown, "display"), "none");
    }

    public List <String> getNewAccounts () {
        return getTextList (newAccounts);
    }

    public void deleteNewAccounts (String... accounts) {
        for (WebElement element : waitForElements (newAccounts)) {
            if (Arrays.asList (accounts).contains (getText (element).trim ())) {
                element.findElement (By.xpath ("./..//button")).click ();
            }
        }
    }

    public String getOverlayMessage () {
        String overlayMsg = getText (overlayMessage);
        waitForElementInvisible (overlayMessage);
        return overlayMsg;
    }

    public Map <String, String> getFieldErrors () {
        Map <String, String> fieldErrors = new HashMap <> ();
        waitForElements (fieldError).forEach (error -> {
            fieldErrors.put (error.findElement (By.xpath ("./../label")).getText (), getText (error));
        });
        return fieldErrors;
    }

}
