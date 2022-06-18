/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.utils.PageHelper.Carrier.COURIER;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
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
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.utils.PageHelper.Carrier;

/**
 * @author jpatel
 *
 */
@Test (groups = { "clonoSeq", "regression", "golden-retriever" })
public class CollectionDateTestSuite extends NewOrderTestBase {

    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();
    private NewShipment      shipment         = new NewShipment ();
    private Accession        accession        = new Accession ();

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
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, COURIER, trackingNumber);

        newOrderClonoSeq.clickSaveAndActivate ();
        assertEquals (newOrderClonoSeq.getToastError (), validateErrorMsg);
        testLog ("Order activation failed with Toast Error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void courierCollectionDateEqualsShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (0);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, COURIER, trackingNumber);

        newOrderClonoSeq.clickSaveAndActivate ();
        assertEquals (newOrderClonoSeq.getToastError (), validateErrorMsg);
        testLog ("Order activation failed with Toast Error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void courierCollectionDateLessThanShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (-1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, COURIER, trackingNumber);

        newOrderClonoSeq.activateOrder ();
        assertEquals (newOrderClonoSeq.getOrderStatus (), Active);
        testLog ("Order activation successful without error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void otherCollectionDateEqualsShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (0);
        List <Carrier> carriers = new ArrayList <Carrier> (Arrays.asList (Carrier.values ()));
        carriers.remove (COURIER);
        Carrier randomCarrier = carriers.get (new Random ().nextInt (carriers.size ()));

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, randomCarrier, trackingNumber);

        newOrderClonoSeq.activateOrder ();
        assertEquals (newOrderClonoSeq.getOrderStatus (), Active);
        testLog ("Order activation successful without error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void collectionDateGreaterThanShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (1);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, null, null);

        newOrderClonoSeq.clickSaveAndActivate ();
        assertEquals (newOrderClonoSeq.getToastError (), validateErrorMsg);
        testLog ("Order activation failed with Toast Error");
    }

    /**
     * NOTE: SR-T4202
     * 
     * @sdlc.requirements SR-4420:R1
     */
    public void collectionDateEqualsShipmentArrival () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (0);

        createOrderAndCompleteAccession (newTrialProtocolPatient (), specimenDto, null, null);

        newOrderClonoSeq.activateOrder ();
        assertEquals (newOrderClonoSeq.getOrderStatus (), Active);
        testLog ("Order activation successful without error");
    }

    /**
     * NOTE: SR-T4206
     * 
     * @sdlc.requirements SR-4420:R2
     */
    public void collectionDateLessThanOrEqualsPatientDOB () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (0);
        Patient patient = newTrialProtocolPatient ();
        patient.dateOfBirth = genDate (0);

        createOrderAndCompleteAccession (patient, specimenDto, null, null);
        newOrderClonoSeq.enterCollectionDate (genDate (-1));
        newOrderClonoSeq.clickSave ();
        assertEquals (newOrderClonoSeq.getToastError (), validateErrorMsg);
        testLog ("Error on Order Save, Collection Date is less than Patient DOB");

        newOrderClonoSeq.enterCollectionDate (genDate (0));
        newOrderClonoSeq.clickSave ();
        assertEquals (newOrderClonoSeq.getToastSuccess (), validdateSuccessMsg);
        testLog ("Successful order saved, Collection Date is equal to Patient DOB");

        newOrderClonoSeq.enterCollectionDate (genDate (-1));
        newOrderClonoSeq.clickSaveAndActivate ();
        assertEquals (newOrderClonoSeq.getToastError (), validateErrorMsg);
        testLog ("Error on Order Save and Activate, Collection Date is less than Patient DOB");

        newOrderClonoSeq.enterCollectionDate (genDate (0));
        newOrderClonoSeq.activateOrder ();
        assertEquals (newOrderClonoSeq.getOrderStatus (), Active);
        testLog ("Order Activated, Collection Date is equal to Patient DOB");
    }

    /**
     * NOTE: SR-T4206
     * 
     * @sdlc.requirements SR-4420:R2
     */
    public void collectionDateGreaterThanPatientDOB () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genDate (0);
        Patient patient = newTrialProtocolPatient ();
        patient.dateOfBirth = genDate (-1);

        createOrderAndCompleteAccession (patient, specimenDto, null, null);
        testLog ("Successful order saved, Collection Date is greater than Patient DOB");

        newOrderClonoSeq.activateOrder ();
        assertEquals (newOrderClonoSeq.getOrderStatus (), Active);
        testLog ("Order Activated, Collection Date is greater than Patient DOB");
    }

    private void createOrderAndCompleteAccession (Patient patient, Specimen specimenDto, Carrier carrier,
                                                  String trackingNumber) {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            patient,
                                                            icdCodes,
                                                            ID_BCell2_CLIA,
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
        newOrderClonoSeq.isCorrectPage ();
    }

}
