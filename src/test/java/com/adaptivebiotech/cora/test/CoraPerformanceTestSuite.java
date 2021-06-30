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
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.mira.MirasList;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientsList;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.ui.task.TaskList;
import com.adaptivebiotech.test.utils.PerformanceHelper;
import com.adaptivebiotech.test.utils.SauceHelper;

@Test (groups = "performance")
public class CoraPerformanceTestSuite extends CoraBaseBrowser {

    private static final String  PAGE_LOAD_METRIC_NAME              = "Page load";
    private static final int     PAGE_LOAD_METRIC_THRESHOLD         = 8000;
    private static final String  PERFORMANCE_SCORE_METRIC_NAME      = "Performance Score";
    private static final double  PERFORMANCE_SCORE_METRIC_THRESHOLD = 0.4;

    private Login                login;
    private CoraPage             cora;
    private OrdersList           oList;
    private OrderTestsList       otList;
    private ShipmentList         shList;
    private ContainerList        cList;
    private TaskList             tList;
    private MirasList            mList;
    private PatientsList         pList;
    private Map <String, String> sauceJobId  = new HashMap <> ();

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

    @Test (priority = 1)
    public void loadListOfOrders () {
        oList.isCorrectTopNavRow2 ();
        navigateTo (coraTestUrl + "/cora/orders");
        oList.isCorrectPage ();
        sauceJobId.put ("orders", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfOrderTests () {
        oList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/ordertests");
        otList.isCorrectPage ();
        sauceJobId.put ("orderTests", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfShipments () {
        oList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/shipments");
        shList.isCorrectPage ();
        sauceJobId.put ("shipments", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfContainers () {
        oList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/containers");
        cList.isCorrectPage ();
        sauceJobId.put ("containers", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfTasks () {
        oList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/tasks");
        tList.isCorrectPage ();
        sauceJobId.put ("tasks", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfMiras () {
        oList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/miras/list");
        mList.isCorrectPage ();
        sauceJobId.put ("miras", getSessionId ());
    }

    @Test (priority = 1)
    public void loadListOfPatients () {
        oList.isCorrectPage ();
        navigateTo (coraTestUrl + "/cora/patients/list");
        pList.isCorrectPage ();
        sauceJobId.put ("patients", getSessionId ());
    }

    @Test (priority = 1)
    public void filterContainers () {
        oList.isCorrectPage ();
        cora.searchContainer (freezerAB018078 ());
        cList.isCorrectPage ();
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
