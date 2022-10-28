/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Freezer;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Plate;
import static com.adaptivebiotech.cora.ui.container.ContainersList.BulkMoveAction.BulkMoveToFreezer;
import static com.adaptivebiotech.cora.ui.container.ContainersList.BulkMoveAction.BulkMoveToMyCustody;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainersList;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = { "regression", "dingo" })
public class BulkMoveTestSuite extends ContainerTestBase {

    private Login                    login                  = new Login ();
    private OrdersList               ordersList             = new OrdersList ();
    private ContainersList           containersList         = new ContainersList ();
    private MyCustody                myCustody              = new MyCustody ();
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
    public void containersListBulkMoveUI () {
        Containers containers = setupTwoPlates ();
        containersToDeactivate.set (containers);
        containersList.searchContainerIdsOrNames (getContainerIDs (containers));
        containersList.clickBulkMoveContainers ();
        assertFalse (containersList.scanFieldDisplayed ());
        verifyContainerSelection (containers);
        List <String> expectedActions = asList (BulkMoveToMyCustody.text, BulkMoveToFreezer.text);
        assertEquals (containersList.getBulkMoveActions (), expectedActions);
        testLog (format ("SR-3229:R2: Containers list page contained actions: %1$s, %2$s",
                         expectedActions.get (0),
                         expectedActions.get (1)));

        containersList.selectBulkMoveAction (BulkMoveToMyCustody);
        assertFalse (containersList.isFreezerDropdownEnabled ());
        testLog ("SR-3229:R4: Bulk Move to My Custody option did not allow user to select freezer");
        containersList.selectBulkMoveAction (BulkMoveToFreezer);
        assertTrue (containersList.isFreezerDropdownEnabled ());
        testLog ("SR-3229:R4: Bulk Move to Freezer option allowed user to select freezer");
    }

    /**
     * @sdlc.requirements SR-3229:R1, SR-3229:R2, SR-3229:R4
     */
    public void myCustodyBulkMoveUI () {
        Containers containers = setupTwoPlates ();
        containersToDeactivate.set (containers);
        myCustody.gotoMyCustody ();
        myCustody.clickBulkMoveContainers ();
        assertFalse (myCustody.scanFieldDisplayed ());
        verifyContainerSelection (containers);
        assertTrue (myCustody.bulkMoveActionDropdownDisabled ());
        String expectedAction = BulkMoveToFreezer.text;
        assertEquals (myCustody.getCurrentBulkMoveAction (), expectedAction);
        testLog (format ("SR-3229:R2: My Custody page contained action: %s", expectedAction));

        assertTrue (myCustody.isFreezerDropdownEnabled ());
        testLog ("SR-3229:R4: Bulk Move to Freezer option allowed user to select freezer");
    }

