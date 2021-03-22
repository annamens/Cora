package com.adaptivebiotech.cora.utils.mira;

import java.util.UUID;

public class MiraTargetInfo {
    public String targetEnvironmentType    = "test";
    public String targetHost;
    public String targetMiraNumber;
    public String targetExpansionNumber;
    public String targetWorkspace          = "Adaptive-Testing";
    public UUID   targetProjectId;
    public UUID   targetAccountId;
    public String targetFlowcellId         = "XMIRASCENARIO";
    public String targetSpecimenNumber;
    public String targetSpecimenType;
    public String targetSpecimenSource;
    public String targetSpecimenComparment;
    public String targetSpecimenCollDate;
    public String fastForwardStage;
    public String fastForwardStatus;
    public String fastForwardSubstatusCode = "";
    public String fastForwardSubstatusMsg  = "";

    public MiraTargetInfo (String targetHost,
                           String miraId,
                           String specimenId,
                           String expansionId,
                           String projectId,
                           String accountId,
                           String specimenType,
                           String specimenSource,
                           String specimenCompartment,
                           String specimenCollectionDate,
                           String fastForwardStage,
                           String fastForwardStatus) {
        this.targetHost = targetHost;
        this.targetMiraNumber = miraId;
        this.targetSpecimenNumber = specimenId;
        this.targetExpansionNumber = expansionId;
        this.targetProjectId = UUID.fromString (projectId);
        this.targetAccountId = UUID.fromString (accountId);
        this.targetSpecimenType = specimenType;
        this.targetSpecimenSource = specimenSource;
        this.targetSpecimenComparment = specimenCompartment;
        this.targetSpecimenCollDate = specimenCollectionDate;
        this.fastForwardStage = fastForwardStage;
        this.fastForwardStatus = fastForwardStatus;

    }

}
