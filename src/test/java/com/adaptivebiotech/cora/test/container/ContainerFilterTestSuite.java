/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018018;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018055;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainersList;
import com.adaptivebiotech.cora.ui.container.ContainersList.Building;
import com.adaptivebiotech.cora.ui.container.ContainersList.Category;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

@Test (groups = { "regression", "fox terrier" })
public class ContainerFilterTestSuite extends ContainerTestBase {

    private Login                    login                  = new Login ();
    private OrdersList               ordersList             = new OrdersList ();
    private ContainersList           containersList         = new ContainersList ();
    private NewShipment              newShipment            = new NewShipment ();
    private ThreadLocal <Containers> containersToDeactivate = new ThreadLocal <> ();
    private final Container          freezerAB018055        = freezerAB018055 ();
    private final Container          freezerAB018018        = freezerAB018018 ();

    @AfterMethod (alwaysRun = true)
    public void afterMethod () {
        coraApi.deactivateContainers (containersToDeactivate.get ());
    }

    /**
     * @sdlc.requirements SR-9904:R1
     */
    // assumes freezerAB018055 is in 1551, has capacity
    public void applyFilters () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        ContainerType targetContainerType = ContainerType.Tube;
        ContainerType wrongContainerType = ContainerType.Conical;
        Container targetFreezer = freezerAB018055;
        Container wrongFreezer = freezerAB018018;
        Building targetBuilding = Building.building1551;
        Building wrongBuilding = Building.buildingSSF;
        Category targetCategory = Category.Diagnostic;
        Category wrongCategory = Category.Batch;
        newShipment.createShipment (targetContainerType, freezerAB018055.name);
        containersToDeactivate.set (new Containers (newShipment.getPrimaryContainers (targetContainerType).list));
        Container container = newShipment.getPrimaryContainers (targetContainerType).list.get (0);
        ordersList.clickContainers ();

        // category
        containersList.searchContainerIdOrName (container.containerNumber);
        containersList.setCategoryFilter (wrongCategory);
        containersList.clickFilter ();
        assertFalse (containersList.containerIsDisplayed (container));
        testLog ("Mismatching category filter did not return container");
        containersList.setCategoryFilter (targetCategory);
        containersList.clickFilter ();
        assertTrue (containersList.containerIsDisplayed (container));
        testLog ("Matching category filter returned container");

        // building
        containersList.setBuildingFilter (wrongBuilding);
        containersList.clickFilter ();
        assertFalse (containersList.containerIsDisplayed (container));
        testLog ("SR-9904:R1 Mismatching building filter did not return container");
        containersList.setBuildingFilter (targetBuilding);
        containersList.clickFilter ();
        assertTrue (containersList.containerIsDisplayed (container));
        testLog ("SR-9904:R1 Matching building filter returned container");

        // currentLocation
        containersList.setCurrentLocationFilter (wrongFreezer.name);
        containersList.clickFilter ();
        assertFalse (containersList.containerIsDisplayed (container));
        testLog ("Mismatching current location filter did not return container");
        containersList.setCurrentLocationFilter (targetFreezer.name);
        containersList.clickFilter ();
        assertTrue (containersList.containerIsDisplayed (container));
        testLog ("Matching current location filter returned container");

        // containerType
        containersList.setContainerTypeFilter (wrongContainerType);
        containersList.clickFilter ();
        assertFalse (containersList.containerIsDisplayed (container));
        testLog ("Mismatching container type filter did not return container");
        containersList.setContainerTypeFilter (targetContainerType);
        containersList.clickFilter ();
        assertTrue (containersList.containerIsDisplayed (container));
        testLog ("Matching container type filter returned container");
    }

}
