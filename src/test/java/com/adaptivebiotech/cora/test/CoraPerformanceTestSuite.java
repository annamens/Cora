/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018078;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.PerformanceHelper.checkGreaterThanThreshold;
import static com.adaptivebiotech.test.utils.PerformanceHelper.checkLessThanThreshold;
import static com.adaptivebiotech.test.utils.PerformanceHelper.softAssert;
import static com.seleniumfy.test.utils.Environment.sauceKey;
import static com.seleniumfy.test.utils.Environment.sauceOptions;
import static com.seleniumfy.test.utils.Environment.sauceUser;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import com.adaptivebiotech.common.dto.saucelabs.Metrics.MetricData;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainersList;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.ShipmentsList;
import com.adaptivebiotech.cora.ui.task.TasksList;
import com.adaptivebiotech.test.utils.SauceHelper;

@Test (groups = "performance")
public class CoraPerformanceTestSuite extends CoraBaseBrowser {

    private final String         extendedDebugging  = "extendedDebugging";
    private final String         capturePerformance = "capturePerformance";
    private Login                login              = new Login ();
    private CoraPage             cora               = new CoraPage ();
    private OrdersList           ordersList         = new OrdersList ();
    private OrderTestsList       orderTestsList     = new OrderTestsList ();
    private ShipmentsList        shipmentList       = new ShipmentsList ();
    private ContainersList       containerList      = new ContainersList ();
    private TasksList            taskList           = new TasksList ();
    private MirasList            mirasList          = new MirasList ();
    private PatientsList         patientsList       = new PatientsList ();
    private Map <String, String> sauceJobId         = new HashMap <> ();

    @BeforeSuite
    public void beforeSuite () {
        sauceOptions.put (extendedDebugging, "true");
        sauceOptions.put (capturePerformance, "true");
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
    }

    @AfterSuite
    public void cleanUp () {
        sauceOptions.remove (extendedDebugging);
        sauceOptions.remove (capturePerformance);
    }

    @Test (priority = 1)
    public void loadListOfOrders () {
        ordersList.isCorrectTopNavRow2 ();
        navigateTo (coraTestUrl + "/cora/orders");
        ordersList.isCorrectPage ();
        sauceJobId.put ("orders", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfOrderTests () {
        ordersList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/ordertests");
        orderTestsList.isCorrectPage ();
        sauceJobId.put ("orderTests", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfShipments () {
        ordersList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/shipments");
        shipmentList.isCorrectPage ();
        sauceJobId.put ("shipments", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfContainers () {
        ordersList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/containers");
        containerList.isCorrectPage ();
        sauceJobId.put ("containers", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfTasks () {
        ordersList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/tasks");
        taskList.isCorrectPage ();
        sauceJobId.put ("tasks", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfMiras () {
        ordersList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/miras/list");
        mirasList.isCorrectPage ();
        sauceJobId.put ("miras", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfPatients () {
        ordersList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/patients/list");
        patientsList.isCorrectPage ();
        sauceJobId.put ("patients", getSessionId ());
    }

    @Test (priority = 1)
    public void filterContainers () {
        ordersList.isCorrectPage ();
        cora.searchContainer (freezerAB018078 ());
        containerList.isCorrectPage ();
        sauceJobId.put ("filterContainers", getSessionId ());
    }

    @Test (priority = 2)
    public void assertMetrics () {
        String pageLoad = "Page load";
        int pageLoadThreshold = 8000;
        String performanceScore = "Performance Score";
        double performanceScoreThreshold = 0.4;

        SauceHelper sauce = new SauceHelper (sauceUser, sauceKey);
        MetricData data;

        for (Map.Entry <String, String> k : sauceJobId.entrySet ()) {
            data = sauce.getPerformanceMetrics (k.getValue ()).items.stream ()
                                                                    .filter (m -> m.metric_data != null && !m.page_url.contains ("login"))
                                                                    .findFirst ().get ().metric_data;
            checkGreaterThanThreshold (data.score, performanceScoreThreshold, performanceScore, k.getKey ());
            checkLessThanThreshold (data.load, pageLoadThreshold, pageLoad, k.getKey ());
        }
        softAssert.assertAll ();
    }
}
