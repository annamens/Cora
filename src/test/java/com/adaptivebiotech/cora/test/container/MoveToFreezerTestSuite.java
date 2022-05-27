/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.container;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Plate;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.DryIce;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.TestHelper.randomWords;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.ContainerHistory;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.ContainersList;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.container.History;
import com.adaptivebiotech.cora.ui.container.MyCustody;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.BatchAccession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

@Test (groups = "regression")
public class MoveToFreezerTestSuite extends ContainerTestBase {

    private ThreadLocal <String> downloadDir   = new ThreadLocal <> ();
    private Login                login         = new Login ();
    private OrdersList           ordersList    = new OrdersList ();
    private ContainersList       containerList = new ContainersList ();
    private Detail               detail        = new Detail ();
    private History              history       = new History ();
    private MyCustody            myCustody     = new MyCustody ();
    private NewShipment          shipment      = new NewShipment ();
    private BatchAccession       accession     = new BatchAccession ();

    @BeforeMethod
    public void beforeMethod (Method test) {
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc.requirements 126.MoveMetadata
     */
    public void move_primary_to_freezer () {
        String comment = randomWords (10);
        Containers containers = coraApi.addContainers (new Containers (
                stream (ContainerType.values ()).filter (t -> !t.isHolding)
                                                .map (t -> container (t))
                                                .collect (toList ())));

        ordersList.clickContainers ();
        Container freezer;
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
            containerList.moveToFreezer (primary, freezer);

            // test: go to detail page to verify location
            ordersList.gotoContainerDetail (primary);
            detail.isCorrectPage ();
            Container actual = detail.parsePrimaryDetail ();
            actual.comment = comment;
            assertTrue (primary.location.startsWith (freezer.location));
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
            ordersList.searchContainer (primary);
            Containers listContainers = containerList.getContainers ();
            assertEquals (listContainers.findContainerByNumber (primary).location, primary.location);
        }
        coraApi.deactivateContainers (containers);
    }

    /**
     * @sdlc.requirements 126.MoveMetadata
     */
    public void move_child_to_freezer () {
        String manifestName = "intakemanifest_holding_w_child";
        String manifestTemplatePath = getSystemResource (format ("batch/%s_template.xlsx", manifestName)).getPath ();
        File manifestFileName = new File (join ("/", downloadDir.get (), manifestName + ".xlsx"));
        DateTimeFormatter fmt = ofPattern ("yyddhhmmss");

        try (FileInputStream inputStream = new FileInputStream (manifestTemplatePath);
                Workbook workbook = WorkbookFactory.create (inputStream);
                FileOutputStream outputStream = openOutputStream (manifestFileName)) {
            Sheet sheet = workbook.getSheetAt (0);
            sheet.getRow (38).getCell (9).setCellValue (now ().format (fmt));
            sheet.getRow (39).getCell (9).setCellValue (now ().plusSeconds (1L).format (fmt));
            workbook.write (outputStream);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }

        // setup for holding containers with children
        ordersList.selectNewBatchShipment ();
        shipment.isBatchOrGeneral ();
        shipment.enterShippingCondition (DryIce);
        shipment.clickSave ();
        shipment.clickAccessionTab ();

        accession.isCorrectPage ();
        accession.uploadIntakeManifest (manifestFileName.getAbsolutePath ());
        accession.clickIntakeComplete ();
        accession.clickShipmentTab ();
        Containers containers = shipment.getBatchContainers ();

        Container freezer;
        for (Container holding : containers.list) {
            Container child = holding.containerType.equals (Plate) ? holding : holding.children.get (0);
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
                location = join (" : ", coraTestUser, child.root.containerNumber, "Position A:1");
                break;
            case SlideBox5:
            case SlideBox5CS:
            case SlideTube:
            case SlideTubeCS:
            case OtherSlideBox:
            case OtherSlideBoxCS:
                location = join (" : ", coraTestUser, child.root.containerNumber);
                break;
            default:
                location = join (" : ", coraTestUser, child.root.containerNumber, "Position 1");
                break;
            }
            shipment.clickContainers ();
            containerList.moveToFreezer (child, freezer);

            // test: go to child detail page to verify location
            containerList.gotoContainerDetail (child);
            detail.isCorrectPage ();
            Container actual = detail.parsePrimaryDetail ();
            actual.comment = child.comment;
            assertTrue (actual.location.startsWith (freezer.location));
            verifyDetails (actual, child);

            // test: go to child history page to verify location
            detail.gotoHistory ();
            history.isCorrectPage ();
            List <ContainerHistory> histories = history.getHistories ();
            if (Plate.equals (actual.containerType)) {
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

            if (!Plate.equals (actual.containerType)) {
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
        containers.list.forEach (c -> {
            ordersList.searchContainer (c);
            Containers listContainers = containerList.getContainers ();
            String location = c.containerType.equals (Plate) ? c.location : join (" : ", c.location, c.containerNumber);
            assertEquals (listContainers.findContainerByNumber (c).location, location);
        });

        // cleanup
        ordersList.gotoMyCustody ();
        myCustody.isCorrectPage ();
        containers.list.forEach (c -> {
            if (!coraTestUser.equals (c.location))
                myCustody.takeCustody (c);
        });
        myCustody.sendContainersToFreezer (containers, freezerDestroyed);
    }
}
