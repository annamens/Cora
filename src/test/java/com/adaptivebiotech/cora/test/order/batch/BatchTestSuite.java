/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.batch;

import static com.adaptivebiotech.cora.utils.PageHelper.LinkShipment.SalesforceOrder;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.ClassLoader.getSystemResource;
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
    }

    @AfterMethod (alwaysRun = true)
    public void afterMethod (Method test) {
        if (!isNullOrEmpty (orderNumber.get ()))
            resetBatchOrder (orderNumber.get ());
    }

    public void happypath () {
        Map <String, Map <String, String>> sample = new HashMap <> ();
        sample.put ("SAMPLE_NAME", singletonMap ("workflow", "selenium-batch-eos-" + nanoTime ()));

        String intakeManifest = join ("/", downloadDir.get (), "eos-intakemanifest.xlsx");
        String preManifest = join ("/", downloadDir.get (), "eos-premanifest.xlsx");
        prepManifestFile (getSystemResource ("batch/eos-intakemanifest.xlsx").getPath (), intakeManifest, sample);
        prepManifestFile (getSystemResource ("batch/eos-premanifest.xlsx").getPath (), preManifest, sample);

        login.doLogin ();
        ordersList.isCorrectPage ();
        String shipmentNumber = newShipment.createBatchShipment (SalesforceOrder);
        testLog ("created a new batch shipment: " + shipmentNumber);

        accession.completeBatchAccession (intakeManifest);
        testLog ("completed the batch accession");

        batch.createBatchOrder (sfdcOrder, shipmentNumber, preManifest);
        orderNumber.set (batch.getOrderNumber ());
        testLog ("activated the batch order");
    }
}
