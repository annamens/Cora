/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.CLEP_clonoseq;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.EDTA;
import static com.adaptivebiotech.cora.test.CoraEnvironment.portalIvdTestUrl;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Fail;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.INDETERMINATE;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.MUTATED;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.NO_CLONES;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.QC_FAILURE;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.UNMUTATED;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.isIVD;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.portalTestUrl;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.IGH;
import static com.adaptivebiotech.test.utils.DateHelper.genDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BCells;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.LymphNode;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.PBMC;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellPellet;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellSuspension;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.FFPEScrolls;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Failed;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastFinishedPipelineJobId;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.sampleName;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.Boolean.TRUE;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.ShmResultData;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus;
import com.adaptivebiotech.picasso.dto.ReportRender.ShmSequence;
import com.adaptivebiotech.pipeline.api.PipelineApi;
import com.adaptivebiotech.pipeline.dto.Sample;
import com.adaptivebiotech.pipeline.dto.shm.SHMheader.EricSampleCall;
import com.adaptivebiotech.pipeline.dto.shm.ShmResult;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Note:
 * - this feature is in Prod, we don't need a test for feature flag off anymore
 * 
 * @author Jaydeepkumar Patel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "nutmeg" })
public class IghvOrderTestSuite extends NewOrderTestBase {

    private Physician            IgHVPhysician;
    private Physician            NYPhysician;
    private NewOrderClonoSeq     diagnostic                       = new NewOrderClonoSeq ();
    private ReportClonoSeq       reportClonoSeq                   = new ReportClonoSeq ();
    private OrcaHistory          history                          = new OrcaHistory ();
    private OrderStatus          orderStatus                      = new OrderStatus ();
    private OrderDetailClonoSeq  orderDetailClonoSeq              = new OrderDetailClonoSeq ();

    private final String         c91_10                           = "C91.10";
    private final String         c83_00                           = "C83.00";
    private final String         c90_00                           = "C90.00";

    private final String         tsvOverridePathO1O2              = azPipelineNorth + "/210612_NB552467_0088_AH3CH7BGXJ/v3.1/20210614_0809/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3CH7BGXJ_0_CLINICAL-CLINICAL_96343-05BC.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathO3O4              = azPipelineFda + "/210615_NB551732_0294_AH3G53BGXJ/v3.1/20210617_0828/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev24/H3G53BGXJ_0_CLINICAL-CLINICAL_96633-08MC-UA001BM.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathO5O6O7O8          = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathOrcaIgHVO1O8      = azPipelineNorth + "/210605_NB552488_0035_AHFFJ2BGXJ/v3.1/20210607_1834/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/HFFJ2BGXJ_0_CLINICAL-CLINICAL_01159-11MC.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathOrcaIgHVO2O6O7    = azPipelineFda + "/210603_NB552480_0036_AH2C2LBGXJ/v3.1/20210605_1317/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev23/H2C2LBGXJ_0_CLINICAL-CLINICAL_111034-01LC.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathOrcaIgHVO3        = azPipelineFda + "/210608_NB500953_0936_AH3G3KBGXJ/v3.1/20210610_0431/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev23/H3G3KBGXJ_0_CLINICAL-CLINICAL_111730-01MC-2115301589D.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathOrcaIgHVO4        = azPipelineNorth + "/210602_NB552492_0027_AH3GK5BGXJ/v3.1/20210604_2027/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3GK5BGXJ_0_CLINICAL-CLINICAL_102589-01MC-B20-229.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathOrcaIgHVO5        = azPipelineFda + "/210602_NB552492_0027_AH3GK5BGXJ/v3.1/20210604_1958/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev23/H3GK5BGXJ_0_CLINICAL-CLINICAL_109306-01MC-jb20-67.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathOrcaIgHVO9        = azPipelineNorth + "/180122_NB501661_0323_AH3KF2BGX5/v3.0/20180124_1229/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev4/H3KF2BGX5_0_MDAnderson-Thompson_PH-5N.adap.txt.results.tsv.gz";
    private final String         lastFinishedPipelineJobIdO1O2    = "8a7a94db77a26ee1017a01c874c67394";
    private final String         lastFinishedPipelineJobIdO3O4    = "8a7a958877a26e74017a176ecd2b1b45";
    private final String         lastFinishedPipelineOrcaIgHVO1O8 = "8a7a94db77a26ee10179dfbd004f5955";
    private final String         lastFinishedPipelineOrcaIgHVO6   = "12345678901234567890";
    private final String         lastFinishedPipelineOrcaIgHVO2O7 = "8a7a958877a26e740179d9e8beaf3b48";
    private final String         lastFinishedPipelineOrcaIgHVO3   = "8a7a958877a26e740179f2dbc180183a";
    private final String         lastFinishedPipelineOrcaIgHVO4   = "8a7a94db77a26ee10179d04ea1a739e9";
    private final String         lastFinishedPipelineOrcaIgHVO5   = "8a7a958877a26e740179d5e2e51821ce";
    private final String         sampleNameO1O2                   = "96343-05BC";
    private final String         sampleNameO3O4                   = "96633-08MC-UA001BM";
    private final String         sampleNameOrcaIgHVO1O8           = "01159-11MC";
    private final String         sampleNameOrcaIgHVO2O6O7         = "111034-01LC";
    private final String         sampleNameOrcaIgHVO3             = "111730-01MC-2115301589D";
    private final String         sampleNameOrcaIgHVO4             = "102589-01MC-B20-229";
    private final String         sampleNameOrcaIgHVO5             = "109306-01MC-jb20-67";
    private final String         sampleNameOrcaIgHVO9             = "PH-5N";
    private final String         shmDataSourcePathOrcaIgHVO9      = azPipelineNorth + "/180122_NB501661_0323_AH3KF2BGX5/v3.0/20180124_1229";
    private final String         workSpaceNameOrcaIgHVO9          = "MDAnderson-Thompson";

