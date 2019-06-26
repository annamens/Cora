package com.adaptivebiotech.test.scenariobuilder;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.PageHelper.formatDt2;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.OrderCategory.Diagnostic;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BCells;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.TestSkus.RUOID;
import static com.adaptivebiotech.test.utils.PageHelper.TestSkus.RUOMRD;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static com.adaptivebiotech.test.utils.TestHelper.setDate;
import static com.adaptivebiotech.utils.TestHelper.freezerDestroyed;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static com.seleniumfy.test.utils.HttpClientHelper.put;
import static java.util.Arrays.asList;
import static java.util.Calendar.DATE;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;
import static java.util.Collections.addAll;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.fail;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import com.adaptivebiotech.common.dto.Orders.Order;
import com.adaptivebiotech.common.dto.Orders.OrderProperties;
import com.adaptivebiotech.common.dto.Orders.OrderTest;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.common.dto.Physician;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Diagnostic.Account;
import com.adaptivebiotech.cora.dto.Diagnostic.Task;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.MoveContainer;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.dto.Research.TechTransfer;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.Sample;
import com.adaptivebiotech.cora.dto.SpecimenResponse;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.test.cora.CoraBaseBrowser;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.seleniumfy.test.utils.Timeout;

public class ScenarioBuilderTestBase extends CoraBaseBrowser {

    private final Container freezerDestroyed = freezerDestroyed ();

    private int[] dateToIntArr (Calendar target) {
        return new int[] {
                target.get (YEAR), target.get (MONTH) + 1, target.get (DATE), target.get (HOUR), target.get (MINUTE),
                target.get (SECOND), target.get (MILLISECOND)
        };
    }

    private Diagnostic buildDiagnosticOrder (Patient patient) {
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.account = new Account ("4a8d76af-2273-4d7f-8853-ba80467b570f");
        diagnostic.patient = patient;
        diagnostic.provider = new Physician ("4b700554-4999-4585-8708-fd87161b3319");

        Specimen specimen = new Specimen ();
        specimen.sampleType = Blood;
        specimen.collectionDate = dateToIntArr (setDate (-10));
        specimen.reconciliationDate = dateToIntArr (setDate (0));
        specimen.properties = new Specimen.SpecimenProperties (formatDt2.format (setDate (12).getTime ()));
        diagnostic.specimen = specimen;

        Shipment shipment = new Shipment ();
        shipment.category = Diagnostic;
        shipment.status = "IntakeComplete";
        shipment.arrivalDate = dateToIntArr (setDate (0));
        shipment.carrier = "UPS";
        shipment.condition = Ambient;
        shipment.expectedRecordType = "Order";

        Container container = new Container ();
        container.containerType = Tube;
        container.contentsLocked = true;
        container.integrity = "Pass";
        shipment.containers.add (container);
        diagnostic.shipment = shipment;
        return diagnostic;
    }

    protected Diagnostic buildDiagnosticOrder (Patient patient, Order order, Stage stage) {
        Diagnostic diagnostic = buildDiagnosticOrder (patient);
        diagnostic.order = order;
        diagnostic.fastForwardStatus = stage;
        return diagnostic;
    }

    protected Order order (OrderProperties properties, OrderTest... test) {
        Order order = new Order ();
        order.name = "Selenium Test Order";
        order.properties = properties;
        addAll (order.tests, test);
        return order;
    }

    protected Stage stage (StageName name, StageStatus status) {
        Stage stage = new Stage ();
        stage.stageName = name;
        stage.stageStatus = status;
        stage.actor = "selenium test";
        return stage;
    }

    private Task task () {
        Task task = new Task ();
        task.name = "Clonoseq Report Scenario Test Helper";
        task.description = "Moves ClonoSeq workflows through the correct stages for tests.";
        task.status = Active;
        task.stageName = "ClonoSeq2_WorkflowNanny";
        task.stageStatus = "Ready";
        task.configId = "04d48793-c0c7-4b76-ab2d-53e2ef65891e";
        task.configName = "ReportScenarioTestHelper";
        return task;
    }

