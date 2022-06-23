/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.batch;

import static com.adaptivebiotech.cora.utils.PageHelper.LinkShipment.SalesforceOrder;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.Clarity;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthPipeline;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.FINISHED;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSave;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSharing;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastFinishedPipelineJobId;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.sampleName;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.workspaceName;
import static java.lang.Boolean.TRUE;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.assertEquals;
import static org.testng.util.Strings.isNullOrEmpty;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.collections4.map.HashedMap;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.Batch;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.BatchAccession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.test.utils.PageHelper.StageName;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class IghvBatchTestSuite extends BatchTestBase {

    private final String         sfdcOrder   = "10000534";
    private Login                login       = new Login ();
    private OrdersList           ordersList  = new OrdersList ();
    private NewShipment          newShipment = new NewShipment ();
    private BatchAccession       accession   = new BatchAccession ();
    private Batch                batch       = new Batch ();
    private OrcaHistory          history     = new OrcaHistory ();
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

    /**
     * @sdlc.requirements SR-9373:R3,R4,R7
     */
    @Test (groups = "golden-retriever")
    public void happypath () {
        String tsvCliafmt = azPipelineClia + "/210612_NB552467_0088_AH3CH7BGXJ/v3.1/20210614_0809/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3CH7BGXJ_0_CLINICAL-CLINICAL_%s.adap.txt.results.tsv.gz";
        String IvdFmt = azPipelineFda + "/210612_NB552467_0088_AH3CH7BGXJ/v3.1/20210614_0746/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev23/H3CH7BGXJ_0_CLINICAL-CLINICAL_%s.adap.txt.results.tsv.gz";
        Map <String, Map <String, String>> samples = new HashedMap <> ();
        samples.put ("SAMPLE_NAME1",
                     Stream.of (new String[][] {
                             { "workflow", "selenium-ISS80020-" + nanoTime () },
                             { "sampleName", "107303-01MC" },
                             { "pipelineJobId", "8a7a94db77a26ee1017a01c874c67394" },
                             { "tsvOverridePath", format (tsvCliafmt, "107303-01MC") }
                     }).collect (toMap (data -> data[0], data -> data[1])));
        samples.put ("SAMPLE_NAME2",
                     Stream.of (new String[][] {
                             { "workflow", "selenium-ISS80021-" + nanoTime () },
                             { "sampleName", "109687-01MC" },
                             { "pipelineJobId", "8a7a958877a26e74017a0767439c592c" },
                             { "tsvOverridePath", format (IvdFmt, "109687-01MC") }
                     }).collect (toMap (data -> data[0], data -> data[1])));
        samples.put ("SAMPLE_NAME3",
                     Stream.of (new String[][] {
                             { "workflow", "selenium-ISS80022-" + nanoTime () },
                             { "sampleName", "108313-02MC-2426306" },
                             { "pipelineJobId", "8a7a958877a26e74017a0767439c592c" },
                             { "tsvOverridePath", format (IvdFmt, "108313-02MC-2426306") }
                     }).collect (toMap (data -> data[0], data -> data[1])));
        samples.put ("SAMPLE_NAME4",
                     Stream.of (new String[][] {
                             { "workflow", "selenium-ISS80023-" + nanoTime () },
                             { "sampleName", "54198-04MC" },
                             { "pipelineJobId", "8a7a94db77a26ee1017a01c874c67394" },
                             { "tsvOverridePath", format (tsvCliafmt, "54198-04MC") }
                     }).collect (toMap (data -> data[0], data -> data[1])));
        samples.put ("SAMPLE_NAME5",
                     Stream.of (new String[][] {
                             { "workflow", "selenium-ISS80006-" + nanoTime () },
                             { "sampleName", "96343-05BC" },
                             { "pipelineJobId", "8a7a94db77a26ee1017a01c874c67394" },
                             { "tsvOverridePath", format (tsvCliafmt, "96343-05BC") }
                     }).collect (toMap (data -> data[0], data -> data[1])));

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
        orderNumber.set (batch.getOrderNumber ());
        testLog ("activated the batch order for IgHV: " + orderNumber.get ());

        samples.entrySet ().forEach (s -> {
            history.gotoOrderDebug (s.getValue ().get ("workflow"));
            history.setWorkflowProperty (lastAcceptedTsvPath, s.getValue ().get ("tsvOverridePath"));
            history.setWorkflowProperty (lastFinishedPipelineJobId, s.getValue ().get ("pipelineJobId"));
            history.setWorkflowProperty (sampleName, s.getValue ().get ("sampleName"));
            history.setWorkflowProperty (workspaceName, "CLINICAL-CLINICAL");
            history.setWorkflowProperty (disableHiFreqSave, TRUE.toString ());
            history.setWorkflowProperty (disableHiFreqSharing, TRUE.toString ());
            history.forceStatusUpdate (NorthQC, Ready);
            testLog (format ("[%s] set workflow to %s/%s", s.getValue ().get ("workflow"), NorthQC, Ready));
        });

        samples.entrySet ().forEach (s -> {
            String sample = s.getValue ().get ("workflow");
            history.gotoOrderDebug (s.getValue ().get ("workflow"));
            history.waitFor (NorthQC, Finished);
            history.waitFor (SecondaryAnalysis, Finished, FINISHED);

            if (s.getKey ().equals ("SAMPLE_NAME5")) {
                assertEquals (history.parseStatusHistory ().stream ()
                                     .filter (stage -> ShmAnalysis.equals (stage.stageName)).count (),
                              0);
                testLog (format ("[%s] there is no ShmAnalysis performed for regression", sample));

                List <StageName> stages = asList (Clarity, NorthPipeline, NorthQC, SecondaryAnalysis);
                assertEquals (history.getWorkflowStages (), stages);
                testLog (format ("[%s] found this workflow config: %s", sample, stages));
            } else {
                history.waitFor (ShmAnalysis, Finished);
                testLog (format ("[%s] workflow is at %s/%s", ShmAnalysis, Finished, sample));

                history.parseStatusHistory ().stream ()
                       .filter (stage -> ShmAnalysis.equals (stage.stageName))
                       .filter (stage -> Finished.equals (stage.stageStatus)).forEach (stage -> {
                           assertEquals (stage.subStatusMessage, "Saved result to shm_results table.");
                       });
                testLog (format ("[%s] there is ShmAnalysis performed for regression sample: ", sample));

                List <StageName> stages = asList (Clarity, NorthPipeline, NorthQC, SecondaryAnalysis, ShmAnalysis);
                assertEquals (history.getWorkflowStages (), stages);
                testLog (format ("[%s] found this workflow config: %s", sample, stages));
            }
        });
    }
}
