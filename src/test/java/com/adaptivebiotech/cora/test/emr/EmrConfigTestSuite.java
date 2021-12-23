package com.adaptivebiotech.cora.test.emr;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.emr.EmrTransforms;
import com.adaptivebiotech.cora.dto.emr.EmrTransforms.Condition;
import com.adaptivebiotech.cora.dto.emr.EmrTransforms.EmrMapping;
import com.adaptivebiotech.cora.dto.emr.EmrTransforms.EmrPatient;
import com.adaptivebiotech.cora.dto.emr.EmrTransforms.IcdCode;
import com.adaptivebiotech.cora.dto.emr.EmrTransforms.IcdCodes;
import com.adaptivebiotech.cora.test.CoraDbTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.CreateEmrConfig;
import com.adaptivebiotech.cora.ui.debug.EmrConfigDetails;
import com.adaptivebiotech.cora.ui.debug.EmrConfigs;
import com.adaptivebiotech.cora.ui.order.OrdersList;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 *
 */
@Test (groups = "regression")
public class EmrConfigTestSuite extends CoraDbTestBase {

    private Login            login             = new Login ();
    private OrdersList       ordersList        = new OrdersList ();
    private EmrConfigs       emrConfigs        = new EmrConfigs ();
    private CreateEmrConfig  createEmrConfig   = new CreateEmrConfig ();
    private EmrConfigDetails emrConfigDetails  = new EmrConfigDetails ();

    private final String     accountAddedMsg   = "Accounts successfully added.";
    private final String     accountDeletedMsg = "Accounts successfully deleted.";
    private final String     newEmrSavedMsg    = "New EMR Config successfully saved.";
    private final String     emrUpdatedMsg     = "EMR Config successfully updated.";
    private final String     emrEditedMsg      = "EMR Config Edited Test Suite will be cloned";
    private final String     fixErrorsMsg      = "Please fix the highlighted errors.";
    private final String     notValidJsonMsg   = "Not valid JSON format.";

    private final String     emrConfigId       = randomUUID ().toString ();
    private final String     createEmrQuery    = format ("select * from cora.emr_configs where id = '%s'", emrConfigId);
    private final String     emrAccountsQuery  = "select * from cora.accounts where id in (select account_id from cora.emr_config_account_xref where emr_config_id = '%s') ORDER BY name DESC";
    private final String     emrAuditQuery     = "select schema_name, table_name, action, action_tstamp_clk, app_user_name, audit.jsonb_minus(row_curr, row_prev), row_curr from audit.logged_actions where app_user_name = '%s' order by action_tstamp_clk desc limit 20";

