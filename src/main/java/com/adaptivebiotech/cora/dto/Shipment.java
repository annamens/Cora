package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.ArrayList;
import java.util.List;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.test.utils.PageHelper.OrderCategory;
import com.adaptivebiotech.test.utils.PageHelper.ShippingCondition;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Shipment {

    public String            id;
    public String            shipmentNumber;
    public String            link;
    public OrderCategory     category;
    public String            status;
    public Object            arrivalDate;
    public ShippingCondition condition;
    public String            carrier;
    public String            trackingNumber;
    public String            expectedRecordType;
    public List <Container>  containers = new ArrayList <> ();

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
