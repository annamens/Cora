package com.adaptivebiotech.cora.test.smoke;

import static com.adaptivebiotech.cora.utils.PageHelper.LinkShipment.SalesforceOrder;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.LinkType.Project;
import static com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.MrdBatchReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.TestHelper.newPatient;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.mira.Mira;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.Batch;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.ui.task.TaskList;
import com.adaptivebiotech.cora.ui.utilities.AuditTool;
import com.adaptivebiotech.cora.ui.utilities.BarcodeComparisonTool;
import com.adaptivebiotech.ui.cora.Login;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import com.adaptivebiotech.ui.cora.task.Task;
import com.adaptivebiotech.ui.cora.task.TaskDetail;
import com.adaptivebiotech.ui.cora.task.TaskStatus;

@Test (groups = "smoke")
public class SmokeTestSuite extends CoraBaseBrowser {

    private Login        login;
    private Diagnostic   diagnostic;
    private Task         task;
    private Shipment     shipment;
    private Batch        batch;
    private OrdersList   oList;
    private PatientsList pList;

    @BeforeMethod
    public void beforeMethod () {
        openBrowser (coraTestUrl);
        login = new Login ();
        diagnostic = new Diagnostic ();
        task = new Task ();
        shipment = new Shipment ();
        batch = new Batch ();
        oList = new OrdersList ();
        pList = new PatientsList ();
    }

    /**
     * Note: SR-T1968
     */
    public void login_logout () {
        login.isCorrectPage ();
        login.enterUsername ("invalid");
        login.enterPassword ("invalid");
        login.clickSignIn ();
        assertEquals (login.getLoginError (), "Invalid username/password");
        testLog ("Invalid username/password error message was displayed");

        login.doLogin (coraTestUser, coraTestPass);
        oList.isCorrectPage ();
        testLog ("Orders List page was displayed");

        oList.clickSignOut ();
        login.isCorrectPage ();
        testLog ("Sign In page was displayed");
    }

    /**
     * Note: SR-T2015
     */
    public void global_navigation () {
        login.doLogin ();
        oList.isCorrectTopNavRow1 (coraTestUser);
        oList.isCorrectTopNavRow2 ();
        testLog (join ("\n",
                       "header nav contained the elements",
                       "CORA",
                       "+ New",
                       "Orders",
                       "Order Tests",
                       "Shipments",
                       "Containers",
                       "Tasks",
                       "MIRAs",
                       "Patients",
                       "Utilities icon",
                       coraTestUser,
                       "Sign Out",
                       "'?' help icon"));

        List <String> menu = asList ("Diagnostic Shipment",
                                     "Diagnostic Order",
                                     "Batch Shipment",
                                     "Batch Order",
                                     "General Shipment",
                                     "Container",
                                     "MIRA",
                                     "Task");
        oList.clickNew ();
        assertEquals (oList.getNewPopupMenu (), menu);
        oList.clickNew ();
        testLog ("dropdown menu displayed the following options:");
        testLog (join ("\n", menu));

        oList.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        testLog ("new diagnostic shipment page was displayed");

        shipment.selectNewDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        testLog ("new diagnostic order page was displayed");

        diagnostic.selectNewBatchShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.clickSave ();
        assertTrue (shipment.getHeaderShipmentNum ().matches ("Batch Shipment # SH-\\d{5,}"));
        testLog ("shipment page was displayed with 'Batch Shipment' in the header");

        shipment.selectNewBatchOrder ();
        batch.isCorrectPage ();
        testLog ("new batch order page was displayed");

        batch.selectNewGeneralShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.clickSave ();
        assertTrue (shipment.getHeaderShipmentNum ().matches ("General Shipment # SH-\\d{5,}"));
        testLog ("shipment page was displayed with 'General Shipment' in the header");

        shipment.selectNewContainer ();
        AddContainer addContainer = new AddContainer ();
        addContainer.isCorrectPage ();
        testLog ("add new container page was displayed");

        addContainer.selectNewMira ();
        Mira mira = new Mira ();
        mira.isCorrectPage ();
        testLog ("add new MIRA page was displayed");

        mira.selectNewTask ();
        task.isCorrectPage ();
        testLog ("add new task page was displayed");

        task.clickOrders ();
        oList.isCorrectPage ();
        testLog ("Orders List page was displayed");
        assertTrue (oList.isHeaderNavHighlighted ("Orders"));
        testLog ("'Orders' header nav element was highlighted");

        oList.clickOrderTests ();
        OrderTestsList oTestList = new OrderTestsList ();
        oTestList.isCorrectPage ();
        testLog ("Order Test List page was displayed");
        assertTrue (oTestList.isHeaderNavHighlighted ("Order Tests"));
        testLog ("'Order Tests' header nav element was highlighted");

        oTestList.clickShipments ();
        ShipmentList sList = new ShipmentList ();
        sList.isCorrectPage ();
        testLog ("Shipment List page was displayed");
        assertTrue (sList.isHeaderNavHighlighted ("Shipments"));
        testLog ("'Shipments' header nav element was highlighted");

        sList.clickContainers ();
        ContainerList cList = new ContainerList ();
        cList.isCorrectPage ();
        testLog ("Container List page was displayed");
        assertTrue (cList.isHeaderNavHighlighted ("Containers"));
        testLog ("'Containers' header nav element was highlighted");

        cList.clickTasks ();
        TaskList tList = new TaskList ();
        tList.isCorrectPage ();
        testLog ("Task List page was displayed");
        assertTrue (tList.isHeaderNavHighlighted ("Tasks"));
        testLog ("'Tasks' header nav element was highlighted");

        tList.clickMiras ();
        MirasList mList = new MirasList ();
        mList.isCorrectPage ();
        testLog ("MIRAs List page was displayed");
        assertTrue (mList.isHeaderNavHighlighted ("MIRAs"));
        testLog ("'MIRAs' header nav element was highlighted");

        mList.clickPatients ();
        pList.isCorrectPage ();
        testLog ("Patients List (PHI) page was displayed");
        assertTrue (pList.isHeaderNavHighlighted ("Patients"));
        testLog ("'Patients' header nav element was highlighted");

        List <String> utilities = asList ("Audit Tool", "Barcode Comparison Tool");
        pList.clickUtilities ();
        assertEquals (pList.getUtilitiesMenu (), utilities);
        pList.clickUtilities ();
        testLog ("Utilities dropdown menu was displayed and contained these options:");
        testLog (join ("\n", utilities));

        pList.selectAuditTool ();
        AuditTool auditTool = new AuditTool ();
        auditTool.isCorrectPage ();
        testLog ("Audit Tool page was displayed");

        auditTool.selectBarcodeComparisonTool ();
        BarcodeComparisonTool barcodeTool = new BarcodeComparisonTool ();
        barcodeTool.isCorrectPage ();
        testLog ("Barcode Comparison Tool page was displayed");

        barcodeTool.clickCora ();
        oList.isCorrectPage ();
        testLog ("Orders List page was displayed");
        assertTrue (oList.isHeaderNavHighlighted ("Orders"));
        testLog ("'Orders' header nav element was highlighted");

        String support = "corasupport@adaptivebiotech.com";
        assertEquals (oList.getMailTo (), support);
        testLog ("'?' help icon was a 'mailto' link to " + support);
    }

