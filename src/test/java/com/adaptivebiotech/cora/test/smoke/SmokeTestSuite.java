package com.adaptivebiotech.cora.test.smoke;

import static com.adaptivebiotech.cora.utils.PageHelper.LinkShipment.SalesforceOrder;
import static com.adaptivebiotech.cora.utils.TestHelper.newPatient;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.TubeBox5x5;
import static com.adaptivebiotech.test.utils.PageHelper.LinkType.Project;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.MrdBatchReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.mira.NewMira;
import com.adaptivebiotech.cora.ui.order.Batch;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.ui.task.Task;
import com.adaptivebiotech.cora.ui.task.TaskDetail;
import com.adaptivebiotech.cora.ui.task.TaskList;
import com.adaptivebiotech.cora.ui.task.TaskStatus;
import com.adaptivebiotech.cora.ui.utilities.AuditTool;
import com.adaptivebiotech.cora.ui.utilities.BarcodeComparisonTool;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;

@Test (groups = "smoke")
public class SmokeTestSuite extends CoraBaseBrowser {

    private Login            login        = new Login ();
    private NewOrderClonoSeq diagnostic   = new NewOrderClonoSeq ();
    private Task             task         = new Task ();
    private NewShipment      shipment     = new NewShipment ();
    private Batch            batch        = new Batch ();
    private NewMira          mira         = new NewMira ();
    private AddContainer     addContainer = new AddContainer ();
    private OrdersList       oList        = new OrdersList ();
    private PatientsList     pList        = new PatientsList ();
    private MirasList        mList        = new MirasList ();
    private ContainerList    cList        = new ContainerList ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        openBrowser (coraTestUrl);
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
                                     "clonoSEQ Diagnostic Order",
                                     "T-Detect Diagnostic Order",
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

        shipment.selectNewClonoSEQDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        testLog ("new diagnostic order page was displayed");

        diagnostic.selectNewBatchShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.clickSave ();
        assertTrue (shipment.getHeaderShipmentNumber ().matches ("Batch Shipment # SH-\\d{5,}"));
        testLog ("shipment page was displayed with 'Batch Shipment' in the header");

        shipment.selectNewBatchOrder ();
        batch.isCorrectPage ();
        testLog ("new batch order page was displayed");

        batch.selectNewGeneralShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.clickSave ();
        assertTrue (shipment.getHeaderShipmentNumber ().matches ("General Shipment # SH-\\d{5,}"));
        testLog ("shipment page was displayed with 'General Shipment' in the header");

        shipment.selectNewContainer ();
        addContainer.isCorrectPage ();
        testLog ("add new container page was displayed");

        addContainer.selectNewMira ();
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
        mList.isCorrectPage ();
        testLog ("MIRAs List page was displayed");
        assertTrue (mList.isHeaderNavHighlighted ("MIRAs"));
        testLog ("'MIRAs' header nav element was highlighted");

        mList.clickPatients ();
        pList.isCorrectPage ();
        testLog ("Patients List (PHI) page was displayed");
        assertTrue (pList.isHeaderNavHighlighted ("Patients"));
        testLog ("'Patients' header nav element was highlighted");

        List <String> utilities = asList ("Alerts",
                                          "Audit Tool",
                                          "Barcode Comparison Tool",
                                          "Patient Merge Tool");
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
        oList.selectNewClonoSEQDiagnosticOrder ();
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
        String ordernum = diagnostic.getOrderNumber ();
        assertTrue (ordernum.matches ("D-\\d{6}"), ordernum);
        testLog (format ("Diagnostic Order page displayed an order number, %s, in the order header", ordernum));

        Patient patient = newPatient ();
        diagnostic.createNewPatient (patient);
        diagnostic.clickSave ();
        assertEquals (diagnostic.getPatientName (), patient.fullname);
        testLog ("Patient Information section displayed " + patient.fullname);

        diagnostic.clickPatientCode ();
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
        shipment.linkShipmentTo (SalesforceOrder, sforder);
        shipment.selectBatchSpecimenContainerType (Tube);
        shipment.clickSave ();
        testLog ("batch shipment saved successfully");

