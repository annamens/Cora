/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
/**
 * 
 */
package com.adaptivebiotech.cora.db;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.sql.ResultSet;
import com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus;
import com.adaptivebiotech.pipeline.dto.shm.ShmResult;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class ShmResultData {

    public String            id;
    public String            order_test_id;
    public ShmMutationStatus report_type;
    public ShmResult         shm_result;
    public int               iteration;
    public String            created;
    public String            modified;
    public String            created_by;
    public String            modified_by;

    public ShmResultData (ResultSet rs) {
        try {
            this.id = rs.getString ("id");
            this.order_test_id = rs.getString ("order_test_id");
            this.report_type = ShmMutationStatus.valueOf (rs.getString ("report_type"));
            this.shm_result = mapper.readValue (rs.getString ("shm_result"), ShmResult.class);
            this.iteration = rs.getInt ("iteration");
            this.created = rs.getString ("created");
            this.created_by = rs.getString ("created_by");
            this.modified = rs.getString ("modified");
            this.modified_by = rs.getString ("modified_by");
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