    /**
     * Note: SR-T3684
     * 
     * @sdlc.requirements SR-6757:R1, R2, R3, R4
     */
    public void verifyEmrConfigEditPage () {
        login.doLogin ();
        ordersList.isCorrectPage ();

        emrConfigs.gotoEmrConfigPage ();
        emrConfigs.isCorrectPage ();

        assertTrue (emrConfigs.isCreateEmrConfigPresent ());
        testLog ("STEP 2.1 - EMR Config page contains - Create EMR Config button.");

        assertEquals (emrConfigs.getTableHeaders (),
                      asList ("EMR Config ID", "EMR Type", "Display Name", "ISS", "Trust Email", "Properties"));
        testLog ("STEP 2.2 - Table with the following columns: EMR Config ID, EMR Type, Display Name, ISS, Trust Email, Properties");

        emrConfigs.clickCreateEmrConfig ();
        createEmrConfig.isCorrectPage ();

        assertFalse (createEmrConfig.isSaveEnabled ());
        testLog ("Create new EMR Config page displays and contains the following elements:");
        testLog ("STEP 3.1 - disabled Save button");

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
        testLog ("the following text fields with no default values:");
        testLog ("STEP 3.2 - EMR Config Id, EMR Type, Display Name, Client Id, ISS, Client Secret, 2 Users Emails fields: Key, Email, 2 Properties fields: Key, Value");

        assertFalse (createEmrConfig.isTrustEmailChecked ());
        testLog ("STEP 3.3 - Trust Email checkbox, default unchecked");
        assertEquals (createEmrConfig.getVersion (), "0");
        testLog ("STEP 3.4 - Version field with default value 0");
        assertEquals (createEmrConfig.getTransforms (), "{}");
        testLog ("STEP 3.5 - Transforms field with default value {}");

        createEmrConfig.enterEmrConfigId (emrConfigId);
        createEmrConfig.clickSave ();
        assertEquals (createEmrConfig.getOverlayMessage (), fixErrorsMsg);

        Map <String, String> fieldErrors = createEmrConfig.getFieldErrors ();
        String mandatoryFieldError = "Mandatory field.";
        assertEquals (fieldErrors.get ("EMR Type"), mandatoryFieldError);
        assertEquals (fieldErrors.get ("Display Name"), mandatoryFieldError);
        assertTrue (fieldErrors.get ("Client Id (UUID)").contains (mandatoryFieldError));
        assertEquals (fieldErrors.get ("ISS"), mandatoryFieldError);
        assertEquals (fieldErrors.get ("Client Secret"), mandatoryFieldError);
        testLog ("STEP 4 - Alert messages display on required fields: EMR Type, Display Name, Client Id, ISS, Client Secret");

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
        assertEquals (createEmrConfig.getOverlayMessage (), newEmrSavedMsg);
        testLog ("STEP 5.1 - Message displays indicating EMR config was saved");
        assertTrue (emrConfigDetails.isCloneVisible ());
        testLog ("STEP 5.2 - Clone button displays");

        List <Map <String, Object>> queryResults = coraDBClient.executeSelectQuery (createEmrQuery);
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
        testLog ("STEP 6 - Result is 1 row that contains the above values");

        String user1 = "12345678", email1 = "email1@email.email";
        String user2 = "87654321", email2 = "email2@email.email";
        String propKey1 = "test1", propValue1 = "value1";
        String propKey2 = "test2", propValue2 = "value2";
        String[] accounts = new String[] { "WhatacompanyX", "Test Account 1 x" };
        EmrTransforms transforms = genTransforms ();
        emrConfigDetails.checkTrustEmail ();
        emrConfigDetails.enterUserEmail (0, user1, email1);
        emrConfigDetails.clickAddUser ();
        emrConfigDetails.enterUserEmail (1, user2, email2);
        emrConfigDetails.enterProperty (0, propKey1, propValue1);
        emrConfigDetails.clickAddProperty ();
        emrConfigDetails.enterProperty (1, propKey2, propValue2);
        emrConfigDetails.clearTransforms ();
        emrConfigDetails.enterTransforms (mapper.writeValueAsString (transforms));
        emrConfigDetails.selectAccounts (accounts);
        emrConfigDetails.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessages (2), asList (accountAddedMsg, emrUpdatedMsg));
        testLog ("STEP 7 - Messages display indicating EMR config was updated and accounts were added.");

        Map <String, String> jsonObjUserEmail = new HashMap <> ();
        jsonObjUserEmail.put (user1, email1);
        jsonObjUserEmail.put (user2, email2);
        Map <String, String> jsonObjProperty = new HashMap <> ();
        jsonObjProperty.put (propKey1, propValue1);
        jsonObjProperty.put (propKey2, propValue2);

        queryResults = coraDBClient.executeSelectQuery (createEmrQuery);
        assertTrue (queryResults.size () == 1);
        queryEmrData = queryResults.get (0);
        assertEquals (queryEmrData.get ("emr_type").toString (), emrType);
        assertEquals (queryEmrData.get ("display_name").toString (), displayName);
        assertEquals (queryEmrData.get ("iss").toString (), iss);
        assertEquals (queryEmrData.get ("client_id").toString (), clientId);
        assertEquals (queryEmrData.get ("client_secret").toString (), clientSecret);
        assertTrue (Boolean.valueOf (queryEmrData.get ("trust_email").toString ()));
        assertEquals (toJsonString (queryEmrData.get ("users")), mapper.writeValueAsString (jsonObjUserEmail));
        assertEquals (queryEmrData.get ("version").toString (), "0");
        assertEquals (toJsonString (queryEmrData.get ("properties")), mapper.writeValueAsString (jsonObjProperty));
        assertEquals (toEmrTransforms (toJsonString (queryEmrData.get ("transforms"))), transforms);
        testLog ("STEP 8 - Result is 1 row that contains the above values");

