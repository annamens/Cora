package com.adaptivebiotech.test.cora.shipment;

import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.test.cora.order.OrderTestBase;
import com.adaptivebiotech.ui.cora.order.Diagnostic;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.junit.Assert.assertEquals;

@Test (groups = { "regression" })
public class DoubleClickSaveTest extends OrderTestBase {

    private Diagnostic diagnostic;

    public void doubleClickSave () {
        diagnostic = new Diagnostic ();
        diagnostic.clickNewDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.enterPatientICD_Codes (icdCode);
        diagnostic.clickSave();
        String orderNum = diagnostic.getOrderNum();

        Shipment shipment = new Shipment ();
        shipment.clickNewDiagnosticShipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.enterDiagnosticSpecimenContainerType (Tube);

        shipment.doubleClickSave();
        ShipmentList shipmentList = new ShipmentList();
        shipmentList.goToShipments();
        List<com.adaptivebiotech.cora.dto.Shipment> shipmentWithOrderNum = shipmentList.getAllShipments().stream()
                .filter(s -> s.link != null && s.link.equals(orderNum)).collect(Collectors.toList());
        assertEquals(1, shipmentWithOrderNum.size());
    }
}
