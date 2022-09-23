/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.Streck;
import static com.adaptivebiotech.cora.utils.JsonUtils.getDataFromJsonString;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.Anticoagulant;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.CollectionDate;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.ContainerIntegrity;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.ContainerUnlabeled;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.General;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.InsufficientMatchingIdentifiers;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.MRN;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.MajorDateOfBirth;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.MajorPatientName;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.MinorDateOfBirth;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.MinorPatientName;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.NoTRF;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.NumberOfSamples;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.PHIPresent;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SampleAmount;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.ShippingConditions;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenStabilityIntegrity;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenType;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.TRFHandwritten;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.UniqueSpecimenId;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee.CLINICAL_TRIALS;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyHoldType.ORDER_HOLD;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyHoldType.SPECIMEN_HOLD;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Element;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.DiscrepancyResolutions;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.utils.PageHelper.Discrepancy;
import com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyHoldType;

@Test (groups = { "regression", "irish-wolfhound" })
public class DiscrepancyTestSuite extends CoraBaseBrowser {

    private final String[]                               icdCodes           = { "Z63.1" };
    private Login                                        login              = new Login ();
    private OrdersList                                   ordersList         = new OrdersList ();
    private NewOrderClonoSeq                             newOrderClonoSeq   = new NewOrderClonoSeq ();
    private NewShipment                                  shipment           = new NewShipment ();
    private Accession                                    accession          = new Accession ();
    private DiscrepancyResolutions                       discrepancyRes     = new DiscrepancyResolutions ();

    private ThreadLocal <Boolean>                        cfDna              = new ThreadLocal <> ();
    private ThreadLocal <Boolean>                        specimenActivation = new ThreadLocal <> ();

    private final Map <Discrepancy, DiscrepancyHoldType> streckOrder        = Stream.of (new Object[][] {
            { ShippingConditions, SPECIMEN_HOLD },
            { ContainerIntegrity, SPECIMEN_HOLD },
            { General, ORDER_HOLD },
            { TRFHandwritten, ORDER_HOLD },
            { NoTRF, ORDER_HOLD },
            { SpecimenStabilityIntegrity, SPECIMEN_HOLD },
            { SpecimenType, SPECIMEN_HOLD },
            { CollectionDate, ORDER_HOLD },
            { Anticoagulant, SPECIMEN_HOLD },
            { NumberOfSamples, ORDER_HOLD },
            { SampleAmount, SPECIMEN_HOLD },
            { MajorPatientName, SPECIMEN_HOLD },
            { MinorPatientName, ORDER_HOLD },
            { MRN, ORDER_HOLD },
            { UniqueSpecimenId, ORDER_HOLD },
            { ContainerUnlabeled, SPECIMEN_HOLD },
            { MajorDateOfBirth, SPECIMEN_HOLD },
            { MinorDateOfBirth, ORDER_HOLD },
            { InsufficientMatchingIdentifiers, ORDER_HOLD },
            { PHIPresent, ORDER_HOLD }
    }).collect (Collectors.toMap (data -> (Discrepancy) data[0], data -> (DiscrepancyHoldType) data[1]));

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        cfDna.set (featureFlags.cfDNA);
        specimenActivation.set (featureFlags.specimenActivation);
    }

    /**
     * NOTE: SR-T4312
     * 
     * @sdlc.requirements SR-12363:R1, R2
     */
    public void validateDiscrepancySeverity () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        String query = "SELECT * FROM cora.discrepancies WHERE shipment_id = '%s' AND discrepancy_type = '%s' ORDER BY created DESC";

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen ());
        testLog ("Non Streck Order No: " + order.orderNumber + ", id: " + order.id);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();

        Arrays.stream (Discrepancy.values ()).forEach (d -> {
            accession.clickAddContainerSpecimenDiscrepancy ();
            accession.addDiscrepancy (d, "Discrepancy: " + d.text, CLINICAL_TRIALS);
            Element holdType = accession.getDiscrepancyHoldType ();
            accession.clickDiscrepancySave ();

            List <Map <String, Object>> queryResults = coraDb.executeSelect (format (query,
                                                                                     shipmentId,
                                                                                     d.text));
            String jsonStr = queryResults.get (0).get ("properties").toString ();
            String severity = getDataFromJsonString (jsonStr, "Severity");
            assertEquals (severity, d.severity.name (), d.text);
            assertNull (holdType.text, "Accession page, Non Streck " + d.text);
        });
        testLog ("validate Major/Minor discrepancy  and specimen/order hold on accession page");

        accession.clickDiscrepancyResolutionsTab ();
        discrepancyRes.isCorrectPage ();
        Arrays.stream (Discrepancy.values ()).forEach (d -> {
            Element holdType = discrepancyRes.getDiscrepancyHoldType (d);
            assertNull (holdType.text, "Discrepancy page, Non Streck " + d.text);
        });
        testLog ("validate specimen/order hold on discrepany page");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.enterCompartment (CellFree);
        newOrderClonoSeq.enterAntiCoagulant (Streck);
        newOrderClonoSeq.clickSave ();
        testLog ("Update order to Streck");

        discrepancyRes.gotoDiscrepancy (shipmentId);
        Arrays.stream (Discrepancy.values ()).forEach (d -> {
            Element holdType = discrepancyRes.getDiscrepancyHoldType (d);
            assertEquals (holdType.text, streckOrder.get (d).text, "Discrepancy page, Streck " + d.text);
            assertEquals (holdType.color, streckOrder.get (d).color, "Discrepancy page, Streck " + d.text);
        });
        testLog ("validate specimen/order hold on discrepany page");

        discrepancyRes.clickAccessionTab ();
        accession.isCorrectPage ();
        Arrays.stream (Discrepancy.values ()).forEach (d -> {
            accession.clickAddContainerSpecimenDiscrepancy ();
            accession.addDiscrepancy (d, "Discrepancy: " + d.text, CLINICAL_TRIALS);
            Element holdType = accession.getDiscrepancyHoldType ();
            accession.clickDiscrepancySave ();

            List <Map <String, Object>> queryResults = coraDb.executeSelect (format (query,
                                                                                     shipmentId,
                                                                                     d.text));
            String jsonStr = queryResults.get (0).get ("properties").toString ();
            String severity = getDataFromJsonString (jsonStr, "Severity");
            assertEquals (severity, d.severity.name (), d.text);
            assertEquals (holdType.text, streckOrder.get (d).text, "Accession page, Streck " + d.text);
            assertEquals (holdType.color, streckOrder.get (d).color, "Accession page, Streck " + d.text);
        });
        testLog ("validate Major/Minor discrepancy  and specimen/order hold on accession page");
    }

}
