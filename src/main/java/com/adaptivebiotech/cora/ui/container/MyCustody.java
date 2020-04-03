package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class MyCustody extends ContainerList {

    @Override
    public void isCorrectPage () {
        assertTrue (waitUntilVisible (".navbar"));
        assertTrue (waitUntilVisible (".content"));
        assertTrue (waitUntilVisible (".active.highlight[uisref='main.containers.custody']"));
        assertTrue (waitUntilVisible ("#container-scan-input"));
    }

    public Containers getContainers () {
        return new Containers (waitForElements (".containers-list tbody tr").stream ().map (el -> {
            Container c = new Container ();
            c.id = getConId (getAttribute (el, "[ng-bind*='containerNumber']", "href"));
            c.containerNumber = getText (el, "[ng-bind*='containerNumber']");
            c.containerType = getContainerType (getText (el, "[ng-bind*='containerTypeDisplayName']"));
            c.name = getText (el, "[ng-bind*='containerDetail.container.displayName']");
            c.location = getText (el, "[ng-bind*='containerDetail.container.lastMovedBy']");
            return c;
        }).collect (toList ()));
    }
}
