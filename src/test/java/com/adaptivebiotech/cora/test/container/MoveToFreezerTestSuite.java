package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.DryIce;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;

@Test (groups = "regression")
public class MoveToFreezerTestSuite extends ContainerTestBase {

    private String        downloadDir;
    private Login         login;
    private OrdersList    oList;
    private ContainerList cList;
    private Detail        detail;
    private History       history;
    private Container     freezer;
    private Containers    containers;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
        doCoraLogin ();
        login = new Login ();
        login.doLogin ();
        oList = new OrdersList ();
        oList.isCorrectPage ();
        cList = new ContainerList ();
        detail = new Detail ();
        history = new History ();
        containers = new Containers ();
        containers.list = new ArrayList <> ();
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void move_primary_to_freezer () {
        for (ContainerType type : ContainerType.values ())
            if (!type.isHolding)
                containers.list.add (container (type));
        containers = addContainers (containers);
        String comment = randomWords (10);

        oList.clickContainers ();
        for (Container primary : containers.list) {
            switch (primary.containerType) {
            case MatrixTube:
                freezer = freezerAB018055;
                break;
            case Slide:
            case SlideWithCoverslip:
                freezer = freezerAB039003;
                break;
            default:
                freezer = freezerAB018078;
                break;
            }

            primary.depleted = true;
            primary.comment = comment;
            cList.moveToFreezer (primary, freezer);

            // test: go to detail page to verify location
            oList.gotoContainerDetail (primary);
            detail.isCorrectPage ();
            Container actual = detail.parsePrimaryDetail ();
            actual.comment = comment;
            assertTrue (primary.location.startsWith (freezer.name));
            verifyDetails (actual, primary);

            // test: go to history page to verify location
            detail.gotoHistory ();
            history.isCorrectPage ();
            List <ContainerHistory> histories = history.getHistories ();
            assertEquals (histories.size (), 2);
            verifyMovedToContainer (histories.get (0), actual);
            verifyTookCustody (histories.get (1));

            history.clickContainers ();
        }

        // test: go to containers list for the given freezer and verify
        for (Container primary : containers.list) {
            oList.searchContainerByContainerId (primary);
            Containers listContainers = cList.getContainers ();
            assertEquals (listContainers.findContainerByNumber (primary).location, primary.location);
        }

    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void move_child_to_freezer () {

        // setup for holding containers with children
        oList.selectNewBatchShipment ();
        Shipment shipment = new Shipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (DryIce);
        shipment.clickSave ();
        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();

        String manifestName = "intakemanifest_holding_w_child";
        String manifestFileName = join ("/", downloadDir, manifestName + ".xlsx");
        String manifestTemplatePath = getSystemResource (manifestName + "_template.xlsx").getPath ();
        DateTimeFormatter fmt = ofPattern ("yyddhhmmss");

        try (FileInputStream inputStream = new FileInputStream (new File (manifestTemplatePath));
                Workbook workbook = WorkbookFactory.create (inputStream);
                FileOutputStream outputStream = openOutputStream (new File (manifestFileName))) {
            Sheet sheet = workbook.getSheetAt (0);
            sheet.getRow (38).getCell (9).setCellValue (now ().format (fmt));
            sheet.getRow (39).getCell (9).setCellValue (now ().plusSeconds (1L).format (fmt));
            workbook.write (outputStream);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }

        accession.uploadIntakeManifest (new File (manifestFileName).getAbsolutePath ());
        accession.clickIntakeComplete ();
        accession.gotoShipment ();
        containers = shipment.getBatchContainers ();

        for (Container holding : containers.list) {
            Container child = holding.containerType.equals (ContainerType.Plate) ? holding : holding.children.get (0);
            child.depleted = true;
            child.comment = randomWords (10);

            switch (child.containerType) {
            case Plate:
                child.depleted = false;
            case MatrixTube:
                freezer = freezerAB018055;
                break;
            case Slide:
            case SlideWithCoverslip:
                freezer = freezerAB039003;
                break;
            default:
                freezer = freezerAB018078;
                break;
            }

            String location;
            switch (holding.containerType) {
            case Plate:
                location = "";
                break;
            case TubeBox5x5:
            case TubeBox5x10:
            case TubeBox9x9:
            case TubeBox10x10:
            case VacutainerBox7x7:
            case MatrixRack:
            case MatrixRack4x6:
                location = String.join (" : ", coraTestUser, child.root.containerNumber, "Position A:1");
                break;
            case SlideBox5:
            case SlideBox5CS:
            case SlideTube:
            case SlideTubeCS:
            case OtherSlideBox:
            case OtherSlideBoxCS:
                location = String.join (" : ", coraTestUser, child.root.containerNumber);
                break;
            default:
                location = String.join (" : ", coraTestUser, child.root.containerNumber, "Position 1");
                break;
            }
            shipment.clickContainers ();
            cList.moveToFreezer (child, freezer);

            // test: go to child detail page to verify location
            cList.gotoContainerDetail (child);
            detail.isCorrectPage ();
            Container actual = detail.parsePrimaryDetail ();
            actual.comment = child.comment;
            assertTrue (actual.location.startsWith (freezer.name));
            verifyDetails (actual, child);

            // test: go to child history page to verify location
            detail.gotoHistory ();
            history.isCorrectPage ();
            List <ContainerHistory> histories = history.getHistories ();
            if (ContainerType.Plate.equals (actual.containerType)) {
                assertEquals (histories.size (), 2);
                verifyMovedToContainer (histories.get (0), actual);
                verifyTookCustody (histories.get (1));
            } else {
                assertEquals (histories.size (), 3);
                verifyMovedToContainer (histories.get (0), actual);
                verifyTookCustody (histories.get (1), actual);

                actual.location = location;
                actual.comment = null;
                verifyMovedTo (histories.get (2), actual);
            }

            if (!ContainerType.Plate.equals (actual.containerType)) {
                // test: go to holding detail page to verify location
                history.gotoContainerDetail (holding);
                detail.isCorrectPage ();
                actual = detail.parseHoldingDetail ();
                holding.location = coraTestUser;
                assertEquals (actual.children.size (), holding.children.size () - 1);
                holding.depleted = holding.depleted == null ? false : holding.depleted;
                verifyDetailsChild (actual, holding);

                // test: go to holding history page to verify location
                detail.gotoHistory ();
                history.isCorrectPage ();
                histories = history.getHistories ();
                assertEquals (histories.size (), 1);
                verifyTookCustody (histories.get (0));
            }
        }

        // test: go to containers list for the given freezer and verify
        for (Container c : containers.list) {
            oList.searchContainerByContainerId (c);
            Containers listContainers = cList.getContainers ();
            String location = c.containerType.equals (ContainerType.Plate) ? c.location : String.join (" : ",
                                                                                                       c.location,
                                                                                                       c.containerNumber);
            assertEquals (listContainers.findContainerByNumber (c).location, location);
        }
    }
}
