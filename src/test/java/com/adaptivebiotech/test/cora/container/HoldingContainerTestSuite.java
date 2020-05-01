package com.adaptivebiotech.test.cora.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Conical;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Freezer;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.MatrixTube;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.MatrixTube5ml;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.OtherTube;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Plate;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Slide;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.SlideWithCoverslip;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Vacutainer;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import java.util.EnumSet;
import java.util.List;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.cora.ui.container.AddContainer;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;

@Test (groups = { "container" })
public class HoldingContainerTestSuite extends ContainerTestBase {

    private CoraPage     main;
    private AddContainer add;
    private MyCustody    my;
    private Detail       detail;
    private History      history;
    private Container    child;
    private Containers   containers;

    @BeforeTest
    public void beforeTest () {
        containers = addContainers (new Containers (
                EnumSet.allOf (ContainerType.class).parallelStream ().filter (ct -> !ct.equals (Freezer))
                       .map (ct -> container (ct)).collect (toList ())));
    }

    @BeforeMethod
    public void beforeMethod () {
        main = new CoraPage ();
        add = new AddContainer ();
        my = new MyCustody ();
        detail = new Detail ();
        history = new History ();
    }

    @AfterTest
    public void afterTest () {
        doCoraLogin ();
        deactivateContainers (containers);
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void holdingContainers () {
        Containers topContainers = addContainers (new Containers (asList (container (Plate))));
        main.clickNewContainer ();
        EnumSet.allOf (ContainerType.class).stream ()
               .filter (ct -> ct.isHolding && !Freezer.equals (ct) && !Plate.equals (ct))
               .forEach (ct -> add.addContainer (ct, 1));
        add.clickSave ();
        topContainers = add.getContainers ();

        // test: holding containers don't have Holding Container btn and no depleted dropdown
        main.gotoMyCustody ();
        topContainers.list.stream ().forEach (c -> my.isHoldingContainer (c));
        deactivateContainers (topContainers);
    }

    /**
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void tube () {
        child = addContainers (Tube, null, null, 1).list.get (0);
        child.depleted = true;

        // test: set holding container, depletion and add comment
        main.gotoMyCustody ();
        int pass = 0;
        for (Container holding : containers.list) {
            child.comment = randomWords (10);
            my.setHoldingContainer (child, holding);
            if (child.root != null && child.root.containerNumber.equals (holding.containerNumber)) {
                switch (holding.containerType) {
                case TubeBox5x5:
                case TubeBox5x10:
                    assertEquals (child.location,
                                  String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1"));
                    break;
                case TubeBox9x9:
                case TubeBox10x10:
                default:
                    assertEquals (child.location,
                                  String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1"));
                    break;
                }

                // test: go to child detail page to verify location
                main.gotoContainerDetail (child);
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

                main.gotoMyCustody ();
                ++pass;
            }
        }
        containers.list.add (child);
    }

    /**
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void matrixTube5ml () {
        child = addContainers (MatrixTube5ml, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comments
        main.gotoMyCustody ();
        containers.list.stream ().forEach (h -> my.setHoldingContainer (child, h));
        containers.list.add (child);

        // test: go to child detail page to verify location
        main.gotoContainerDetail (child);
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
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void matrixTube () {
        child = addContainers (MatrixTube, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comment
        main.gotoMyCustody ();
        containers.list.stream ().forEach (h -> my.setHoldingContainer (child, h));
        containers.list.add (child);
        assertEquals (child.location, String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1"));

        // test: go to child detail page to verify location
        main.gotoContainerDetail (child);
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
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void otherTube () {
        child = addContainers (OtherTube, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comment
        main.gotoMyCustody ();
        containers.list.stream ().forEach (h -> my.setHoldingContainer (child, h));
        containers.list.add (child);

        // test: go to child detail page to verify location
        main.gotoContainerDetail (child);
        detail.isCorrectPage ();
        Container actual = detail.parsePrimaryDetail ();
        child.depleted = false; // no successful move, depletion is not set
        verifyDetails (actual, child);

        // test: go to child history page to verify comment
        detail.gotoHistory ();
        history.isCorrectPage ();
        List <ContainerHistory> histories = history.getHistories ();
        assertEquals (histories.size (), 1);
        verifyTookCustody (histories.get (0));
    }

    /**
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void vacutainer () {
        child = addContainers (Vacutainer, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comment
        main.gotoMyCustody ();
        containers.list.stream ().forEach (h -> my.setHoldingContainer (child, h));
        containers.list.add (child);
        assertEquals (child.location, String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1"));

        // test: go to child detail page to verify location
        main.gotoContainerDetail (child);
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
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void conical () {
        child = addContainers (Conical, null, null, 1).list.get (0);
        child.depleted = true;
        child.comment = randomWords (10);

        // test: set holding container, depletion and add comments
        main.gotoMyCustody ();
        containers.list.stream ().forEach (h -> my.setHoldingContainer (child, h));
        containers.list.add (child);

        // test: go to child detail page to verify location
        main.gotoContainerDetail (child);
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
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void slide_wo_coverslip () {
        child = addContainers (Slide, null, null, 1).list.get (0);
        child.depleted = true;

        // test: set holding container, depletion and add comment
        main.gotoMyCustody ();
        int pass = 0;
        for (Container holding : containers.list) {
            child.comment = randomWords (10);
            my.setHoldingContainer (child, holding);
            if (child.root != null && child.root.containerNumber.equals (holding.containerNumber)) {
                switch (holding.containerType) {
                case SlideBox5:
                case SlideBox5CS:
                case SlideTube:
                case SlideTubeCS:
                case OtherSlideBox:
                case OtherSlideBoxCS:
                    assertEquals (child.location,
                                  String.join (" : ", coraTestUser, child.root.containerNumber));
                    break;
                default:
                    assertEquals (child.location,
                                  String.join (" : ", coraTestUser, child.root.containerNumber, "Position 1"));
                    break;
                }

                // test: go to child detail page to verify location
                main.gotoContainerDetail (child);
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

                main.gotoMyCustody ();
                ++pass;
            }
        }
        containers.list.add (child);
    }

    /**
     * @sdlc_requirements 126.MoveMetadata, 126.TransformHoldingContainer
     */
    public void slide_with_coverslip () {
        child = addContainers (SlideWithCoverslip, null, null, 1).list.get (0);
        child.depleted = true;

        // test: set holding container, depletion and add comment
        main.gotoMyCustody ();
        int pass = 0;
        for (Container holding : containers.list) {
            child.comment = randomWords (10);
            my.setHoldingContainer (child, holding);
            if (child.root != null && child.root.containerNumber.equals (holding.containerNumber)) {
                switch (holding.containerType) {
                case SlideBox5:
                case SlideBox5CS:
                case SlideTube:
                case SlideTubeCS:
                case OtherSlideBox:
                case OtherSlideBoxCS:
                    assertEquals (child.location,
                                  String.join (" : ", coraTestUser, child.root.containerNumber));
                    break;
                default:
                    assertEquals (child.location,
                                  String.join (" : ", coraTestUser, child.root.containerNumber, "Position 1"));
                    break;
                }

                // test: go to child detail page to verify location
                main.gotoContainerDetail (child);
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

                main.gotoMyCustody ();
                ++pass;
            }
        }
        containers.list.add (child);
    }
}
