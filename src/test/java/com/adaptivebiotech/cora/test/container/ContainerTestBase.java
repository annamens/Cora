package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018055;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018078;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB039003;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerDestroyed;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static com.seleniumfy.test.utils.HttpClientHelper.put;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import java.util.stream.IntStream;
import org.testng.annotations.AfterSuite;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;

public class ContainerTestBase extends CoraBaseBrowser {

    protected final Container freezerDestroyed = freezerDestroyed ();
    protected final Container freezerAB018055  = freezerAB018055 ();
    protected final Container freezerAB018078  = freezerAB018078 ();
    protected final Container freezerAB039003  = freezerAB039003 ();

    @AfterSuite (groups = "regression")
    public void containerAfterSuite () {
        new Login ().doLogin ();
        OrdersList oList = new OrdersList ();
        oList.isCorrectPage ();
        oList.gotoMyCustody ();
        MyCustody my = new MyCustody ();
        my.isCorrectPage ();
        my.sendAllMyCustody (freezerDestroyed ());
    }

    protected Container container (ContainerType type) {
        return container (type, null, null);
    }

    private Container container (ContainerType type, String cbarcode, Container croot) {
        Container container = new Container ();
        container.containerType = type;
        container.contentsLocked = false;
        container.usesBarcodeAsId = false;
        container.barcode = cbarcode;
        container.root = croot;
        return container;
    }

    protected Containers addContainers (ContainerType type, String barcode, Container root, int num) {
        return addContainers (new Containers (IntStream.range (0, num).mapToObj (i -> {
            return container (type, barcode, root);
        }).collect (toList ())));
    }

    protected Containers addContainers (Containers containers) {
        try {
            String url = coraTestUrl + "/cora/api/v1/containers/addEntries";
            String result = post (url, body (mapper.writeValueAsString (containers.list)));
            return new Containers (
                    mapper.readValue (result, HttpResponse.class).containers.parallelStream ().map (c -> {
                        c.location = coraTestUser;
                        return c;
                    }).collect (toList ()));
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    protected Containers deactivateContainers (Containers containers) {
        try {
            containers.list.parallelStream ().forEach (c -> c.isActive = false);
            String url = coraTestUrl + "/cora/api/v1/containers/updateEntries";
            String result = put (url, body (mapper.writeValueAsString (containers.list)));
            return new Containers (mapper.readValue (result, HttpResponse.class).containers);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    protected void verifyDetails (Container actual, Container expected) {
        assertEquals (actual.containerNumber, expected.containerNumber);
        assertEquals (actual.containerType, expected.containerType);
        assertEquals (actual.name, expected.name);
        assertEquals (actual.depleted, expected.depleted);
        assertEquals (actual.location, expected.location);
    }

    protected void verifyMovedTo (ContainerHistory history, Container actual) {
        assertEquals (history.activity, "Moved to Location");
        assertEquals (history.comment, actual.comment);
        assertEquals (history.location, actual.location);
        assertEquals (history.activityBy, coraTestUser);
    }

    protected void verifyTookCustody (ContainerHistory history, Container actual) {
        assertEquals (history.activity, "Took Custody");
        if (actual == null)
            assertNull (history.comment);
        else
            assertEquals (history.comment, actual.comment);
        assertEquals (history.location, coraTestUser);
        assertEquals (history.activityBy, coraTestUser);
    }

    protected void verifyTookCustody (ContainerHistory history) {
        verifyTookCustody (history, null);
    }
}
