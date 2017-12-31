package com.operationalsystems.pomodorotimer.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utilities to handle data conversions. Expected format
 * is ISO-8601 with second-level precision.
 */

public class DataUtil {
    private static final DateFormat DF;

    /**
     * Static initializer to set up date formats.
     */
    static {
        DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        DF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private DataUtil() {}

    /**
     * Converts a Firebase string value to a Java Date object.
     * @param dbVal String from database format.
     * @return Java date.
     * @throws ParseException If the database value is not in the expected format.
     */
    public static Date dateFromDb(String dbVal) throws ParseException {
        if (dbVal == null)
            return null;
        return DF.parse(dbVal);
    }

    /**
     * Convert a date value to database storage format.
     * @param dt Java date value
     * @return String for storing in Firebase
     */
    public static String dbFromDate(Date dt) {
        if (dt == null)
            return null;
        return DF.format(dt);
    }
}
