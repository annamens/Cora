package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.TubeBox5x5;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class DepletionTestSuite extends ContainerTestBase {

    private Login                    login      = new Login ();
    private OrdersList               ordersList = new OrdersList ();
    private MyCustody                myCustody  = new MyCustody ();
    private Detail                   detail     = new Detail ();
    private History                  history    = new History ();
    private ThreadLocal <Containers> containers = new ThreadLocal <> ();

    @BeforeMethod
    public void beforeMethod () {
        containers.set (coraApi.addContainers (new Containers (asList (container (Tube), container (TubeBox5x5)))));

        login.doLogin ();
        ordersList.isCorrectPage ();
        ordersList.gotoMyCustody ();
        myCustody.isCorrectPage ();
    }

    @AfterMethod
    public void afterMethod () {
        coraApi.deactivateContainers (containers.get ());
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void move_primary_to_freezer () {

        // test: move primary to freezer, set depletion and comment
        Container child = SerializationUtils.clone (containers.get ().list.get (0));
        child.depleted = true;
        child.comment = "send to " + freezerAB018055.name;
        myCustody.moveToFreezer (child, freezerAB018055);

        // test: go to detail page to verify depletion
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        actual.comment = child.comment;
        assertTrue (actual.location.startsWith (freezerAB018055.name));
        verifyDetails (actual, child);

        // test: go to history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedTo (histories.get (0), actual);
        verifyTookCustody (histories.get (1));
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void move_child_to_freezer () {
        Container child = SerializationUtils.clone (containers.get ().list.get (0));
        Container holding = SerializationUtils.clone (containers.get ().list.get (1));
        myCustody.setHoldingContainer (child, holding);

        // test: move child container to freezer, set depletion and comment
        child.depleted = true;
        child.comment = "send to " + freezerAB018055.name;
        myCustody.moveToFreezer (child, freezerAB018055);

        // test: go to primary detail page to verify depletion
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        actual.comment = child.comment;
        assertTrue (actual.location.startsWith (freezerAB018055.name));
        verifyDetails (actual, child);

        // test: go to primary history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 4);
        verifyMovedTo (histories.get (0), actual);
        verifyTookCustody (histories.get (1), actual);
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void move_holding_to_freezer () {
        Container child = SerializationUtils.clone (containers.get ().list.get (0));
        Container holding = SerializationUtils.clone (containers.get ().list.get (1));
        myCustody.setHoldingContainer (child, holding);

        // test: move holding container to freezer, set depletion on child and comment
        String comment = "send to " + freezerAB018078.name;
        child.depleted = true;
        myCustody.scan (holding);
        myCustody.setChildDepletion (child);
        myCustody.clickFreezer ();
        myCustody.selectFreezer (holding, freezerAB018078, comment);

        // test: go to holding detail page to verify depletion
        myCustody.gotoContainerDetail (holding);
        detail.isCorrectPage ();
        Container actual = detail.parseHoldingDetail ();
        actual.comment = comment;
        assertTrue (actual.location.startsWith (freezerAB018078.name));
        verifyDetails (actual, holding);

        // test: go to holding history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedToContainer (histories.get (0), actual);
        verifyTookCustody (histories.get (1));

        // test: go to child detail page to verify depletion
        history.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container childUI = detail.parseChildDetail ();
        assertTrue (childUI.location.startsWith (freezerAB018078.name));
        verifyDetails (childUI, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        histories = history.getHistories ();
        childUI.comment = comment;
        assertEquals (histories.size (), 3);
        ContainerHistory historyRow = histories.get (0);
        assertEquals (historyRow.activity, "Moved to Location");
        assertEquals (historyRow.comment, childUI.comment);
        String[] locationSplit = childUI.location.split (actual.containerNumber);
        assertTrue (historyRow.location.startsWith (locationSplit[0]));
        assertTrue (historyRow.location.endsWith (locationSplit[1]));
        assertEquals (historyRow.activityBy, coraTestUser);
        childUI.location = String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1");
        childUI.comment = null;
        verifyMovedTo (histories.get (1), childUI);
        verifyTookCustody (histories.get (2));
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void set_holding_container () {
        Container holding = SerializationUtils.clone (containers.get ().list.get (1));
        Container child = SerializationUtils.clone (containers.get ().list.get (0));
        child.depleted = true;
        child.comment = randomWords (10);

        // test: add to a holding container, mark it as depleted and comment
        myCustody.setHoldingContainer (child, holding);

        // test: go to child detail page to verify depletion
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parseChildDetail ();
        actual.comment = child.comment;
        child.location = String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1");
        verifyDetails (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedTo (histories.get (0), actual);
        verifyTookCustody (histories.get (1));
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void remove_holding_container () {
        Container child = SerializationUtils.clone (containers.get ().list.get (0));
        Container holding = SerializationUtils.clone (containers.get ().list.get (1));
        myCustody.setHoldingContainer (child, holding);

        // test: remove from a holding container, mark it as depleted and comment
        child.depleted = true;
        child.comment = randomWords (10);
        myCustody.removeFromHoldingContainer (child, holding);

        // test: go to primary detail page to verify depletion
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        actual.comment = child.comment;
        verifyDetailsChild (actual, child);

        // test: go to primary history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 3);
        verifyTookCustody (histories.get (0), actual);
    }
}
