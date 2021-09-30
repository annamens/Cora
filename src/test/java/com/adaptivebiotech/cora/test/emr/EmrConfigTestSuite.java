package com.adaptivebiotech.cora.test.emr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraDbTestBase;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.workflow.CreateEmrConfig;
import com.adaptivebiotech.cora.ui.workflow.EmrConfig;
import com.adaptivebiotech.cora.ui.workflow.EmrConfigDetails;
import com.adaptivebiotech.test.utils.Logging;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 *
 */
@Test (groups = "regression")
public class EmrConfigTestSuite extends CoraDbTestBase {

    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private EmrConfig        emrConfig        = new EmrConfig ();
    private CreateEmrConfig  createEmrConfig  = new CreateEmrConfig ();
    private EmrConfigDetails emrConfigDetails = new EmrConfigDetails ();

    private final String     accountAddedMsg  = "Accounts successfully added.";
    private final String     emrUpdatedMsg    = "EMR Config successfully updated.";
    private final String     fixErrorsMsg     = "Please fix the highlighted errors.";

    private final String     createEmrQuery   = "select * from cora.emr_configs where id = '%s'";
    private final String     emrAccountsQuery = "select * from cora.accounts where id in (select account_id from cora.emr_config_account_xref where emr_config_id = '%s') ORDER BY name DESC";
    private final String     emrAuditQuery    = "select schema_name, table_name, action, action_tstamp_clk, app_user_name, audit.jsonb_minus(row_curr, row_prev), row_curr from audit.logged_actions where app_user_name = '%s' order by action_tstamp_clk desc limit 20";

