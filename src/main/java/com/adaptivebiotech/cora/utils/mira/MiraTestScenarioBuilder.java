package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.info;

import com.adaptivebiotech.cora.utils.mira.mirasource.MiraSourceInfo;
import com.adaptivebiotech.cora.utils.mira.mirasource.SourceSpecimenInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestFastForwardInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestSampleInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestScenarioConfig;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestScenarioInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestScenarioProjectInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestSpecimenInfo;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestTechTransferInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

        TestTechTransferInfo testTechTransferInfo = new TestTechTransferInfo (miraTargetInfo.getTargetWorkspace (),
                miraTargetInfo.getTargetFlowcellId (),
                sourceSpecimenInfos.length);

        for (SourceSpecimenInfo sourceSpecimenInfo : sourceSpecimenInfos) {

            String tsvPath = miraTsvCopier.copyTsvFile (miraTargetInfo.getTargetMiraNumber (),
                                                        miraTargetInfo.getTargetSpecimenNumber (),
                                                        sourceSpecimenInfo.getTsvPath (),
                                                        miraSourceInfo.getSourceMiraId (),
                                                        miraSourceInfo.getSourceSpecimenId ());
            info ("tsvPath is: " + tsvPath);

            TestSpecimenInfo specimen = buildSpecimen (sourceSpecimenInfo, miraTargetInfo, miraSourceInfo, tsvPath);

            testTechTransferInfo.addSpecimen (specimen);

        }

        TestScenarioInfo testScenarioInfo = new TestScenarioInfo (testTechTransferInfo,
                new TestScenarioProjectInfo (miraTargetInfo.getTargetProjectId (),
                        miraTargetInfo.getTargetAccountId ()),
                buildForward (miraTargetInfo),
                new TestScenarioConfig ());

        miraHttpClient.doCoraApiLogin ();
        miraHttpClient.postTestScenarioToCora (testScenarioInfo);

    }

    private TestSpecimenInfo buildSpecimen (SourceSpecimenInfo sourceSpecimenInfo, MiraTargetInfo miraTargetInfo,
                                            MiraSourceInfo miraSourceInfo, String tsvPath) {

        String miraTargetSample = sourceSpecimenInfo.getTargetWorkflowName (miraSourceInfo.getSourceSpecimenId (),
                                                                            miraSourceInfo.getSourceMiraId (),
                                                                            miraTargetInfo.getTargetSpecimenNumber (),
                                                                            miraTargetInfo.getTargetMiraNumber ());

        ObjectNode properties = new ObjectMapper ().createObjectNode ();
        properties.put ("Treatment", sourceSpecimenInfo.getPoolIndicator ());

        ObjectNode projProperties = new ObjectMapper ().createObjectNode ();
        projProperties.put ("Var1", miraTargetInfo.getTargetMiraNumber ());
        projProperties.put ("Var2", miraTargetInfo.getTargetExpansionNumber ());
        projProperties.put ("Var3", sourceSpecimenInfo.getCellCount ());

        TestSpecimenInfo specimenInfo = new TestSpecimenInfo (miraTargetSample, miraTargetSample,
                miraTargetInfo.getTargetSpecimenType (),
                miraTargetInfo.getTargetSpecimenSource (), miraTargetInfo.getTargetSpecimenCompartment (),
                miraTargetInfo.getTargetSpecimenCollDate (), properties, projProperties);

        TestSampleInfo sampleInfo = new TestSampleInfo (miraTargetSample, miraTargetSample,
                sourceSpecimenInfo.getPoolIndicator (), tsvPath);

        specimenInfo.addSample (sampleInfo);

        return specimenInfo;
    }

    private TestFastForwardInfo buildForward (MiraTargetInfo miraTargetInfo) {

        TestFastForwardInfo fastForwardInfo = new TestFastForwardInfo (miraTargetInfo.getFastForwardStage (),
                miraTargetInfo.getFastForwardStatus (), miraTargetInfo.getFastForwardSubstatusCode (),
                miraTargetInfo.getFastForwardSubstatusMsg ());

        return fastForwardInfo;
    }

}
