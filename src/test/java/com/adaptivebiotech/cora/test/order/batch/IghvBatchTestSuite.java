/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.batch;

import static com.adaptivebiotech.cora.utils.PageHelper.LinkShipment.SalesforceOrder;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.FINISHED;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSave;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSharing;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static java.lang.Boolean.TRUE;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.System.nanoTime;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.assertEquals;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.Batch;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.BatchAccession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class IghvBatchTestSuite extends BatchTestBase {

    private final String         sfdcOrder       = "10000534";
    private final String         tsvOverridePath = azPipelineNorth + "/210612_NB552467_0088_AH3CH7BGXJ/v3.1/20210614_0809/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3CH7BGXJ_0_CLINICAL-CLINICAL_96343-05BC.adap.txt.results.tsv.gz";
    private Login                login           = new Login ();
    private OrdersList           ordersList      = new OrdersList ();
    private NewShipment          newShipment     = new NewShipment ();
    private BatchAccession       accession       = new BatchAccession ();
    private Batch                batch           = new Batch ();
    private OrcaHistory          history         = new OrcaHistory ();
    private ThreadLocal <String> downloadDir     = new ThreadLocal <> ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
    }

    /**
     * @sdlc.requirements SR-9373:R1,R2,R3,R4
     */
    @Test (groups = "fox-terrier")
    public void happypath () {
        Map <String, String> samples = Stream.of (new String[][] {
                { "SAMPLE_NAME1", "selenium-batch-ighv-" + nanoTime () },
                { "SAMPLE_NAME2", "selenium-batch-ighv-" + nanoTime () },
                { "SAMPLE_NAME3", "selenium-batch-ighv-" + nanoTime () },
                { "SAMPLE_NAME4", "selenium-batch-ighv-" + nanoTime () },
                { "SAMPLE_NAME5", "selenium-batch-ighv-" + nanoTime () },
                { "SAMPLE_NAME6", "selenium-batch-ighv-" + nanoTime () }
        }).collect (toMap (data -> data[0], data -> data[1]));
        String intakeManifest = join ("/", downloadDir.get (), "ighv-intakemanifest.xlsx");
        String preManifest = join ("/", downloadDir.get (), "ighv-premanifest.xlsx");
        prepManifestFile (getSystemResource ("batch/ighv-intakemanifest.xlsx").getPath (), intakeManifest, samples);
        prepManifestFile (getSystemResource ("batch/ighv-premanifest.xlsx").getPath (), preManifest, samples);

        login.doLogin ();
        ordersList.isCorrectPage ();
        String shipmentNumber = newShipment.createBatchShipment (SalesforceOrder);
        testLog ("created a new batch shipment for IgHV: " + shipmentNumber);

        accession.completeBatchAccession (intakeManifest);
        testLog ("completed the batch accession for IgHV");

        batch.createBatchOrder (sfdcOrder, shipmentNumber, preManifest);
        testLog ("activated the batch order for IgHV");

        String orderNumber = batch.getOrderNumber ();
        samples.entrySet ().forEach (s -> {
            history.gotoOrderDebug (s.getValue ());
            history.isCorrectPage ();
            history.setWorkflowProperty (lastAcceptedTsvPath, tsvOverridePath);
            history.setWorkflowProperty (disableHiFreqSave, TRUE.toString ());
            history.setWorkflowProperty (disableHiFreqSharing, TRUE.toString ());
            history.forceStatusUpdate (SecondaryAnalysis, Ready);
            testLog (format ("set workflow for %s to %s/%s", s.getValue (), SecondaryAnalysis, Ready));

            history.waitFor (SecondaryAnalysis, Finished, FINISHED);
            history.waitFor (ShmAnalysis, Finished);
            history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
            testLog (format ("workflow is at %s/%s/%s for %s", ClonoSEQReport, Awaiting, CLINICAL_QC, s.getValue ()));

            List <Stage> stages = history.parseStatusHistory ();
            if (s.getKey ().equals ("SAMPLE_NAME6")) {
                stages.stream ()
                      .filter (stage -> ShmAnalysis.equals (stage.stageName))
                      .filter (stage -> Finished.equals (stage.stageStatus)).forEach (stage -> {
                          assertEquals (stage.subStatusMessage, "SHM Analysis is not enabled for the workflow.");
                      });
                testLog ("there is no ShmAnalysis performed for regression sample: " + s.getValue ());
            } else {
                stages.stream ()
                      .filter (stage -> ShmAnalysis.equals (stage.stageName))
                      .filter (stage -> Finished.equals (stage.stageStatus)).forEach (stage -> {
                          assertEquals (stage.subStatusMessage, "Saved result to shm_results table.");
                      });
                testLog ("there is ShmAnalysis performed for regression sample: " + s.getValue ());
            }
        });

        resetBatchOrder (orderNumber);
    }
}
