package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.container.ContainerList.BulkMoveAction;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.test.utils.Logging;
import com.seleniumfy.test.utils.Timeout;

@Test (groups = "regression")
public class BulkMoveTestSuite extends ContainerTestBase {

    private Login           login                = new Login ();
    private OrdersList      ordersList           = new OrdersList ();
    private ContainerList   containerList        = new ContainerList ();
    private Detail          detail               = new Detail ();
    private History         history              = new History ();
    private Containers      containers;
    private final Container targetFreezer        = freezerAB018078;
    private final Container catchAllFreezer      = dumbwaiter;
    private final Container platesRacksFreezer   = freezerAB018016;
    private final Container noPlatesRacksFreezer = freezerAB039003;
    private final String    noCapacityError      = "Failed to move containers to Freezer %1$s. Unable to find a location for container of type: %2$s.";

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.clickContainers ();
    }

    @AfterMethod (alwaysRun = true)
    public void afterMethod () {
        coraApi.deactivateContainers (containers);
    }

    /**
     * @sdlc.requirements SR-3229:R1, SR-3229:R2
     */
    public void bulkMoveUI () {
        containers = setupContainers ();
        List <String> containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                        .collect (Collectors.toList ());
        containerList.searchContainerIdOrName (containerNumbers.stream ().collect (Collectors.joining (",")));
        containerList.clickBulkMoveContainers ();
        assertFalse (containerList.scanFieldDisplayed ());
        containerList.selectContainerToBulkMove (containerNumbers.get (0));
        containerList.selectContainerToBulkMove (containerNumbers.get (1));
        containerList.rowIsSelected (containerNumbers.get (0));
        containerList.rowIsSelected (containerNumbers.get (1));
        testLog ("SR-3229:R1: Clicking bulk move button enables container selection");
        List <String> expectedActions = asList (BulkMoveAction.BulkMoveToMyCustody.text,
                                                BulkMoveAction.BulkMoveToFreezer.text);
        assertEquals (containerList.getBulkMoveActions (), expectedActions);
        testLog (format ("SR-3229:R2: Containers list page contains actions: %1$s, %2$s",
                         expectedActions.get (0),
                         expectedActions.get (1)));
        containerList.selectBulkMoveAction (BulkMoveAction.BulkMoveToMyCustody);
        assertFalse (containerList.isFreezerDropdownEnabled ());
        containerList.selectBulkMoveAction (BulkMoveAction.BulkMoveToFreezer);
        assertTrue (containerList.isFreezerDropdownEnabled ());
    }

    /**
     * @sdlc.requirements SR-3229:R2, SR-3229:R4, SR-3229:R6, SR-3229:R7, SR-3229:R11
     */
    public void bulkMoveToFreezer () {
        containers = setupContainers ();
        String containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        containerList.searchContainerIdOrName (containerNumbers);
        String moveToFreezerComment = randomWords (10);
        Containers parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToFreezer (targetFreezer, moveToFreezerComment);
        testLog ("SR-3229:R2: User is able to add custom comment to the Bulk Move to Freezer action");
        testLog ("SR-3229:R4: User is able to add a destination freezer to the Bulk Move to Freezer action");
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R6: User is presented with a success message after bulk move completion");
        /*
         * disabled for now due to bug SR-8664
         * 
         * containerList.clickSuccessMessageLink ();
         * List <String> windows = new ArrayList <> (getDriver ().getWindowHandles ());
         * getDriver ().switchTo ().window (windows.get (1));
         * String expectedUrl = coraTestUrl + format ("/cora/containers/list?searchText=%s",
         * containerNumbers);
         * assertEquals (getCurrentUrl (), expectedUrl);
         * testLog("R6: clicking the success message link displays container list filtered to the containers moved"
         * );
         * getDriver ().switchTo ().window (windows.get (0));
         */
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        for (int i = 0; i < containers.list.size (); i++) {
            Container container = containers.list.get (i);
            verifyMoveToFreezer (container, targetFreezer, moveToFreezerComment);
            testLog (format ("SR-3229:R11: Bulk Move to Freezer action moves selected item, %s, to destination freezer",
                             container.containerNumber));
            assertEquals (container.location, parsedContainers.list.get (i).location);
            testLog (format ("SR-3229:R7: Containers list location matches location in container details for %s",
                             container.containerNumber));
        }
    }

    private void verifyMoveToFreezer (Container container, Container freezer, String comment) {
        ordersList.gotoContainerDetail (container);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        actual.comment = comment;
        assertTrue (actual.location.startsWith (freezer.name));
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedToContainer (histories.get (0), actual);
        verifyTookCustody (histories.get (1));
        container.location = String.join (" : ",
                                          histories.get (0).location,
                                          container.containerNumber);
        verifyDetails (actual, container);
    }

    public void allContainerTypesToFreezer () {
        containers = setupAllContainerTypes ();
        String allContainerNumbers = containers.list.stream ()
                                                    .map (container -> container.containerNumber)
                                                    .collect (Collectors.joining (","));
        String containerNumbers = containers.list.stream ()
                                                 .filter (container -> !container.containerType.equals (ContainerType.MatrixTube))
                                                 .map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        String matrixTubeContainer = containers.list.stream ()
                                                    .filter (container -> container.containerType.equals (ContainerType.MatrixTube))
                                                    .map (container -> container.containerNumber).findFirst ().get ();
        containerList.searchContainerIdOrName (containerNumbers);
        Containers parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToFreezer (catchAllFreezer, null);
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        containerList.searchContainerIdOrName (matrixTubeContainer);
        parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToFreezer (freezerAB018055, null);
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        containerList.searchContainerIdOrName (allContainerNumbers);
        parsedContainers = containerList.getContainers ();
        for (int i = 0; i < containers.list.size (); i++) {
            Container container = containers.list.get (i);
            Container freezer;
            switch (container.containerType) {
            case MatrixTube:
                freezer = freezerAB018055;
                break;
            default:
                freezer = dumbwaiter;
                break;
            }
            verifyMoveToFreezer (container, freezer, null);
            Container parsedContainer = parsedContainers.list.stream ()
                                                             .filter (c -> container.containerNumber.equals (c.containerNumber))
                                                             .findFirst ().get ();
            assertEquals (container.location, parsedContainer.location);
        }
    }

    public void allContainerTypesToCustody () {
        containers = setupAllContainerTypes ();
        String allContainerNumbers = containers.list.stream ()
                                                    .map (container -> container.containerNumber)
                                                    .collect (Collectors.joining (","));
        String containerNumbers = containers.list.stream ()
                                                 .filter (container -> !container.containerType.equals (ContainerType.MatrixTube))
                                                 .map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        String matrixTubeContainer = containers.list.stream ()
                                                    .filter (container -> container.containerType.equals (ContainerType.MatrixTube))
                                                    .map (container -> container.containerNumber).findFirst ().get ();
        containerList.searchContainerIdOrName (containerNumbers);
        Containers parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToFreezer (catchAllFreezer, null);
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        containerList.searchContainerIdOrName (matrixTubeContainer);
        parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToFreezer (freezerAB018055, null);
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        containerList.searchContainerIdOrName (allContainerNumbers);
        parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToCustody (null);
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        for (int i = 0; i < containers.list.size (); i++) {
            Container container = containers.list.get (i);
            Container parsedContainer = parsedContainers.list.stream ()
                                                             .filter (c -> container.containerNumber.equals (c.containerNumber))
                                                             .findFirst ().get ();
            verifyMoveToCustody (container, null);
            assertEquals (container.location, parsedContainer.location);
        }
    }

    public void allContainersToInvalidFreezers () {
        containers = setupAllContainerTypes ();
        String allContainerNumbers = containers.list.stream ()
                                                    .map (container -> container.containerNumber)
                                                    .collect (Collectors.joining (","));
        String nonPlatesRacks = containers.list.stream ()
                                               .filter (container -> !container.containerType.equals (ContainerType.Plate) ^ !container.containerType.equals (ContainerType.MatrixRack) ^ !container.containerType.equals (ContainerType.MatrixRack4x6))
                                               .map (container -> container.containerNumber)
                                               .collect (Collectors.joining (","));
        String platesRacks = containers.list.stream ()
                                            .filter (container -> container.containerType.equals (ContainerType.Plate) || container.containerType.equals (ContainerType.MatrixRack) || container.containerType.equals (ContainerType.MatrixRack4x6))
                                            .map (container -> container.containerNumber)
                                            .collect (Collectors.joining (","));
        containerList.searchContainerIdOrName (allContainerNumbers);
        containerList.bulkMoveAllToFreezer (platesRacksFreezer, null);
        // failure expected. need to check error message
        for (Container container : containers.list) {
            verifyNoMove (container); // is this overkill?
        }
        containerList.searchContainerIdOrName (nonPlatesRacks);
        containerList.bulkMoveAllToFreezer (platesRacksFreezer, null);
        // failure expected. need to check error message
        containerList.searchContainerIdOrName (platesRacks);
        containerList.bulkMoveAllToFreezer (noPlatesRacksFreezer, null);
        // failure expected. need to check error message
        for (Container container : containers.list) {
            verifyNoMove (container); // is this overkill?
        }

    }

    private void verifyNoMove (Container container) {
        ordersList.gotoContainerDetail (container);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        assertEquals (actual.location, String.join (" : ", coraTestUser, container.containerNumber));
        // verify no new history
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 1);
    }

    /**
     * @sdlc.requirements SR-3229:R2, SR-3229:R3, SR-3229:R7
     */
    public void bulkMoveToMyCustody () {
        containers = setupContainers ();
        String containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        containerList.searchContainerIdOrName (containerNumbers);
        String moveToCustodyComment = randomWords (10);
        Containers parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToFreezer (targetFreezer, null);
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        // do a separate move for matrix tube to AB018014
        containerList.bulkMoveAllToCustody (moveToCustodyComment);
        testLog ("SR-3229:R2: User is able to add custom comment to Bulk Move to My Custody action");
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        for (int i = 0; i < containers.list.size (); i++) {
            Container container = containers.list.get (i);
            verifyMoveToCustody (container, moveToCustodyComment);
            testLog (format ("SR-3229:R3: Bulk Move to My Custody action moves selected item, %s, to user's custody",
                             container.containerNumber));
            assertEquals (container.location, parsedContainers.list.get (i).location);
            testLog (format ("SR-3229:R7: Containers list location matches location in container details for %s",
                             container.containerNumber));
        }
    }

    private void verifyMoveToCustody (Container container, String comment) {
        ordersList.gotoContainerDetail (container);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        actual.comment = comment;
        assertEquals (actual.location, String.join (" : ", coraTestUser, container.containerNumber));
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 3);
        verifyTookCustody (histories.get (0), actual);
        container.location = String.join (" : ",
                                          histories.get (0).location,
                                          container.containerNumber);
        verifyDetails (actual, container);
    }

    private Containers setupContainers () {
        Containers containers = coraApi.addContainers (new Containers (
                asList (container (ContainerType.Plate), container (ContainerType.Plate))));
        String containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        Logging.info ("created containers: " + containerNumbers);
        return containers;
    }

    private Containers setupAllContainerTypes () {
        Containers containers = coraApi.addContainers (new Containers (
                stream (ContainerType.values ())
                                                .map (t -> container (t))
                                                .filter (t -> !t.containerType.equals (ContainerType.Freezer))
                                                .collect (toList ())));
        String containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        Logging.info ("created containers: " + containerNumbers);
        return containers;
    }

    private Containers waitForUpdatedContainers (Containers previousContainers) {
        Containers parsedContainers = null;
        Timeout timer = new Timeout (millisRetry, waitRetry);
        while (!timer.Timedout ()) {
            parsedContainers = containerList.getContainers ();
            String parsedLocation = parsedContainers.list.get (0).location;
            String previousLocation = previousContainers.list.get (0).location;
            if (!parsedLocation.equals (previousLocation)) {
                return parsedContainers;
            }
            timer.Wait ();
        }
        fail ("No container update detected");
        return null;
    }
}
