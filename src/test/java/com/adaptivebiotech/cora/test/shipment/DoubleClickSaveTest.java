package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.junit.Assert.assertEquals;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.order.OrderTestBase;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.ui.cora.Login;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

@Test (groups = "regression")
public class DoubleClickSaveTest extends OrderTestBase {

    private Diagnostic   diagnostic;
    private Shipment     shipment;
    private ShipmentList shipmentList;

    @BeforeMethod
    public void beforeMethod () {
        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
        diagnostic = new Diagnostic ();
        shipment = new Shipment ();
        shipmentList = new ShipmentList ();
    }

    public void doubleClickSave () {
        diagnostic.selectNewDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (physicianTRF);
        diagnostic.enterPatientICD_Codes (icdCode);
        diagnostic.clickSave ();
        String orderNum = diagnostic.getOrderNum ();

        diagnostic.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (Tube);

        shipment.doubleClickSave ();
        shipmentList.goToShipments ();
        List <com.adaptivebiotech.cora.dto.Shipment> shipmentWithOrderNum;
        shipmentWithOrderNum = shipmentList.getAllShipments ().stream ()
                                           .filter (s -> s.link != null && s.link.equals (orderNum))
                                           .collect (Collectors.toList ());
        assertEquals (1, shipmentWithOrderNum.size ());
    }
}
