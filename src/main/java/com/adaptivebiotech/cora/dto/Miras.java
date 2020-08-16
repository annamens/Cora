package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import java.util.List;
import com.adaptivebiotech.cora.utils.PageHelper.MiraPanel;
import com.adaptivebiotech.test.utils.PageHelper.OrderStatus;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class Miras {

    public List <Mira> list;

    public Miras () {}

    public Miras (List <Mira> list) {
        this.list = list;
    }

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    public static final class Mira {

        public String        id;
        public String        miraId;
        public MiraPanel     panel;
        public int           numPools;
        public String        asid;
        public String        lastActivity;
        public OrderStatus   status;
        public List <String> stages;
        public String        stageStatus;

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
