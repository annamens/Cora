package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
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

@Test (groups = "regression")
public class BulkMoveTestSuite extends ContainerTestBase {

    private Login         login         = new Login ();
    private OrdersList    ordersList    = new OrdersList ();
    private ContainerList containerList = new ContainerList ();
    private Detail        detail        = new Detail ();
    private History       history       = new History ();
    private Containers    containers;

    @BeforeSuite
    public void beforeSuite () {
        coraApi.login ();
        containers = setupContainers ();
    }

    @AfterSuite
    public void afterSuite () {
        coraApi.deactivateContainers (containers);
    }

    @BeforeMethod
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.clickContainers ();
    }

    /**
     * @sdlc.requirements SR-3229:R1, SR-3229:R2
     */
    public void bulkMoveUI () {
        List <String> containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                        .collect (Collectors.toList ());
        containerList.searchContainerIdOrName (containerNumbers.stream ().collect (Collectors.joining (",")));
        containerList.clickBulkMoveContainers ();
        assertFalse (containerList.scanFieldDisplayed ());
        containerList.selectContainerToBulkMove (containerNumbers.get (0));
        containerList.selectContainerToBulkMove (containerNumbers.get (1));
        containerList.rowIsSelected (containerNumbers.get (0));
        containerList.rowIsSelected (containerNumbers.get (1));
        testLog ("SR-3229:R1: clicking bulk move button enables container selection");
        List <String> expectedActions = asList (BulkMoveAction.BulkMoveToMyCustody.text,
                                                BulkMoveAction.BulkMoveToFreezer.text);
        assertEquals (containerList.getBulkMoveActions (), expectedActions);
        testLog (format ("SR-3229:R2: containers list page contains actions: %1$s, %2$s",
                         expectedActions.get (0),
                         expectedActions.get (1)));
        containerList.selectBulkMoveAction (BulkMoveAction.BulkMoveToMyCustody);
        assertFalse (containerList.isFreezerDropdownEnabled ());
        containerList.selectBulkMoveAction (BulkMoveAction.BulkMoveToFreezer);
        assertTrue (containerList.isFreezerDropdownEnabled ());
    }

    /**
     * @sdlc.requirements SR-3229:R2, SR-3229:R3, SR-3229:R4, SR-3229:R6, SR-3229:R7, SR-3229:R11
     */
    public void happyPath () {
        String containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        containerList.searchContainerIdOrName (containerNumbers);
        Container targetFreezer = freezerAB018078;
        String moveToFreezerComment = randomWords (10);
        containerList.bulkMoveAllToFreezer (targetFreezer, moveToFreezerComment);
        testLog ("SR-3229:R2: user is able to add custom comment to the Bulk Move to Freezer action");
        testLog ("SR-3229:R4: user is able to add a destination freezer to the Bulk Move to Freezer action");
        assertTrue (containerList.bulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R6: user was presented with a success message after bulk move completion");
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
        // verify each container has expected history and details
        Containers parsedContainers = containerList.getContainers ();
        for (int i = 0; i < containers.list.size (); i++) {
            Container container = containers.list.get (i);
            Container parsedContainer = parsedContainers.list.get (i);
            ordersList.gotoContainerDetail (container);
            detail.isCorrectPage ();
            Container actual = detail.parsePrimaryDetail ();
            actual.comment = moveToFreezerComment;
            String parsedLocation = parsedContainer.location;
            assertEquals (actual.location, parsedLocation);
            assertNotEquals (actual.location, coraTestUser);
            testLog (format ("SR-3229:R7: containers list location matches location in container details for %s",
                             container.containerNumber));
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
            testLog (format ("SR-3229:R11: Bulk Move to Freezer action moves selected item, %s, to destination freezer",
                             container.containerNumber));
        }
        ordersList.clickContainers ();
        containerList.searchContainerIdOrName (containerNumbers);
        String moveToCustodyComment = randomWords (10);
        containerList.bulkMoveAllToCustody (moveToCustodyComment);
        testLog ("SR-3229:R2: user is able to add custom comment to Bulk Move to Freezer action");
        // verify each container has expected history and details
        parsedContainers = containerList.getContainers ();
        for (int i = 0; i < containers.list.size (); i++) {
            Container container = containers.list.get (i);
            Container parsedContainer = parsedContainers.list.get (i);
            ordersList.gotoContainerDetail (container);
            detail.isCorrectPage ();
            Container actual = detail.parsePrimaryDetail ();
            actual.comment = moveToCustodyComment;
            String parsedLocation = parsedContainer.location;
            assertEquals (actual.location, parsedLocation);
            assertNotEquals (actual.location, coraTestUser);
            testLog (format ("SR-3229:R7: containers list location matches location in container details for %s",
                             container.containerNumber));
            detail.gotoHistory ();
            history.isCorrectPage ();
            List <ContainerHistory> histories = history.getHistories ();
            assertEquals (histories.size (), 3);
            verifyTookCustody (histories.get (0), actual);
            container.location = String.join (" : ",
                                              histories.get (0).location,
                                              container.containerNumber);
            verifyDetails (actual, container);
            testLog (format ("SR-3229:R3: Bulk Move to My Custody action moves selected item, %s, to user's custody",
                             container.containerNumber));
        }
    }

    private Containers setupContainers () {
        Containers containers = coraApi.addContainers (new Containers (
                asList (container (ContainerType.Plate), container (ContainerType.Plate))));
        String containerNumbers = containers.list.stream ().map (container -> container.containerNumber)
                                                 .collect (Collectors.joining (","));
        Logging.info ("created containers: " + containerNumbers);
        return containers;
    }

}
