package com.adaptivebiotech.cora.dto;

import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class TaskHistory {

    public StageName      stage;
    public StageStatus    status;
    public StageSubstatus substatus;
    public String         message;
    public String         actor;
    public String         timestamp;
}
