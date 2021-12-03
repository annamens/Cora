package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_tdetect_all_tests;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.BillingNewOrder;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "tDetectOrder" })
public class TDetectOrderTestSuite extends CoraBaseBrowser {

    private Login           login           = new Login ();
    private OrdersList      ordersList      = new OrdersList ();
    private NewOrderTDetect newOrderTDetect = new NewOrderTDetect ();
    private BillingNewOrder billing         = new BillingNewOrder ();
    private Shipment        shipment        = new Shipment ();
    private Accession       accession       = new Accession ();

    /**
     * NOTE: SR-T3243
     */
    public void validateTDetectOrderActivation () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        coraApi.login ();
        // create T-Detect diagnostic order
        String orderNum = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (non_CLEP_tdetect_all_tests),
                                                              TestHelper.newPatient (),
                                                              new String[] {},
                                                              DateUtils.getPastFutureDate (-3),
                                                              Assay.COVID19_DX_IVD,
                                                              ChargeType.Client,
                                                              TestHelper.getRandomAddress ());

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (ContainerType.SlideBox5CS);
        shipment.clickAddSlide ();
        shipment.clickAddSlide ();
        shipment.clickSave ();
        Logging.testLog ("STEP 1.1 - Shipment saves successfully");
        Containers containers = shipment.getPrimaryContainers (ContainerType.SlideBox5CS);
        assertTrue (containers.list.size () == 1);
        Container container = containers.list.get (0);
        assertTrue (container.containerNumber.matches ("CO-\\d{6}"));
        assertTrue (container.children.size () == 3);
        assertTrue (container.children.get (0).containerNumber.matches ("CO-\\d{6}"));
        assertTrue (container.children.get (1).containerNumber.matches ("CO-\\d{6}"));
        assertTrue (container.children.get (2).containerNumber.matches ("CO-\\d{6}"));
        Logging.testLog ("STEP 1.2 - Shipment bx and slides have CO-#");

        shipment.gotoAccession ();
        accession.completeAccession ();

        // activate order
        billing.isCorrectPage ();
        newOrderTDetect.activateOrder ();
        Logging.testLog ("STEP 2 - Order is Active.");
    }

}
