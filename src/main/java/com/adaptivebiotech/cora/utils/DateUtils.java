package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.setDate;
import java.time.ZoneId;

public class DateUtils {

    /**
     * Get date from current date in MM/dd/uuuu format
     * 
     * @param days
     *            no of days to add/remove from current date
     * @return
     */
    public static String getPastFutureDate (final int days) {
        return formatDt1.format (setDate (days).getTime ().toInstant ()
                                               .atZone (ZoneId.systemDefault ()));
    }

}
