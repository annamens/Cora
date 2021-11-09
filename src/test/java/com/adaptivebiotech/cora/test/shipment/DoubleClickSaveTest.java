package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertEquals;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.utils.TestHelper;

@Test (groups = "regression")
public class DoubleClickSaveTest extends CoraBaseBrowser {

    private NewOrderClonoSeq diagnostic   = new NewOrderClonoSeq ();
    private Shipment         shipment     = new Shipment ();
    private ShipmentList     shipmentList = new ShipmentList ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
    }

    public void doubleClickSave () {
        diagnostic.selectNewClonoSEQDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (TestHelper.physicianTRF ());
        diagnostic.enterPatientICD_Codes ("A01.02");
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
        assertEquals (shipmentWithOrderNum.size (), 1);
    }
}
