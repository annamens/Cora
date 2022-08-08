/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018018;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018078;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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

@Test (groups = { "regression", "fox-terrier" })
public class ContainerFilterTestSuite extends ContainerTestBase {

    private Login           login           = new Login ();
    private OrdersList      ordersList      = new OrdersList ();
    private ContainersList  containersList  = new ContainersList ();
    private NewShipment     newShipment     = new NewShipment ();
    private final Container freezerAB018078 = freezerAB018078 ();
    private final Container freezerAB018018 = freezerAB018018 ();

    /**
     * Note: assumes freezerAB018078 is in 1551, has unlimited capacity
     * 
     * @sdlc.requirements SR-9904:R1
     */
    public void applyFilters () {
        ContainerType targetContainerType = ContainerType.Tube;
        ContainerType wrongContainerType = ContainerType.Conical;
        Container targetFreezer = freezerAB018078;
        Container wrongFreezer = freezerAB018018;
        Building targetBuilding = Building.WA_1551;
        Building wrongBuilding = Building.SSF;
        Category targetCategory = Category.Diagnostic;
        Category wrongCategory = Category.Batch;
        login.doLogin ();
        ordersList.isCorrectPage ();
        newShipment.createShipment (targetContainerType, targetFreezer.name);
        Containers containers = newShipment.getPrimaryContainers (targetContainerType);
        Container container = containers.list.get (0);
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

        coraApi.deactivateContainers (containers);
    }

}