    /**
     * Note: SR-T1960
     */
    public void new_diagnostic_order () {
        login.doLogin ();
        oList.isCorrectPage ();
        oList.selectNewDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        testLog ("new diagnostic order page was displayed");

        diagnostic.clickPickPhysician ();
        diagnostic.enterPhysicianLastname ("UVT-Physician");
        diagnostic.clickSearch ();
        assertEquals (diagnostic.getPhysicianResults ().stream ()
                                .filter (p -> p.firstName.equals ("Matt"))
                                .filter (p -> p.lastName.equals ("UVT-Physician")).count (),
                      1);
        testLog ("search results in the modal included Matt UVT-Physician");

        diagnostic.selectPhysician (1);
        diagnostic.clickSelectPhysician ();
        testLog ("modal closed");
        assertEquals (diagnostic.getProviderName (), "Matt UVT-Physician");
        testLog ("Matt UVT-Physician displayed in the Diagnostic Order page's Ordering Physician section");

        diagnostic.clickSave ();
        String ordernum = diagnostic.getOrderNum ();
        assertTrue (ordernum.matches ("D-\\d{6}"), ordernum);
        testLog (format ("Diagnostic Order page displayed an order number, %s, in the order header", ordernum));

        Patient patient = newPatient ();
        diagnostic.createNewPatient (patient);
        diagnostic.clickSave ();
        assertEquals (diagnostic.getPatientName (), patient.fullname);
        testLog ("Patient Information section displayed " + patient.fullname);

        diagnostic.clickPatientCode (Pending);
        PatientDetail patientDetail = new PatientDetail ();
        patientDetail.isCorrectPage ();
        assertEquals (patientDetail.getFirstName (), patient.firstName);
        assertEquals (patientDetail.getLastName (), patient.lastName);
        testLog (format ("Patient Details page for '%s' was displayed", patient.fullname));
    }

    /**
     * Note: SR-T2443
     */
    public void new_batch_order_shipment () {
        String sforder = "00017277";
        login.doLogin ();
        oList.isCorrectPage ();
        oList.selectNewBatchShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.linkShipmentTo (SalesforceOrder, sforder, 1);
        shipment.selectBatchSpecimenContainerType (Tube);
        shipment.clickSave ();
        testLog (format ("batch shipment saved successfully"));

        String sh = shipment.getShipmentNum ();
        testLog (format ("%s displayed", sh));

        oList.selectNewBatchOrder ();
        batch.isCorrectPage ();
        batch.searchOrder (sforder);
    }

    /**
     * Note: SR-T2449
     */
    public void new_task () {
        String taskname = "Selenium - Mrd Batch Report - " + System.nanoTime ();

        login.doLogin ();
        oList.isCorrectPage ();
        oList.selectNewTask ();
        task.isCorrectPage ();
        task.selectTask ("Mrd Batch Report");
        task.enterTaskName (taskname);
        task.linkTaskTo (Project, "SeanRulez");
        task.clickRun ();

        TaskDetail detail = new TaskDetail ();
        detail.isCorrectPage ();
        testLog ("task details page was displayed");
        assertEquals (detail.getHeaderTaskName (), taskname);
        testLog ("task details page header displayed: " + taskname);

        detail.clickTaskStatus ();
        TaskStatus status = new TaskStatus ();
        status.isCorrectPage ();
        status.waitFor (MrdBatchReport, Ready);
        testLog ("task status was " + Ready);
    }
}
