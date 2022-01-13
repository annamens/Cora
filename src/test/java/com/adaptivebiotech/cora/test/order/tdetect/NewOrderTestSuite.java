package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;

/**
 * Note:
 * - ICD codes and patient billing address are not required
 */
@Test (groups = "regression")
public class NewOrderTestSuite extends CoraBaseBrowser {

    private final List <String> headers    = asList ("Customer Instructions",
                                                     "Order Notes",
                                                     "Ordering Physician",
                                                     "Patient",
                                                     "Specimen",
                                                     "Order Test",
                                                     "Billing",
                                                     "T-Detect Order Authorization",
                                                     "Attachments",
                                                     "Messages",
                                                     "History");
    private Login               login      = new Login ();
    private OrdersList          ordersList = new OrdersList ();
    private NewOrderTDetect     diagnostic = new NewOrderTDetect ();

    /**
     * @sdlc_requirements SR-7907:R12
     */
    @Test (groups = "corgi")
    public void sections_order () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        diagnostic.selectNewTDetectDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        assertEquals (diagnostic.getSectionHeaders (), headers);
        testLog ("found the Order Authorization section below the Billing section of the T-Detect order form");
    }
}
