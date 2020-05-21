package com.adaptivebiotech.cora.ui.container;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.ConicalBox6x6;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.getContainerType;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import org.openqa.selenium.WebElement;

import java.util.List;

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
        return new Containers (waitForElements (".containers-list > tbody > tr").stream ().map (el -> {
            List<WebElement> columns = el.findElements(locateBy("td"));
            Container c = new Container ();
            String containerType = getText (columns.get(1));
            c.containerType = containerType != null && !containerType.equals("Unsupported") ? getContainerType (getText (columns.get(1))) : null;
            c.id = getConId (getAttribute (columns.get(2), "a", "href"));
            c.containerNumber = getText (columns.get(2));
            c.contents = getText (columns.get(3));
            c.name = getText (columns.get(4)) != null ? getText (columns.get(4)) : "";
            c.location = getText (columns.get(5));
            return c;
        }).collect (toList ()));
    }

//    public void sendAllMyCustody(Container destination) {
//        getContainers().list.stream().filter(container -> !container.containerType.equals(ConicalBox6x6) && !container.contents.contains("SP-")).forEach(container -> {
//            try {
//                moveToFreezer(container, destination);
//            } catch (Exception e) {
//
//            }
//        });
//    }
}
