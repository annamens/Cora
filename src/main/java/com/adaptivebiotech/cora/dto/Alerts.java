package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.cora.dto.AlertType;

/**
 * @author jpatel
 *         <a href="mailto:jpatel@adaptivebiotech.com">jpatel@adaptivebiotech.com</a>
 */
public final class Alerts {

    // end-point /cora/api/v1/external/alerts/summary
    public List <Alert> orderAlerts = new ArrayList <> ();

    public Alerts () {}

    public Alerts (List <Alert> list) {
        this.orderAlerts = list;
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class Alert {

        public String    id;
        public AlertType alertType;

        @Override
        public String toString () {
            try {
                return mapper.writeValueAsString (this);
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
        }
    }

}
