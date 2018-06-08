package com.adaptivebiotech.test.scenariobuilder;

import static com.adaptivebiotech.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.utils.PageHelper.StageStatus.Ready;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.adaptivebiotech.dto.HttpResponse;
import com.adaptivebiotech.dto.Research;

@Test (groups = { "cora" })
public class BarTestSuite extends ScenarioBuilderTestBase {

    @BeforeTest
    public void beforeTest () {
        Research research = buildResearchOrder ();
        research.project = getTestProject ();
        research.fastForwardStatus = stage (SecondaryAnalysis, Ready);

        HttpResponse response = newResearchOrder (research);
        System.out.println ("projectId=" + response.projectId);
    }

    public void doBar () {

    }
}
