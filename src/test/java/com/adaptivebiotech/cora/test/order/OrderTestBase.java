package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.diagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.getAccounts;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.getCDxTest;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.getPhysicians;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.getTDxTest;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.order;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.shipment;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.specimen;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.POSITIVE;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static java.util.Arrays.asList;
import java.util.ArrayList;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Workflow;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;
import com.adaptivebiotech.test.utils.PageHelper;

public class OrderTestBase extends CoraBaseBrowser {

    private final String testAccount = "SEA_QA Test";

    protected AssayResponse.CoraTest genTDxTest (PageHelper.Assay assay) {
        AssayResponse.CoraTest test = new AssayResponse.CoraTest ();
        test.testId = getTDxTest (assay).id;
        test.workflowProperties = defaultWorkflowProperties ();
        return test;
    }

    protected AssayResponse.CoraTest genCDxTest (PageHelper.Assay assay, String tsvPath) {
        AssayResponse.CoraTest test = new AssayResponse.CoraTest ();
        test.testId = getCDxTest (assay).id;
        test.workflowProperties = new Workflow.WorkflowProperties ();
        test.workflowProperties.disableHiFreqSave = true;
        test.workflowProperties.disableHiFreqSharing = true;
        test.workflowProperties.tsvOverridePath = tsvPath;
        return test;
    }

    protected AssayResponse.CoraTest genTcrTest (PageHelper.Assay assay, String flowcell, String tsvPath) {
        AssayResponse.CoraTest test = new AssayResponse.CoraTest ();
        test.testId = getCDxTest (assay).id;
        test.flowcell = flowcell;
        test.pipelineConfigOverride = "classic.calib";
        test.tsvPath = tsvPath;
        return test;
    }

    private Workflow.WorkflowProperties defaultWorkflowProperties () {
        Workflow.WorkflowProperties wProperties = new Workflow.WorkflowProperties ();
        wProperties.flowcell = "HM7N7BGXF";
        wProperties.workspaceName = "Hospital12deOctubre-MartinezLopez";
        wProperties.sampleName = "860011348";
        return wProperties;
    }

    protected Diagnostic buildCovidOrder (Patient patient, Workflow.Stage stage, AssayResponse.CoraTest test) {
        Diagnostic.Account account = getAccounts (testAccount);
        Diagnostic diagnostic = diagnosticOrder (account, physician (), patient, null, shipment ());
        diagnostic.order = order (null, test);
        diagnostic.order.orderType = TDx;
        diagnostic.order.mrn = patient.mrn;
        diagnostic.order.billingType = patient.billingType;
        diagnostic.order.specimenDeliveryType = CustomerShipment;
        diagnostic.order.specimenDto = specimen ();
        diagnostic.order.specimenDto.name = test.workflowProperties.sampleName;
        diagnostic.order.specimenDto.properties = null;
        diagnostic.order.panels = asList (new Diagnostic.Panel ("132d9440-8f75-46b8-b084-efe06346dfd4"));
        diagnostic.fastForwardStatus = stage;
        diagnostic.task = null;
        diagnostic.dxResults = positiveDxResult ();
        return diagnostic;
    }

    protected Diagnostic buildDiagnosticOrder (Patient patient, Workflow.Stage stage, AssayResponse.CoraTest... tests) {
        Diagnostic.Account account = getAccounts (testAccount);
        Diagnostic diagnostic = diagnosticOrder (account, physician (), patient, specimen (), shipment ());
        diagnostic.order = order (new Orders.OrderProperties (patient.billingType, CustomerShipment, "C91.00"), tests);
        diagnostic.order.mrn = patient.mrn;
        diagnostic.specimen.collectionDate = new int[] { 2019, 4, 1, 18, 6, 59, 639 };
        diagnostic.specimen.reconciliationDate = new int[] { 2019, 5, 10, 18, 6, 59, 639 };
        diagnostic.specimen.properties = new Specimen.SpecimenProperties ("2019-03-20");
        diagnostic.shipment.arrivalDate = new int[] { 2019, 4, 15, 11, 11, 59, 639 };
        diagnostic.fastForwardStatus = stage;
        return diagnostic;
    }

    private ClassifierOutput positiveDxResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = "COVID19";
        dxResult.classifierVersion = "v1.0";
        dxResult.dxScore = 52.24133872081212d;
        dxResult.posteriorProbability = 1.0d;
        dxResult.countEnhancedSeq = 128;
        dxResult.containerVersion = "dx-classifiers/covid-19:d23228f";
        dxResult.pipelineVersion = "v3.1-385-g1340003";
        dxResult.dxStatus = POSITIVE;
        dxResult.configVersion = "dx.covid19.rev3";
        dxResult.uniqueProductiveTemplates = 222554;
        dxResult.qcFlags = new ArrayList <> ();
        return dxResult;
    }

    private Physician physician () {
        return getPhysicians ("Selenium", "Test1", "SEA_QA Test").stream ()
                                                                 .filter (p -> p.npi != null && p.npi.length () > 0)
                                                                 .findAny ().get ();
    }
}
