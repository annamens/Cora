package com.adaptivebiotech.test.scenariobuilder;

import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.utils.TestHelper.newScenarioBuilderPatient;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.adaptivebiotech.common.dto.Orders;
import com.adaptivebiotech.common.dto.Orders.Order;
import com.adaptivebiotech.common.dto.Orders.OrderProperties;
import com.adaptivebiotech.common.dto.Orders.OrderTest;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import com.adaptivebiotech.ui.cora.order.OrderList;
import com.adaptivebiotech.ui.cora.workflow.History;

@Test (groups = { "regression" })
public class TrackingTestSuite extends ScenarioBuilderTestBase {

    private Stage         secondaryAnalysis = stage (SecondaryAnalysis, Ready);
    private AssayResponse assays;
    private String        eos_id;
    private String        eos_mrd;

    @BeforeTest
    public void beforeTest () {
        assays = getTests ();
        eos_id = assays.get (ID_BCell2_CLIA).id;
        eos_mrd = assays.get (MRD_BCell2_CLIA).id;
    }

    public void aboveLoq () {
        OrderTest testId = new OrderTest (eos_id, "s3://cora-data-test/tsv/cellfree-above-loq/id.tsv.gz");
        OrderTest testMrd = new OrderTest (eos_mrd, "s3://cora-data-test/tsv/cellfree-above-loq/mrd.tsv.gz");

        Patient patient = newScenarioBuilderPatient ();
        Order order = order (new OrderProperties (patient.billingType, CustomerShipment), testId, testMrd);
        newDiagnosticOrder (buildDiagnosticOrder (patient, order, secondaryAnalysis));
        patient.id = getPatient (patient).id;

        System.out.println ("test=" + patient.id);

        OrderList list = new OrderList ();
        list.doOrderTestSearch (patient.id);
        Orders orders = list.getOrderTests ();

        Order test = orders.list.parallelStream ().filter (o -> o.tests.get (0).assay.equals (ID_BCell2_CLIA))
                                .findAny ().get ();

        System.out.println ("test=" + test.workflow.sampleName);

        History history = new History ();
        history.gotoOrderDebug (test.workflow.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();

        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isOrderStatusPage ();
        diagnostic.clickReportTab (test.tests.get (0).assay);
        diagnostic.releaseReport ();
    }

    private OrderTest[] eosAboveLoqMrd () {
        OrderTest testId = new OrderTest (eos_id, "s3://cora-data-test/tsv/cellfree-above-loq/id.tsv.gz");
        OrderTest testMrd = new OrderTest (eos_mrd, "s3://cora-data-test/tsv/cellfree-above-loq/mrd.tsv.gz");
        return new OrderTest[] { testId, testMrd };
    }

    private OrderTest[] eosAboveLodBelowLoqMrd () {
        OrderTest testId = new OrderTest (eos_id, "s3://cora-data-test/tsv/cellfree-below-loq/id.tsv.gz");
        OrderTest testMrd = new OrderTest (eos_mrd, "s3://cora-data-test/tsv/cellfree-below-loq/mrd.tsv.gz");
        return new OrderTest[] { testId, testMrd };
    }

    private OrderTest[] eosBelowLodMrd () {
        OrderTest testId = new OrderTest (eos_id, "s3://cora-data-test/tsv/cellfree-below-lod/id.tsv.gz");
        OrderTest testMrd = new OrderTest (eos_mrd, "s3://cora-data-test/tsv/cellfree-below-lod/mrd.tsv.gz");
        return new OrderTest[] { testId, testMrd };
    }

    private OrderTest[] eosNegativeMrd () {
        OrderTest testId = new OrderTest (eos_id, "s3://cora-data-test/tsv/cellfree-not-detected/id.tsv.gz");
        OrderTest testMrd = new OrderTest (eos_mrd, "s3://cora-data-test/tsv/cellfree-not-detected/id.tsv.gz");
        return new OrderTest[] { testId, testMrd };
    }
}
