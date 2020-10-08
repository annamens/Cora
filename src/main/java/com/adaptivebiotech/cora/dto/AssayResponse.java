package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.HttpResponse.Meta;
import com.adaptivebiotech.test.utils.PageHelper.Assay;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class AssayResponse {

    public Meta             meta;
    public List <OrderTest> objects;

    public OrderTest get (Assay assay) {
        return objects.parallelStream ().filter (o -> assay.test.equals (o.name)).findAny ().get ();
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
