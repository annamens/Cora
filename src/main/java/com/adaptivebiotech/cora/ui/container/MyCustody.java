package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
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
public class MyCustody extends ContainerList {

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".active.highlight[uisref='main.containers.custody']"));
        assertTrue (waitUntilVisible (scan));
    }

    public Containers getContainers () {
        return new Containers (waitForElements (".containers-list > tbody > tr").stream ().map (el -> {
            List <WebElement> columns = el.findElements (locateBy ("td"));
            Container c = new Container ();
            c.id = getConId (getAttribute (columns.get (1), "a", "href"));
            c.containerNumber = getText (columns.get (1));
            String containerType = getText (columns.get (2));
            c.containerType = containerType != null && !containerType.equals ("Unsupported") ? getContainerType (containerType) : null;
            c.specimenId = getText (columns.get (3));
            c.name = getText (columns.get (4));
            c.location = getText (columns.get (5));
            String capacity = getText (columns.get (6));
            c.capacity = Strings.isNullOrEmpty (capacity) ? 0 : Integer.parseInt (capacity);
            return c;
        }).collect (toList ()));
    }

    public void sendAllMyCustody (Container destination) {
        if (getMyCustodySize () > 0)
            getContainers ().list.stream ()
                                 .filter (container -> (container.contents == null && !container.specimenId.contains ("SP-")))
                                 .forEach (container -> moveToFreezer (container, destination));
    }
}
