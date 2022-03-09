/**
* Copyright (c) 2017 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.time.LocalDateTime;
import java.util.List;
import org.openqa.selenium.WebElement;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author jpatel
 *
 */
public final class Reminders {

    // end-point /cora/api/v1/external/reminders/active
    public List <Reminder> reminders;

    public Reminders () {}

    public Reminders (List <Reminder> list) {
        this.reminders = list;
    }

    @Override
    public String toString () {
        return toStringOverride (this);
    }

    public static final class Reminder {

        public String        id;
        public ReminderType  reminderType;
        public String        referencedEntityId;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime created;
        @JsonFormat (shape = JsonFormat.Shape.STRING)
        public LocalDateTime due;
        public String        description;
        @JsonAlias ("orderSummary")
        public Order         order;
        public String        placeOrderUrl;
        public String        remindersListUrl;
        public WebElement    removeSign;

        public Reminder () {}

        @Override
        public String toString () {
            return toStringOverride (this);
        }

        @Override
        public boolean equals (Object o) {
            return equalsOverride (this, (Reminder) o);
        }

        public class ReminderType {
            public String id;
            public String name;
            public String referencedEntityType;
        }
    }
}
