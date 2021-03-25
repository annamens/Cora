package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.info;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.dto.Research.Project;
import com.adaptivebiotech.cora.dto.Research.TechTransfer;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.ProjectProperties;
import com.adaptivebiotech.cora.dto.Specimen.Sample;
import com.adaptivebiotech.cora.dto.Specimen.SpecimenProperties;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.dto.mirasource.MiraSourceInfo;
import com.adaptivebiotech.cora.dto.mirasource.SourceSpecimenInfo;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.TestSkus;

/**
 * build MIRA test scenario
 * 
 * @author mgrossman
 *
 */
public class MiraTestScenarioBuilder {

    private MiraHttpClient miraHttpClient;
    private MiraTsvCopier  miraTsvCopier;

    public MiraTestScenarioBuilder (MiraHttpClient miraHttpClient,
                                    MiraTsvCopier miraTsvCopier) {

        this.miraHttpClient = miraHttpClient;
        this.miraTsvCopier = miraTsvCopier;
    }

    public void buildTestScenarioAndPostToCora (MiraTargetInfo miraTargetInfo,
                                                MiraSourceInfo miraSourceInfo) {

        SourceSpecimenInfo[] sourceSpecimenInfos = miraSourceInfo.getSourceSpecimenInfos ();

        info ("number of mira tests is: " + sourceSpecimenInfos.length);

        List <Specimen> specimens = new ArrayList <> (sourceSpecimenInfos.length);

        for (SourceSpecimenInfo sourceSpecimenInfo : sourceSpecimenInfos) {

            String tsvPath = miraTsvCopier.copyTsvFile (miraTargetInfo.getTargetMiraNumber (),
                                                        miraTargetInfo.getTargetSpecimenNumber (),
                                                        sourceSpecimenInfo.getTsvPath (),
                                                        miraSourceInfo.getSourceMiraId (),
                                                        miraSourceInfo.getSourceSpecimenId ());
            info ("tsvPath is: " + tsvPath);

            Specimen specimen = buildSpecimen (sourceSpecimenInfo, miraTargetInfo, miraSourceInfo, tsvPath);

            specimens.add (specimen);
        }

        TechTransfer techTransfer = buildTechTransfer (miraTargetInfo, specimens);
        Project project = buildProject (miraTargetInfo);

        Research research = buildResearch (techTransfer, project, buildForward (miraTargetInfo));

        miraHttpClient.doCoraApiLogin ();
        miraHttpClient.postTestScenarioToCora (research);

    }

    private Research buildResearch (TechTransfer techTransfer, Project project, Stage stage) {
        Research research = new Research (techTransfer);
        research.project = project;
        research.fastForwardStatus = stage;

        return research;
    }

    private Specimen buildSpecimen (SourceSpecimenInfo sourceSpecimenInfo,
                                    MiraTargetInfo miraTargetInfo,
                                    MiraSourceInfo miraSourceInfo,
                                    String tsvPath) {
        String miraTargetSample = sourceSpecimenInfo.getTargetWorkflowName (miraSourceInfo.getSourceSpecimenId (),
                                                                            miraSourceInfo.getSourceMiraId (),
                                                                            miraTargetInfo.getTargetSpecimenNumber (),
                                                                            miraTargetInfo.getTargetMiraNumber ());

        SpecimenProperties specimenProperties = new SpecimenProperties ();
        specimenProperties.Treatment = sourceSpecimenInfo.getPoolIndicator ();

        ProjectProperties projectProperties = new ProjectProperties ();
        projectProperties.Var1 = miraTargetInfo.getTargetMiraNumber ();
        projectProperties.Var2 = miraTargetInfo.getTargetExpansionNumber ();
        projectProperties.Var3 = sourceSpecimenInfo.getCellCount ().toString ();

        List <Sample> samples = new ArrayList <> (1);
        Sample sample = new Sample ();
        sample.name = miraTargetSample;
        sample.externalId = miraTargetSample;
        sample.test = sourceSpecimenInfo.getPoolIndicator ()
                                        .equals ("US") ? TestSkus.MIRAUNSORTED : TestSkus.MIRASORTED;
        sample.tsvPath = tsvPath;
        samples.add (sample);

        Specimen specimen = new Specimen ();
        specimen.name = miraTargetSample;
        specimen.externalSubjectId = miraTargetSample;
        specimen.sampleType = SpecimenType.valueOf (miraTargetInfo.getTargetSpecimenType ());
        specimen.sampleSource = SpecimenSource.valueOf (miraTargetInfo.getTargetSpecimenSource ());
        specimen.compartment = miraTargetInfo.getTargetSpecimenCompartment ();
        specimen.collectionDate = miraTargetInfo.getTargetSpecimenCollDate ();
        specimen.properties = specimenProperties;
        specimen.projectProperties = projectProperties;
        specimen.samples = samples;

        return specimen;

    }

    private TechTransfer buildTechTransfer (MiraTargetInfo miraTargetInfo, List <Specimen> specimens) {

        TechTransfer techTransfer = new TechTransfer ();
        techTransfer.workspace = miraTargetInfo.getTargetWorkspace ();
        techTransfer.flowcellId = miraTargetInfo.getTargetFlowcellId ();
        techTransfer.specimens = specimens;

        return techTransfer;
    }

    private Stage buildForward (MiraTargetInfo miraTargetInfo) {

        Stage fastForwardInfo = new Stage ();
        fastForwardInfo.stageName = StageName.valueOf (miraTargetInfo.getFastForwardStage ());
        fastForwardInfo.stageStatus = StageStatus.valueOf (miraTargetInfo.getFastForwardStatus ());
        fastForwardInfo.subStatusCode = miraTargetInfo.getFastForwardSubstatusCode ();
        fastForwardInfo.subStatusMessage = miraTargetInfo.getFastForwardSubstatusMsg ();

        return fastForwardInfo;
    }

    private Project buildProject (MiraTargetInfo miraTargetInfo) {
        Project project = new Project ();
        project.id = miraTargetInfo.getTargetProjectId ().toString ();
        project.accountId = miraTargetInfo.getTargetAccountId ().toString ();

        return project;
    }

}
