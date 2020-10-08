package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.TubeBox5x5;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
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

    private Login      login;
    private OrdersList oList;
    private MyCustody  my;
    private Detail     detail;
    private History    history;
    private Container  child;
    private Container  holding;

    @BeforeMethod
    public void beforeMethod () {
        doCoraLogin ();
        Containers testContainers = addContainers (new Containers (asList (container (Tube), container (TubeBox5x5))));
        child = testContainers.list.get (0);
        holding = testContainers.list.get (1);

        login = new Login ();
        login.doLogin ();
        oList = new OrdersList ();
        oList.isCorrectPage ();
        oList.gotoMyCustody ();

        detail = new Detail ();
        history = new History ();
        my = new MyCustody ();
        my.isCorrectPage ();
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void move_primary_to_freezer () {

        // test: move primary to freezer, set depletion and comment
        child.depleted = true;
        child.comment = "send to " + freezerAB018055.name;
        my.moveToFreezer (child, freezerAB018055);

        // test: go to detail page to verify depletion
        my.gotoContainerDetail (child);
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
        my.setHoldingContainer (child, holding);

        // test: move child container to freezer, set depletion and comment
        child.depleted = true;
        child.comment = "send to " + freezerAB018055.name;
        my.moveToFreezer (child, freezerAB018055);

        // test: go to primary detail page to verify depletion
        my.gotoContainerDetail (child);
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
        my.setHoldingContainer (child, holding);

        // test: move holding container to freezer, set depletion on child and comment
        String comment = "send to " + freezerAB018078.name;
        child.depleted = true;
        my.scan (holding);
        my.setChildDepletion (child);
        my.clickFreezer ();
        my.selectFreezer (holding, freezerAB018078, comment);

        // test: go to holding detail page to verify depletion
        my.gotoContainerDetail (holding);
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
        verifyMovedTo (histories.get (0), actual);
        verifyTookCustody (histories.get (1));

        // test: go to child detail page to verify depletion
        history.gotoContainerDetail (child);
        detail.isCorrectPage ();
        actual = detail.parseChildDetail ();
        assertTrue (actual.location.startsWith (freezerAB018078.name));
        verifyDetails (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        histories = history.getHistories ();
        actual.comment = comment;
        assertEquals (histories.size (), 3);
        verifyMovedTo (histories.get (0), actual);
        actual.location = String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1");
        actual.comment = null;
        verifyMovedTo (histories.get (1), actual);
        verifyTookCustody (histories.get (2));
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void set_holding_container () {
        child.depleted = true;
        child.comment = randomWords (10);

        // test: add to a holding container, mark it as depleted and comment
        my.setHoldingContainer (child, holding);

        // test: go to child detail page to verify depletion
        my.gotoContainerDetail (child);
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
        my.setHoldingContainer (child, holding);

        // test: remove from a holding container, mark it as depleted and comment
        child.depleted = true;
        child.comment = randomWords (10);
        my.removeFromHoldingContainer (child, holding);

        // test: go to primary detail page to verify depletion
        my.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        actual.comment = child.comment;
        verifyDetails (actual, child);

        // test: go to primary history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 3);
        verifyTookCustody (histories.get (0), actual);
    }
}