    /**
     * Note: SR-T3684
     * 
     * @throws JSONException
     * 
     * @sdlc.requirements SR-6757:R1, R2, R3, R4
     */
    public void verifyEmrConfigEditPage () throws JSONException {
        login.doLogin ();
        ordersList.isCorrectPage ();

        emrConfig.gotoEmrConfigPage ();
        emrConfig.isCorrectPage ();

        assertTrue (emrConfig.isCreateEmrConfigPresent ());
        Logging.testLog ("STEP 2.1 - EMR Config page contains - Create EMR Config butto");

        List <Map <String, String>> emrConfigTable = emrConfig.getEmrConfigTable ();
        assertEquals (emrConfigTable.get (0).keySet (),
                      new HashSet <> (Arrays.asList ("EMR Config ID",
                                                     "EMR Type",
                                                     "Display Name",
                                                     "ISS",
                                                     "Trust Email",
                                                     "Properties")));
        Logging.testLog ("STEP 2.2 - Table with the following columns: EMR Config ID, EMR Type, Display Name, ISS, Trust Email, Properties");

        emrConfig.clickCreateEmrConfig ();
        createEmrConfig.isCorrectPage ();

        assertFalse (createEmrConfig.isSaveEnabled ());
        Logging.testLog ("Create new EMR Config page displays and contains the following elements:");
        Logging.testLog ("STEP 3.1 - disabled Save button");

        assertTrue (createEmrConfig.getEmrConfigId ().isEmpty ());
        assertTrue (createEmrConfig.getEmrType ().isEmpty ());
        assertTrue (createEmrConfig.getDisplayName ().isEmpty ());
        assertTrue (createEmrConfig.getClientId ().isEmpty ());
        assertTrue (createEmrConfig.getIss ().isEmpty ());
        assertTrue (createEmrConfig.getClientSecret ().isEmpty ());

        List <Map <String, String>> userEmails = createEmrConfig.getUserEmails ();
        assertTrue (userEmails.size () == 1);
        assertTrue (userEmails.get (0).get ("key").isEmpty ());
        assertTrue (userEmails.get (0).get ("email").isEmpty ());

        List <Map <String, String>> properties = createEmrConfig.getProperties ();
        assertTrue (properties.size () == 1);
        assertTrue (properties.get (0).get ("key").isEmpty ());
        assertTrue (properties.get (0).get ("value").isEmpty ());
        Logging.testLog ("the following text fields with no default values:");
        Logging.testLog ("STEP 3.2 - EMR Config Id, EMR Type, Display Name, Client Id, ISS, Client Secret, 2 Users Emails fields: Key, Email, 2 Properties fields: Key, Value");

        assertFalse (createEmrConfig.isTrustEmailChecked ());
        Logging.testLog ("STEP 3.3 - Trust Email checkbox, default unchecked");
        assertEquals (createEmrConfig.getVersion (), "0");
        Logging.testLog ("STEP 3.4 - Version field with default value 0");
        assertEquals (createEmrConfig.getTransforms (), "{}");
        Logging.testLog ("STEP 3.5 - Transforms field with default value {}");

        String emrConfigId = UUID.randomUUID ().toString ();
        Logging.info ("EMR Id: " + emrConfigId);
        createEmrConfig.enterEmrConfigId (emrConfigId);
        createEmrConfig.clickSave ();
        assertEquals (createEmrConfig.getOverlayMessage (), "Please fix the highlighted errors.");

        Map <String, String> fieldErrors = createEmrConfig.getFieldErrors ();
        String mandatoryFieldError = "Mandatory field.";
        assertEquals (fieldErrors.get ("EMR Type"), mandatoryFieldError);
        assertEquals (fieldErrors.get ("Display Name"), mandatoryFieldError);
        assertTrue (fieldErrors.get ("Client Id (UUID)").contains (mandatoryFieldError));
        assertEquals (fieldErrors.get ("ISS"), mandatoryFieldError);
        assertEquals (fieldErrors.get ("Client Secret"), mandatoryFieldError);
        Logging.testLog ("STEP 4 - Alert messages display on required fields: EMR Type, Display Name, Client Id, ISS, Client Secret");

        String emrType = "EpicDSTU2";
        String displayName = "New Test Site";
        String clientId = "12341234-abcd-1234-abcd-1234abcd1234";
        String iss = "https://cora-test.dna.corp.adaptivebiotech.com";
        String clientSecret = "NO_SECRET";
        createEmrConfig.enterEmrType (emrType);
        createEmrConfig.enterDisplayName (displayName);
        createEmrConfig.enterClientId (clientId);
        createEmrConfig.enterIss (iss);
        createEmrConfig.enterClientSecret (clientSecret);
        createEmrConfig.clickSave ();
        assertEquals (createEmrConfig.getOverlayMessage (), "New EMR Config successfully saved.");
        Logging.testLog ("STEP 5.1 - Message displays indicating EMR config was saved");
        assertTrue (emrConfigDetails.isCloneVisible ());
        Logging.testLog ("STEP 5.2 - Clone button displays");

        List <Map <String, Object>> queryResults = coraDBClient.executeSelectQuery (String.format (createEmrQuery,
                                                                                                   emrConfigId));
        Logging.info ("Query Results: " + queryResults);
        assertTrue (queryResults.size () == 1);
        Map <String, Object> queryEmrData = queryResults.get (0);
        assertEquals (queryEmrData.get ("emr_type").toString (), emrType);
        assertEquals (queryEmrData.get ("display_name").toString (), displayName);
        assertEquals (queryEmrData.get ("iss").toString (), iss);
        assertEquals (queryEmrData.get ("client_id").toString (), clientId);
        assertEquals (queryEmrData.get ("client_secret").toString (), clientSecret);
        assertFalse (Boolean.valueOf (queryEmrData.get ("trust_email").toString ()));
        assertEquals (queryEmrData.get ("users").toString (), "{}");
        assertEquals (queryEmrData.get ("version").toString (), "0");
        assertEquals (queryEmrData.get ("properties").toString (), "{}");
        assertEquals (queryEmrData.get ("transforms").toString (), "{}");
        Logging.testLog ("STEP 6 - Result is 1 row that contains the above values");

        String user1 = "12345678", email1 = "email1@email.email";
        String user2 = "87654321", email2 = "email2@email.email";
        String propKey1 = "test1", propValue1 = "value1";
        String propKey2 = "test2", propValue2 = "value2";
        String transforms = "{ \"icdCodes\": { \"icd10\": [ { \"type\": \"codingMap\", \"systems\": [ \"http://hl7.org/fhir/sid/icd-9-cm\", \"http://hl7.org/fhir/sid/icd-9-cm/diagnosis\" ], \"mappings\": [ [ \"20300\", \"C90.00\" ], [ \"20301\", \"C90.01\" ], [ \"20302\", \"C90.02\" ] ], \"toSystem\": \"http://hl7.org/fhir/sid/icd-10-cm\" }, { \"type\": \"codingFilter\", \"includeCodes\": [ \"C90.00\", \"C90.01\", \"C90.02\" ], \"includeSystems\": [ \"http://hl7.org/fhir/sid/icd-10-cm\", \"urn:oid:2.16.840.1.113883.6.90\" ] } ], \"condition\": [ { \"type\": \"verificationStatus\", \"exclude\": \"refuted,entered-in-error\" } ] }, \"patientName\": [ { \"type\": \"currentPeriod\" }, { \"csv\": \"old,anonymous,maiden,temp\", \"type\": \"useExclude\" }, { \"csv\": \"usual,official,nickname\", \"type\": \"useSort\" } ] }";
        String[] accounts = new String[] { "WhatacompanyX", "Test Account 1 x" };
        emrConfigDetails.checkTrustEmail ();
        emrConfigDetails.enterUserEmail (0, user1, email1);
        emrConfigDetails.clickAddUser ();
        emrConfigDetails.enterUserEmail (1, user2, email2);
        emrConfigDetails.enterProperty (0, propKey1, propValue1);
        emrConfigDetails.clickAddProperty ();
        emrConfigDetails.enterProperty (1, propKey2, propValue2);
        emrConfigDetails.clearTransforms ();
        emrConfigDetails.enterTransforms (transforms);
        emrConfigDetails.selectAccounts (accounts);
        emrConfigDetails.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessages (),
                      Arrays.asList (accountAddedMsg, emrUpdatedMsg));
        Logging.testLog ("STEP 7 - Messages display indicating EMR config was updated and accounts were added.");