    protected Research buildResearchOrder () {
        SimpleDateFormat formatDt = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
        String uniqueId = randomWords (1) + "-test-" + randomUUID ();
        TechTransfer techTransfer = new TechTransfer ();
        techTransfer.workspace = "Adaptive-Testing";
        techTransfer.flowcellId = "XTESTSCENARIOX";

        Sample id = new Sample ();
        id.name = "1-" + uniqueId;
        id.externalId = id.name;
        id.test = RUOID;
        id.tsvPath = "s3n://cora-data-test/orca/96e0e401-a1d7-47dc-b917-ba3e3187cb5e/HWMMCBGX2_0_Genentech-Wilderbeek_6600579760-04.adap.txt.results.tsv";

        Sample mrd = new Sample ();
        mrd.name = "2-" + uniqueId;
        mrd.externalId = mrd.name;
        mrd.test = RUOMRD;
        mrd.tsvPath = "s3n://cora-data-test/orca/96e0e401-a1d7-47dc-b917-ba3e3187cb5e/JandJ_highSharing.tsv";

        Specimen specimen = new Specimen ();
        specimen.name = uniqueId;
        specimen.externalSubjectId = specimen.name;
        specimen.sampleType = Blood;
        specimen.sampleSource = BCells;
        specimen.collectionDate = formatDt.format (setDate (-10).getTime ());
        specimen.samples = asList (id, mrd);
        techTransfer.specimens = asList (specimen);

        Research research = new Research ();
        research.techTransfer = techTransfer;
        return research;
    }

    protected Specimen getSpecimen (String specimenId) {
        SpecimenResponse response = getSpecimenLoop (specimenId);
        Specimen specimen = new Specimen ();
        specimen.id = response.id;
        specimen.key = response.key;
        specimen.name = response.name;
        specimen.specimenNumber = response.specimenNumber;
        return specimen;
    }

    private SpecimenResponse getSpecimenLoop (String specimenId) {
        try {
            String url = coraTestUrl + "/cora/api/v1/specimens/" + specimenId;
            SpecimenResponse response = mapper.readValue (get (url), SpecimenResponse.class);
            Timeout timer = new Timeout (millisRetry, waitRetry);
            do {
                timer.Wait ();
                response = mapper.readValue (get (url), SpecimenResponse.class);
            } while (!timer.Timedout () && response.name == null);
            if (response.name == null)
                fail ("response.name is null");
            return response;
        } catch (Exception e) {
            error (e.getMessage (), e);
            throw new RuntimeException (e);
        }
    }

    protected HttpResponse newResearchOrder (Research research) {
        try {
            String url = coraTestUrl + "/cora/api/v1/test/scenarios/researchTechTransfer";
            return mapper.readValue (post (url, body (mapper.writeValueAsString (research)), headers),
                                     HttpResponse.class);
        } catch (Exception e) {
            error (e.getMessage (), e);
            throw new RuntimeException (e);
        }
    }

    // protected Project getTestProject () {
    // try {
    // String url = coraTestUrl + "/cora/api/v1/projects/search?name=Adaptive-Testing";
    // Project project = mapper.readValue (get (url), ProjectResponse.class).objects.get (0);
    // project.accountId = project.account != null ? project.account.id : null;
    // return project;
    // } catch (Exception e) {
    // error (e.getMessage (), e);
    // throw new RuntimeException (e);
    // }
    // }

    private Shipment shipment (ContainerType type, ContainerType childType, int num) {
        Shipment shipment = new Shipment ();
        // shipment.arrivalDate = setDate (1);
        // shipment.category = "Ambient";
        shipment.condition = Ambient;
        shipment.status = "Arrived";

        Container container = new Container ();
        container.containerType = type;
        container.contentsLocked = true;
        container.capacity = num;
        container.children = IntStream.range (0, num).parallel ().mapToObj (i -> {
            Container child = new Container ();
            child.integrity = "Pass";
            child.containerType = childType;
            child.contentsLocked = false;
            return child;
        }).collect (toList ());

        return shipment;
    }

    private Shipment newShipment (Shipment shipment) {
        try {
            String url = coraTestUrl + "/cora/api/v1/shipments/entry";
            return mapper.readValue (post (url, body (mapper.writeValueAsString (shipment))), Shipment.class);
        } catch (Exception e) {
            error (e.getMessage (), e);
            throw new RuntimeException (e);
        }
    }

    private MoveContainer destroyContainer (Container container) {
        try {
            takeCustody (container);

            info ("destroying container[" + container.containerNumber + "]: " + container.id);
            MoveContainer move = new MoveContainer ();
            move.container = new Container ();
            String url = coraTestUrl + "/cora/api/v1/containers/" + container.id + "/storeInFreezer/" + freezerDestroyed.id;
            return mapper.readValue (put (url, body (mapper.writeValueAsString (move)), headers), MoveContainer.class);
        } catch (Exception e) {
            error (e.getMessage (), e);
            throw new RuntimeException (e);
        }
    }

    private MoveContainer takeCustody (Container container) {
        try {
            info ("take custody[" + container.containerNumber + "]: " + container.id);
            Map <String, String> body = new HashMap <> ();
            body.put ("comments", "api take custody");
            String url = coraTestUrl + "/cora/api/v1/containers/" + container.id + "/takeCustody";
            return mapper.readValue (put (url, body (mapper.writeValueAsString (body)), headers), MoveContainer.class);
        } catch (Exception e) {
            error (e.getMessage (), e);
            throw new RuntimeException (e);
        }
    }
}
