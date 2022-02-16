package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Conical;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Freezer;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.MatrixTube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.MatrixTube5ml;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.OtherTube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Plate;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Slide;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.SlideWithCoverslip;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import java.util.List;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;

@Test (groups = "regression")
public class HoldingContainerTestSuite extends ContainerTestBase {

    private Login                    login        = new Login ();
    private OrdersList               ordersList   = new OrdersList ();
    private AddContainer             addContainer = new AddContainer ();
    private MyCustody                myCustody    = new MyCustody ();
    private Detail                   detail       = new Detail ();
    private History                  history      = new History ();
    private ThreadLocal <Containers> containers   = new ThreadLocal <> ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        containers.set (coraApi.addContainers (new Containers (
                allOf (ContainerType.class).parallelStream ().filter (ct -> !ct.equals (Freezer))
                                           .map (ct -> container (ct)).collect (toList ()))));

        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    @AfterMethod
    public void afterMethod () {
        ordersList.gotoMyCustody ();
        myCustody.isCorrectPage ();
        myCustody.sendContainersToFreezer (containers.get (), freezerDestroyed);
    }

    /**
     * @sdlc.requirements 126.MoveMetadata
     */
    public void holding_containers () {
        Containers topContainers = coraApi.addContainers (new Containers (asList (container (Plate))));
        Container child = topContainers.list.get (0);

        ordersList.selectNewContainer ();
        allOf (ContainerType.class).stream ().filter (ct -> ct.isHolding && !Freezer.equals (ct) && !Plate.equals (ct))
                                   .forEach (ct -> addContainer.addContainer (ct, 1));
        addContainer.clickSave ();
        topContainers = addContainer.getContainers ();

        // test: holding containers don't have Holding Container btn and no depleted dropdown
        addContainer.gotoMyCustody ();
        topContainers.list.stream ().forEach (c -> myCustody.isHoldingContainer (c));
        coraApi.deactivateContainers (topContainers);
        containers.get ().list.add (child);
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void tube () {
        Container child = coraApi.addContainers (Tube, null, null, 1).list.get (0);
        child.depleted = true;

        // test: set holding container, depletion and add comment
        ordersList.gotoMyCustody ();
        int pass = 0;
        for (Container holding : containers.get ().list) {
            child.comment = randomWords (10);
            myCustody.setHoldingContainer (child, holding);
            if (child.root != null && child.root.containerNumber.equals (holding.containerNumber)) {
                switch (holding.containerType) {
                case TubeBox5x5:
                case TubeBox5x10:
                case TubeBox9x9:
                case TubeBox10x10:
                default:
                    assertEquals (child.location,
                                  join (" : ", coraTestUser, child.root.containerNumber, "Position A:1"));
                    break;
                }

                // test: go to child detail page to verify location
                myCustody.gotoContainerDetail (child);
                detail.isCorrectPage ();
                Container actual = detail.parsePrimaryDetail ();
                actual.comment = child.comment;
                verifyDetails (actual, child);

                // test: go to child history page to verify comment
                detail.gotoHistory ();
                history.isCorrectPage ();
                List <ContainerHistory> histories = history.getHistories ();
                verifyMovedTo (histories.get (0), actual);
                verifyTookCustody (histories.get (1), pass == 0 ? null : actual);

                history.gotoMyCustody ();
                ++pass;
            }
        }
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void matrix_tube_5ml () {
        Container child = coraApi.addContainers (MatrixTube5ml, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comments
        ordersList.gotoMyCustody ();
        containers.get ().list.stream ().forEach (h -> myCustody.setHoldingContainer (child, h));

        // test: go to child detail page to verify location
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        verifyDetails (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedTo (histories.get (0), child);
        verifyTookCustody (histories.get (1));
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void matrix_tube () {
        Container child = coraApi.addContainers (MatrixTube, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comment
        ordersList.gotoMyCustody ();
        containers.get ().list.stream ().forEach (h -> myCustody.setHoldingContainer (child, h));
        assertEquals (child.location, join (" : ", coraTestUser, child.root.containerNumber, "Position A:1"));

        // test: go to child detail page to verify location
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        verifyDetails (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedTo (histories.get (0), child);
        verifyTookCustody (histories.get (1));
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void other_tube () {
        Container child = coraApi.addContainers (OtherTube, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comment
        ordersList.gotoMyCustody ();
        containers.get ().list.stream ().forEach (h -> myCustody.setHoldingContainer (child, h));
        containers.get ().list.add (child);

        // test: go to child detail page to verify location
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        child.depleted = false; // no successful move, depletion is not set
        verifyDetailsChild (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 1);
        verifyTookCustody (histories.get (0));
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void vacutainer () {
        Container child = coraApi.addContainers (Vacutainer, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comment
        ordersList.gotoMyCustody ();
        containers.get ().list.stream ().forEach (h -> myCustody.setHoldingContainer (child, h));
        assertEquals (child.location, join (" : ", coraTestUser, child.root.containerNumber, "Position A:1"));

        // test: go to child detail page to verify location
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        verifyDetails (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedTo (histories.get (0), child);
        verifyTookCustody (histories.get (1));
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void conical () {
        Container child = coraApi.addContainers (Conical, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comments
        ordersList.gotoMyCustody ();
        containers.get ().list.stream ().forEach (h -> myCustody.setHoldingContainer (child, h));

        // test: go to child detail page to verify location
        myCustody.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        verifyDetails (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 2);
        verifyMovedTo (histories.get (0), child);
        verifyTookCustody (histories.get (1));
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void slide_wo_coverslip () {
        Container child = coraApi.addContainers (Slide, null, null, 1).list.get (0);
        child.depleted = true;

        // test: set holding container, depletion and add comment
        ordersList.gotoMyCustody ();
        int pass = 0;
        for (Container holding : containers.get ().list) {
            child.comment = randomWords (10);
            myCustody.setHoldingContainer (child, holding);
            if (child.root != null && child.root.containerNumber.equals (holding.containerNumber)) {
                switch (holding.containerType) {
                case SlideBox5:
                case SlideBox5CS:
                case SlideTube:
                case SlideTubeCS:
                case OtherSlideBox:
                case OtherSlideBoxCS:
                    assertEquals (child.location, join (" : ", coraTestUser, child.root.containerNumber));
                    break;
                default:
                    assertEquals (child.location, join (" : ", coraTestUser, child.root.containerNumber, "Position 1"));
                    break;
                }

                // test: go to child detail page to verify location
                myCustody.gotoContainerDetail (child);
                detail.isCorrectPage ();
                Container actual = detail.parsePrimaryDetail ();
                actual.comment = child.comment;

                // test: go to child history page to verify comment
                detail.gotoHistory ();
                history.isCorrectPage ();
                List <ContainerHistory> histories = history.getHistories ();
                ContainerHistory historyRow = histories.get (0);

                switch (holding.containerType) {
                case SlideBox5:
                case SlideTube:
                case OtherSlideBox:
                    verifyDetailsChild (actual, child);
                    verifyMovedToContainer (historyRow, actual);
                    break;
                default:
                    verifyDetails (actual, child);
                    verifyMovedTo (historyRow, actual);
                    break;
                }
                verifyTookCustody (histories.get (1), pass == 0 ? null : actual);

                history.gotoMyCustody ();
                ++pass;
            }
        }
    }

    /**
     * @sdlc.requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void slide_with_coverslip () {
        Container child = coraApi.addContainers (SlideWithCoverslip, null, null, 1).list.get (0);
        child.depleted = true;

        // test: set holding container, depletion and add comment
        ordersList.gotoMyCustody ();
        int pass = 0;
        for (Container holding : containers.get ().list) {
            child.comment = randomWords (10);
            myCustody.setHoldingContainer (child, holding);
            if (child.root != null && child.root.containerNumber.equals (holding.containerNumber)) {
                switch (holding.containerType) {
                case SlideBox5:
                case SlideBox5CS:
                case SlideTube:
                case SlideTubeCS:
                case OtherSlideBox:
                case OtherSlideBoxCS:
                    assertEquals (child.location, join (" : ", coraTestUser, child.root.containerNumber));
                    break;
                default:
                    assertEquals (child.location, join (" : ", coraTestUser, child.root.containerNumber, "Position 1"));
                    break;
                }

                // test: go to child detail page to verify location
                myCustody.gotoContainerDetail (child);
                detail.isCorrectPage ();
                Container actual = detail.parsePrimaryDetail ();
                actual.comment = child.comment;
                switch (holding.containerType) {
                case SlideBox5CS:
                case SlideTubeCS:
                case OtherSlideBoxCS:
                    verifyDetailsChild (actual, child);
                    break;
                default:
                    verifyDetails (actual, child);
                    break;
                }

                // test: go to child history page to verify comment
                detail.gotoHistory ();
                history.isCorrectPage ();
                List <ContainerHistory> histories = history.getHistories ();
                ContainerHistory historyRow = histories.get (0);
                switch (holding.containerType) {
                case SlideBox5CS:
                case SlideTubeCS:
                case OtherSlideBoxCS:
                    verifyMovedToContainer (historyRow, actual);
                    break;
                default:
                    verifyMovedTo (historyRow, actual);
                    break;
                }

                verifyTookCustody (histories.get (1), pass == 0 ? null : actual);
                history.gotoMyCustody ();
                ++pass;
            }
        }
    }
}
