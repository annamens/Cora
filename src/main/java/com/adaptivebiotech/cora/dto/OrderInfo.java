package com.adaptivebiotech.cora.dto;

import com.adaptivebiotech.test.utils.PageHelper.ReportType;

public final class OrderInfo {
    public String     externalSubjectId1;
    public String     sampleName;        // sample name in Clonality or tracking
    public String     orderNum;          // like "R-048487"; order number of ClonalitySample or
                                         // trackingsample
    public String     orderName;         // order name of ClonalitySample
    public ReportType type;              // Clonality or Tracking
    public String     orderDate;         // like : "20200120";
    public String     orderDate_ISO_DATE;// like: "2020-01-20"
    public String     reportFileName;    // ClinicalReportFileName ; like "A120921397.pdf";
    public String     reportNum;         // report number like "A120921397
    public String     pipelineVersion;

}
