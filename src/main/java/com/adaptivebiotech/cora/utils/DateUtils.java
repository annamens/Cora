package com.adaptivebiotech.cora.utils;

import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.setDate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    public static ZoneId utcZoneId = ZoneId.of ("UTC");
    public static ZoneId pstZoneId = ZoneId.of (ZoneId.SHORT_IDS.get ("PST"));

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

    /**
     * Get date from current date in formatDate format
     * 
     * @param days
     *            no of days to add/remove from current date
     * @param formatDate
     *            DateTimeFormatter format e.g., MM/dd/uuuu
     * @param zoneId
     *            ZoneId
     * @return
     */
    public static String getPastFutureDate (final int days, final DateTimeFormatter formatDate, ZoneId zoneId) {
        return formatDate.format (setDate (days).getTime ().toInstant ()
                                                .atZone (zoneId));
    }

    /**
     * convert given date in fromPattern to toPattern
     * 
     * @param dateToConvert
     *            String date to convert
     * @param fromPattern
     * @param toPattern
     * @return String date in toPattern
     */
    public static String convertDateFormat (String dateToConvert, String fromPattern, String toPattern) {
        DateFormat fromFormat = new SimpleDateFormat (fromPattern);
        DateFormat toFormat = new SimpleDateFormat (toPattern);
        Date date = null;
        try {
            date = fromFormat.parse (dateToConvert);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
        return toFormat.format (date);
    }

}
