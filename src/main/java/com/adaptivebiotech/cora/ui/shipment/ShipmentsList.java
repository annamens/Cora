package com.adaptivebiotech.cora.ui.shipment;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ShipmentsList extends CoraPage {

    public ShipmentsList () {
        staticNavBarHeight = 90;
    }

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active[title='Shipments']"));
        pageLoading ();
    }

    public List <Shipment> getAllShipments () {
        pageLoading ();
        return getShipments ();
    }

    public List <Shipment> getShipments () {
        return waitForElements ("[ng-repeat='shipmentSummary in ctrl.shipments']").stream ().map (el -> {
            Shipment s = new Shipment ();
            s.shipmentNumber = getText (el, "[ng-bind='::shipmentSummary.shipment.shipmentNumber']");
            s.link = getText (el, "[ng-bind='::shipmentSummary.orderNumber']");
            return s;
        }).collect (toList ());
    }

    public void goToShipments () {
        String url = "/cora/shipments?sort=arrivaldate&ascending=true&arrivaldate=today";
        assertTrue (navigateTo (coraTestUrl + url));
        pageLoading ();
    }

    public void clickShipment (String shipmentId) {
        assertTrue (click ("//table//*[text()='" + shipmentId + "']"));
        pageLoading ();
    }
}
