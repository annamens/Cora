/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.db;

import static com.seleniumfy.test.utils.Logging.error;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import com.adaptivebiotech.common.dto.Server;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.ShmResultData;
import com.adaptivebiotech.test.utils.DbClientHelper;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public class CoraDb extends DbClientHelper {

    private final List <String> deleteOrders  = asList ("delete from cora.specimen_order_xref where order_id IN (%s)",
                                                        "delete from cora.order_tests where order_id IN (%s)",
                                                        "delete from cora.order_billing where order_id IN (%s)",
                                                        "delete from cora.order_panel_xref where order_id IN (%s)",
                                                        "delete from cora.order_messages where order_id IN (%s)");
    private final List <String> deletePatient = asList ("delete from cora.orders where patient_id = '%s'",
                                                        "delete from cora.providers_patients where patient_id = '%s'",
                                                        "delete from cora.patient_billing where patient_id = '%s'",
                                                        "delete from cora.patients where id = '%s'");

    public CoraDb (Server database, Server jumpbox) {
        super (database, jumpbox);
    }

    public ShmResultData getShmResult (UUID orderTestId) {
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

    public Shipment getShipmentProperties (String shipmentNumber) {
        String query = "select * from cora.shipments where shipment_number = '" + shipmentNumber + "';";
        info ("query is: " + query);

        try (Statement statement = connection.createStatement ();
                ResultSet resultSet = statement.executeQuery (query)) {
            return resultSet.next () ? new Shipment (resultSet) : null;
        } catch (Exception e) {
            error (query, e);
            throw new RuntimeException (e);
        }
    }

    public void deleteOrdersFromDB (List <String> id) {
        String orderIds = id.stream ().collect (joining ("','", "'", "'"));
        for (String deleteQuery : deleteOrders) {
            executeUpdate (format (deleteQuery, orderIds));
        }
    }

    public void deletePatientFromDB (String id) {
        for (String deleteQuery : deletePatient) {
            executeUpdate (format (deleteQuery, id));
        }
    }
}
