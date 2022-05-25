/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
/**
 * 
 */
package com.adaptivebiotech.cora.db;

import static com.seleniumfy.test.utils.Logging.error;
import static com.seleniumfy.test.utils.Logging.info;
import java.sql.ResultSet;
import java.sql.Statement;
import com.adaptivebiotech.common.dto.Server;
import com.adaptivebiotech.cora.dto.ShmResultData;
import com.adaptivebiotech.test.utils.DbClientHelper;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class CoraDb extends DbClientHelper {

    public CoraDb (Server database, Server jumpbox) {
        super (database, jumpbox);
    }

    public ShmResultData getShmResult (String orderTestId) {
        String query = "select * from orca.shm_results where order_test_id = '" + orderTestId + "'";
        info ("query is: " + query);

        try (Statement statement = connection.createStatement ();
                ResultSet resultSet = statement.executeQuery (query)) {
            return resultSet.next () ? new ShmResultData (resultSet) : null;
        } catch (Exception e) {
            error (query, e);
            throw new RuntimeException (e);
        }
    }
}
