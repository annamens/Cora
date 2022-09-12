/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.dto;

import static com.adaptivebiotech.test.utils.TestHelper.equalsOverride;
import static com.adaptivebiotech.test.utils.TestHelper.toStringOverride;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.fasterxml.jackson.annotation.JsonAlias;

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

        public UUID          id;
        public ReminderType  reminderType;
        public UUID          referencedEntityId;
        public LocalDateTime created;
        public LocalDateTime due;
        public String        description;
        @JsonAlias ("orderSummary")
        public Order         order;
        public String        placeOrderUrl;
        public String        remindersListUrl;

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