        queryResults = coraDBClient.executeSelectQuery (String.format (createEmrQuery,
                                                                       emrConfigId));
        Logging.info ("Query Results: " + queryResults);
        assertTrue (queryResults.size () == 1);
        queryEmrData = queryResults.get (0);
        assertEquals (queryEmrData.get ("emr_type").toString (), emrType);
        assertEquals (queryEmrData.get ("display_name").toString (), displayName);
        assertEquals (queryEmrData.get ("iss").toString (), iss);
        assertEquals (queryEmrData.get ("client_id").toString (), clientId);
        assertEquals (queryEmrData.get ("client_secret").toString (), clientSecret);
        assertTrue (Boolean.valueOf (queryEmrData.get ("trust_email").toString ()));
        JSONObject jsonObjUserEmail = new JSONObject ();
        JSONObject jsonObjProperty = new JSONObject ();
        try {
            jsonObjUserEmail.put (user1, email1);
            jsonObjUserEmail.put (user2, email2);
            jsonObjProperty.put (propKey1, propValue1);
            jsonObjProperty.put (propKey2, propValue2);
        } catch (JSONException e) {
            throw new RuntimeException (e);
        }
        assertEquals (convertToJsonString (queryEmrData.get ("users").toString ()),
                      jsonObjUserEmail.toString ());
        assertEquals (queryEmrData.get ("version").toString (), "0");
        assertEquals (convertToJsonString (queryEmrData.get ("properties").toString ()), jsonObjProperty.toString ());
        assertEquals (convertToJsonString (queryEmrData.get ("transforms").toString ()),
                      convertToJsonString (transforms));
        Logging.testLog ("STEP 8 - Result is 1 row that contains the above values");

        queryResults = coraDBClient.executeSelectQuery (String.format (emrAccountsQuery,
                                                                       emrConfigId));
        Logging.info ("Query Results: " + queryResults);
        assertTrue (queryResults.size () == 2);
        Logging.testLog ("STEP 9.1 - Two rows are returned");
        assertEquals (queryResults.get (0).get ("name").toString (), accounts[0]);
        assertEquals (queryResults.get (1).get ("name").toString (), accounts[1]);
        Logging.testLog ("STEP 9.2 - Account names are WhatacompanyX and Test Account 1 X");

