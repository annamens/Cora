package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018078;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.PerformanceHelper.softAssert;
import static com.seleniumfy.test.utils.Environment.sauceKey;
import static com.seleniumfy.test.utils.Environment.sauceOptions;
import static com.seleniumfy.test.utils.Environment.sauceUser;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.common.dto.saucelabs.Metrics;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainersList;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.ShipmentsList;
import com.adaptivebiotech.cora.ui.task.TasksList;
import com.adaptivebiotech.test.utils.PerformanceHelper;
import com.adaptivebiotech.test.utils.SauceHelper;

@Test (groups = "performance")
public class CoraPerformanceTestSuite extends CoraBaseBrowser {

    private static final String  PAGE_LOAD_METRIC_NAME              = "Page load";
    private static final int     PAGE_LOAD_METRIC_THRESHOLD         = 8000;
    private static final String  PERFORMANCE_SCORE_METRIC_NAME      = "Performance Score";
    private static final double  PERFORMANCE_SCORE_METRIC_THRESHOLD = 0.4;

    private Login                login                              = new Login ();
    private CoraPage             cora                               = new CoraPage ();
    private OrdersList           ordersList                         = new OrdersList ();
    private OrderTestsList       orderTestsList                     = new OrderTestsList ();
    private ShipmentsList        shipmentList                       = new ShipmentsList ();
    private ContainersList       containerList                      = new ContainersList ();
    private TasksList            taskList                           = new TasksList ();
    private MirasList            mirasList                          = new MirasList ();
    private PatientsList         patientsList                       = new PatientsList ();
    private Map <String, String> sauceJobId                         = new HashMap <> ();

    static {
        sauceOptions.put ("extendedDebugging", "true");
        sauceOptions.put ("capturePerformance", "true");
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
    }

    @AfterSuite
    public void cleanUp () {
        sauceOptions.remove ("extendedDebugging");
        sauceOptions.remove ("capturePerformance");
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
        SauceHelper sauce = new SauceHelper (sauceUser, sauceKey);
        Metrics metrics;
        Metrics.MetricData data;

        for (Map.Entry <String, String> k : sauceJobId.entrySet ()) {
            metrics = sauce.getPerformanceMetrics (k.getValue ());
            data = getMetricData (metrics);
            PerformanceHelper.checkGreaterThanThreshold (data.score,
                                                         PERFORMANCE_SCORE_METRIC_THRESHOLD,
                                                         PERFORMANCE_SCORE_METRIC_NAME,
                                                         k.getKey ());
            PerformanceHelper.checkLessThanThreshold (data.load,
                                                      PAGE_LOAD_METRIC_THRESHOLD,
                                                      PAGE_LOAD_METRIC_NAME,
                                                      k.getKey ());
        }
        softAssert.assertAll ();
    }

    private Metrics.MetricData getMetricData (Metrics metrics) {
        Metrics.MetricData data = new Metrics.MetricData ();
        for (Metrics.MetricItem d : metrics.items) {
            if (d.metric_data == null || d.page_url.contains ("login")) {
                continue;
            } else {
                data = d.metric_data;
                break;
            }
        }
        return data;
    }

}
