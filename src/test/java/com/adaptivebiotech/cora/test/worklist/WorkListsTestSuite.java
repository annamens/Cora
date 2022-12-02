/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.worklist;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.trial_clonoseq;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.Streck;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Advisory;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Alarm;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Expired;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Warning;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenType;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee.CLINICAL_TRIALS;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.DateHelper.pstZoneId;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.testng.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.StabilityStatus;
import com.adaptivebiotech.cora.dto.WorkListColumnHeaders;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.worklist.WorkList;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentsList;
import com.adaptivebiotech.test.utils.DateHelper;

/**
 * @author Srinivas Annameni
 *         <a href="mailto:sannameni@adaptivebiotech.com">sannameni@adaptivebiotech.com</a>
 */
@Test (groups = "irish-wolfhound")
public class WorkListsTestSuite extends CoraBaseBrowser {

    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();
    private NewShipment      shipment         = new NewShipment ();
    private Accession        accession        = new Accession ();
    private OrderTestsList   orderTestsList   = new OrderTestsList ();
    private ShipmentsList    shipmentsList    = new ShipmentsList ();
    private WorkList         workList         = new WorkList ();

    /**
     * @sdlc.requirements SR-13570
     */
    @Test (groups = { "smoke", "irish-wolfhound" })
    public void coraOrdersWorkLists () {
        login.doLogin ();
        ordersList.isCorrectPage (); // Cora page-orders page
        ordersList.clickQueriesButton ();
        List <String> querylist = new ArrayList <> ();
        querylist = ordersList.getQueriesList ();
        querylist.remove ("Frozen Specimens to Expiration");// failing due to bug:SR 13674
        for (String workListItem : querylist) {
            ordersList.clickWorkList (workListItem);
            workList.isCorrectPage ();
            assertEquals (workList.getColumnList (workListItem),
                          WorkListColumnHeaders.getColumnlist (workListItem),
                          "Failed to verify -" + workListItem);
            testLog (workListItem + "- PASSED");
            workList.navigateBack ();
            ordersList.isCorrectPage ();
            ordersList.clickQueriesButton ();
        }
    }

