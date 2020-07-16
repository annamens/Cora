package com.adaptivebiotech.cora.ui.shipment;

import com.adaptivebiotech.common.dto.Orders;
import com.adaptivebiotech.common.dto.Orders.Order;
import com.adaptivebiotech.common.dto.Orders.OrderTest;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Workflow;
import com.adaptivebiotech.test.utils.PageHelper.DateRange;
import com.adaptivebiotech.test.utils.PageHelper.OrderCategory;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.seleniumfy.test.utils.Timeout;

import java.util.List;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.getAssay;
import static com.adaptivebiotech.test.utils.PageHelper.DateRange.Last30;
import static com.adaptivebiotech.test.utils.PageHelper.OrderCategory.Diagnostic;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ShipmentList extends CoraPage {

    public ShipmentList() {
        staticNavBarHeight = 90;
    }

    public List<Shipment> getAllShipments () {
        pageLoading ();
        return getShipments ();
    }

    public List<Shipment> getShipments () {
        return waitForElements ("[ng-repeat='shipmentSummary in ctrl.shipments']").stream ().map (el -> {
            Shipment s = new Shipment ();
            s.shipmentNumber = getText (el, "[ng-bind='::shipmentSummary.shipment.shipmentNumber']");
            s.link = getText (el, "[ng-bind='::shipmentSummary.orderNumber']");
            return s;
        }).collect(toList());
    }

    public void goToShipments () {
        String url = "/cora/shipments?sort=arrivaldate&ascending=true&arrivaldate=today";
        assertTrue (navigateTo (coraTestUrl + url));
        pageLoading ();
    }
}
