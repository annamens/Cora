/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.hl7;

import static com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput.DiseaseType.LYME;
import static com.adaptivebiotech.pipeline.utils.TestHelper.DxStatus.POSITIVE;
import java.util.ArrayList;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;

public class HL7TestBase extends CoraBaseBrowser {

    protected ClassifierOutput positiveLymeResult () {
        ClassifierOutput dxResult = new ClassifierOutput ();
        dxResult.disease = LYME;
        dxResult.classifierVersion = "v2.0";
        dxResult.dxScore = 105.83867731819977d;
        dxResult.countEnhancedSeq = 71;
        dxResult.containerVersion = "dx-classifiers/lyme:8532b3f";
        dxResult.pipelineVersion = "v3.1-613-g1b391bc";
        dxResult.dxStatus = POSITIVE;
        dxResult.configVersion = "dx.lyme.rev2";
        dxResult.uniqueProductiveTemplates = 72905;
        dxResult.qcFlags = new ArrayList <> ();
        return dxResult;
    }
}
