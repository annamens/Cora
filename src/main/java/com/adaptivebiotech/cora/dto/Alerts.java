package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.fasterxml.jackson.annotation.JsonAlias;

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
        return toStringOverride (this);
    }

    public static final class Alert {

        public String           id;
        public AlertType        alertType;

        // used by OrdersList and AlertsList Dora UI pages
        public String           color;
        @JsonAlias ("orderSummary")
        public Order            order;
        public Physician        physician;
        public Patient          patient;
        public Specimen         specimen;
        public List <OrderTest> tests = new ArrayList <> ();
        public String           referencedEntityId;
        public List <String>    recipients;

        // end-point /cora/api/v2/alerts/create
        public String           alertTypeName;

        @Override
        public String toString () {
            return toStringOverride (this);
        }
    }

}
