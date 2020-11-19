package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.CDx;
import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.OrderCategory.Diagnostic;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Cancelled;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSeq2_WorkflowNanny;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.encodeUrl;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.addAll;
import static org.testng.Assert.fail;
import com.adaptivebiotech.cora.dto.AccountsResponse;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.AssayResponse.Test;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.adaptivebiotech.cora.dto.Diagnostic.Task;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.dto.Research.TechTransfer;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.SpecimenProperties;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.utils.PageHelper.OrderType;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.seleniumfy.test.utils.Timeout;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class TestScenarioBuilder {

    private static final long   millisRetry  = 3000000l;      // 50mins
    private static final long   waitRetry    = 5000l;         // 5sec
    public static AssayResponse coraCDxTests = getTests (CDx);
    public static AssayResponse coraTDxTests = getTests (TDx);

    public synchronized static Test getCDxTest (Assay assay) {
        return coraCDxTests.get (assay);
    }

    public synchronized static Test getTDxTest (Assay assay) {
        return coraTDxTests.get (assay);
    }

    public synchronized static AssayResponse getTests (OrderType type) {
        try {
            String id = null;
            switch (type) {
            case CDx:
                id = "63780203-caeb-483d-930c-8392afb5d927";
                break;
            case TDx:
                id = "f0ac48ed-7527-4e1b-9a45-afb4c58e680d";
                break;
            }
            return mapper.readValue (get (coraTestUrl + "/cora/api/v1/tests?categoryId=" + id), AssayResponse.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public synchronized static Account getAccounts (Account account) {
        try {
            String url = encodeUrl (coraTestUrl + "/cora/api/v1/accounts?", "name=" + account.name);
            AccountsResponse response = mapper.readValue (get (url), AccountsResponse.class);
            if (response.objects.size () == 0)
                return account;
            else {
                account.id = response.objects.get (0).id;
                return account;
            }
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public synchronized static OrderTest[] getOrderTest (String term) {
        try {
            String url = coraTestUrl + "/cora/api/v1/orderTests/search?search=" + term + "&sort=DueDate&limit=500";
            OrderTest[] tests = mapper.readValue (get (url), OrderTest[].class);
            Timeout timer = new Timeout (millisRetry, waitRetry);
            while (!timer.Timedout () && (tests.length == 0 || stream (tests).anyMatch (ot -> ot.workflowName == null && !Cancelled.equals (ot.status)))) {
                timer.Wait ();
                tests = mapper.readValue (get (url), OrderTest[].class);
            }
            if (tests.length == 0)
                fail ("unable to create order");
            if (stream (tests).anyMatch (ot -> ot.workflowName == null && !Cancelled.equals (ot.status)))
                fail ("workflowName is null");

            for (OrderTest test : tests) {
                url = coraTestUrl + "/cora/api/v1/specimens/specimenNumber/" + test.specimenNumber;
                test.specimen = mapper.readValue (get (url), Specimen.class);
                if (test.specimen.subjectCode == null) {
                    url = coraTestUrl + "/cora/api/v1/orderTests/patientOrSubjectCode/" + test.id;
                    test.specimen.subjectCode = mapper.readValue (get (url), Integer.class);
                }
            }

            return tests;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    private synchronized static OrderTest[] waitForOrderReady (String orderId) {
        try {
            String url = coraTestUrl + "/cora/api/v1/orderTests/order/" + orderId;
            OrderTest[] tests = mapper.readValue (get (url), OrderTest[].class);
            Timeout timer = new Timeout (millisRetry, waitRetry);
            while (!timer.Timedout () && (tests.length == 0 || stream (tests).anyMatch (ot -> ot.sampleName == null))) {
                timer.Wait ();
                tests = mapper.readValue (get (url), OrderTest[].class);
            }
            if (tests.length == 0)
                fail ("unable to create order");
            if (stream (tests).anyMatch (ot -> ot.sampleName == null))
                fail ("sampleName is null");
            for (OrderTest test : tests)
                if (test.specimen.subjectCode == null) {
                    url = coraTestUrl + "/cora/api/v1/orderTests/patientOrSubjectCode/" + test.id;
                    test.specimen.subjectCode = mapper.readValue (get (url), Integer.class);
                }

            return tests;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public synchronized static HttpResponse newDiagnosticOrder (Diagnostic diagnostic) {
        try {
            String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticClarity";
            HttpResponse response = mapper.readValue (post (url, body (mapper.writeValueAsString (diagnostic))),
                                                      HttpResponse.class);
            diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
            return response;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public synchronized static HttpResponse newCovidOrder (Diagnostic diagnostic) {
        try {
            String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticDx";
            HttpResponse response = mapper.readValue (post (url, body (mapper.writeValueAsString (diagnostic))),
                                                      HttpResponse.class);
            url = coraTestUrl + "/cora/api/v1/orderTests/order/" + response.orderId;
            diagnostic.orderTests = asList (waitForOrderReady (response.orderId));
            return response;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public synchronized static HttpResponse newResearchOrder (Research research) {
        try {
            String url = coraTestUrl + "/cora/api/v1/test/scenarios/researchTechTransfer";
            return mapper.readValue (post (url, body (mapper.writeValueAsString (research))), HttpResponse.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public synchronized static Order order (OrderProperties properties, OrderTest... tests) {
        Order order = new Order ();
        order.name = "Selenium Test Order";
        order.properties = properties;
        addAll (order.tests, tests);
        return order;
    }

    public synchronized static Stage stage (StageName name, StageStatus status) {
        Stage stage = new Stage ();
        stage.stageName = name;
        stage.stageStatus = status;
        stage.subStatusCode = "";
        stage.subStatusMessage = "";
        stage.drilldownUrl = "";
        stage.actor = "selenium test";
        return stage;
    }

    public synchronized static Specimen specimen () {
        Specimen specimen = new Specimen ();
        specimen.sampleType = Blood;
        specimen.collectionDate = new int[] { 2019, 4, 1, 18, 6, 59, 639 };
        specimen.reconciliationDate = new int[] { 2019, 5, 10, 18, 6, 59, 639 };
        specimen.properties = new SpecimenProperties ("2019-03-20");
        return specimen;
    }

    public synchronized static Shipment shipment () {
        Shipment shipment = new Shipment ();
        shipment.category = Diagnostic;
        shipment.status = "IntakeComplete";
        shipment.arrivalDate = new int[] { 2019, 4, 15, 11, 11, 59, 639 };
        shipment.carrier = "UPS";
        shipment.trackingNumber = "";
        shipment.condition = Ambient;
        shipment.expectedRecordType = "Order";

        Container container = new Container ();
        container.containerType = Tube;
        container.contentsLocked = true;
        container.integrity = "Pass";
        shipment.containers.add (container);
        return shipment;
    }

    public synchronized static Task workflowNanny () {
        Task task = new Task ();
        task.name = "Clonoseq Report Scenario Test Helper";
        task.description = "Moves ClonoSeq workflows through the correct stages for tests.";
        task.status = Active;
        task.stageName = ClonoSeq2_WorkflowNanny;
        task.stageStatus = Ready;
        task.configId = "04d48793-c0c7-4b76-ab2d-53e2ef65891e";
        task.configName = "ReportScenarioTestHelper";
        return task;
    }

    public synchronized static Diagnostic diagnosticOrder (Account account,
                                                           Physician physician,
                                                           Patient patient,
                                                           Specimen specimen,
                                                           Shipment shipment) {
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.account = account;
        diagnostic.provider = physician;
        diagnostic.patient = patient;
        diagnostic.specimen = specimen;
        diagnostic.shipment = shipment;
        diagnostic.task = workflowNanny ();
        diagnostic.waitForResults = true;
        return diagnostic;
    }

    public synchronized static Research researchOrder (Specimen... specimens) {
        TechTransfer techTransfer = new TechTransfer ();
        techTransfer.workspace = "Adaptive-Testing";
        techTransfer.flowcellId = "selenium-staging";
        techTransfer.specimens = asList (specimens);
        return new Research (techTransfer);
    }
}
