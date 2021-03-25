package com.adaptivebiotech.cora.dto.mirasource;

public class SourceSpecimenInfo {
    private String tsvPath;
    private String poolIndicator;
    private Integer cellCount;
    private String workflowName;
    
    private String flowcell;
    private String jobId;
    
    
    public String getTargetWorkflowName (String sourceSpecimenId,
                                         String sourceMiraId,
                                         String targetSpecimenId,
                                         String targetMiraId) {
        return workflowName.replace (sourceSpecimenId, targetSpecimenId)
                .replace (sourceMiraId, targetMiraId);
    }
    
    public String getTsvPath () {
        return tsvPath;
    }
    public void setTsvPath (String tsvPath) {
        this.tsvPath = tsvPath;
    }
    public String getPoolIndicator () {
        return poolIndicator;
    }
    public void setPoolIndicator (String poolIndicator) {
        this.poolIndicator = poolIndicator;
    }
    public Integer getCellCount () {
        return cellCount;
    }
    public void setCellCount (Integer cellCount) {
        this.cellCount = cellCount;
    }
    public String getFlowcell () {
        return flowcell;
    }
    public void setFlowcell (String flowcell) {
        this.flowcell = flowcell;
    }
    public String getJobId () {
        return jobId;
    }
    public void setJobId (String jobId) {
        this.jobId = jobId;
    }
    public String getWorkflowName () {
        return workflowName;
    }
    public void setWorkflowName (String workflowName) {
        this.workflowName = workflowName;
    }
}

