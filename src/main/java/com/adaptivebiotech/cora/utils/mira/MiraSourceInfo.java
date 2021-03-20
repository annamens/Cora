package com.adaptivebiotech.cora.utils.mira;

import com.adaptivebiotech.cora.utils.PageHelper.MiraExpansionMethod;
import com.adaptivebiotech.cora.utils.PageHelper.MiraLab;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.cora.utils.PageHelper.MiraType;

/**
 * class that has info about a mira source: workflows, flowcells, tsvs, etc
 * @author mgrossman
 *
 */
public class MiraSourceInfo {
    
    private String sourceMiraId;
    private String sourceSpecimenId;
    private MiraLab miraLab;
    private MiraType miraType;
    private MiraPanel miraPanel;
    private MiraExpansionMethod expansionMethod;
    
    private SourceSpecimenInfo[] specimenInfos;
    
    public MiraLab getMiraLab () {
        return miraLab;
    }


    public void setMiraLab (MiraLab miraLab) {
        this.miraLab = miraLab;
    }


    public MiraType getMiraType () {
        return miraType;
    }


    public void setMiraType (MiraType miraType) {
        this.miraType = miraType;
    }


    public MiraPanel getMiraPanel () {
        return miraPanel;
    }


    public void setMiraPanel (MiraPanel miraPanel) {
        this.miraPanel = miraPanel;
    }
    
    public String getSourceMiraId () {
        return sourceMiraId;
    }


    public void setSourceMiraId (String sourceMiraId) {
        this.sourceMiraId = sourceMiraId;
    }


    public String getSourceSpecimenId () {
        return sourceSpecimenId;
    }


    public void setSourceSpecimenId (String sourceSpecimenId) {
        this.sourceSpecimenId = sourceSpecimenId;
    }


    public SourceSpecimenInfo[] getSpecimenInfos () {
        return specimenInfos;
    }


    public void setSpecimenInfos (SourceSpecimenInfo[] specimenInfos) {
        this.specimenInfos = specimenInfos;
    }


    public MiraExpansionMethod getExpansionMethod () {
        return expansionMethod;
    }


    public void setExpansionMethod (MiraExpansionMethod expansionMethod) {
        this.expansionMethod = expansionMethod;
    }


    public static class SourceSpecimenInfo {
        private String tsvPath;
        private String poolIndicator;
        private Integer cellCount;
        private String workflowName;
        
        private String flowcell;
        private String jobId;
        
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

}