        String sh = shipment.getShipmentNumber ();
        testLog (format ("%s displayed", sh));

        oList.selectNewBatchOrder ();
        batch.isCorrectPage ();
        batch.searchOrder (sforder);
        assertEquals (batch.getShipments ().stream ().filter (s -> sh.equals (s.shipmentNumber)).count (), 1);
        testLog (format ("%s displayed", sh));
    }

    /**
     * Note: SR-T2443
     */
    public void new_general_shipment_and_mira () {
        login.doLogin ();
        oList.isCorrectPage ();
        oList.selectNewGeneralShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.selectBatchSpecimenContainerType (Tube);
        shipment.clickSave ();
        testLog ("general shipment saved successfully");

        String sh = shipment.getShipmentNumber ();
        testLog (format ("%s displayed", sh));

        shipment.selectNewMira ();
        mira.isCorrectPage ();
        mira.selectLab (MiraLab.AntigenMapProduction);
        testLog (format ("%s displayed below Lab dropdown", MiraLab.AntigenMapProduction));

        mira.clickMiras ();
        mira.ignoredUnsavedChanges ();
        mList.isCorrectPage ();
        mList.searchMira ("M-10");
        mList.getMiras ().list.forEach (m -> assertTrue (m.miraId.startsWith ("M-10"), m.miraId));
        testLog ("search results were MIRA IDs that start with 'M-10'");
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

    /**
     * Note: SR-T1879
     */
    public void container () {
        String freezer = "AB018115 (Ambient)";

        login.doLogin ();
        oList.isCorrectPage ();
        oList.selectNewContainer ();
        addContainer.isCorrectPage ();
        testLog ("add new container page was displayed");

        addContainer.pickContainerType (TubeBox5x5);
        addContainer.enterQuantity (1);
        addContainer.clickAdd ();
        Containers containers = addContainer.getContainers ();
        assertEquals (containers.list.size (), 1);
        assertEquals (containers.list.get (0).containerType, TubeBox5x5);
        assertNotNull (containers.list.get (0).containerNumber);
        testLog (TubeBox5x5.label + " displayed below the Add Container(s) section");

        addContainer.setContainerLocation (1, freezer);
        addContainer.clickSave ();
        assertFalse (addContainer.isAddContainerHeaderVisible ());
        assertFalse (addContainer.isContainerTypeVisible ());
        assertFalse (addContainer.isQuantityVisible ());
        assertFalse (addContainer.isAddBtnVisible ());
        testLog ("Add Container(s) section did not displayed");

        assertTrue (addContainer.isGenerateContainerLabelsVisible ());
        testLog ("Generate Container Labels button was displayed");

        containers = addContainer.getContainers ();
        Container test = containers.list.get (0);
        assertTrue (test.containerNumber.matches ("CO-\\d{6}"), test.containerNumber);
        testLog (TubeBox5x5.label + " table displayed a " + test.containerNumber);

        addContainer.clickContainers ();
        cList.isCorrectPage ();
        cList.searchContainerIdOrName (test.containerNumber);
        cList.setCategory (ContainerList.Category.Any);
        cList.setCurrentLocationFilter (freezer);
        cList.setContainerType (TubeBox5x5);
        cList.setGroupBy (ContainerList.GroupBy.None);
        cList.clickFilter ();
        containers = cList.getContainers ();
        assertEquals (containers.list.stream ().filter (c -> {
            return test.containerNumber.equals (c.containerNumber);
        }).count (), 1);
        testLog (test.containerNumber + " displayed in the filter results");

        int CUSTODYBEGIN = cList.getMyCustodySize ();
        cList.takeCustody (test);
        testLog (format ("message displayed indicating %s is in my custody", test.containerNumber));

        // it takes few seconds to reflect the updated Custody Size
        doWait (1000);
        int CUSTODYEND = cList.getMyCustodySize ();
        assertEquals (CUSTODYEND - CUSTODYBEGIN, 1);
        testLog ("the delta between before and after scan for My Custody size is 1");
    }

}
