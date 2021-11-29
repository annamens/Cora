package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertEquals;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.api.CoraApi;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;

@Test (groups = "regression")
public class DoubleClickSaveTest extends CoraBaseBrowser {

    private CoraApi          coraApi      = new CoraApi ();
    private Login            login        = new Login ();
    private OrdersList       ordersList   = new OrdersList ();
    private NewOrderClonoSeq diagnostic   = new NewOrderClonoSeq ();
    private Shipment         shipment     = new Shipment ();
    private ShipmentList     shipmentList = new ShipmentList ();

    public void doubleClickSave () {
        login.doLogin ();
        ordersList.isCorrectPage ();

        coraApi.login ();
        diagnostic.selectNewClonoSEQDiagnosticOrder ();
        diagnostic.isCorrectPage ();
        diagnostic.selectPhysician (coraApi.getPhysician (non_CLEP_clonoseq));
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
