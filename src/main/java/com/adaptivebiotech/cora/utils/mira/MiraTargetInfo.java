package com.adaptivebiotech.cora.utils.mira;

import java.util.UUID;

public class MiraTargetInfo {
    private String targetEnvironmentType    = "test";
    private String targetHost;
    private String targetMiraNumber;
    private String targetExpansionNumber;
    private String targetWorkspace          = "Adaptive-Testing";
    private UUID   targetProjectId;
    private UUID   targetAccountId;
    private String targetFlowcellId         = "XMIRASCENARIO";
    private String targetSpecimenNumber;
    private String targetSpecimenType;
    private String targetSpecimenSource;
    private String targetSpecimenCompartment;
    private String targetSpecimenCollDate;
    private String fastForwardStage;
    private String fastForwardStatus;
    private String fastForwardSubstatusCode = "";
    private String fastForwardSubstatusMsg  = "";

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
        this.targetSpecimenCompartment = specimenCompartment;
        this.targetSpecimenCollDate = specimenCollectionDate;
        this.fastForwardStage = fastForwardStage;
        this.fastForwardStatus = fastForwardStatus;

    }

    public String getTargetEnvironmentType () {
        return targetEnvironmentType;
    }

    public String getTargetHost () {
        return targetHost;
    }

    public String getTargetMiraNumber () {
        return targetMiraNumber;
    }

    public String getTargetExpansionNumber () {
        return targetExpansionNumber;
    }

    public String getTargetWorkspace () {
        return targetWorkspace;
    }

    public UUID getTargetProjectId () {
        return targetProjectId;
    }

    public UUID getTargetAccountId () {
        return targetAccountId;
    }

    public String getTargetFlowcellId () {
        return targetFlowcellId;
    }

    public String getTargetSpecimenNumber () {
        return targetSpecimenNumber;
    }

    public String getTargetSpecimenType () {
        return targetSpecimenType;
    }

    public String getTargetSpecimenSource () {
        return targetSpecimenSource;
    }

    public String getTargetSpecimenCompartment () {
        return targetSpecimenCompartment;
    }

    public String getTargetSpecimenCollDate () {
        return targetSpecimenCollDate;
    }

    public String getFastForwardStage () {
        return fastForwardStage;
    }

    public String getFastForwardStatus () {
        return fastForwardStatus;
    }

    public String getFastForwardSubstatusCode () {
        return fastForwardSubstatusCode;
    }

    public String getFastForwardSubstatusMsg () {
        return fastForwardSubstatusMsg;
    }

}
