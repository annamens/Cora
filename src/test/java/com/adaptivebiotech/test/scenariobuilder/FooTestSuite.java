package com.adaptivebiotech.test.scenariobuilder;

import static com.adaptivebiotech.utils.PageHelper.Assay.Clonality_BCell_2;
import static com.adaptivebiotech.utils.PageHelper.ChargeType.Client;
import static com.adaptivebiotech.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.utils.TestHelper.newPatient;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.adaptivebiotech.dto.Diagnostic;
import com.adaptivebiotech.dto.HttpResponse;
import com.adaptivebiotech.dto.Orders.Order;
import com.adaptivebiotech.dto.Orders.OrderProperties;
import com.adaptivebiotech.dto.Orders.OrderTest;
import com.adaptivebiotech.dto.Patient;
import com.adaptivebiotech.ui.cora.order.OrderList;
import com.adaptivebiotech.ui.cora.workflow.History;

@Test (groups = { "cora" })
public class FooTestSuite extends ScenarioBuilderTestBase {

    private Patient    patient;
    private Diagnostic diagnostic;

    @BeforeTest
    public void beforeTest () {
        patient = newPatient ();
        patient.dateOfBirth = "1999-01-01";
        patient.billingType = Client;

        OrderTest test = new OrderTest ();
        test.testId = getTests ().getTest (Clonality_BCell_2).id;
        test.tsvPath = "s3://adaptive-fda-test-case-archive/tsv/above-loq.id.tsv.gz";

        diagnostic = buildDiagnosticOrder (patient, physician1);
        diagnostic.order = order (new OrderProperties (patient.billingType, CustomerShipment), test);
        diagnostic.fastForwardStatus = stage (SecondaryAnalysis, Ready);

        HttpResponse response = newDiagnosticOrder (diagnostic);
        diagnostic.specimen = getSpecimen (response.specimenId);
        patient.id = response.patientId;
    }

    public void doFoo () {
        History history = new History ();
        history.gotoOrderDebug (diagnostic.specimen.name);
        assertTrue (history.waitForReport ());

        OrderList list = new OrderList ();
        list.doOrderTestSearch (patient.id);
//        list.doOrderTestSearch ("f590622f-ffb1-4c9e-8c53-529cf5e879f1");

        Order order = list.getOrderTests ().list.get (0);
        list.clickOrder (order.name);

        com.adaptivebiotech.ui.cora.order.Diagnostic d = new com.adaptivebiotech.ui.cora.order.Diagnostic ();
        d.isActiveOrderStatusPage ();
        d.clickReportTab (order.tests.get (0).assay);
        d.releaseReport ();
    }
}
