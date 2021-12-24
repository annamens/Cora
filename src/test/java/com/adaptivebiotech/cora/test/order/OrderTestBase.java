package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.dto.Orders.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.utils.PageHelper.OrderType.TDx;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.diagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.order;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.shipment;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.specimen;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.NEGATIVE;
import static java.util.Arrays.asList;
import java.util.ArrayList;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Workflow;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;

public class OrderTestBase extends CoraBaseBrowser {

    protected AssayResponse.CoraTest genCDxTest (Assay assay, String tsvPath) {
        AssayResponse.CoraTest test = coraApi.getCDxTest (assay);
        test.workflowProperties = new Workflow.WorkflowProperties ();
        test.workflowProperties.disableHiFreqSave = true;
        test.workflowProperties.disableHiFreqSharing = true;
        test.workflowProperties.notifyGateway = true;
        test.workflowProperties.tsvOverridePath = tsvPath;
        return test;
    }

    protected AssayResponse.CoraTest genTcrTest (Assay assay, String flowcell, String tsvPath) {
        AssayResponse.CoraTest test = coraApi.getCDxTest (assay);
        test.workflowProperties = new Workflow.WorkflowProperties ();
        test.workflowProperties.notifyGateway = true;
        test.flowcell = flowcell;
        test.pipelineConfigOverride = "classic.calib";
        test.tsvPath = tsvPath;
        return test;
    }

    protected Diagnostic buildCovidOrder (Patient patient, Workflow.Stage stage, AssayResponse.CoraTest test) {
        Diagnostic diagnostic = diagnosticOrder (coraApi.getPhysician (TDetect_client), patient, null, shipment ());
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
        diagnostic.dxResults = negativeDxResult ();
        return diagnostic;
    }

    protected Diagnostic buildDiagnosticOrder (Patient patient, Stage stage, CoraTest... tests) {
        Diagnostic diagnostic = diagnosticOrder (coraApi.getPhysician (clonoSEQ_client),
                                                 patient,
                                                 specimen (),
                                                 shipment ());
        diagnostic.order = order (new Orders.OrderProperties (patient.billingType, CustomerShipment, "C91.00"), tests);
        diagnostic.order.mrn = patient.mrn;
        diagnostic.specimen.collectionDate = new int[] { 2019, 4, 1, 18, 6, 59, 639 };
        diagnostic.specimen.reconciliationDate = new int[] { 2019, 5, 10, 18, 6, 59, 639 };
        diagnostic.specimen.properties = new Specimen.SpecimenProperties ("2019-03-20");
        diagnostic.shipment.arrivalDate = new int[] { 2019, 4, 15, 11, 11, 59, 639 };
        diagnostic.fastForwardStatus = stage;
        return diagnostic;
    }

    private ClassifierOutput negativeDxResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = "COVID19";
        dxResult.classifierVersion = "v1.0";
        dxResult.dxScore = -6.2313718717738125d;
        dxResult.posteriorProbability = 0.0019628913932077255d;
        dxResult.countEnhancedSeq = 15;
        dxResult.containerVersion = "dx-classifiers/covid-19:d23228f";
        dxResult.pipelineVersion = "v3.1-385-g1340003";
        dxResult.dxStatus = NEGATIVE;
        dxResult.configVersion = "dx.covid19.rev1";
        dxResult.uniqueProductiveTemplates = 251880;
        dxResult.qcFlags = new ArrayList <> ();
        return dxResult;
    }
}
