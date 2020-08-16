package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
public final class ContainerHistory {

    public String activityDate;
    public String activity;
    public String comment;
    public String location;
    public String activityBy;

    @Override
    public String toString () {
        try {
            return mapper.writeValueAsString (this);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    @Override
    public boolean equals (Object o) {

        if (o == this)
            return true;

        if (! (o instanceof ContainerHistory))
            return false;

        ContainerHistory c = (ContainerHistory) o;
        return new EqualsBuilder ().append (activityDate, c.activityDate)
                                   .append (activity, c.activity)
                                   .append (comment, c.comment)
                                   .append (location, c.location)
                                   .append (activityBy, c.activityBy)
                                   .isEquals ();
    }
}
