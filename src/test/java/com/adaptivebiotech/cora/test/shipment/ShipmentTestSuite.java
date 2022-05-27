/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Refrigerated;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.getAllShippingConditions;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;

@Test (groups = "regression")
public class ShipmentTestSuite extends CoraBaseBrowser {

    private final String[]   icdCodes         = { "Z63.1" };
    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();
    private NewShipment      shipment         = new NewShipment ();
    private ShipmentDetail   shipmentDetail   = new ShipmentDetail ();
    private Accession        accession        = new Accession ();
    private Specimen         specimen         = bloodSpecimen ();

    /**
     * @sdlc.requirements SR-9174
     */
    @Test (groups = "entlebucher")
    public void diagnosticShipment () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            icdCodes,
                                                            ID_BCell2_CLIA,
                                                            specimen);

        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        List <String> expected = getAllShippingConditions ();
        assertEquals (shipment.getAllShippingConditions (), expected);
        testLog (join ("\n\t",
                       "The following options display in the Shipping Condition dropdown:",
                       join ("\n\t", expected)));

        shipment.enterShippingCondition (Refrigerated);
        shipment.enterOrderNumber (order.orderNumber);
        shipment.selectDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        String shipmentId = shipment.getShipmentId ();
        shipment.clickAccessionTab ();
        accession.completeAccession ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.activateOrder ();
        testLog (format ("Able to generate order with %s shipping condition", Refrigerated));

        newOrderClonoSeq.gotoShipmentEntry (shipmentId);
        shipmentDetail.isCorrectPage ();
        assertEquals (shipmentDetail.getShippingCondition (), Refrigerated);
        testLog (format ("Shipping Condition displayed %s on the Shipment Details page", Refrigerated));
    }
}
