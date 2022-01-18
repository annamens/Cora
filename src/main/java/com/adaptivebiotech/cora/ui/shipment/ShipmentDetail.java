package com.adaptivebiotech.cora.ui.shipment;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Orders.OrderCategory;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Shipment.ShippingCondition;
import com.adaptivebiotech.cora.ui.CoraPage;

/**
 * @author jpatel
 *
 */
public class ShipmentDetail extends CoraPage {

    private final String orderNo                  = "[data-ng-bind='ctrl.entry.order.orderNumber']";
    private final String activeTab                = "[role='tablist'] .active a";
    private final String arrivalDateTime          = "[data-ng-bind='ctrl.entry.shipment.arrivalDate | localDateTime']";
    private final String category                 = "[data-ng-bind='ctrl.entry.shipment.category']";
    private final String shipmentNo               = ".shipment-details [data-ng-bind='ctrl.entry.shipment.shipmentNumber']";
    private final String shippingCondition        = "[data-ng-bind='ctrl.entry.shipment.condition']";
    private final String carrier                  = "[data-ng-bind='ctrl.entry.shipment.carrier']";
    private final String trackingNo               = "[data-ng-bind='ctrl.entry.shipment.trackingNumber']";
    private final String status                   = "[ng-bind='ctrl.entry | shipmentEntryStatus']";
    private final String specimenId               = "[data-ng-bind='ctrl.entry.specimen.specimenNumber']";
    private final String containerType            = "[data-ng-bind$='containerTypeDisplayName']";
    private final String containerQuantity        = "[data-ng-bind='ctrl.entry.containers.length']";
    private final String specimenApprovalStatus   = "[data-ng-bind='ctrl.entry.specimen.approvalStatus']";
    private final String specimenApprovalDateTime = "[data-ng-bind^='ctrl.entry.specimen.approvedDate']";

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

    public Shipment getShipmentDetails () {
        Shipment shipment = new Shipment ();
        shipment.arrivalDate = getText (arrivalDateTime);
        shipment.category = OrderCategory.valueOf (getText (category).trim ());
        shipment.shipmentNumber = getText (shipmentNo);
        shipment.condition = ShippingCondition.valueOf (getText (shippingCondition));
        shipment.carrier = getAttribute (carrier, "value");
        shipment.trackingNumber = getAttribute (trackingNo, "value");
        shipment.status = getText (status);
        return shipment;
    }

    public String getSpecimenId () {
        return getText (specimenId);
    }

    public String getContainerType () {
        return getText (containerType);
    }

    public String getContainerQuantity () {
        return getText (containerQuantity);
    }

    public String getSpecimenApprovalStatus () {
        return getText (specimenApprovalStatus);
    }

    public String getSpecimenApprovalDateTime () {
        return getText (specimenApprovalDateTime);
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
            String displayNameCss = "[data-ng-bind='container.displayName']";
            c.name = isElementPresent (displayNameCss) && isElementVisible (displayNameCss) ? getText (el,
                                                                                                       displayNameCss) : null;
            String integrityCss = "[data-ng-bind='container.integrity']";
            c.integrity = isElementPresent (integrityCss) && isElementVisible (integrityCss) ? getText (el,
                                                                                                        integrityCss) : null;;
            c.shipmentNumber = shipmentNum;

            if (isElementPresent (el, ".container-table")) {
                String css = "[data-ng-repeat='child in container.children']";
                List <Container> children = findElements (el, css).stream ().map (childRow -> {
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
