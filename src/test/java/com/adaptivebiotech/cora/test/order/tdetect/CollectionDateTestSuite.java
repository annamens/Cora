/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.utils.PageHelper.Carrier.COURIER;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Shipment.ShippingCondition;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.utils.PageHelper.Carrier;

/**
 * @author jpatel
 *
 */
@Test (groups = { "tDetect", "regression", "golden-retriever" })
public class CollectionDateTestSuite extends NewOrderTestBase {

    private Login           login           = new Login ();
    private OrdersList      ordersList      = new OrdersList ();
    private NewOrderTDetect newOrderTDetect = new NewOrderTDetect ();
    private NewShipment     shipment        = new NewShipment ();
    private Accession       accession       = new Accession ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void courierCollectionDateGreaterThanShipmentArrival () {
        String collectionDate = genDate (1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), collectionDate, COURIER, trackingNumber);

        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateErrorMsg);
        testLog ("Order activation failed with Toast Error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void courierCollectionDateEqualsShipmentArrival () {
        String collectionDate = genDate (0);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), collectionDate, COURIER, trackingNumber);

        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateErrorMsg);
        testLog ("Order activation failed with Toast Error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void courierCollectionDateLessThanShipmentArrival () {
        String collectionDate = genDate (-1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), collectionDate, COURIER, trackingNumber);

        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order activation successful without error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void otherCollectionDateEqualsShipmentArrival () {
        String collectionDate = genDate (0);
        List <Carrier> carriers = new ArrayList <Carrier> (Arrays.asList (Carrier.values ()));
        carriers.remove (COURIER);
        Carrier randomCarrier = carriers.get (new Random ().nextInt (carriers.size ()));

        createOrderAndCompleteAccession (newTrialProtocolPatient (), collectionDate, randomCarrier, trackingNumber);

        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order activation successful without error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void collectionDateGreaterThanShipmentArrival () {
        String collectionDate = genDate (1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), collectionDate, null, null);

        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateErrorMsg);
        testLog ("Order activation failed with Toast Error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void collectionDateEqualsShipmentArrival () {
        String collectionDate = genDate (0);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), collectionDate, null, null);

        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order activation successful without error");
    }

    /**
     * NOTE: SR-T4206
     * 
     * @sdlc.requirements SR-4420:R2
     */
    public void collectionDateLessThanOrEqualsPatientDOB () {
        String collectionDate = genDate (0);
        Patient patient = newTrialProtocolPatient ();
        patient.dateOfBirth = genDate (0);

        createOrderAndCompleteAccession (patient, collectionDate, null, null);
        newOrderTDetect.enterCollectionDate (genDate (-1));
        newOrderTDetect.clickSave ();
        assertEquals (newOrderTDetect.getToastError (), validateErrorMsg);
        testLog ("Error on Order Save, Collection Date is less than Patient DOB");

        newOrderTDetect.enterCollectionDate (genDate (0));
        newOrderTDetect.clickSave ();
        assertEquals (newOrderTDetect.getToastSuccess (), validdateSuccessMsg);
        testLog ("Successful order saved, Collection Date is equal to Patient DOB");

        newOrderTDetect.enterCollectionDate (genDate (-1));
        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateErrorMsg);
        testLog ("Error on Order Save and Activate, Collection Date is less than Patient DOB");

        newOrderTDetect.enterCollectionDate (genDate (0));
        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order Activated, Collection Date is equal to Patient DOB");
    }

    /**
     * NOTE: SR-T4206
     * 
     * @sdlc.requirements SR-4420:R2
     */
    public void collectionDateGreaterThanPatientDOB () {
        String collectionDate = genDate (0);
        Patient patient = newTrialProtocolPatient ();
        patient.dateOfBirth = genDate (-1);

        createOrderAndCompleteAccession (patient, collectionDate, null, null);
        testLog ("Successful order saved, Collection Date is greater than Patient DOB");

        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order Activated, Collection Date is greater than Patient DOB");
    }

    private void createOrderAndCompleteAccession (Patient patient, String collectionDate, Carrier carrier,
                                                  String trackingNumber) {
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                          patient,
                                                          icdCodes,
                                                          collectionDate.toString (),
                                                          COVID19_DX_IVD);
        testLog ("Order created: " + order.orderNumber);

        if (carrier != null && trackingNumber != null) {
            shipment.createShipment (ShippingCondition.Ambient,
                                     carrier,
                                     trackingNumber,
                                     order.orderNumber,
                                     Tube,
                                     "");
        } else {
            shipment.createShipment (order.orderNumber, Tube);
        }

        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickAccessionItemPass (accession.specimens[1]);
        accession.completeAccession ();
        newOrderTDetect.isCorrectPage ();
    }

}