    /**
     * @sdlc.requirements SR-13570
     */
    @Test (groups = { "smoke", "irish-wolfhound" })
    public void coraOrderTestsWorkLists () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.clickOrderTests ();// order tests tab
        orderTestsList.isCorrectPage ();
        orderTestsList.clickQueriesButton ();
        for (String workListItem : orderTestsList.getQueriesList ()) {
            orderTestsList.clickWorkList (workListItem);
            workList.isCorrectPage ();
            assertEquals (workList.getColumnList (workListItem),
                          WorkListColumnHeaders.getColumnlist (workListItem),
                          "Failed to verify -" + workListItem);
            testLog (workListItem + "- PASSED");
            workList.navigateBack ();
            orderTestsList.isCorrectPage ();
            orderTestsList.clickQueriesButton ();
        }
    }

    /**
     * @sdlc.requirements SR-13570
     */
    @Test (groups = { "smoke", "irish-wolfhound" })
    public void coraOrderShipmentsWorkLists () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.clickShipments (); // shipments tab
        shipmentsList.isCorrectPage ();
        shipmentsList.clickQueriesButton ();
        for (String workListItem : shipmentsList.getQueriesList ()) {
            shipmentsList.clickWorkList (workListItem);
            workList.isCorrectPage ();
            assertEquals (workList.getColumnList (workListItem),
                          WorkListColumnHeaders.getColumnlist (workListItem),
                          "Failed to verify -" + workListItem);
            testLog (workListItem + "- PASSED");
            workList.navigateBack ();
            shipmentsList.isCorrectPage ();
            shipmentsList.clickQueriesButton ();
        }
    }

    /**
     * NOTE: SR-T4345
     * 
     * @sdlc.requirements SR-13570
     */
    @Test (groups = { "regression", "irish-wolfhound" })
    public void verifyAwaitingResolution () {

        Specimen specimenDto = bloodSpecimen ();
        Patient patient = newTrialProtocolPatient ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        specimenDto.collectionDate = genLocalDate (0);
        Assay assayTest = ID_BCell2_CLIA;
        String[] icdCode = { "B17.2" };
        String worklistItem = "Awaiting Resolution Clinical Trial (PHI)";

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (trial_clonoseq),
                                                            patient,
                                                            icdCode,
                                                            assayTest,
                                                            specimenDto);
        specimenDto.specimenNumber = newOrderClonoSeq.getSpecimenId ();
        testLog ("Awaiting resolution Order No: " + order.orderNumber);
        testLog ("Specimen ID: " + specimenDto.specimenNumber);
        shipment.createShipment (order.orderNumber, Vacutainer);
        UUID shipmentId = accession.getShipmentId ();
        accession.gotoAccession (shipmentId);
        accession.clickAddContainerSpecimenDiscrepancy ();
        accession.addDiscrepancy (SpecimenType, "This is a specimen/container discrepancy", CLINICAL_TRIALS);
        accession.clickDiscrepancySave ();
        accession.gotoAccession (shipmentId);
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        accession.clickOrders ();
        ordersList.clickQueriesButton ();
        ordersList.clickWorkList (worklistItem);

        updateCollectionDateAndVerifyBloodStabilityWindow (0, Advisory, specimenDto.specimenNumber, order.orderNumber);
        updateCollectionDateAndVerifyBloodStabilityWindow (-2, Warning, specimenDto.specimenNumber, order.orderNumber);
        updateCollectionDateAndVerifyBloodStabilityWindow (-6, Alarm, specimenDto.specimenNumber, order.orderNumber);
        updateCollectionDateAndVerifyBloodStabilityWindow (-7, Expired, specimenDto.specimenNumber, order.orderNumber);

        updateIsolationDateAndVerifyPlasmaStabilityWindow (0, Advisory, specimenDto.specimenNumber, order.orderNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-40, Warning, specimenDto.specimenNumber, order.orderNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-42, Alarm, specimenDto.specimenNumber, order.orderNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-45, Expired, specimenDto.specimenNumber, order.orderNumber);
    }

    private void updateIsolationDateAndVerifyPlasmaStabilityWindow (int dateDifference,
                                                                    StabilityStatus stabilityWindow,
                                                                    String spec_id, String orderNum) {
        String query = "UPDATE cora.specimens SET isolation_date = '%s' where specimen_number ='%s';";
        int updateCount = coraDb.executeUpdate (format (query,
                                                        DateHelper.genDate (dateDifference,
                                                                            ISO_LOCAL_DATE_TIME,
                                                                            pstZoneId),
                                                        spec_id));
        assertEquals (updateCount, 1);
        workList.refresh ();
        assertEquals (workList.getStabilizationWindow (orderNum).color, stabilityWindow.rgba);

        if (dateDifference > -45) {
            assertEquals (workList.getStabilizationWindow (orderNum).text,
                          format ("Streck (Plasma) - %s days left", 45 + dateDifference));
        } else {
            assertEquals (workList.getStabilizationWindow (orderNum).text,
                          format ("Streck (Plasma) - Expired %s days ago", Math.abs (45 + dateDifference)));
        }

        testLog ("Order Details: " + workList.getStabilizationWindow (orderNum).text + ", Styling: " + stabilityWindow);
    }

    private void updateCollectionDateAndVerifyBloodStabilityWindow (int dateDifference,
                                                                    StabilityStatus stabilityWindow, String spec_id,
                                                                    String orderNum) {
        String query = "UPDATE cora.specimens SET collection_date = '%s' where specimen_number = '%s';";
        int updateCount = coraDb.executeUpdate (format (query,
                                                        DateHelper.genDate (dateDifference,
                                                                            ISO_LOCAL_DATE_TIME,
                                                                            pstZoneId),
                                                        spec_id));
        assertEquals (updateCount, 1);
        workList.refresh ();
        assertEquals (workList.getStabilizationWindow (orderNum).color, stabilityWindow.rgba);

        if (dateDifference > -7) {
            assertEquals (workList.getStabilizationWindow (orderNum).text,
                          format ("Streck (Blood) - %s days left", 7 + dateDifference));
        } else {
            assertEquals (workList.getStabilizationWindow (orderNum).text,
                          format ("Streck (Blood) - Expired %s days ago", Math.abs (7 + dateDifference)));
        }

        testLog ("Order Details: " + workList.getStabilizationWindow (orderNum).text + ", Styling: " + stabilityWindow);
    }
}
