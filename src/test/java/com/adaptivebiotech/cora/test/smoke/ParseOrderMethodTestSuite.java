package com.adaptivebiotech.cora.test.smoke;

import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
@Test
public class ParseOrderMethodTestSuite extends CoraBaseBrowser {

    private Login               coraLogin           = new Login ();
    private OrdersList          ordersList          = new OrdersList ();
    private NewOrderClonoSeq    newOrderClonoSeq    = new NewOrderClonoSeq ();
    private NewOrderTDetect     newOrderTDetect     = new NewOrderTDetect ();
    private OrderDetailClonoSeq orderDetailClonoSeq = new OrderDetailClonoSeq ();
    private OrderDetailTDetect  orderDetailTDetect  = new OrderDetailTDetect ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        coraLogin.doLogin ();
        ordersList.isCorrectPage ();
    }

    @Test
    public void ClonoseqPending () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/entry/diagnostic/4a18f772-79bf-4a0d-a116-de34efd013bd");
        Order order = newOrderClonoSeq.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

    @Test
    public void TDetectPending () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/dx/a81019c2-3a21-493e-a8ce-1f6b6da0057f");
        Order order = newOrderTDetect.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

    @Test
    public void ClonoseqShipmentPending () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/entry/diagnostic/a41d23ec-3b18-46a0-95e0-43c1d6a1d29a");
        Order order = newOrderClonoSeq.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

    @Test
    public void TDetectShipmentPending () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/dx/54f08158-4538-4caf-9777-7d50089a6ccb");
        Order order = newOrderTDetect.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

    @Test
    public void ClonoseqActive () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/details/bd34fcbb-052d-4267-92c8-86636a65e4b3");
        Order order = orderDetailClonoSeq.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

    @Test
    public void TDetectActive () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/dx/9dd18feb-7aac-42fa-8618-77e9591051a3");
        Order order = newOrderTDetect.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

    @Test
    public void ClonoseqCompleted () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/details/7ea9332e-7609-41b7-87ac-1fc01556cdb2");
        Order order = orderDetailClonoSeq.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

    @Test
    public void TDetectCompleted () {
        ordersList.navigateTo ("https://cora-test.dna.corp.adaptivebiotech.com/cora/order/details/93b78f87-3e07-46ab-8305-738118deb68d");
        Order order = orderDetailTDetect.parseOrder ();

        System.out.println ("Order: " + order.order_number + ", Order: " + order);
        System.out.println ("Cora Attachments Size: " + order.orderAttachments.size ());
        System.out.println ("Ship Attachments Size: " + order.shipmentAttachments.size ());
        System.out.println ("Dora Attachments Size: " + order.doraAttachments.size ());
        System.out.println ("Cora Attachments: " + order.orderAttachments);
        System.out.println ("Ship Attachments: " + order.shipmentAttachments);
        System.out.println ("Dora Attachments: " + order.doraAttachments);
        System.out.println ("Trf Attachments: " + order.trf);

    }

}
