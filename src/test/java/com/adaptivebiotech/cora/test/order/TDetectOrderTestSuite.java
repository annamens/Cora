package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
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

    private OrderDetailTDetect orderDetailTDetect = new OrderDetailTDetect ();
    private Billing            billing            = new Billing ();
    private Shipment           shipment           = new Shipment ();
    private Accession          accession          = new Accession ();

    /**
     * NOTE: SR-T3243
     */
    public void validateTDetectOrderActivation () {
        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
        // create T-Detect diagnostic order
        String orderNum = orderDetailTDetect.createTDetectOrder (TestHelper.physicianTRF (),
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
        orderDetailTDetect.activateOrder ();
        Logging.testLog ("STEP 2 - Order is Active.");
    }

}
