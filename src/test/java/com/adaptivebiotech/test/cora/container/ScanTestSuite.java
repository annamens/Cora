package com.adaptivebiotech.test.cora.container;

import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.ui.cora.CoraPage;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.*;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

@Test (groups = {"container", "regression" })
public class ScanTestSuite extends ContainerTestBase {

    private final String error1   = "Cannot find container %s";
    private final String error2   = "Cannot uniquely identify container %s";
    private final String error3   = "Cannot take custody of container as shipment %s is in arrived state.";
    private final String error4   = "Cannot take custody of container %s";
    private final String error5   = "Container %s is not in your custody. Please take custody of it first";
    private final String error6   = "Container %s is not a holding container. Choose another container.";
    private final String error7   = "No space available in container %s. Choose another container.";
    private final String error8   = "%s failed verification. Unknown location";
    private final String error9   = "Container %s is already in %s";
    private final String error10  = "%s failed verification. Current location is:\n%s";
    private final String scanTest = "CO-" + System.nanoTime ();
    private CoraPage     main;
    private MyCustody    my;
    private Shipment     shipment;
    private Container    child1;
    private Container    child2;
    private Container    holding;
    private Containers   containers1;
    private Containers   containers2;

    @BeforeTest
    public void beforeTest () {
        Containers mytestContainers = addContainers (new Containers (
                asList (container (Slide), container (Slide), container (SlideBox5))));
        child1 = mytestContainers.list.get (0);
        child2 = mytestContainers.list.get (1);
        holding = mytestContainers.list.get (2);

        openBrowser ();
        main = new CoraPage ();
        main.doLogin ();

        // setup for shipment is in arrived state
        main.clickNewDiagnosticShipment ();
        shipment = new Shipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterDiagnosticSpecimenContainerType (Tube);
        shipment.clickAddContainer ();
        shipment.setContainerName (1, scanTest);
        shipment.setContainerName (2, scanTest);
        shipment.clickSave ();
        containers1 = shipment.getPrimaryContainers (Tube);
        closeBrowser ();

        my = new MyCustody ();
    }

    /**
     * @sdlc_requirements 126.ContainersListValidScan
     */
    public void InventoryListView () {
        main.gotoContainersList ();

        // test: container doesn't exist
        ContainerList list = new ContainerList ();
        list.scan ("xxxxxx");
        assertEquals (list.getScanError (), format (error1, "xxxxxx"));

        // test: container name isn't unique
        list.scan (scanTest);
        assertEquals (list.getScanError (), format (error2, scanTest));

        // test: container's shipment is in Arrived state
        Container testContainer = containers1.list.get (0);
        list.scan (testContainer);
        assertEquals (list.getScanError (), format (error3, testContainer.shipmentNumber));

        // test: container is a freezer
        list.scan (freezerDestroyed);
        assertEquals (list.getScanError (), format (error4, freezerDestroyed.containerNumber));
    }

    /**
     * @sdlc_requirements 126.ContainersListValidScan
     */
    public void MyCustodyView () {
        main.gotoMyCustody ();
        my.isCorrectPage ();

        // test: container doesn't exist
        my.scan ("xxxxxx");
        assertEquals (my.getScanError (), format (error1, "xxxxxx"));

        // test: container name isn't unique
        my.scan (scanTest);
        assertEquals (my.getScanError (), format (error2, scanTest));

        // test: container's shipment is in Arrived state
        Container testContainer = containers1.list.get (1);
        my.scan (testContainer);
        assertEquals (my.getScanError (), format (error3, testContainer.shipmentNumber));

        // test: container is a freezer
        my.scan (freezerDestroyed);
        assertEquals (my.getScanError (), format (error4, freezerDestroyed.containerNumber));
    }

    /**
     * @sdlc_requirements 126.TransformHoldingContainer
     */
    public void HoldingContainer () {

        // setup for a full holding container
        main.clickNewBatchShipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (Ambient);
        shipment.clickSave ();
        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();
        accession.uploadIntakeManifest ("intakemanifest_full_slidebox.xlsx");
        accession.clickIntakeComplete ();
        accession.gotoShipment ();
        containers2 = shipment.getBatchContainers ();

        // test: container doesn't exist
        main.gotoMyCustody ();
        my.isCorrectPage ();
        my.scanAndClickHoldingContainer (child1);
        my.chooseHoldingContainer ("xxxxxx");
        assertEquals (my.getScanError (), format (error1, "xxxxxx"));

        // test: container name isn't unique
        my.chooseHoldingContainer (scanTest);
        assertEquals (my.getScanError (), format (error2, scanTest));

        // test: container's shipment is in Arrived state (not in My Custody)
        Container testContainer = containers1.list.get (1);
        my.chooseHoldingContainer (testContainer);
        assertEquals (my.getScanError (), format (error5, testContainer.containerNumber));

        // test: container is a freezer
        my.chooseHoldingContainer (freezerDestroyed);
        assertEquals (my.getScanError (), format (error5, freezerDestroyed.containerNumber));

        // test: is not a holding container
        my.chooseHoldingContainer (child1);
        assertEquals (my.getScanError (), format (error6, child1.containerNumber));

        // test: no space available
        testContainer = containers2.list.get (0);
        my.chooseHoldingContainer (testContainer);
        assertEquals (my.getScanError (), format (error7, testContainer.containerNumber));

        // test: happy path
        my.finalizeHoldingContainer (child1, holding);

        // test: is already set to the holding container
        my.setHoldingContainerTest (child1, holding);
        assertEquals (my.getScanError2 (), format (error9, child1.containerNumber, holding.containerNumber));
        my.clickGoBack ();
    }

    /**
     * @sdlc_requirements 126.MoveScanToVerify
     */
    public void ScanToVerify () {

        // setup for a holding container with children
        main.gotoMyCustody ();
        my.isCorrectPage ();
        my.setHoldingContainer (child2, holding);

        // test: container doesn't exist
        my.scan (holding);
        my.scanToVerify ("xxxxxx");
        assertEquals (my.getScanVerifyError (), format (error8, "xxxxxx"));

        // test: happy path
        my.scanToVerify (child2);
        my.isVerified (child2);
        assertEquals (my.getScanVerifyText (), format ("1 of %d verified", holding.children.size ()));

        // test: container is a freezer
        my.scanToVerify (freezerDestroyed);
        assertEquals (my.getScanVerifyError (),
                      format (error10, freezerDestroyed.containerNumber, freezerDestroyed.name));
    }
}
