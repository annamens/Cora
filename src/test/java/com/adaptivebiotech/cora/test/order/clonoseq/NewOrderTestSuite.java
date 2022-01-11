package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class NewOrderTestSuite extends CoraBaseBrowser {

    private final List <String> headers    = asList ("Customer Instructions",
                                                     "Order Notes",
                                                     "Ordering Physician",
                                                     "Patient Information",
                                                     "Specimen",
                                                     "Order Test",
                                                     "Billing",
                                                     "clonoSEQ Order Authorization",
                                                     "Attachments",
                                                     "History");
    private Login               login      = new Login ();
    private OrdersList          ordersList = new OrdersList ();
    private NewOrderClonoSeq    diagnostic = new NewOrderClonoSeq ();

    public void sections_order () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        diagnostic.selectNewClonoSEQDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        assertEquals (diagnostic.getSectionHeaders (), headers);
        testLog ("found the Order Authorization section below the Billing section of the clonoSEQ order form");
    }
}
