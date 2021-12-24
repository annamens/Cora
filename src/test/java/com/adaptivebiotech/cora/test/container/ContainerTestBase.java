package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.utils.TestHelper.dumbwaiter;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018055;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB018078;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerAB039003;
import static com.adaptivebiotech.cora.utils.TestHelper.freezerDestroyed;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;

public class ContainerTestBase extends CoraBaseBrowser {

    protected final Container freezerDestroyed = freezerDestroyed ();
    protected final Container dumbwaiter       = dumbwaiter ();
    protected final Container freezerAB018055  = freezerAB018055 ();
    protected final Container freezerAB018078  = freezerAB018078 ();
    protected final Container freezerAB039003  = freezerAB039003 ();

    protected Container container (ContainerType type) {
        Container container = new Container ();
        container.containerType = type;
        container.contentsLocked = false;
        container.usesBarcodeAsId = false;
        return container;
    }

    protected void verifyDetails (Container actual, Container expected) {
        assertEquals (actual.containerNumber, expected.containerNumber);
        assertEquals (actual.containerType, expected.containerType);
        assertEquals (actual.name, expected.name);
        assertEquals (actual.depleted, expected.depleted);
        assertEquals (actual.location, expected.location);
    }

    /**
     * When child container is moved to holding container
     * 
     * @param actual
     * @param expected
     */
    protected void verifyDetailsChild (Container actual, Container expected) {
        assertEquals (actual.containerNumber, expected.containerNumber);
        assertEquals (actual.containerType, expected.containerType);
        assertEquals (actual.name, expected.name);
        assertEquals (actual.depleted, expected.depleted);
        assertEquals (actual.location, String.join (" : ", expected.location, actual.containerNumber));
    }

    protected void verifyMovedTo (ContainerHistory history, Container actual) {
        assertEquals (history.activity, "Moved to Location");
        assertEquals (history.comment, actual.comment);
        assertEquals (history.location, actual.location);
        assertEquals (history.activityBy, coraTestUser);
    }

    /**
     * When holding container is moved to freezer
     * 
     * @param history
     * @param actual
     */
    protected void verifyMovedToContainer (ContainerHistory history, Container actual) {
        assertEquals (history.activity, "Moved to Location");
        assertEquals (history.comment, actual.comment);
        assertEquals (String.join (" : ", history.location, actual.containerNumber), actual.location);
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
