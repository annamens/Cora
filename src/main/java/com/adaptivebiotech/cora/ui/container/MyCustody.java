/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Freezer;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.getContainerType;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.google.common.base.Strings;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class MyCustody extends ContainersList {

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active.highlight[uisref='main.containers.custody']"));
        assertTrue (waitUntilVisible (scan));
    }

    public Containers getContainers () {
        return new Containers (waitForElements (".containers-list > tbody > tr").stream ().map (el -> {
            List <WebElement> columns = findElements (el, "td");
            Container c = new Container ();
            c.id = getConId (getAttribute (columns.get (2), "a", "href"));
            c.containerNumber = getText (columns.get (2));
            String containerType = getText (columns.get (3));
            c.containerType = containerType != null && !containerType.equals ("Unsupported") ? getContainerType (containerType) : null;
            c.specimenId = getText (columns.get (4));
            c.name = getText (columns.get (5));
            c.location = getText (columns.get (6));
            String capacity = getText (columns.get (7));
            c.capacity = Strings.isNullOrEmpty (capacity) ? 0 : Integer.parseInt (capacity);
            return c;
        }).collect (toList ()));
    }

    public void sendAllMyCustody (Container freezer) {
        if (getMyCustodySize () > 0)
            sendContainersToFreezer (getContainers (), freezer);
    }

    public void sendContainersToFreezer (Containers containers, Container freezer) {
        containers.list.stream ()
                       .filter (container -> container.contents == null)
                       .filter (container -> container.specimenId == null || !container.specimenId.contains ("SP-"))
                       .filter (container -> !Freezer.equals (container.containerType))
                       .forEach (container -> moveToFreezer (container, freezer));

    }

    public void bulkMoveToFreezer (Containers containers, Container freezer, String comment) {
        clickBulkMoveContainers ();
        selectBulkMoveFreezer (freezer);
        setBulkMoveComment (comment);
        containers.list.forEach (c -> selectContainerToBulkMove (c));
        clickBulkMoveBtn ();
    }
}