    /**
     * @sdlc.requirements SR-3229:R2, SR-3229:R3, SR-3229:R4, SR-3229:R6, SR-3229:R7, SR-3229:R11
     */
    public void containersListHappyPath () {
        Containers containers = setupTwoPlates ();
        containersToDeactivate.set (containers);
        List <String> allContainerIDs = getContainerIDs (containers);
        containersList.searchContainerIdsOrNames (allContainerIDs);
        String moveToFreezerComment = randomWords (10);
        containersList.bulkMoveAllToFreezer (catchAllFreezer, moveToFreezerComment);
        containersList.waitForBulkMoveComplete ();
        assertTrue (containersList.isBulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R6: User was presented with a success message after bulk move completion");

        containersList.clickSuccessMessageLink ();
        Containers parsedContainers = containersList.getContainers ();
        verifySuccessMessageLink (parsedContainers);
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
        containersList.searchContainerIdsOrNames (allContainerIDs);
        String moveToCustodyComment = randomWords (10);
        containersList.bulkMoveAllToCustody (moveToCustodyComment);
        containersList.waitForBulkMoveComplete ();
        assertTrue (containersList.isBulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R6: User was presented with a success message after bulk move completion");

        parsedContainers = containersList.getContainers ();
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

    /**
     * @sdlc.requirements SR-3229:R2, SR-3229:R3, SR-3229:R4, SR-3229:R6, SR-3229:R7, SR-3229:R11
     */
    public void myCustodyHappyPath () {
        Containers containers = setupTwoPlates ();
        containersToDeactivate.set (containers);
        myCustody.gotoMyCustody ();
        String moveToFreezerComment = randomWords (10);
        myCustody.bulkMoveToFreezer (containers, catchAllFreezer, moveToFreezerComment);
        myCustody.waitForBulkMoveComplete ();
        assertTrue (myCustody.isBulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R6: User was presented with a success message after bulk move completion");

        myCustody.clickContainers ();
        containersList.isCorrectPage ();
        containersList.searchContainerIdsOrNames (getContainerIDs (containers));
        Containers parsedContainers = containersList.getContainers ();
        assertEquals (getContainerIDs (parsedContainers), getContainerIDs (containers));
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
    }

    public void moveAllContainerTypes () {
        Containers containers = setupAllMoveableContainerTypes ();
        containersToDeactivate.set (containers);
        List <String> allContainerIDs = getContainerIDs (containers);
        containersList.searchContainerIdsOrNames (allContainerIDs);
        containersList.bulkMoveAllToFreezer (catchAllFreezer);
        containersList.waitForBulkMoveComplete ();
        assertTrue (containersList.isBulkMoveSuccessMessageDisplayed ());
        testLog ("SR-3229:R6: User was presented with a success message after bulk move completion");

        Containers parsedContainers = containersList.getContainers ();
        parsedContainers.list.stream ()
                             .forEach (container -> assertTrue (container.location.contains (catchAllFreezer.name)));
        history.clickContainers ();
        containersList.searchContainerIdsOrNames (allContainerIDs);
        containersList.bulkMoveAllToCustody ();
        containersList.waitForBulkMoveComplete ();
        parsedContainers = containersList.getContainers ();
        parsedContainers.list.stream ()
                             .forEach (container -> assertTrue (container.location.equals (join (" : ",
                                                                                                 coraTestUser,
                                                                                                 container.containerNumber))));
    }

    /**
     * @sdlc.requirements SR-3229:R5
     */
    public void moveAllContainersToInvalidFreezer () {
        Containers containers = setupAllMoveableContainerTypes ();
        containersToDeactivate.set (containers);
        List <String> allContainerIDs = getContainerIDs (containers);
        containersList.searchContainerIdsOrNames (allContainerIDs);
        Containers expectedContainersFailed = new Containers (containers.list.stream ()
                                                                             .filter (container -> container.containerType.name ()
                                                                                                                          .equals (ContainerType.MatrixTube.name ()) || container.containerType.equals (ContainerType.ConicalBox6x6))
                                                                             .collect (toList ()));
        containersList.bulkMoveAllToFreezer (invalidFreezer);
        verifyMoveErrorMessage (expectedContainersFailed, invalidFreezer);
        for (Container container : containers.list) {
            verifyStillInCustody (container);
        }
        testLog ("SR-3229:R5: Moving to freezer without sufficient space resulted in an error message stating containers could not be moved to freezer. No containers were moved");
    }

    private void verifyContainerSelection (Containers containers) {
        containersList.selectContainerToBulkMove (containers.list.get (0));
        assertTrue (containersList.rowIsSelected (containers.list.get (0)));
        assertFalse (containersList.rowIsSelected (containers.list.get (1)));
        containersList.selectContainerToBulkMove (containers.list.get (1));
        assertTrue (containersList.rowIsSelected (containers.list.get (0)));
        assertTrue (containersList.rowIsSelected (containers.list.get (1)));
        testLog ("SR-3229:R1: Clicking bulk move button enabled container selection");
    }

    private void verifySuccessMessageLink (Containers expectedContainers) {
        containersList.navigateToTab (1);
        containersList.isCorrectPage ();
        Set <String> parsedContainerIDs = new HashSet <> (getContainerIDs (containersList.getContainers ()));
        Set <String> expectedContainerIDs = new HashSet <> (getContainerIDs (expectedContainers));
        assertEquals (parsedContainerIDs, expectedContainerIDs);
        testLog ("SR-3229:R6: Clicking the success message link displayed container list filtered to the containers moved");
        containersList.navigateToTab (0);
    }

    private void verifyMoveToFreezer (Container containerFromList, Container expectedFreezer, String expectedComment) {
        detail.gotoContainerDetail (containerFromList);
        Container containerFromDetails = detail.parsePrimaryDetail ();
        assertEquals (containerFromDetails.location, containerFromList.location);
        assertTrue (containerFromDetails.location.contains (expectedFreezer.location));
        detail.clickHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        containerFromList.comment = expectedComment;
        containerFromList.depleted = containerFromList.depleted == null ? false : containerFromList.depleted;
        verifyMovedToContainer (histories.get (0), containerFromList);
        verifyDetails (containerFromDetails, containerFromList);
    }

    private void verifyMoveToCustody (Container containerFromList, String expectedComment) {
        detail.gotoContainerDetail (containerFromList);
        Container containerFromDetails = detail.parsePrimaryDetail ();
        assertEquals (containerFromDetails.location, containerFromList.location);
        String expectedLocation = join (" : ", coraTestUser, containerFromList.containerNumber);
        assertEquals (containerFromDetails.location, expectedLocation);
        detail.clickHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        containerFromList.comment = expectedComment;
        containerFromList.depleted = containerFromList.depleted == null ? false : containerFromList.depleted;
        verifyTookCustody (histories.get (0), containerFromList);
        verifyDetails (containerFromDetails, containerFromList);
    }

    private void verifyMoveErrorMessage (Containers expectedContainersFailed, Container invalidFreezer) {
        String error = containersList.getBulkMoveErrorMessage ();
        info ("bulk move error message: " + error);
        Pattern failedToMovePattern = Pattern.compile ("Failed to move containers .* to Freezer " + Pattern.quote (invalidFreezer.name));
        assertTrue (failedToMovePattern.matcher (error).find ());
        Set <String> actualContainerNamesFailed = getSubstringsFromRegex ("CO-\\d{7}", error);
        Set <String> expectedContainerNamesFailed = getContainerIDs (expectedContainersFailed).stream ()
                                                                                              .collect (toSet ());
        assertEquals (actualContainerNamesFailed, expectedContainerNamesFailed);
        Set <String> actualContainerTypeErrors = getSubstringsFromRegex ("(There are 1[^.]*\\. The capacity for [^.]*\\.)",
                                                                         error);
        Set <String> expectedContainerTypeErrors = expectedContainersFailed.list.stream ()
                                                                                .map (container -> format ("There are 1 %1$s selected. The capacity for %1$s is 0.",
                                                                                                           container.containerType.displayText))
                                                                                .collect (toSet ());
        assertEquals (actualContainerTypeErrors, expectedContainerTypeErrors);
    }

    private Set <String> getSubstringsFromRegex (String pattern, String error) {
        Matcher matcher = Pattern.compile (pattern).matcher (error);
        Set <String> set = new HashSet <String> ();
        while (matcher.find ()) {
            set.add (matcher.group ());
        }
        return set;
    }

    private void verifyStillInCustody (Container container) {
        detail.gotoContainerDetail (container);
        Container containerFromDetails = detail.parsePrimaryDetail ();
        assertEquals (containerFromDetails.location, String.join (" : ", coraTestUser, container.containerNumber));
        detail.clickHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 1);
    }

    private Containers setupTwoPlates () {
        Containers containers = coraApi.addContainers (new Containers (asList (container (Plate), container (Plate))));
        info ("created containers: " + Arrays.toString (getContainerIDs (containers).toArray ()));
        return containers;
    }

    private Containers setupAllMoveableContainerTypes () {
        Containers containers = coraApi.addContainers (new Containers (
                stream (ContainerType.values ()).map (t -> container (t))
                                                .filter (t -> !t.containerType.equals (Freezer))
                                                .collect (toList ())));
        info ("created containers: " + Arrays.toString (getContainerIDs (containers).toArray ()));
        return containers;
    }

    private List <String> getContainerIDs (Containers containers) {
        return containers.list.stream ().map (container -> container.containerNumber).collect (toList ());
    }
}
