package com.adaptivebiotech.cora.ui.shipment;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.CoraPage;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;

/**
 * @author jpatel
 *
 */
public class ShipmentDetail extends CoraPage {

    private final String orderNo   = "[data-ng-bind='ctrl.entry.order.orderNumber']";
    private final String activeTab = "[role='tablist'] .active a";

    @Override
    public void isCorrectPage () {
        assertTrue (isTextInElement (activeTab, "SHIPMENT"));
        assertTrue (waitUntilVisible (orderNo));
    }

    public String getOrderNo () {
        return getText (orderNo);
    }

    public void clickOrderNo () {
        assertTrue (click (orderNo));
    }

    public Containers getPrimaryContainers (ContainerType type) {
        String lines = "[class='container-section'] [data-ng-repeat='container in ctrl.entry.containers']";
        String shipmentNum = getText ("[data-ng-bind='ctrl.entry.shipment.shipmentNumber']");
        return new Containers (waitForElements (lines).stream ().map (el -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (el, "[data-ng-bind$='container.containerNumber']", "href"));
            c.containerNumber = getText (el, "[data-ng-bind$='container.containerNumber']");
            c.containerType = type;
            c.location = getText (el, "[data-ng-bind='container | intakeLocation']");
            c.name = isElementPresent ("[data-ng-bind='container.displayName']") ? getText (el,
                                                                                            "[data-ng-bind='container.displayName']") : null;
            c.integrity = isElementPresent ("[data-ng-bind='container.integrity']") ? getText (el,
                                                                                               "[data-ng-bind='container.integrity']") : null;
            c.shipmentNumber = shipmentNum;

            if (isElementPresent (el, ".container-table")) {
                String css = "[data-ng-repeat='child in container.children']";
                List <Container> children = el.findElements (locateBy (css)).stream ().map (childRow -> {
                    Container childContainer = new Container ();
                    childContainer.id = getConId (getAttribute (childRow,
                                                                "[data-ng-bind='child.containerNumber']",
                                                                "href"));
                    childContainer.containerNumber = getText (childRow, "[data-ng-bind='child.containerNumber']");
                    childContainer.name = getText (childRow, "[data-ng-bind='child.displayName']");
                    childContainer.integrity = getText (childRow, "[data-ng-bind='child.integrity']");
                    childContainer.root = c;
                    return childContainer;
                }).collect (toList ());
                c.children = children;
            }
            return c;
        }).collect (toList ()));
    }

    public void clickContainerNo (String containerNo) {
        assertTrue (click ("//table//a[text()='" + containerNo + "']"));
    }
}
