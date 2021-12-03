package com.adaptivebiotech.cora.api;

import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.CDx;
import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.encodeUrl;
import static com.seleniumfy.test.utils.HttpClientHelper.formPost;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static com.seleniumfy.test.utils.HttpClientHelper.put;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.fail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import com.adaptivebiotech.cora.dto.AccountsResponse;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.Orders.Alert;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Physician.PhysicianType;
import com.adaptivebiotech.cora.dto.ProvidersResponse;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.utils.PageHelper.OrderType;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.seleniumfy.test.utils.HttpClientHelper;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class CoraApi {

    private final long  millisRetry = 3000000l;                                        // 50mins
    private final long  waitRetry   = 5000l;                                           // 5sec
    public final Header username    = new BasicHeader ("X-Api-UserName", coraTestUser);

    public void login () {
        login (coraTestUser, coraTestPass);
        resetheaders ();
    }

    public void login (String user, String pass) {
        Map <String, String> forms = new HashMap <> ();
        forms.put ("userName", user);
        forms.put ("password", pass);
        formPost (coraTestUrl + "/cora/login", forms);
    }

    public void resetheaders () {
        HttpClientHelper.resetheaders ();
        HttpClientHelper.headers.get ().add (username);
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
        String result = post (url, body (mapper.writeValueAsString (containers.list)));
        return new Containers (
                mapper.readValue (result, HttpResponse.class).containers.parallelStream ().map (c -> {
                    c.location = coraTestUser;
                    return c;
                }).collect (toList ()));
    }

    public Containers deactivateContainers (Containers containers) {
        containers.list.parallelStream ().forEach (c -> c.isActive = false);
        String url = coraTestUrl + "/cora/api/v1/containers/updateEntries";
        String result = put (url, body (mapper.writeValueAsString (containers.list)));
        return new Containers (mapper.readValue (result, HttpResponse.class).containers);
    }

    public void storeContainer (Containers containers, Container freezer) {
        String url = coraTestUrl + "/cora/debug/storeContainer";
        containers.list.forEach (c -> {
            Map <String, String> props = new HashMap <> ();
            props.put ("containerId", c.id);
            props.put ("rootContainerId", freezer.id);
            post (url, body (mapper.writeValueAsString (props)));
        });
    }

    public void setWorkflowProperties (OrderTest orderTest, Map <WorkflowProperty, String> properties) {
        String url = coraTestUrl + "/cora/debug/forceWorkflowProperty";
        properties.entrySet ().forEach (wp -> {
            Map <String, String> props = new HashMap <> ();
            props.put ("propertyName", wp.getKey ().name ());
            props.put ("propertyValue", wp.getValue ());
            props.put ("workflowId", orderTest.workflowId);
            post (url, body (mapper.writeValueAsString (props)));
        });
    }

    public void resetAccountLogin (String email) {
        Map <String, String> params = new HashMap <> ();
        params.put ("emails", email);
        post (coraTestUrl + "/cora/debug/resetAccountLoginSubmit", body (mapper.writeValueAsString (params)));
    }

    public void removeWorkflowHold (OrderTest orderTest, WorkflowProperty property, String message) {
        String url = coraTestUrl + "/cora/api/v1/orderTests/removeWorkflowHold/" + orderTest.workflowId;
        Map <String, String> payload = new HashMap <> ();
        payload.put ("holdAtStageName", property.name ());
        payload.put ("subStatusMessage", message);
        post (url, body (mapper.writeValueAsString (payload)));
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
        return put (coraTestUrl + "/cora/api/v1/providers", body (mapper.writeValueAsString (physician)));
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

    public Specimen getSpecimenByMunber (String specimenNumber) {
        String url = coraTestUrl + "/cora/api/v1/specimens/specimenNumber/" + specimenNumber;
        return mapper.readValue (get (url), Specimen.class);
    }

    public OrderTest[] getOrderTest (String orderId) {
        String url = coraTestUrl + "/cora/api/v1/orderTests/order/" + orderId;
        return mapper.readValue (get (url), OrderTest[].class);
    }

    public Integer getPatientOrSubjectCode (String orderTestId) {
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

        for (String term : terms)
            args.add (term);

        String url = encodeUrl (coraTestUrl + "/cora/api/v1/orders/search?", args.toArray (new String[] {}));
        return mapper.readValue (get (url), Order[].class);
    }

    public OrderTest[] searchOrderTests (String term) {
        return searchOrderTests (asList ("search=" + term));
    }

    public OrderTest[] searchOrderTests (List <String> terms) {
        List <String> args = new ArrayList <> ();
        args.add ("sort=DueDate");
        args.add ("limit=500");

        for (String term : terms)
            args.add (term);

        String url = encodeUrl (coraTestUrl + "/cora/api/v1/orderTests/search?", args.toArray (new String[] {}));
        return mapper.readValue (get (url), OrderTest[].class);
    }

    public OrderTest[] waitForOrderReady (String orderId) {
        OrderTest[] tests = getOrderTest (orderId);
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout () && (tests.length == 0 || stream (tests).anyMatch (ot -> ot.sampleName == null))) {
            timer.Wait ();
            tests = getOrderTest (orderId);
        }
        if (tests.length == 0)
            fail ("unable to create order");
        if (stream (tests).anyMatch (ot -> ot.sampleName == null))
            fail ("sampleName is null");

        for (OrderTest test : tests)
            if (test.specimen.subjectCode == null)
                test.specimen.subjectCode = getPatientOrSubjectCode (test.id);

        return tests;
    }

    public HttpResponse newDiagnosticOrder (Diagnostic diagnostic) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticClarity";
        HttpResponse response = mapper.readValue (post (url, body (mapper.writeValueAsString (diagnostic))),
                                                  HttpResponse.class);
        diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
        return response;
    }

    public HttpResponse createPortalJob (Diagnostic diagnostic) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/createPortalJob";
        HttpResponse response = mapper.readValue (post (url, body (mapper.writeValueAsString (diagnostic))),
                                                  HttpResponse.class);
        diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
        return response;
    }

    public HttpResponse newCovidOrder (Diagnostic diagnostic) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticDx";
        HttpResponse response = mapper.readValue (post (url, body (mapper.writeValueAsString (diagnostic))),
                                                  HttpResponse.class);
        diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
        return response;
    }

    public HttpResponse newResearchOrder (Research research) {
        String url = coraTestUrl + "/cora/api/v1/test/scenarios/researchTechTransfer";
        return mapper.readValue (post (url, body (mapper.writeValueAsString (research))), HttpResponse.class);
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
        return mapper.readValue (put (url, body (mapper.writeValueAsString (patient))), Patient.class);
    }

    public void setAlerts (Alert alert) {
        post (coraTestUrl + "/cora/api/v2/alerts/create", body (mapper.writeValueAsString (alert)));
    }
}