        String updateDisplayName = "Edited Test Suite";
        String updateTransforms = "{ \"patientName\": [ { \"type\": \"currentPeriod\" }, { \"csv\": \"old,anonymous,maiden,temp\", \"type\": \"useExclude\" }, { \"csv\": \"usual,official,nickname\", \"type\": \"useSort\" } ] }";
        emrConfigDetails.gotoEmrConfigDetailsPage (emrConfigId);
        emrConfigDetails.clearDisplayName ();
        emrConfigDetails.enterDisplayName (updateDisplayName);
        emrConfigDetails.clickDeleteUser (1);
        emrConfigDetails.clickDeleteProperty (1);
        emrConfigDetails.clearTransforms ();
        emrConfigDetails.enterTransforms (updateTransforms);
        emrConfigDetails.deleteAttachedAccounts (accounts[0]);
        emrConfigDetails.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessages (),
                      Arrays.asList ("Accounts successfully deleted.", "EMR Config successfully updated."));
        Logging.testLog ("STEP 10 - Messages display indicating EMR config was updated and account was deleted.");

        queryResults = coraDBClient.executeSelectQuery (String.format (createEmrQuery,
                                                                       emrConfigId));
        Logging.info ("Query Results: " + queryResults);
        assertTrue (queryResults.size () == 1);
        queryEmrData = queryResults.get (0);
        assertEquals (queryEmrData.get ("emr_type").toString (), emrType);
        assertEquals (queryEmrData.get ("display_name").toString (), updateDisplayName);
        assertEquals (queryEmrData.get ("iss").toString (), iss);
        assertEquals (queryEmrData.get ("client_id").toString (), clientId);
        assertEquals (queryEmrData.get ("client_secret").toString (), clientSecret);
        assertTrue (Boolean.valueOf (queryEmrData.get ("trust_email").toString ()));
        JSONObject updateJsonObjUserEmail = new JSONObject ();
        JSONObject updateJsonObjProperty = new JSONObject ();
        try {
            updateJsonObjUserEmail.put (user1, email1);
            updateJsonObjProperty.put (propKey1, propValue1);
        } catch (JSONException e) {
            throw new RuntimeException (e);
        }
        assertEquals (convertToJsonString (queryEmrData.get ("users").toString ()),
                      updateJsonObjUserEmail.toString ());
        assertEquals (queryEmrData.get ("version").toString (), "0");
        assertEquals (convertToJsonString (queryEmrData.get ("properties").toString ()),
                      updateJsonObjProperty.toString ());
        assertEquals (convertToJsonString (queryEmrData.get ("transforms").toString ()),
                      convertToJsonString (updateTransforms));
        Logging.testLog ("STEP 11 - Result is 1 row that contains the above values");

        queryResults = coraDBClient.executeSelectQuery (String.format (emrAccountsQuery,
                                                                       emrConfigId));
        Logging.info ("Query Results: " + queryResults);
        assertTrue (queryResults.size () == 1);
        Logging.testLog ("STEP 12.1 - One row is returned");
        assertEquals (queryResults.get (0).get ("name").toString (), accounts[1]);
        Logging.testLog ("STEP 12.2 - Account name is Test Account 1 X");

        emrConfigDetails.gotoEmrConfigDetailsPage (emrConfigId);
        emrConfigDetails.clearDisplayName ();
        emrConfigDetails.clearTransforms ();
        emrConfigDetails.enterTransforms (updateTransforms.substring (0, updateTransforms.lastIndexOf ("}")));
        emrConfigDetails.clickShowTransformsAsJson ();
        assertEquals (emrConfigDetails.getOverlayMessage (), "Not valid JSON format.");
        Logging.testLog ("STEP 13 - Error message displays indicating JSON format is not valid.");

        emrConfigDetails.clickSave ();
        Logging.testLog ("STEP 14 - EMR config is not saved.");

        emrConfigDetails.clickClone ();
        assertEquals (emrConfigDetails.getOverlayMessage (), "EMR Config Edited Test Suite will be cloned");
        createEmrConfig.isCorrectPage ();

        assertTrue (createEmrConfig.getEmrConfigId ().isEmpty ());
        assertEquals (createEmrConfig.getEmrType (), emrType);
        assertTrue (createEmrConfig.getDisplayName ().isEmpty ());
        assertEquals (createEmrConfig.getClientId (), clientId);
        assertTrue (createEmrConfig.isTrustEmailChecked ());
        assertEquals (createEmrConfig.getVersion (), "0");
        assertEquals (createEmrConfig.getIss (), iss);
        assertEquals (createEmrConfig.getClientSecret (), clientSecret);
        List <Map <String, String>> cloneUserEmails = createEmrConfig.getUserEmails ();
        assertEquals (cloneUserEmails.get (0).get ("key"), user1);
        assertEquals (cloneUserEmails.get (0).get ("email"), email1);
        List <Map <String, String>> cloneProperties = createEmrConfig.getProperties ();
        assertEquals (cloneProperties.get (0).get ("key"), propKey1);
        assertEquals (cloneProperties.get (0).get ("value"), propValue1);
        assertEquals (convertToJsonString (createEmrConfig.getTransforms ()),
                      convertToJsonString (updateTransforms));

        assertTrue (createEmrConfig.getNewAccounts ().size () == 1);
        assertEquals (createEmrConfig.getNewAccounts ().get (0), accounts[1]);
        Logging.testLog ("STEP 15 - validate cloned fields");

        createEmrConfig.enterEmrConfigId (emrConfigId);
        String clonedDisplayName = "Cloned Config";
        createEmrConfig.enterDisplayName (clonedDisplayName);
        createEmrConfig.clickSave ();
        createEmrConfig.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessage (), fixErrorsMsg);
        Logging.testLog ("STEP 13 - Error message displays indicating JSON format is not valid.");

        String clonedEmrConfigId = UUID.randomUUID ().toString ();
        createEmrConfig.clearEmrConfigId ();
        createEmrConfig.enterEmrConfigId (clonedEmrConfigId);
        createEmrConfig.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessages (),
                      Arrays.asList (accountAddedMsg, emrUpdatedMsg));

        emrConfigDetails.clickCancel ();
        emrConfigTable = emrConfig.getEmrConfigTable ();
        boolean isEmrConfig = false, isCloneConfig = false;
        for (Map <String, String> map : emrConfigTable) {
            if (map.get ("EMR Config ID").equals (emrConfigId)) {
                isEmrConfig = true;
                assertEquals (map.get ("Display Name"), "Edited Test Suite");
            }
            if (map.get ("EMR Config ID").equals (clonedEmrConfigId)) {
                isCloneConfig = true;
                assertEquals (map.get ("Display Name"), "Cloned Config");
            }
        }
        assertTrue (isEmrConfig);
        assertTrue (isCloneConfig);
        Logging.testLog ("STEP 18 - EMR configs 'Edited Test Site' and 'Cloned Config' display in the table.");

        queryResults = coraDBClient.executeSelectQuery (String.format (emrAuditQuery,
                                                                       CoraEnvironment.coraTestUser));
        Logging.info ("Query Results: " + queryResults);

        Set <String> auditTables = new HashSet <> ();
        for (Map <String, Object> map : queryResults) {
            auditTables.add (map.get ("table_name").toString ());
        }
        Logging.info ("Audit Actions: " + auditTables);
        Set <String> expectedTables = new HashSet <> (Arrays.asList ("emr_configs",
                                                                     "emr_config_account_xref"));
        assert (auditTables.containsAll (expectedTables));

        Logging.testLog ("STEP 19 - validate audit table entry");
    }

    public String convertToJsonString (String string) {
        try {
            return new JSONObject (string).toString ();
        } catch (JSONException e) {
            throw new RuntimeException (e);
        }
    }
}
