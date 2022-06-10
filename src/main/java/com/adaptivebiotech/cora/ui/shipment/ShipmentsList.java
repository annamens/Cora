/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.shipment;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.EnumUtils.getEnum;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Orders.OrderCategory;
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

    public Shipment getShipmentForOrder (String orderNumber) {
        return parseShipment (scrollTo (waitForElement (format ("//*[*[a='%s']]", orderNumber))));
    }

    public List <Shipment> getShipments () {
        return waitForElements (".shipments-table tbody tr").stream ().map (tr -> {
            return parseShipment (tr);
        }).collect (toList ());
    }

    private Shipment parseShipment (WebElement tr) {
        Shipment s = new Shipment ();
        s.shipmentNumber = getText (tr, "td:nth-child(1)");
        s.arrivalDate = getText (tr, "td:nth-child(2)");
        s.category = getEnum (OrderCategory.class, getText (tr, "td:nth-child(3)"));
        s.link = getAttribute (tr, "td:nth-child(4)", "href");
        s.status = getText (tr, "td:nth-child(5)");
        return s;
    }

    public void goToShipments () {
        String url = "/cora/shipments?sort=arrivaldate&ascending=true&arrivaldate=today";
        assertTrue (navigateTo (coraTestUrl + url));
        pageLoading ();
    }

    public void clickShipment (String shipmentId) {
        assertTrue (click ("//table//*[text()='" + shipmentId + "']"));
    }
}
