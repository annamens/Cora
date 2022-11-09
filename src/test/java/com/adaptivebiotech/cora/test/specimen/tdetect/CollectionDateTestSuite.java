/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.specimen.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_trial;
import static com.adaptivebiotech.cora.utils.PageHelper.Carrier.COURIER;
import static com.adaptivebiotech.cora.utils.PageHelper.Carrier.OTHER;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Shipment.ShippingCondition;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.specimen.SpecimenTestBase;
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
public class CollectionDateTestSuite extends SpecimenTestBase {

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
    public void courierCollectionDateShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genLocalDate (1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, COURIER, trackingNumber);

        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateToastErrorMsg);
        assertEquals (newOrderTDetect.getCollectionDateErrorMsg (), collectionDateErrorMsg);
        testLog ("Order activation failed, when collection Date > Arrival Date, carrier = courier");

        newOrderTDetect.enterCollectionDate (genLocalDate (-1));
        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order activated successfully, when collection Date <= Arrival Date, carrier = courier");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void otherCollectionDateShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genLocalDate (1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, OTHER, trackingNumber);

        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateToastErrorMsg);
        assertEquals (newOrderTDetect.getCollectionDateErrorMsg (), collectionDateErrorMsg);
        testLog ("Order activation failed, when collection Date > Arrival Date, carrier = FedEx/UPS/Other");

        newOrderTDetect.enterCollectionDate (genLocalDate (0));
        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateToastErrorMsg);
        assertEquals (newOrderTDetect.getCollectionDateErrorMsg (), collectionDateErrorMsg);
        testLog ("Order activation failed, when collection Date = Arrival Date, carrier = FedEx/UPS/Other");

        newOrderTDetect.enterCollectionDate (genLocalDate (-1));
        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order activated successfully, when collection Date < Arrival Date, carrier = FedEx/UPS/Other");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void noCarrierCollectionDateShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genLocalDate (1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, null, null);

        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateToastErrorMsg);
        assertEquals (newOrderTDetect.getCollectionDateErrorMsg (), collectionDateErrorMsg);
        testLog ("Order activation failed, when collection Date > Arrival Date, no carrier");

        newOrderTDetect.enterCollectionDate (genLocalDate (0));
        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateToastErrorMsg);
        assertEquals (newOrderTDetect.getCollectionDateErrorMsg (), collectionDateErrorMsg);
        testLog ("Order activation failed, when collection Date = Arrival Date, no carrier");

        newOrderTDetect.enterCollectionDate (genLocalDate (-1));
        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order activated successfully, when collection Date < Arrival Date, no carrier");
    }

    /**
     * NOTE: SR-T4206
     * 
     * @sdlc.requirements SR-4420:R2
     */
    public void collectionDatePatientDOB () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genLocalDate (-2);
        Patient patient = newTrialProtocolPatient ();
        patient.dateOfBirth = genDate (-2);

        createOrderAndCompleteAccession (patient, specimenDto, null, null);

        newOrderTDetect.enterCollectionDate (genLocalDate (-3));
        newOrderTDetect.clickOrderSave ();
        assertEquals (newOrderTDetect.getToastError (), validateToastErrorMsg);
        assertEquals (newOrderTDetect.getCollectionDateErrorMsg (), collectionDateErrorMsg);
        testLog ("Error on Order Save, Collection Date < Patient DOB");

        newOrderTDetect.enterCollectionDate (genLocalDate (-2));
        newOrderTDetect.clickOrderSave ();
        assertEquals (newOrderTDetect.getToastSuccess (), validateSuccessMsg);
        testLog ("Successful order saved, Collection Date = Patient DOB");

        newOrderTDetect.enterCollectionDate (genLocalDate (-1));
        newOrderTDetect.clickOrderSave ();
        assertEquals (newOrderTDetect.getToastSuccess (), validateSuccessMsg);
        testLog ("Successful order saved, Collection Date > Patient DOB");

        newOrderTDetect.enterCollectionDate (genLocalDate (-3));
        newOrderTDetect.clickSaveAndActivate ();
        assertEquals (newOrderTDetect.getToastError (), validateToastErrorMsg);
        assertEquals (newOrderTDetect.getCollectionDateErrorMsg (), collectionDateErrorMsg);
        testLog ("Order activation failed, Collection Date < Patient DOB");

        newOrderTDetect.enterCollectionDate (genLocalDate (-2));
        newOrderTDetect.activateOrder ();
        assertEquals (newOrderTDetect.getOrderStatus (), Active);
        testLog ("Order Activated, Collection Date >= Patient DOB");
    }

    private void createOrderAndCompleteAccession (Patient patient,
                                                  Specimen specimenDto,
                                                  Carrier carrier,
                                                  String trackingNumber) {
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_trial),
                                                          patient,
                                                          icdCodes,
                                                          COVID19_DX_IVD,
                                                          specimenDto);
        testLog ("Order created: " + order.orderNumber);

        if (carrier != null && trackingNumber != null) {
            shipment.createShipment (ShippingCondition.Ambient,
                                     carrier,
                                     trackingNumber,
                                     order.orderNumber,
                                     Tube,
                                     "");
            testLog ("Shipment created with carrier: " + carrier);
        } else {
            shipment.createShipment (order.orderNumber, Tube);
            testLog ("Shipment created without carrier");
        }

        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickAccessionItemPass (accession.specimens[1]);
        accession.completeAccession ();
        newOrderTDetect.isCorrectPage ();
    }

}
