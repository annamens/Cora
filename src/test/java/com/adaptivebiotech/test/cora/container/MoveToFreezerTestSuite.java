package com.adaptivebiotech.test.cora.container;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.DryIce;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.ClassLoader.getSystemResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.adaptivebiotech.cora.ui.container.ContainerList;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;

//import org.apache.poi.EncryptedDocumentException;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;

@Test (groups = { "container" })
public class MoveToFreezerTestSuite extends ContainerTestBase {

    private CoraPage      main;
    private ContainerList list;
    private Detail        detail;
    private History       history;
    private Container     freezer;
    private Containers    containers;

    @BeforeMethod
    public void beforeMethod () {
        main = new CoraPage ();
        list = new ContainerList ();
        detail = new Detail ();
        history = new History ();

        containers = new Containers ();
        containers.list = new ArrayList<>();
    }

    @AfterMethod
    public void afterMethod () {
        if (containers.list != null && !containers.list.isEmpty ())
            deactivateContainers (containers);
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void movePrimaryToFreezer () {
        for (ContainerType type : ContainerType.values ())
            if (!type.isHolding)
                containers.list.add (container (type));
        containers = addContainers (containers);
        String comment = randomWords (10);

        main.gotoContainersList ();
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
            list.moveToFreezer (primary, freezer);

            // test: go to detail page to verify location
            main.gotoContainerDetail (primary);
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
            verifyMovedTo (histories.get (0), actual);
            verifyTookCustody (histories.get (1));

            main.gotoContainersList ();
        }

        // test: go to containers list for the given freezer and verify
        for (Container freezer : new Container[] { freezerAB018055, freezerAB018078, freezerAB039003 }) {
            main.showFreezerContents (freezer);
            Containers listContainers = list.getContainers ();
            for (Container primary : containers.list) {
                if (primary.location.startsWith (freezer.name))
                    assertEquals (listContainers.findContainerByNumber (primary).location, primary.location);
            }
        }
    }

    /**
     * @sdlc_requirements 126.MoveMetadata
     */
    public void moveChildToFreezer () {

        // setup for holding containers with children
        main.clickNewBatchShipment ();
        Shipment shipment = new Shipment ();
        shipment.isCorrectPage ();
        shipment.enterShippingCondition (DryIce);
        shipment.clickSave ();
        shipment.gotoAccession ();

        Accession accession = new Accession ();
        accession.isCorrectPage ();


        String excelFilePath = getSystemResource ("intakemanifest_holding_w_child_template.xlsx").getPath();
        String excelFilePath2 = getSystemResource ("intakemanifest_holding_w_child.xlsx").getPath();

        try {
            FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
            Workbook workbook = WorkbookFactory.create(inputStream);

            Sheet sheet = workbook.getSheetAt(0);

            sheet.getRow(38).getCell(9).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyddhhmmSS")));
            sheet.getRow(39).getCell(9).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyddhhmmSS")));

            inputStream.close();

            FileOutputStream outputStream = new FileOutputStream(excelFilePath2);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        accession.uploadIntakeManifest ("intakemanifest_holding_w_child.xlsx");
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
            main.gotoContainersList ();
            list.moveToFreezer (child, freezer);

            // test: go to child detail page to verify location
            main.gotoContainerDetail (child);
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
                verifyMovedTo (histories.get (0), actual);
                verifyTookCustody (histories.get (1));
            } else {
                assertEquals (histories.size (), 3);
                verifyMovedTo (histories.get (0), actual);
                verifyTookCustody (histories.get (1), actual);

                actual.location = location;
                actual.comment = null;
                verifyMovedTo (histories.get (2), actual);
            }

            if (!ContainerType.Plate.equals (actual.containerType)) {
                // test: go to holding detail page to verify location
                main.gotoContainerDetail (holding);
                detail.isCorrectPage ();
                actual = detail.parseHoldingDetail ();
                holding.location = coraTestUser;
                assertEquals (actual.children.size (), holding.children.size () - 1);
                verifyDetails (actual, holding);

                // test: go to holding history page to verify location
                detail.gotoHistory ();
                history.isCorrectPage ();
                histories = history.getHistories ();
                assertEquals (histories.size (), 1);
                verifyTookCustody (histories.get (0));
            }
        }

        // test: go to containers list for the given freezer and verify
        for (Container freezer : new Container[] { freezerAB018055, freezerAB018078, freezerAB039003 }) {
            main.showFreezerContents (freezer);
            Containers listContainers = list.getContainers ();
            for (Container c : containers.list) {
                Container child = c.containerType.equals (ContainerType.Plate) ? c : c.children.get (0);
                if (child.location.startsWith (freezer.name))
                    assertEquals (listContainers.findContainerByNumber (child).location, child.location);
            }
        }
    }
}