    private final String         noResultsAvailable               = "No result available";
    private final String         beginIghvMutationStatus          = "IGHV MUTATION STATUS";
    private final String         beginClonalityResult             = "CLONALITY RESULT";
    private final String         endThisSampleFailed              = "This sample failed the quality control";
    private ThreadLocal <String> downloadDir                      = new ThreadLocal <> ();

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();

        // IgHVPhysician Physician
        IgHVPhysician = coraApi.getPhysician (non_CLEP_clonoseq);

        // NY Physician
        NYPhysician = coraApi.getPhysician (CLEP_clonoseq);
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));

        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
    }

    /**
     * Note:
     * - SR-T3689
     * - non-CLEP physician
     * - specimen type / source: Cell pellet / PBMC
     * - icd codes: C83.00
     */
    public void verifyIgHVStageAndReportFeatureOrder1CLIA () {
        Assay assayTest = ID_BCell2_CLIA;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          CellPellet,
                                          PBMC,
                                          new String[] { c83_00, c91_10 },
                                          "Order 1 Flag On");

        validateFlagsOnDebugPage (orderDetails.specimenDto.sampleName, "true", "true");
        testLog ("step 1 - ighvAnalysisEnabled and ighvReportEnabled are true");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathO1O2,
                           lastFinishedPipelineJobIdO1O2,
                           sampleNameO1O2,
                           null,
                           null);
        testLog ("step 2 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 2 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest);
        assertTrue (isCLIAIGHVFlagPresent);
        testLog ("step 3 - CLIA-IGHV flag appears just below the Report tab ");

        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertNotNull (reportData.shmReportResult);
        testLog ("step 4 - SHM analysis results are included in reportData.json within shmReportResult property");
    }

    /**
     * Note:
     * - SR-T3689
     * - CLEP physician
     * - specimen type / source: Cell pellet / PBMC
     * - icd codes: C83.00
     * 
     * @sdlc.requirements SR-6656:R1, R3, R4, R5, R6
     */
    public void verifyIgHVStageAndReportFeatureOrder2CLIA () {
        Assay assayTest = ID_BCell2_CLIA;
        Order orderDetails = createOrder (NYPhysician,
                                          assayTest,
                                          CellPellet,
                                          PBMC,
                                          new String[] { c83_00 },
                                          "Order 2 Flag On");

        validateFlagsOnDebugPage (orderDetails.specimenDto.sampleName, "false", "true");
        testLog ("step 5 - ighvAnalysisEnabled is true, ighvReportEnabled is false (or absent)");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathO1O2,
                           lastFinishedPipelineJobIdO1O2,
                           sampleNameO1O2,
                           null,
                           null);
        testLog ("step 6 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 6 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest);
        assertFalse (isCLIAIGHVFlagPresent);
        testLog ("step 7 - CLIA-IGHV flag does not appear below the Report tab ");

        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertNull (reportData.shmReportResult);
        testLog ("step 8 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Note:
     * - SR-T3689
     * - non-CLEP physician
     * - specimen type / source: gDNA / Bone Marrow
     * - icd codes: C91.10
     * 
     * @sdlc.requirements SR-6656:R1, R3, R4, R5, R6
     */
    public void verifyIgHVStageAndReportFeatureOrder3IVD () {
        Assay assayTest = ID_BCell2_IVD;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          gDNA,
                                          BoneMarrow,
                                          new String[] { c91_10 },
                                          "Order 3 Flag On");

        validateFlagsOnDebugPage (orderDetails.specimenDto.sampleName, "true", "true");
        testLog ("step 9, order3 - ighvAnalysisEnabled and ighvReportEnabled are true");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathO3O4,
                           lastFinishedPipelineJobIdO3O4,
                           sampleNameO3O4,
                           null,
                           null);
        testLog ("step 10 - 1 - order3 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 10 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest);
        assertTrue (isCLIAIGHVFlagPresent);
        testLog ("step 11, order3 - CLIA-IGHV flag appears below the Report tab ");

        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertNotNull (reportData.shmReportResult);
        testLog ("step 12, order3 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Note:
     * - SR-T3689
     * - non-CLEP physician
     * - specimen type / source: Blood
     * - icd codes: C91.10
     * 
     * @sdlc.requirements SR-6656:R1, R3, R4, R5, R6
     */
    public void verifyIgHVStageAndReportFeatureOrder4IVD () {
        Assay assayTest = ID_BCell2_IVD;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c91_10 },
                                          "Order 4 Flag On");

        validateFlagsOnDebugPage (orderDetails.specimenDto.sampleName, "true", "true");
        testLog ("step 9, order4 - ighvAnalysisEnabled and ighvReportEnabled are true");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathO3O4,
                           lastFinishedPipelineJobIdO3O4,
                           sampleNameO3O4,
                           null,
                           null);
        testLog ("step 10 - 1 - order4 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 10 - 2 - order4 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest);
        assertTrue (isCLIAIGHVFlagPresent);
        testLog ("step 11, order3 - CLIA-IGHV flag appears below the Report tab ");

        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertNotNull (reportData.shmReportResult);
        testLog ("step 12, order3 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Note:
     * - SR-T3689
     * - non-CLEP physician
     * - specimen type / source: FFPE Scrolls / Lymph Node
     * - icd codes: C83.00
     * 
     * @sdlc.requirements SR-6656:R1, R3, R4, R5, R6
     */
    public void verifyIgHVStageAndReportFeatureOrder5CLIA () {
        Order orderDetails = createOrder (IgHVPhysician,
                                          ID_BCell2_CLIA,
                                          FFPEScrolls,
                                          LymphNode,
                                          new String[] { c83_00, c91_10 },
                                          "Order 6 Flag On");

        validateFlagsOnDebugPage (orderDetails.specimenDto.sampleName, null, null);
        testLog ("step 13, order5 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);

        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 14 - 1 - order5 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order5 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Note:
     * - SR-T3689
     * - non-CLEP physician
     * - specimen type / source: Cell Suspension / B cells
     * - icd codes: C91.10
     * 
     * @sdlc.requirements SR-6656:R1, R3, R4, R5, R6
     */
    public void verifyIgHVStageAndReportFeatureOrder6IVD () {
        Order orderDetails = createOrder (IgHVPhysician,
                                          ID_BCell2_CLIA,
                                          CellSuspension,
                                          BCells,
                                          new String[] { c91_10 },
                                          "Order 6 Flag On");

        validateFlagsOnDebugPage (orderDetails.specimenDto.sampleName, null, null);
        testLog ("step 13, order6 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);

        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 14 - 1 - order6 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order6 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Note:
     * - SR-T3689
     * - non-CLEP physician
     * - specimen type / source: Cell pellet / PBMC
     * - icd codes: C90.00
     * 
     * @sdlc.requirements SR-6656:R1, R3, R4, R5, R6
     */
    public void verifyIgHVStageAndReportFeatureOrder7IVD () {
        Order orderDetails = createOrder (IgHVPhysician,
                                          ID_BCell2_CLIA,
                                          CellPellet,
                                          PBMC,
                                          new String[] { c90_00 },
                                          "Order 7 Flag On");

        validateFlagsOnDebugPage (orderDetails.specimenDto.sampleName, null, null);
        testLog ("step 13, order7 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);

        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 14 - 1 - order7 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order7 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Note:
     * - SR-T3728
     * - mutation status: MUTATED
     * 
     * @sdlc.requirements SR-7163:R1, R3, R4, SR-7029:R1
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder1 () {
        Assay assayTest = ID_BCell2_CLIA;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          CellPellet,
                                          PBMC,
                                          new String[] { c83_00 },
                                          "Order 1 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO1O8,
                           lastFinishedPipelineOrcaIgHVO1O8,
                           sampleNameOrcaIgHVO1O8,
                           null,
                           null);
        testLog ("step 1 - order 1 - After SecondaryAnalysis stage, the workflow moved to the ShmAnalysis stage prior to ClonoSEQReport stage");

        releaseReport (assayTest);
        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertEquals (reportData.shmReportResult.mutationStatus, MUTATED);
        assertTrue (reportData.shmReportResult.shmSequenceList.size () >= 1);
        ShmSequence shmSequence = reportData.shmReportResult.shmSequenceList.get (0);
        assertEquals (shmSequence.locus, IGH);
        assertEquals (shmSequence.sequence,
                      "CAGAGACAACGCCAAGAATTCAGTGTATCTTCAAATGGACAGTTTGAGAGTCGAAGACACGGCTACATATTACTGTGCGAGAGACTTATTAACCTCTAGAGCAGCAGCTGGAACAGTAGCTTTTGACATCTGGGGCCAAGGGACA");
        assertEquals (shmSequence.percentMutation.doubleValue (), 12.2448980808d);
        assertTrue (shmSequence.productive);
        assertEquals (shmSequence.vSegment, "IGHV3-21*01");
        testLog ("step 2 - order 1 - validate mutationStatus, shmSequenceListproperty of shmReportResult");

        validateShmResultReportType (orderDetails.orderTestId, reportData.shmReportResult.mutationStatus);
        testLog ("step 4 - order 1 - validate DB report_type matches reportData.json shmReportResult.mutationStatus");
    }

    /**
     * Note:
     * - SR-T3728
     * - mutation status: UNMUTATED
     * 
     * @sdlc.requirements SR-7163:R3, SR-7028:R1
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder2 () {
        Assay assayTest = ID_BCell2_IVD;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c91_10 },
                                          "Order 2 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO2O6O7,
                           lastFinishedPipelineOrcaIgHVO2O7,
                           sampleNameOrcaIgHVO2O6O7,
                           null,
                           null);

        validateShmResultReportType (orderDetails.orderTestId, UNMUTATED);
        testLog ("step 5 - order 2 - Value for orca.shm_results.report_type is UNMUTATED");
    }

    /**
     * Note:
     * - SR-T3728
     * - mutation status: INDETERMINATE
     * 
     * @sdlc.requirements SR-7163:R3, SR-7028:R1
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder3 () {
        Assay assayTest = ID_BCell2_IVD;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c91_10 },
                                          "Order 3 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO3,
                           lastFinishedPipelineOrcaIgHVO3,
                           sampleNameOrcaIgHVO3,
                           null,
                           null);

        validateShmResultReportType (orderDetails.orderTestId, INDETERMINATE);
        testLog ("step 6 - order 3 - Value for orca.shm_results.report_type is INDETERMINATE");
    }

    /**
     * Note:
     * - SR-T3728
     * - mutation status: NO_CLONES
     * 
     * @sdlc.requirements SR-7163:R3, SR-7028:R1
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder4 () {
        Assay assayTest = ID_BCell2_CLIA;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c83_00 },
                                          "Order 4 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO4,
                           lastFinishedPipelineOrcaIgHVO4,
                           sampleNameOrcaIgHVO4,
                           null,
                           null);

        validateShmResultReportType (orderDetails.orderTestId, NO_CLONES);
        testLog ("step 7 - order 4 - Value for orca.shm_results.report_type is NO_CLONES");
    }

    /**
     * Note:
     * - SR-T3728
     * - mutation status: QC_FAILURE
     * - Indeterminate SHM analysis result
     * - primary analysis passed, all clones have qc flags, no results available (QC failure)
     * 
     * @sdlc.requirements SR-7163:R3, SR-7029:R1
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder5 () {
        Assay assayTest = ID_BCell2_IVD;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c91_10 },
                                          "Order 5 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO5,
                           lastFinishedPipelineOrcaIgHVO5,
                           sampleNameOrcaIgHVO5,
                           null,
                           null);

        releaseReport (assayTest);

        String pdfUrl = reportClonoSeq.getReleasedReportPdfUrl ();
        info ("PDF File URL: " + pdfUrl);
        String extractedText = getTextFromPDF (pdfUrl, 4, beginIghvMutationStatus, endThisSampleFailed);
        assertTrue (extractedText.contains (noResultsAvailable));
        testLog ("step 8 - order 5 - In SHM report of the pdf report, it is showing No Result Available for the IGHV Mutation Status");

        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertEquals (reportData.shmReportResult.mutationStatus, QC_FAILURE);
        testLog ("step 9 - order 5 - mutationStatus property contains the value QC_FAILURE");

        validateShmResultReportType (orderDetails.orderTestId, QC_FAILURE, EricSampleCall.INDETERMINATE);
        testLog ("step 10.1 - order 5 - 1 DB record with orca.shm_results.report_type is QC_FAILURE");
        testLog ("step 10.2 - order 5 - 1 DB record with value of 'ericSampleCall' in orca.shm_results.shm_result is INDETERMINATE");
    }

    /**
     * Note:
     * - SR-T3728
     * - mutation status: QC_FAILURE
     * - primary analysis failed
     * 
     * @sdlc.requirements SR-7163:R3, SR-7029:R1
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder6 () {
        Assay assayTest = ID_BCell2_IVD;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c91_10 },
                                          "Order 6 Orca Work");

        // debug page - get workflow properties
        history.gotoOrderDebug (orderDetails.specimenDto.sampleName);

        // set workflow property and force status update
        history.setWorkflowProperty (lastAcceptedTsvPath, tsvOverridePathOrcaIgHVO2O6O7);
        history.setWorkflowProperty (lastFinishedPipelineJobId, lastFinishedPipelineOrcaIgHVO6);
        history.setWorkflowProperty (sampleName, sampleNameOrcaIgHVO2O6O7);
        history.forceStatusUpdate (NorthQC, Failed);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        assertTrue (history.isStagePresent (ClonoSEQReport, Awaiting, CLINICAL_QC));

        releaseReport (assayTest);
        String pdfUrl = reportClonoSeq.getReleasedReportPdfUrl ();
        info ("PDF File URL: " + pdfUrl);
        String extractedText = getTextFromPDF (pdfUrl, 1, beginClonalityResult, endThisSampleFailed);
        assertTrue (extractedText.contains (noResultsAvailable));
        testLog ("step 11 - order 6 - Clonality Result for workflow with failed Primary Analysis (NorthQC) displays No Result Available");

        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertEquals (reportData.shmReportResult.mutationStatus, QC_FAILURE);
        testLog ("step 12 - order 6 - mutationStatus property contains the value QC_FAILURE");

        assertNull (coraDb.getShmResult (orderDetails.orderTestId));
        testLog ("step 13 - order 6 - Query has no rows returned");

    }

    /**
     * Note:
     * - SR-T3728
     * - mutation status: QC_FAILURE
     * - Clinical QC failed
     * 
     * @sdlc.requirements SR-7163:R3, SR-7029:R1
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder7 () {
        Assay assayTest = ID_BCell2_IVD;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c91_10 },
                                          "Order 7 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO2O6O7,
                           lastFinishedPipelineOrcaIgHVO2O7,
                           sampleNameOrcaIgHVO2O6O7,
                           null,
                           null);

        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        orderStatus.isCorrectPage ();
        orderStatus.clickReportTab (assayTest);
        reportClonoSeq.isCorrectPage ();
        reportClonoSeq.setQCstatus (Fail);

        // we will jave multiple ClonoSEQReport/Awaiting/CLINICAL_QC stages, look for the last one
        history.gotoOrderDebug (orderDetails.specimenDto.sampleName);
        history.waitForTopLevel (ClonoSEQReport, Awaiting, CLINICAL_QC);

        releaseReport (assayTest);
        String pdfUrl = reportClonoSeq.getReleasedReportPdfUrl ();
        info ("PDF File URL: " + pdfUrl);
        String extractedText = getTextFromPDF (pdfUrl, 1, beginClonalityResult, endThisSampleFailed);
        assertTrue (extractedText.contains (noResultsAvailable));
        testLog ("step 14 - order 7 - Clonality Result for workflow with failed Clinical QC displays No Result Available");

        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertEquals (reportData.shmReportResult.mutationStatus, QC_FAILURE);
        testLog ("step 15 - order 7 - mutationStatus property contains the value QC_FAILURE");

        validateShmResultReportType (orderDetails.orderTestId, UNMUTATED);
        testLog ("step 16 - order 7 - Value for orca.shm_results.report_type is UNMUTATED");
    }

    /**
     * Note:
     * - SR-T3728
     * - no ShmAnalysis
     * 
     * @sdlc.requirements SR-7163:R1, R3, R4
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder8 () {
        Assay assayTest = ID_BCell2_CLIA;
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          CellPellet,
                                          PBMC,
                                          new String[] { c90_00 },
                                          "Order 8 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO1O8,
                           lastFinishedPipelineOrcaIgHVO1O8,
                           sampleNameOrcaIgHVO1O8,
                           null,
                           null);
        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 17.1 - order 8 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 17.2 - order 8 - SHM Finished stage contains a message that SHM Analysis is not enabled for the workflow");

        releaseReport (assayTest);
        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertNull (reportData.shmReportResult);
        testLog ("step 18 - order 8 - There is no shmReportResult, mutationStatus, or shmSequenceList properties in reportData.json");

        assertNull (coraDb.getShmResult (orderDetails.orderTestId));
        testLog ("step 19 - order 8 - Query has no rows returned");
    }

    /**
     * Note:
     * - SR-T3728
     * - a sample with 2 SHM analysis results, pass and fail respectively
     * 
     * @sdlc.requirements SR-7163:R1, R3, R4
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder9 () {
        Assay assayTest = ID_BCell2_CLIA;
        String passConsensusSeq = "TTCAGTAGACACGTCCATGAACCGCTTCTCCCTGCACATGACCTCTATGACTGCCGCAGACACGGCCCTGTATTATTGTGTCAGAGATGGACCCCCGGCGTTTTGGGGCCAGGGAACC";
        String lowBaseConsensusSeq = "CGGCCCAGTTTCATTGTGCGACAGACCCTTAATTTACATTGTGGTGGTGACTCCTATTGCGACTCTTGGGGCCTTGGAACC";
        Order orderDetails = createOrder (IgHVPhysician,
                                          assayTest,
                                          Blood,
                                          null,
                                          new String[] { c83_00 },
                                          "Order 9 Orca Work");

        forceStatusUpdate (orderDetails.specimenDto.sampleName,
                           tsvOverridePathOrcaIgHVO9,
                           null,
                           sampleNameOrcaIgHVO9,
                           shmDataSourcePathOrcaIgHVO9,
                           workSpaceNameOrcaIgHVO9);

        validateShmAnalysisStagesDrillDownUrl (false);
        testLog ("step 20 - order 9 - ShmAnalysis moved from Ready to Finished status, with SHM Analysis job spawned in portal");

        releaseReport (assayTest);
        ReportRender reportData = getReportDataJsonFile (orderDetails.specimenDto.sampleName);
        assertEquals (reportData.shmReportResult.shmSequenceList.get (0).sequence, passConsensusSeq);
        assertEquals (reportData.shmReportResult.shmSequenceList.size (), 1);
        testLog ("step 21.1 - order 9 - There is only one SHM sequence in shmSequenceList for sequence " + passConsensusSeq + " clone in reportData.json");
        testLog ("step 21.2 - order 9 - There is no SHM sequence in shmSequenceList for sequence " + lowBaseConsensusSeq + " clone in reportData.json");

        ShmResult shmResult = coraDb.getShmResult (orderDetails.orderTestId).shm_result;
        assertEquals (shmResult.clones.size (), 2);
        assertEquals (shmResult.clones.stream ()
                                      .filter (c -> passConsensusSeq.equals (c.Consensus_Sequence))
                                      .filter (c -> "PASS".equals (c.Failure_Flag)).count (),
                      1l);
        testLog ("step 22.1 - order 9 - The one with consensusSequence: " + passConsensusSeq + " had failureFlag: PASS");

        assertEquals (shmResult.clones.stream ()
                                      .filter (c -> lowBaseConsensusSeq.equals (c.Consensus_Sequence))
                                      .filter (c -> "LOW_BASE_COVERAGE".equals (c.Failure_Flag)).count (),
                      1l);
        testLog ("step 22.2 - order 9 - The one with consensusSequence: " + lowBaseConsensusSeq + " had failureFlag: LOW_BASE_COVERAGE");
    }

    /**
     * Note:
     * - SR-T3728
     * - validate shm_results table schema
     * 
     * @sdlc.requirements SR-7163:R1, R2, R3, R4, SR-7029:R1
     */
    @Test (groups = "orcaighv")
    public void validateShmResultsTableSchema () {
        String shmResultsSchema = "select * from information_schema.columns where table_name = 'shm_results' order by ordinal_position asc";
        List <Map <String, Object>> queryResult = coraDb.executeSelect (shmResultsSchema);
        assertEquals (queryResult.size (), 9);

        for (int i = 0; i < queryResult.size (); i++) {
            Map <String, Object> rowMap = queryResult.get (i);
            info ("Column Details: " + rowMap);

            switch (Integer.parseInt (rowMap.get ("ordinal_position").toString ())) {
            case 1:
                validateColumnDetails ("id", "uuid_generate_v4()", "uuid", null, rowMap);
                break;
            case 2:
                validateColumnDetails ("order_test_id", null, "uuid", null, rowMap);
                break;
            case 3:
                validateColumnDetails ("report_type", null, "text", null, rowMap);
                break;
            case 4:
                validateColumnDetails ("shm_result", null, "jsonb", null, rowMap);
                break;
            case 5:
                validateColumnDetails ("iteration", "1", "int4", null, rowMap);
                break;
            case 6:
                validateColumnDetails ("created", "now()", "timestamp", null, rowMap);
                break;
            case 7:
                validateColumnDetails ("modified", "now()", "timestamp", null, rowMap);
                break;
            case 8:
                validateColumnDetails ("created_by", null, "varchar", "256", rowMap);
                break;
            case 9:
                validateColumnDetails ("modified_by", null, "varchar", "256", rowMap);
                break;
            }

        }
        testLog ("step 3 - order 1 - validate orca.shm_results table information schema");
    }

    /**
     * Create and activate order, return order number, sample name and order test id
     * 
     * @param physician
     *            Physician object
     * @param assayTest
     *            Assay Test
     * @param specimenType
     *            SpecimenType
     * @param specimenSource
     *            SpecimenSource
     * @param icdCodes
     *            ICD codes for the order
     * @param orderNotes
     *            order notes
     * @return
     */
    private Order createOrder (Physician physician,
                               Assay assayTest,
                               SpecimenType specimenType,
                               SpecimenSource specimenSource,
                               String[] icdCodes,
                               String orderNotes) {
        // create clonoSEQ diagnostic order
        Specimen specimen = new Specimen ();
        specimen.sampleType = specimenType;
        specimen.sampleSource = specimenSource;
        specimen.anticoagulant = Blood.equals (specimen.sampleType) ? EDTA : null;
        specimen.collectionDate = genDate (-3);
        Order order = diagnostic.createClonoSeqOrder (physician,
                                                      TestHelper.newInternalPharmaPatient (),
                                                      icdCodes,
                                                      assayTest,
                                                      specimen,
                                                      Active,
                                                      Tube);
        info ("Order Number: " + order.orderNumber + ", Order Notes: " + orderNotes);

        String sampleName = orderDetailClonoSeq.getSampleName (assayTest);
        orderStatus.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.expandWorkflowHistory ();

        order.specimenDto = new Specimen ();
        order.specimenDto.sampleName = sampleName;
        order.orderTestId = orderStatus.getOrderTestIdFromUrl ();
        return order;
    }

    /**
     * validate ighvReportEnabled and ighvAnalysisEnabled properties
     */
    private void validateFlagsOnDebugPage (String sampleName,
                                           String expectedIghvReportEnabled,
                                           String expectedIghvAnalysisEnabled) {
        // debug page - get workflow properties
        history.gotoOrderDebug (sampleName);
        Map <String, String> workflowProperties = history.getWorkflowProperties ();

        assertEquals (workflowProperties.get ("ighvReportEnabled"),
                      expectedIghvReportEnabled,
                      "Validate ighvReportEnabled property");
        assertEquals (workflowProperties.get ("ighvAnalysisEnabled"),
                      expectedIghvAnalysisEnabled,
                      "Validate ighvAnalysisEnabled property");

    }

    /**
     * navigate to order debug page using sampleName, override tsvOverridePath,
     * lastFinishedPipelineJobId, sampleNameOverride, shmDataSourcePathand and workspaceName. wait
     * for order to reach ClonoSEQReport stage
     * 
     * @param sampleName
     * @param tsvOverridePath
     * @param lastFinishedPipelineJobId
     * @param sampleNameOverride
     * @param shmDataSourcePath
     * @param workspaceName
     */
    private void forceStatusUpdate (String sampleName,
                                    String tsvPathOverride,
                                    String lastFinishedPipelineJobIdOverride,
                                    String sampleNameOverride,
                                    String shmDataSourcePathOverride,
                                    String workspaceNameOverride) {
        // go to debug page
        history.gotoOrderDebug (sampleName);

        // set workflow property and force status update
        history.setWorkflowProperty (lastAcceptedTsvPath, tsvPathOverride);
        if (lastFinishedPipelineJobIdOverride != null)
            history.setWorkflowProperty (WorkflowProperty.lastFinishedPipelineJobId,
                                         lastFinishedPipelineJobIdOverride);
        if (sampleNameOverride != null)
            history.setWorkflowProperty (WorkflowProperty.sampleName, sampleNameOverride);

        if (shmDataSourcePathOverride != null)
            history.setWorkflowProperty (WorkflowProperty.shmDataSourcePath,
                                         shmDataSourcePathOverride);
        if (workspaceNameOverride != null)
            history.setWorkflowProperty (WorkflowProperty.workspaceName, workspaceNameOverride);

        // skip cloneshare check
        history.setWorkflowProperty (WorkflowProperty.disableHiFreqSave, TRUE.toString ());
        history.setWorkflowProperty (WorkflowProperty.disableHiFreqSharing, TRUE.toString ());

        history.forceStatusUpdate (SecondaryAnalysis, Ready);

        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

    }

    /**
     * validate that ShmAnalysis stage contains Ready and Finished stage status, and pipeline-portal
     * job is created or not depending on isDrilldownUrlNull (if true, drilldownUrl is not present,
     * else contains some value)
     * 
     * @param isDrilldownUrlNull
     */
    private void validateShmAnalysisStagesDrillDownUrl (boolean isDrilldownUrlNull) {
        List <Stage> stages = history.parseStatusHistory ();
        List <Stage> shmAnalysisStages = new LinkedList <> ();
        for (Stage stage : stages) {
            if (stage.stageName.equals (ShmAnalysis))
                shmAnalysisStages.add (stage);
        }

        if (isDrilldownUrlNull) {
            assertEquals (shmAnalysisStages.size (), 2);
            assertEquals (shmAnalysisStages.get (1).stageStatus, Ready);
            assertEquals (shmAnalysisStages.get (0).stageStatus, Finished);
            assertEquals (shmAnalysisStages.get (0).subStatusMessage, "SHM Analysis is not enabled for the workflow.");
            assertNull (shmAnalysisStages.get (0).drilldownUrl);
            assertNull (shmAnalysisStages.get (1).drilldownUrl);
        } else {
            assertEquals (shmAnalysisStages.size (), 3);
            assertEquals (shmAnalysisStages.get (2).stageStatus, Ready);
            assertEquals (shmAnalysisStages.get (1).stageStatus, Awaiting);
            assertEquals (shmAnalysisStages.get (1).subStatusMessage, "SHM Analysis job running");
            assertNotNull (shmAnalysisStages.get (1).drilldownUrl);
            assertEquals (shmAnalysisStages.get (0).stageStatus, Finished);
            assertEquals (shmAnalysisStages.get (0).subStatusMessage, "Saved result to shm_results table.");
            assertNotNull (shmAnalysisStages.get (0).drilldownUrl);
        }

    }

    /**
     * Validate CLIA-IGHV flag on Report if expectedCLIAIGHVFlag is not null, set QC status to pass
     * if setQCStatus is true, and return
     * 
     * @param assayTest
     * @param expectedCLIAIGHVFlag
     * @param setQCStatus
     * @return
     */
    private boolean releaseReport (Assay assayTest) {
        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        orderStatus.isCorrectPage ();
        orderStatus.clickReportTab (assayTest);
        reportClonoSeq.isCorrectPage ();
        boolean isCLIAIGHVFlagPresent = reportClonoSeq.isCLIAIGHVBtnVisible ();
        reportClonoSeq.releaseReport (assayTest, Pass);
        return isCLIAIGHVFlagPresent;
    }

    /**
     * go to debug page, parse reportData.json file
     * 
     * @return ReportRender object for reportData.json file
     */
    private ReportRender getReportDataJsonFile (String sampleName) {
        String reportData = "reportData.json";
        String localFile = join ("/", downloadDir.get (), reportData);

        // get file using get request
        history.gotoOrderDebug (sampleName);
        history.isCorrectPage ();
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileUrl (reportData), localFile);

        ReportRender reportDataJson = mapper.readValue (new File (localFile), ReportRender.class);
        info ("Json File Data " + reportDataJson);
        return reportDataJson;
    }

    /**
     * validate pipeline portal job is completed
     * 
     * @param sampleName
     * @param assayTest
     */
    private void validatePipelineStatusToComplete (String sampleName, Assay assayTest) {
        if (assayTest.equals (ID_BCell2_IVD)) {
            portalTestUrl = portalIvdTestUrl;
            isIVD = true;
        } else
            isIVD = false;

        PipelineApi pipelineApi = new PipelineApi ();
        pipelineApi.addBasicAuth ();
        Sample[] samples = pipelineApi.findFlowcellRuns (sampleName);
        pipelineApi.resetheaders ();
        assertEquals (samples.length, 1, "Validate pipeline portal job is completed");
    }

    /**
     * validate shm_result information schema details
     * 
     * @param colName
     * @param colDefault
     * @param colDataType
     * @param charMaxLength
     * @param rowMap
     */
    private void validateColumnDetails (String colName,
                                        String colDefault,
                                        String colDataType,
                                        String charMaxLength,
                                        Map <String, Object> rowMap) {
        assertEquals (rowMap.get ("table_catalog"), "coradb", "Validate table catalog");
        assertEquals (rowMap.get ("table_schema"), "orca", "Validate table schema");
        assertEquals (rowMap.get ("table_name"), "shm_results", "Validate table name");
        assertEquals (rowMap.get ("column_name"), colName, "Validate first column is " + colName);
        assertEquals (rowMap.get ("column_default"), colDefault, "Validate column_default for " + colName);
        assertEquals (rowMap.get ("udt_name"), colDataType, "Validate udt_name for " + colName);
        assertEquals (String.valueOf (rowMap.get ("character_maximum_length")),
                      String.valueOf (charMaxLength),
                      "Validate max_length for " + colName);
    }

    /**
     * validate report_type of orderTestId in DB is the same as mutationStatus
     * 
     * @param orderTestId
     * @param mutationStatus
     */
    private void validateShmResultReportType (String orderTestId, ShmMutationStatus mutationStatus) {
        validateShmResultReportType (orderTestId, mutationStatus, null);
    }

    /**
     * validate report_type and ericSampleCall of orderTestId in DB is the same as mutationStatus
     * and ericSampleCall respectively
     * 
     * @param orderTestId
     * @param mutationStatus
     * @param ericSampleCall
     */
    private void validateShmResultReportType (String orderTestId,
                                              ShmMutationStatus mutationStatus,
                                              EricSampleCall ericSampleCall) {
        ShmResultData shmResultData = coraDb.getShmResult (orderTestId);
        assertEquals (shmResultData.report_type, mutationStatus);

        if (ericSampleCall != null)
            assertEquals (shmResultData.shm_result.ericSampleCall, ericSampleCall);
    }

    /**
     * get file from URL, read pageNumber, and return extracted text from beginText and endText
     * 
     * @param url
     * @param pageNumber
     * @param beginText
     * @param endText
     * @return
     */
    private String getTextFromPDF (String url, int pageNumber, String beginText, String endText) {
        String pdfFileLocation = join ("/", downloadDir.get (), UUID.randomUUID () + ".pdf");
        info ("PDF File Location: " + pdfFileLocation);

        // get file from URL and save it
        coraApi.get (url, pdfFileLocation);

        // read PDF and extract text
        PdfReader reader = null;
        String extractedText = null;
        try {
            reader = new PdfReader (pdfFileLocation);
            String fileContent = PdfTextExtractor.getTextFromPage (reader, pageNumber);
            info ("File Content: " + fileContent);

            int beginIndex = fileContent.indexOf (beginText);
            int endIndex = fileContent.indexOf (endText);
            extractedText = fileContent.substring (beginIndex, endIndex);
            info ("Extracted Text: " + extractedText);
        } catch (Exception e) {
            throw new RuntimeException (e);
        } finally {
            reader.close ();
        }
        return extractedText;
    }
}