        queryResults = coraDBClient.executeSelectQuery (String.format (emrAccountsQuery,
                                                                       emrConfigId));
        assertTrue (queryResults.size () == 2);
        testLog ("STEP 9.1 - Two rows are returned");
        assertEquals (queryResults.get (0).get ("name").toString (), accounts[0]);
        assertEquals (queryResults.get (1).get ("name").toString (), accounts[1]);
        testLog ("STEP 9.2 - Account names are WhatacompanyX and Test Account 1 X");

        String updateDisplayName = "Edited Test Suite";
        transforms.icdCodes = null;
        emrConfigDetails.gotoEmrConfigDetailsPage (emrConfigId);
        emrConfigDetails.clearDisplayName ();
        emrConfigDetails.enterDisplayName (updateDisplayName);
        emrConfigDetails.clickDeleteUser (1);
        emrConfigDetails.clickDeleteProperty (1);
        emrConfigDetails.clearTransforms ();
        emrConfigDetails.enterTransforms (mapper.writeValueAsString (transforms));
        emrConfigDetails.deleteAttachedAccounts (accounts[0]);
        emrConfigDetails.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessages (2), asList (accountDeletedMsg, emrUpdatedMsg));
        testLog ("STEP 10 - Messages display indicating EMR config was updated and account was deleted.");

        jsonObjUserEmail.remove (user2);
        jsonObjProperty.remove (propKey2);

        queryResults = coraDBClient.executeSelectQuery (createEmrQuery);
        assertTrue (queryResults.size () == 1);
        queryEmrData = queryResults.get (0);
        assertEquals (queryEmrData.get ("emr_type").toString (), emrType);
        assertEquals (queryEmrData.get ("display_name").toString (), updateDisplayName);
        assertEquals (queryEmrData.get ("iss").toString (), iss);
        assertEquals (queryEmrData.get ("client_id").toString (), clientId);
        assertEquals (queryEmrData.get ("client_secret").toString (), clientSecret);
        assertTrue (Boolean.valueOf (queryEmrData.get ("trust_email").toString ()));
        assertEquals (toJsonString (queryEmrData.get ("users")), mapper.writeValueAsString (jsonObjUserEmail));
        assertEquals (queryEmrData.get ("version").toString (), "0");
        assertEquals (toJsonString (queryEmrData.get ("properties")), mapper.writeValueAsString (jsonObjProperty));
        assertEquals (toEmrTransforms (toJsonString (queryEmrData.get ("transforms"))), transforms);
        testLog ("STEP 11 - Result is 1 row that contains the above values");

        queryResults = coraDBClient.executeSelectQuery (String.format (emrAccountsQuery,
                                                                       emrConfigId));
        assertTrue (queryResults.size () == 1);
        testLog ("STEP 12.1 - One row is returned");
        assertEquals (queryResults.get (0).get ("name").toString (), accounts[1]);
        testLog ("STEP 12.2 - Account name is Test Account 1 X");

        emrConfigDetails.gotoEmrConfigDetailsPage (emrConfigId);
        emrConfigDetails.clearDisplayName ();
        emrConfigDetails.clearTransforms ();
        emrConfigDetails.enterTransforms ("{ \"patientName\": [ { \"type\": \"currentPeriod\" }");
        emrConfigDetails.clickShowTransformsAsJson ();
        assertEquals (emrConfigDetails.getOverlayMessage (), notValidJsonMsg);
        testLog ("STEP 13 - Error message displays indicating JSON format is not valid.");

        emrConfigDetails.clickSave ();
        testLog ("STEP 14 - EMR config is not saved.");

        emrConfigDetails.clickClone ();
        assertEquals (emrConfigDetails.getOverlayMessage (), emrEditedMsg);
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
        assertEquals (toEmrTransforms (createEmrConfig.getTransforms ()), transforms);
        assertTrue (createEmrConfig.getNewAccounts ().size () == 1);
        assertEquals (createEmrConfig.getNewAccounts ().get (0), accounts[1]);
        testLog ("STEP 15 - validate cloned fields");

        createEmrConfig.enterEmrConfigId (emrConfigId);
        String clonedDisplayName = "Cloned Config";
        createEmrConfig.enterDisplayName (clonedDisplayName);
        createEmrConfig.clickSave ();
        createEmrConfig.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessage (), fixErrorsMsg);
        testLog ("STEP 16 - Error message displays indicating JSON format is not valid.");

        String clonedEmrConfigId = randomUUID ().toString ();
        createEmrConfig.clearEmrConfigId ();
        createEmrConfig.enterEmrConfigId (clonedEmrConfigId);
        createEmrConfig.clickSave ();
        assertEquals (emrConfigDetails.getOverlayMessages (2), asList (accountAddedMsg, newEmrSavedMsg));
        testLog ("STEP 17 - Messages display indicating EMR config was saved and account was added.");

        emrConfigDetails.clickCancel ();
        assertEquals (emrConfigs.getEmrConfig (emrConfigId).displayName, "Edited Test Suite");
        assertEquals (emrConfigs.getEmrConfig (clonedEmrConfigId).displayName, "Cloned Config");
        testLog ("STEP 18 - EMR configs 'Edited Test Site' and 'Cloned Config' display in the table.");

        queryResults = coraDBClient.executeSelectQuery (format (emrAuditQuery, coraTestUser));
        Set <String> auditTables = new HashSet <> ();
        for (Map <String, Object> map : queryResults) {
            auditTables.add (map.get ("table_name").toString ());
        }
        Set <String> expectedTables = new HashSet <> (asList ("emr_configs", "emr_config_account_xref"));
        assertTrue (auditTables.containsAll (expectedTables));
        testLog ("STEP 19 - validate audit table entry");
    }

    private String toJsonString (Object data) {
        try {
            return new JSONObject ( ((PGobject) data).getValue ()).toString ();
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    private EmrTransforms genTransforms () {
        IcdCode codingMap = new IcdCode ();
        codingMap.type = "codingMap";
        codingMap.systems = asList ("http://hl7.org/fhir/sid/icd-9-cm", "http://hl7.org/fhir/sid/icd-9-cm/diagnosis");
        codingMap.mappings = asList (new EmrMapping ("20300", "C90.00"),
                                     new EmrMapping ("20301", "C90.01"),
                                     new EmrMapping ("20302", "C90.02"));
        codingMap.toSystem = "http://hl7.org/fhir/sid/icd-10-cm";

        IcdCode codingFilter = new IcdCode ();
        codingFilter.type = "codingFilter";
        codingFilter.includeCodes = asList ("C90.00", "C90.01", "C90.02");
        codingFilter.includeSystems = asList ("http://hl7.org/fhir/sid/icd-10-cm", "urn:oid:2.16.840.1.113883.6.90");

        EmrTransforms transforms = new EmrTransforms ();
        transforms.icdCodes = new IcdCodes ();
        transforms.icdCodes.icd10 = asList (codingMap, codingFilter);
        transforms.icdCodes.condition = asList (new Condition ("verificationStatus", "refuted,entered-in-error"));
        transforms.patientName = asList (new EmrPatient ("currentPeriod"),
                                         new EmrPatient ("old,anonymous,maiden,temp", "useExclude"),
                                         new EmrPatient ("usual,official,nickname", "useSort"));
        return transforms;
    }

    private EmrTransforms toEmrTransforms (String data) {
        return mapper.readValue (data, EmrTransforms.class);
    }
}
