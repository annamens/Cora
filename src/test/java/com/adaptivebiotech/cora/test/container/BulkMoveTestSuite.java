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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private Login                    login                  = new Login ();
    private OrdersList               ordersList             = new OrdersList ();
    private ContainerList            containerList          = new ContainerList ();
    private Detail                   detail                 = new Detail ();
    private History                  history                = new History ();
    private final Container          invalidFreezer         = freezerAB018056;
    private final Container          catchAllFreezer        = freezerAB018018;
    private ThreadLocal <Containers> containersToDeactivate = new ThreadLocal <> ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.clickContainers ();
    }

    @AfterMethod (alwaysRun = true)
    public void afterMethod () {
        coraApi.deactivateContainers (containersToDeactivate.get ());
    }

    /**
     * @sdlc.requirements SR-3229:R1, SR-3229:R2, SR-3229:R4
     */
    public void bulkMoveUI () {
        Containers containers = setupTwoPlates ();
        containersToDeactivate.set (containers);
        List <String> allContainerIDs = getContainerIDs (containers);
        containerList.searchContainerIdsOrNames (allContainerIDs);
        containerList.clickBulkMoveContainers ();
        assertFalse (containerList.scanFieldDisplayed ());
        containerList.selectContainerToBulkMove (allContainerIDs.get (0));
        assertTrue (containerList.rowIsSelected (allContainerIDs.get (0)));
        assertFalse (containerList.rowIsSelected (allContainerIDs.get (1)));
        containerList.selectContainerToBulkMove (allContainerIDs.get (1));
        assertTrue (containerList.rowIsSelected (allContainerIDs.get (0)));
        assertTrue (containerList.rowIsSelected (allContainerIDs.get (1)));
        testLog ("SR-3229:R1: Clicking bulk move button enabled container selection");
        List <String> expectedActions = asList (BulkMoveAction.BulkMoveToMyCustody.text,
                                                BulkMoveAction.BulkMoveToFreezer.text);
        assertEquals (containerList.getBulkMoveActions (), expectedActions);
        testLog (format ("SR-3229:R2: Containers list page contained actions: %1$s, %2$s",
                         expectedActions.get (0),
                         expectedActions.get (1)));
        containerList.selectBulkMoveAction (BulkMoveAction.BulkMoveToMyCustody);
        assertFalse (containerList.isFreezerDropdownEnabled ());
        testLog ("SR-3229:R4: Bulk Move to My Custody option did not allow user to select freezer");
        containerList.selectBulkMoveAction (BulkMoveAction.BulkMoveToFreezer);
        assertTrue (containerList.isFreezerDropdownEnabled ());
        testLog ("SR-3229:R4: Bulk Move to Freezer option allowed user to select freezer");
    }

    /**
     * @sdlc.requirements SR-3229:R2, SR-3229:R3, SR-3229:R4, SR-3229:R6, SR-3229:R7, SR-3229:R11
     */
    public void happyPath () {
        Containers containers = setupTwoPlates ();
        containersToDeactivate.set (containers);
        List <String> allContainerIDs = getContainerIDs (containers);
        containerList.searchContainerIdsOrNames (allContainerIDs);
        Containers parsedContainers = containerList.getContainers ();
        String moveToFreezerComment = randomWords (10);
        containerList.bulkMoveAllToFreezer (catchAllFreezer, moveToFreezerComment);
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        verifyMoveSuccessMessage (parsedContainers);
        testLog ("SR-3229:R2: User was able to add custom comment to the Bulk Move to Freezer action");
        testLog ("SR-3229:R4: User was able to add a destination freezer to the Bulk Move to Freezer action");
        for (Container container : containers.list) {
            Container parsedContainer = parsedContainers.list.stream ()
                                                             .filter (c -> container.containerNumber.equals (c.containerNumber))
                                                             .findFirst ().get ();
            verifyMoveToFreezer (parsedContainer, catchAllFreezer, moveToFreezerComment);
            testLog (format ("SR-3229:R11: Bulk Move to Freezer action moved selected item, %s, to destination freezer",
                             container.containerNumber));
            testLog (format ("SR-3229:R2: Comment for %1$s matched expected: %2$s",
                             container.containerNumber,
                             moveToFreezerComment));
            testLog (format ("SR-3229:R7: Containers list location matched location in container details for %s",
                             container.containerNumber));
        }
        history.clickContainers ();
        containerList.searchContainerIdsOrNames (allContainerIDs);
        String moveToCustodyComment = randomWords (10);
        containerList.bulkMoveAllToCustody (moveToCustodyComment);
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R2: User was able to add custom comment to Bulk Move to My Custody action");
        for (Container container : containers.list) {
            Container parsedContainer = parsedContainers.list.stream ()
                                                             .filter (c -> container.containerNumber.equals (c.containerNumber))
                                                             .findFirst ().get ();
            verifyMoveToCustody (parsedContainer, moveToCustodyComment);
            testLog (format ("SR-3229:R3: Bulk Move to My Custody action moved selected item, %s, to user's custody",
                             container.containerNumber));
            testLog (format ("SR-3229:R2: Comment for %1$s matched expected: %2$s",
                             container.containerNumber,
                             moveToCustodyComment));
            testLog (format ("SR-3229:R7: Containers list location matched location in container details for %s",
                             container.containerNumber));
        }
    }

    private void verifyMoveSuccessMessage (Containers expectedContainers) {
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R6: User was presented with a success message after bulk move completion");
        containerList.clickSuccessMessageLink ();
        List <String> windows = new ArrayList <> (getDriver ().getWindowHandles ());
        getDriver ().switchTo ().window (windows.get (1));
        containerList.isCorrectPage ();
        Set <String> parsedContainerIDs = new HashSet <> (getContainerIDs (containerList.getContainers ()));
        Set <String> expectedContainerIDs = new HashSet <> (getContainerIDs (expectedContainers));
        assertEquals (parsedContainerIDs, expectedContainerIDs);
        testLog ("SR-3229:R6: Clicking the success message link displayed container list filtered to the containers moved");
        getDriver ().switchTo ().window (windows.get (0));
    }

    private void verifyMoveToFreezer (Container containerFromList, Container expectedFreezer) {
        verifyMoveToFreezer (containerFromList, expectedFreezer, null);
    }

    private void verifyMoveToFreezer (Container containerFromList,
                                      Container expectedFreezer,
                                      String expectedComment) {
        ordersList.gotoContainerDetail (containerFromList);
        detail.isCorrectPage ();
        Container containerFromDetails = detail.parsePrimaryDetail ();
        assertEquals (containerFromDetails.location, containerFromList.location);
        assertTrue (containerFromDetails.location.contains (expectedFreezer.name));
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        containerFromList.comment = expectedComment;
        containerFromList.name = containerFromList.name.isEmpty () ? null : containerFromList.name;
        containerFromList.depleted = containerFromList.depleted == null ? false : containerFromList.depleted;
        verifyMovedToContainer (histories.get (0), containerFromList);
        verifyDetails (containerFromDetails, containerFromList);
    }

    private void verifyMoveToCustody (Container containerFromList) {
        verifyMoveToCustody (containerFromList, null);
    }

    private void verifyMoveToCustody (Container containerFromList,
                                      String expectedComment) {
        ordersList.gotoContainerDetail (containerFromList);
        detail.isCorrectPage ();
        Container containerFromDetails = detail.parsePrimaryDetail ();
        assertEquals (containerFromDetails.location, containerFromList.location);
        String expectedLocation = String.join (" : ",
                                               coraTestUser,
                                               containerFromList.containerNumber);
        assertEquals (containerFromDetails.location, expectedLocation);
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        containerFromList.comment = expectedComment;
        containerFromList.name = containerFromList.name.isEmpty () ? null : containerFromList.name;
        containerFromList.depleted = containerFromList.depleted == null ? false : containerFromList.depleted;
        verifyTookCustody (histories.get (0), containerFromList);
        verifyDetails (containerFromDetails, containerFromList);
    }

    public void moveAllContainerTypes () {
        Containers containers = setupAllMoveableContainerTypes ();
        containersToDeactivate.set (containers);
        List <String> allContainerIDs = getContainerIDs (containers);
        containerList.searchContainerIdsOrNames (allContainerIDs);
        Containers parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToFreezer (catchAllFreezer);
        assertTrue (containerList.isBulkMoveSuccessMessageDisplayed ());
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        for (Container container : containers.list) {
            Container parsedContainer = parsedContainers.list.stream ()
                                                             .filter (c -> container.containerNumber.equals (c.containerNumber))
                                                             .findFirst ().get ();
            verifyMoveToFreezer (parsedContainer, catchAllFreezer);
        }
        history.clickContainers ();
        containerList.searchContainerIdsOrNames (allContainerIDs);
        parsedContainers = containerList.getContainers ();
        containerList.bulkMoveAllToCustody ();
        parsedContainers = waitForUpdatedContainers (parsedContainers);
        for (Container container : containers.list) {
            Container parsedContainer = parsedContainers.list.stream ()
                                                             .filter (c -> container.containerNumber.equals (c.containerNumber))
                                                             .findFirst ().get ();
            verifyMoveToCustody (parsedContainer);
        }
    }

    /**
     * @sdlc.requirements SR-3229:R5
     */
    public void moveAllContainersToInvalidFreezer () {
        Containers containers = setupAllMoveableContainerTypes ();
        containersToDeactivate.set (containers);
        List <String> allContainerIDs = getContainerIDs (containers);
        containerList.searchContainerIdsOrNames (allContainerIDs);
        Containers expectedContainersFailed = new Containers (containers.list.stream ()
                                                                             .filter (container -> container.containerType.name ()
                                                                                                                          .equals (ContainerType.MatrixTube.name ()) || container.containerType.equals (ContainerType.ConicalBox6x6))
                                                                             .collect (Collectors.toList ()));
        containerList.bulkMoveAllToFreezer (invalidFreezer);
        verifyMoveErrorMessage (expectedContainersFailed);
        for (Container container : containers.list) {
            verifyStillInCustody (container);
        }
        testLog ("SR-3229:R5: Moving to freezer without sufficient space resulted in an error message containing all failed containers. No containers were moved");
    }

    private void verifyMoveErrorMessage (Containers expectedContainersFailed) {
        assertTrue (containerList.isBulkMoveErrorMessageDisplayed ());
        String errorMessage = containerList.getBulkMoveErrorMessage ();
        assertTrue (errorMessage.contains ("Failed to move containers"));
        assertTrue (expectedContainersFailed.list.stream ()
                                                 .allMatch (Container -> errorMessage.contains (Container.containerNumber)));
    }

    private void verifyStillInCustody (Container container) {
        ordersList.gotoContainerDetail (container);
        detail.isCorrectPage ();
        Container containerFromDetails = detail.parsePrimaryDetail ();
        assertEquals (containerFromDetails.location, String.join (" : ", coraTestUser, container.containerNumber));
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 1);
    }

    private Containers setupTwoPlates () {
        Containers containers = coraApi.addContainers (new Containers (
                asList (container (ContainerType.Plate), container (ContainerType.Plate))));
        Logging.info ("created containers: " + Arrays.toString (getContainerIDs (containers).toArray ()));
        return containers;
    }

    private Containers setupAllMoveableContainerTypes () {
        Containers containers = coraApi.addContainers (new Containers (
                stream (ContainerType.values ())
                                                .map (t -> container (t))
                                                .filter (t -> !t.containerType.equals (ContainerType.Freezer))
                                                .collect (toList ())));
        Logging.info ("created containers: " + Arrays.toString (getContainerIDs (containers).toArray ()));
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

    private List <String> getContainerIDs (Containers containers) {
        return containers.list.stream ().map (container -> container.containerNumber)
                              .collect (Collectors.toList ());
    }
}
