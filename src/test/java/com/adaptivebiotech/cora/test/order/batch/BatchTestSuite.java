/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.batch;

import static com.adaptivebiotech.cora.utils.PageHelper.LinkShipment.SalesforceOrder;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.join;
import static java.lang.System.nanoTime;
import static java.util.Collections.singletonMap;
import static org.testng.util.Strings.isNullOrEmpty;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Batch;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.BatchAccession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class BatchTestSuite extends BatchTestBase {

    private final String         sfdcOrder   = "10000535";
    private Login                login       = new Login ();
    private OrdersList           ordersList  = new OrdersList ();
    private NewShipment          newShipment = new NewShipment ();
    private BatchAccession       accession   = new BatchAccession ();
    private Batch                batch       = new Batch ();
    private ThreadLocal <String> downloadDir = new ThreadLocal <> ();
    private ThreadLocal <String> orderNumber = new ThreadLocal <> ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    @AfterMethod (alwaysRun = true)
    public void afterMethod (Method test) {
        if (!isNullOrEmpty (orderNumber.get ()))
            resetBatchOrder (orderNumber.get ());
    }

    @Test (groups = "smoke")
    public void happypath () {
        Map <String, Map <String, String>> sample = new HashMap <> ();
        sample.put ("SAMPLE_NAME", singletonMap ("workflow", "selenium-batch-eos-" + nanoTime ()));

        String intakeManifest = join ("/", downloadDir.get (), "eos-intakemanifest.xlsx");
        String preManifest = join ("/", downloadDir.get (), "eos-premanifest.xlsx");
        prepManifestFile ("batch/eos-intakemanifest.xlsx", intakeManifest, sample);
        prepManifestFile ("batch/eos-premanifest.xlsx", preManifest, sample);

        String shipmentNumber = newShipment.createBatchShipment (SalesforceOrder);
        testLog ("created a new batch shipment: " + shipmentNumber);

        newShipment.selectSpecimenContainerType (ContainerType.TubeBox10x10);
        newShipment.setContainerName (1, "Box1");
        newShipment.clickSave ();
        newShipment.clickAccessionTab ();
        accession.uploadIntakeManifest (intakeManifest);
        accession.clickCreateIntakeDetails ();
        accession.completeBatchAccession ();
        testLog ("completed the batch accession");

        batch.createBatchOrder (sfdcOrder, shipmentNumber, preManifest);
        orderNumber.set (batch.getOrderNumber ());
        testLog ("activated the batch order");
    }

    public void allContainersIntakeComplete () {
        String shipmentNumber = newShipment.createBatchShipment (SalesforceOrder);
        testLog ("created a new batch shipment: " + shipmentNumber);

        Map <ContainerType, String> containerMap = new HashMap <ContainerType, String> ();

        containerMap.put (ContainerType.TubeBox5x5, "TB5x5");
        containerMap.put (ContainerType.TubeBox5x10, "TB5x10");
        containerMap.put (ContainerType.TubeBox9x9, "TB9x9");
        containerMap.put (ContainerType.TubeBox10x10, "TB10x10");
        containerMap.put (ContainerType.VacutainerBox7x7, "VB7x7");
        containerMap.put (ContainerType.ConicalBox6x6, "CB6x6");
        containerMap.put (ContainerType.Plate, "96plate");
        containerMap.put (ContainerType.SlideBox5, "5SBWO");
        containerMap.put (ContainerType.SlideBox5CS, "5SBW");
        containerMap.put (ContainerType.SlideBox25, "25SBWO");
        containerMap.put (ContainerType.SlideBox25CS, "25SBW");
        containerMap.put (ContainerType.SlideBox100, "100SBWO");
        containerMap.put (ContainerType.SlideBox100CS, "100SBW");
        containerMap.put (ContainerType.SlideTube, "STWO");
        containerMap.put (ContainerType.SlideTubeCS, "STW");
        containerMap.put (ContainerType.OtherSlideBox, "OSBWO");
        containerMap.put (ContainerType.OtherSlideBoxCS, "OSBW");

        int i = 1;
        for (Map.Entry <ContainerType, String> entry : containerMap.entrySet ()) {
            newShipment.selectSpecimenContainerType (entry.getKey ());
            newShipment.setContainerName (i, entry.getValue ());
            i++;
        }

        newShipment.clickSave ();
        newShipment.clickAccessionTab ();
        accession.uploadIntakeManifest ("batch/allContainers-intakemanifest.xlsx");
        accession.clickCreateIntakeDetails ();
        accession.completeBatchAccession ();
        testLog ("completed the batch accession with all container types");
    }

}
