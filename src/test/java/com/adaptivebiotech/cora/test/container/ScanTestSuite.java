/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Slide;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.SlideBox5;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import org.apache.commons.lang3.SerializationUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainersList;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.BatchAccession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

@Test (groups = "regression")
public class ScanTestSuite extends ContainerTestBase {

    private final String error1     = "Cannot find container %s";
    private final String error2     = "Cannot uniquely identify container %s";
    private final String error3     = "Cannot take custody of container as shipment %s is in arrived state.";
    private final String error4     = "Cannot take custody of container %s";
    private final String error5     = "Container %s is not in your custody. Please take custody of it first";
    private final String error6     = "Container %s is not a holding container. Choose another container.";
    private final String error7     = "No space available in container %s. Choose another container.";
    private final String error8     = "%s failed verification. Unknown location";
    private final String error9     = "Container %s is already in %s";
    private final String error10    = "%s failed verification. Current location is:\n%s";
    private final String scanTest   = "CO-" + System.nanoTime ();
    private Login        login      = new Login ();
    private OrdersList   ordersList = new OrdersList ();
    private MyCustody    myCustody  = new MyCustody ();
    private NewShipment  shipment   = new NewShipment ();
    private Containers   mytestContainers;
    private Containers   shipContainers;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        mytestContainers = coraApi.addContainers (new Containers (asList (container (Slide), container (SlideBox5))));

        // setup for shipment is in arrived state
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.selectNewDiagnosticShipment ();

        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.selectDiagnosticSpecimenContainerType (Tube);
        shipment.clickAddContainer ();
        shipment.setContainerName (1, scanTest);
        shipment.setContainerName (2, scanTest);
        shipment.clickSave ();
        shipContainers = shipment.getPrimaryContainers (Tube);
        shipment.clickSignOut ();
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    @AfterClass (alwaysRun = true)
    public void afterClass () {
        coraApi.deactivateContainers (mytestContainers);
        coraApi.deactivateContainers (shipContainers);
    }

    /**
     * @sdlc.requirements 126.ContainersListValidScan
     */
    public void inventory_list_view () {
        ordersList.clickContainers ();

        // test: container doesn't exist
        ContainersList list = new ContainersList ();
        list.scan ("xxxxxx");
        assertEquals (list.getScanError (), format (error1, "xxxxxx"));

        // test: container name isn't unique
        list.scan (scanTest);
        assertEquals (list.getScanError (), format (error2, scanTest));

        // test: container's shipment is in Arrived state
        Container testContainer = shipContainers.list.get (0);
        list.scan (testContainer);
        assertEquals (list.getScanError (), format (error3, testContainer.shipmentNumber));

        // test: container is a freezer
        list.scan (freezerDestroyed);
        assertEquals (list.getScanError (), format (error4, freezerDestroyed.containerNumber));
    }

    /**
     * @sdlc.requirements 126.ContainersListValidScan
     */
    public void my_custody_view () {
        ordersList.gotoMyCustody ();
        myCustody.isCorrectPage ();

        // test: container doesn't exist
        myCustody.scan ("xxxxxx");
        assertEquals (myCustody.getScanError (), format (error1, "xxxxxx"));

        // test: container name isn't unique
        myCustody.scan (scanTest);
        assertEquals (myCustody.getScanError (), format (error2, scanTest));

        // test: container's shipment is in Arrived state
        Container testContainer = shipContainers.list.get (1);
        myCustody.scan (testContainer);
        assertEquals (myCustody.getScanError (), format (error3, testContainer.shipmentNumber));

        // test: container is a freezer
        myCustody.scan (freezerDestroyed);
        assertEquals (myCustody.getScanError (), format (error4, freezerDestroyed.containerNumber));
    }

    /**
     * @sdlc.requirements 126.TransformHoldingContainer
     */
    public void holding_container () {
        Container child = SerializationUtils.clone (mytestContainers.list.get (0));
        Container holding = SerializationUtils.clone (mytestContainers.list.get (1));

        // setup for a full holding container
        ordersList.selectNewBatchShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (Ambient);
        shipment.clickSave ();
        shipment.clickAccessionTab ();

        BatchAccession accession = new BatchAccession ();
        accession.isCorrectPage ();
        accession.uploadIntakeManifest (getSystemResource ("batch/intakemanifest_full_slidebox.xlsx").getPath ());
        accession.clickIntakeComplete ();
        accession.clickShipmentTab ();
        Containers containers = shipment.getBatchContainers ();

        // test: container doesn't exist
        ordersList.gotoMyCustody ();
        myCustody.isCorrectPage ();
        myCustody.scanAndClickHoldingContainer (child);
        myCustody.chooseHoldingContainer ("xxxxxx");
        assertEquals (myCustody.getScanError (), format (error1, "xxxxxx"));

        // test: container name isn't unique
        myCustody.chooseHoldingContainer (scanTest);
        assertEquals (myCustody.getScanError (), format (error2, scanTest));

        // test: container's shipment is in Arrived state (not in My Custody)
        Container testContainer = shipContainers.list.get (1);
        myCustody.chooseHoldingContainer (testContainer);
        assertEquals (myCustody.getScanError (), format (error5, testContainer.containerNumber));

        // test: container is a freezer
        myCustody.chooseHoldingContainer (freezerDestroyed);
        assertEquals (myCustody.getScanError (), format (error5, freezerDestroyed.containerNumber));

        // test: is not a holding container
        myCustody.chooseHoldingContainer (child);
        assertEquals (myCustody.getScanError (), format (error6, child.containerNumber));

        // test: no space available
        testContainer = containers.list.get (0);
        myCustody.chooseHoldingContainer (testContainer);
        assertEquals (myCustody.getScanError (), format (error7, testContainer.containerNumber));

        // test: happy path
        myCustody.finalizeHoldingContainer (child, holding);

        // test: is already set to the holding container
        myCustody.setHoldingContainerTest (child, holding);
        assertEquals (myCustody.getScanError2 (), format (error9, child.containerNumber, holding.containerNumber));

        // test: container doesn't exist
        myCustody.closePopup ();
        myCustody.scan (holding);
        myCustody.scanToVerify ("xxxxxx");
        assertEquals (myCustody.getScanVerifyError (), format (error8, "xxxxxx"));

        // test: happy path
        myCustody.scanToVerify (child);
        myCustody.isVerified (child);
        assertEquals (myCustody.getScanVerifyText (), format ("1 of %d verified", holding.children.size ()));

        // test: container is a freezer
        myCustody.scanToVerify (freezerDestroyed);
        assertEquals (myCustody.getScanVerifyError (),
                      format (error10, freezerDestroyed.containerNumber, freezerDestroyed.name));

        myCustody.closePopup ();
        myCustody.sendContainersToFreezer (containers, freezerDestroyed);
    }
}
