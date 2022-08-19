/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.api;

import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.CDx;
import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import com.adaptivebiotech.cora.dto.AccountsResponse;
import com.adaptivebiotech.cora.dto.AlertType;
import com.adaptivebiotech.cora.dto.Alerts;
import com.adaptivebiotech.cora.dto.Alerts.Alert;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.adaptivebiotech.cora.dto.FeatureFlags;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Physician.PhysicianType;
import com.adaptivebiotech.cora.dto.ProvidersResponse;
import com.adaptivebiotech.cora.dto.Reminders;
import com.adaptivebiotech.cora.dto.Reminders.Reminder;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.dto.emr.Config;
import com.adaptivebiotech.cora.dto.emr.TokenData;
import com.adaptivebiotech.cora.utils.PageHelper.OrderType;
import com.adaptivebiotech.test.utils.HttpClientHelper;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class CoraApi extends HttpClientHelper {

    private final long  millisDuration = 3000000l;                                      // 50mins
    private final long  millisPoll     = 5000l;                                         // 5sec
    public final Header username       = new BasicHeader (X_API_USERNAME, coraTestUser);
    public final Header coraBasicAuth  = basicAuth (coraTestUser, coraTestPass);
    public Header       apiToken;

    public void resetheaders () {
        super.resetheaders ();
        headers.get ().add (username);
    }

    public String getAuthToken () {
        return getAuthToken (coraBasicAuth);
    }

    public String getAuthToken (String user, String pass) {
        return getAuthToken (basicAuth (user, pass));
    }

    public String getAuthToken (Header basicAuth) {
        resetheaders ();
        headers.get ().add (basicAuth);
        String token = get (coraTestUrl + "/cora/api/v1/auth/apiToken");
        headers.get ().remove (basicAuth);
        apiToken = new BasicHeader (X_API_TOKEN, token);
        return token;
    }

    public void addTokenAndUsername () {
        if (!headers.get ().contains (apiToken))
            headers.get ().add (apiToken);
        if (!headers.get ().contains (username))
            headers.get ().add (username);
    }

    public Containers addContainers (ContainerType type, String barcode, Container root, int num) {
        return addContainers (new Containers (IntStream.range (0, num).mapToObj (i -> {
            Container container = new Container ();
            container.containerType = type;
            container.contentsLocked = false;
            container.usesBarcodeAsId = false;
            container.barcode = barcode;
            container.root = root;
            return container;
        }).collect (toList ())));
    }

    public Containers addContainers (Containers containers) {
        String url = coraTestUrl + "/cora/api/v1/containers/addEntries";
        String result = post (url, body (containers.list));
        return new Containers (
                mapper.readValue (result, HttpResponse.class).containers.parallelStream ().map (c -> {
                    c.location = coraTestUser;
                    return c;
                }).collect (toList ()));
    }

    public Containers deactivateContainers (Containers containers) {
        containers.list.parallelStream ().forEach (c -> c.isActive = false);
        String url = coraTestUrl + "/cora/api/v1/containers/updateEntries";
        String result = put (url, body (containers.list));
        return new Containers (mapper.readValue (result, HttpResponse.class).containers);
    }

    public void removeWorkflowHold (OrderTest orderTest, WorkflowProperty property, String message) {
        String url = coraTestUrl + "/cora/api/v1/orderTests/removeWorkflowHold/" + orderTest.workflowId;
        Map <String, String> payload = new HashMap <> ();
        payload.put ("holdAtStageName", property.name ());
        payload.put ("subStatusMessage", message);
        post (url, body (payload));
    }

    public Stage[] getWorkflowStages (OrderTest orderTest) {
        String url = coraTestUrl + "/cora/api/v1/reports/clinical/reportHistory/%s/workflow/%s";
        return mapper.readValue (get (format (url, orderTest.id, orderTest.workflowId)), Stage[].class);
    }

    public AssayResponse getTests (OrderType type) {
        String id = null;
        switch (type) {
        case CDx:
            id = "63780203-caeb-483d-930c-8392afb5d927";
            break;
        case TDx:
            id = "f0ac48ed-7527-4e1b-9a45-afb4c58e680d";
            break;
        }
        String url = coraTestUrl + "/cora/api/v1/tests?categoryId=" + id;
        AssayResponse response = mapper.readValue (get (url), AssayResponse.class);
        response.objects.forEach (t -> t.testId = t.id);
        return response;
    }

    public CoraTest getCDxTest (Assay assay) {
        return getTests (CDx).get (assay);
    }

    public CoraTest getTDxTest (Assay assay) {
        return getTests (TDx).get (assay);
    }

    public Account getAccounts (String name) {
        String url = encodeUrl (coraTestUrl + "/cora/api/v1/accounts?", "name=" + name);
        AccountsResponse response = mapper.readValue (get (url), AccountsResponse.class);
        return response.objects.size () == 0 ? null : response.objects.get (0);
    }

    /**
     * Note:
     * - GET /cora/api/v1/providers is returning a like search instead of an exact match
     * 
     * @param first
     *            Physician first name
     * @param last
     *            Physician last name
     * @param account
     *            Physician's account name
     * @return a list of {@link Physician}
     */
    public List <Physician> getProviders (String first, String last, String account) {
        String[] args = { "firstName=" + first, "lastName=" + last, "accountName=" + account };
        String url = encodeUrl (coraTestUrl + "/cora/api/v1/providers?", args);
        List <Physician> physicians = mapper.readValue (get (url), ProvidersResponse.class).objects.stream ()
                                                                                                   .filter (p -> p.firstName.equals (first))
                                                                                                   .filter (p -> p.lastName.equals (last))
                                                                                                   .filter (p -> p.account.name.equals (account))
                                                                                                   .collect (toList ());
        physicians.forEach (p -> {
            p.accountName = p.account.name;
            p.providerFullName = format ("%s %s", p.firstName, p.lastName);
        });
        return physicians;
    }

    public List <Physician> getProvidersByAccount (String account) {
        String[] args = { "accountName=" + account };
        String url = encodeUrl (coraTestUrl + "/cora/api/v1/providers?", args);
        return mapper.readValue (get (url), ProvidersResponse.class).objects;
    }

    public String updateProvider (Physician physician) {
        return put (coraTestUrl + "/cora/api/v1/providers", body (physician));
    }

    public Physician getPhysician (PhysicianType type) {
        List <Physician> physicians = getProviders (type.firstName, type.lastName, type.accountName);
        if (physicians.size () > 1)
            fail ("Salesforce and Orca is out-of-sync");
        return physicians.get (0);
    }

    public Specimen getSpecimenById (String specimenId) {
        String url = coraTestUrl + "/cora/api/v1/specimens/" + specimenId;
        return mapper.readValue (get (url), Specimen.class);
    }

    public Specimen getSpecimenByNumber (String specimenNumber) {
        String url = coraTestUrl + "/cora/api/v1/specimens/specimenNumber/" + specimenNumber;
        return mapper.readValue (get (url), Specimen.class);
    }

    public OrderTest[] getOrderTest (UUID orderId) {
        String url = coraTestUrl + "/cora/api/v1/orderTests/order/" + orderId;
        return mapper.readValue (get (url), OrderTest[].class);
    }

    public Integer getPatientOrSubjectCode (UUID orderTestId) {
        String url = coraTestUrl + "/cora/api/v1/orderTests/patientOrSubjectCode/" + orderTestId;
        return mapper.readValue (get (url), Integer.class);
    }

    public Order[] searchOrders (String term) {
        return searchOrders (asList ("search=" + term));
    }

    public Order[] searchOrders (List <String> terms) {
        List <String> args = new ArrayList <> ();
        args.add ("sort=Created");
        args.add ("ascending=false");
        args.add ("limit=500");

        if (terms != null)
            args.addAll (terms);

        String url = encodeUrl (coraTestUrl + "/cora/api/v1/orders/search?", args.toArray (new String[] {}));
        return mapper.readValue (get (url), Order[].class);
    }

    public OrderTest[] searchOrderTests (Object term) {
        return searchOrderTests (asList ("search=" + term));
    }

    public OrderTest[] searchOrderTests (List <String> terms) {
        List <String> args = new ArrayList <> ();
        args.add ("sort=DueDate");
        args.add ("limit=500");

        if (terms != null)
            args.addAll (terms);

        String url = encodeUrl (coraTestUrl + "/cora/api/v1/orderTests/search?", args.toArray (new String[] {}));
        return mapper.readValue (get (url), OrderTest[].class);
    }

    public OrderTest[] waitForOrderReady (UUID orderId) {
        OrderTest[] tests = getOrderTest (orderId);
        Timeout timer = new Timeout (millisDuration, millisPoll);
        while (!timer.Timedout () && (tests.length == 0 || stream (tests).anyMatch (ot -> ot.sampleName == null))) {
            timer.Wait ();
            tests = getOrderTest (orderId);
        }
        if (tests.length == 0)
            fail ("unable to create order");
        if (stream (tests).anyMatch (ot -> ot.sampleName == null))
            fail ("sampleName is null");

        for (OrderTest test : tests) {
            test.orderId = orderId;
            if (test.specimen.subjectCode == null)
                test.specimen.subjectCode = getPatientOrSubjectCode (test.id);
        }
        return tests;
    }

    public OrderTest[] waitForResearchOrderReady (String sampleName) {
        OrderTest[] tests = searchOrderTests (sampleName);
        Timeout timer = new Timeout (millisDuration, millisPoll);
        while (!timer.Timedout () && (tests.length == 0 || stream (tests).anyMatch (ot -> ot.workflowName == null))) {
            timer.Wait ();
            tests = searchOrderTests (sampleName);
        }
        if (tests.length == 0)
            fail ("unable to create order");
        if (stream (tests).anyMatch (ot -> ot.workflowName == null))
            fail ("workflowName is null");

        for (OrderTest test : tests) {
            test.specimen = getSpecimenByNumber (test.specimenNumber);
            test.test = new CoraTest ();
            test.test.name = test.testName;
            if (test.specimen.subjectCode == null)
                test.specimen.subjectCode = getPatientOrSubjectCode (test.id);
        }

        return tests;
    }

    public void waitForNewPatientToPopulate (String patientCode) {
        Patient[] patients = getPatients (patientCode);
        Timeout timer = new Timeout (600000l, 60000l);
        while (!timer.Timedout () && (patients.length == 0)) {
            timer.Wait ();
            patients = getPatients (patientCode);
        }
        if (patients.length == 0)
            fail ("unable to find patient");
    }

    public HttpResponse newBcellOrder (Diagnostic diagnostic) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticClarity";
        HttpResponse response = mapper.readValue (post (url, body (diagnostic)), HttpResponse.class);
        diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
        return response;
    }

    public HttpResponse newTcellOrder (Diagnostic diagnostic) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/createPortalJob";
        HttpResponse response = mapper.readValue (post (url, body (diagnostic)), HttpResponse.class);
        diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
        return response;
    }

    public HttpResponse newTdetectOrder (Diagnostic diagnostic) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticDx";
        HttpResponse response = mapper.readValue (post (url, body (diagnostic)), HttpResponse.class);
        diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
        return response;
    }

    public HttpResponse newResearchOrder (Research research) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/researchTechTransfer";
        return mapper.readValue (post (url, body (research)), HttpResponse.class);
    }

    public Patient[] getPatients (String searchKeyword) {
        String[] args = { "search=" + searchKeyword, "sort=Patient Code", "ascending=false" };
        return mapper.readValue (get (encodeUrl (coraTestUrl + "/cora/api/v2/patients?", args)), Patient[].class);
    }

    public Patient getPatient (Patient patient) {
        String url = coraTestUrl + "/cora/api/v2/patients/patientCode/" + searchOrders (patient.lastName)[0].patient_code;
        return mapper.readValue (get (url), Patient.class);
    }

    public Patient updatePatient (Patient patient) {
        String url = coraTestUrl + "/cora/api/v2/patients/" + patient.id;
        return mapper.readValue (put (url, body (patient)), Patient.class);
    }

    public Order[] getOrdersForPatient (UUID patientId) {
        String url = coraTestUrl + "/cora/api/v2/patients/list/" + patientId + "/orders";
        return mapper.readValue (get (url), Order[].class);
    }

    public void setAlerts (Alert alert) {
        post (coraTestUrl + "/cora/api/v2/alerts/create", body (alert));
    }

    public Alert updateAlert (Alert alert) {
        String url = coraTestUrl + "/cora/api/v2/alerts/update";
        return mapper.readValue (put (url, body (alert)), Alert.class);
    }

    public FeatureFlags getFeatureFlags () {
        String url = coraTestUrl + "/cora/api/v2/featureFlags/flagsMap";
        return mapper.readValue (get (url), FeatureFlags.class);
    }

    public AlertType getAlertType (String name) {
        return mapper.readValue (get (coraTestUrl + "/cora/api/v2/alertTypes/" + name), AlertType.class);
    }

    public Config getEmrConfig (TokenData tokenData) {
        String response = get (coraTestUrl + "/cora/api/v1/external/getEmrConfig/" + tokenData.configId);
        return mapper.readValue (response, Config.class);
    }

    public Alerts getAlertsSummary (String userName) {
        String url = coraTestUrl + "/cora/api/v1/external/alerts/summary";
        Map <String, String> params = new HashMap <> ();
        params.put ("username", userName);
        return mapper.readValue (post (url, body (params)), Alerts.class);
    }

    public Alert[] getAlertsForOrderId (UUID orderId) {
        String url = coraTestUrl + "/cora/api/v2/alerts/searchUnpaged";
        Map <String, List <UUID>> params = new HashMap <> ();
        params.put ("referencedEntityIds", Arrays.asList (orderId));
        return mapper.readValue (post (url, body (params)), Alert[].class);
    }

    public void dismissAlert (String userName, UUID alertId) {
        String url = coraTestUrl + "/cora/api/v1/external/alerts/" + alertId + "/dismiss";
        Map <String, String> params = new HashMap <> ();
        params.put ("username", userName);
        post (url, body (params));
    }

    public void dismissAlertsForUserName (String userName) {
        Alerts userAlerts = getAlertsSummary (userName);

        for (Alert alert : userAlerts.orderAlerts) {
            dismissAlert (userName, alert.id);
        }
    }

    public void dismissAlertsForOrder (String userName, String orderNo) {
        Alerts userAlerts = getAlertsSummary (userName);

        for (Alert alert : userAlerts.orderAlerts) {
            if (alert.order.orderNumber.equals (orderNo)) {
                dismissAlert (userName, alert.id);
            }
        }
    }

    public Order[] getOrderAttachments (Object orderIdOrNo) {
        String url = coraTestUrl + "/cora/api/v1/attachments/orders/" + orderIdOrNo;
        return mapper.readValue (get (url), Order[].class);
    }

    public Reminders getActiveRemindersSummary (String userName) {
        String url = coraTestUrl + "/cora/api/v1/external/reminders/active";
        Map <String, String> params = new HashMap <> ();
        params.put ("username", userName);
        return mapper.readValue (post (url, body (params)), Reminders.class);
    }

    public void deleteReminder (UUID reminderId, String userName) {
        String url = coraTestUrl + "/cora/api/v1/external/reminders/" + reminderId + "/dismiss";
        Map <String, String> params = new HashMap <> ();
        params.put ("username", userName);
        post (url, body (params));
    }

    public void deleteRemindersForUserName (String userName) {
        Reminders activeReminders = getActiveRemindersSummary (userName);

        for (Reminder rem : activeReminders.reminders) {
            deleteReminder (rem.id, userName);
        }
    }
}
