package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018078;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.seleniumfy.test.utils.Environment.sauceOptions;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.ui.task.TaskList;
import com.adaptivebiotech.common.dto.PerformanceMetrics;

@Test (groups = "performance")
public class CoraPerformanceTestSuite extends CoraBaseBrowser {

    private Login          login;
    private CoraPage       cora;
    private OrdersList     oList;
    private OrderTestsList otList;
    private ShipmentList   shList;
    private ContainerList  cList;
    private TaskList       tList;
    private MirasList      mList;
    private PatientsList   pList;

    static {
        sauceOptions.put ("extendedDebugging", "true");
        sauceOptions.put ("capturePerformance", "true");
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login = new Login ();
        cora = new CoraPage ();
        oList = new OrdersList ();
        otList = new OrderTestsList ();
        shList = new ShipmentList ();
        cList = new ContainerList ();
        tList = new TaskList ();
        mList = new MirasList ();
        pList = new PatientsList ();
        login.doLogin ();
    }

    @AfterSuite
    public void cleanUp () {
        sauceOptions.remove ("extendedDebugging");
        sauceOptions.remove ("capturePerformance");
    }

    public void loadListOfOrders () {
        oList.isCorrectTopNavRow2 ();
        navigateTo(coraTestUrl + "/cora/orders");
        oList.isCorrectPage ();
        PerformanceMetrics listOfOrdersMetrics = obtainPerformanceMetrics();
    }

    public void loadListOfOrderTests () {
        oList.isCorrectPage ();
        //cora.clickOrderTests ();
        navigateTo(coraTestUrl + "/cora/ordertests");
        otList.isCorrectPage ();
        PerformanceMetrics listOfOrderTestsMetrics = obtainPerformanceMetrics();
    }

    public void loadListOfShipments () {
        oList.isCorrectPage ();
        //cora.clickShipments ();
        navigateTo(coraTestUrl + "/cora/shipments");
        shList.isCorrectPage ();
        PerformanceMetrics listOfShipmentsMetrics = obtainPerformanceMetrics();
    }

    public void loadListOfContainers () {
        oList.isCorrectPage ();
        //cora.clickContainers ();
        navigateTo(coraTestUrl + "/cora/containers");
        cList.isCorrectPage ();
        PerformanceMetrics listOfContainersMetrics = obtainPerformanceMetrics();
    }

    public void loadListOfTasks () {
        oList.isCorrectPage ();
        //cora.clickTasks ();
        navigateTo(coraTestUrl + "/cora/tasks");
        tList.isCorrectPage ();
        PerformanceMetrics listOfTasksMetrics = obtainPerformanceMetrics();
    }

    public void loadListOfMiras () {
        oList.isCorrectPage ();
        //cora.clickMiras ();
        navigateTo(coraTestUrl + "/cora/miras/list");
        mList.isCorrectPage ();
        PerformanceMetrics listOfMirasMetrics = obtainPerformanceMetrics();
    }

    public void loadListOfPatients () {
        oList.isCorrectPage ();
        //cora.clickPatients ();
        navigateTo(coraTestUrl + "/cora/patients/list");
        pList.isCorrectPage ();
        PerformanceMetrics listOfPatientsMetrics = obtainPerformanceMetrics();
    }

    public void filterContainers () {
        oList.isCorrectPage ();
        cora.searchContainer (freezerAB018078 ());
        cList.isCorrectPage ();
        PerformanceMetrics filterContainersMetrics = obtainPerformanceMetrics();
    }

    private PerformanceMetrics obtainPerformanceMetrics () {
        testLog ("Current page: " + getDriver ().getCurrentUrl ());
        HashMap<String, Object> metrics = new HashMap <> ();
        metrics.put ("type", "sauce:performance");
        Map<?, ?> perfMetrics = (Map <?, ?>) ((JavascriptExecutor) getDriver ()).executeScript ("sauce:log",
                metrics);
        return PerformanceMetrics.buildPerformanceMetrics (perfMetrics);
    }

}
