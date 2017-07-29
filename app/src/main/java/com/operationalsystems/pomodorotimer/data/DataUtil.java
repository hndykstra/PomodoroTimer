package com.operationalsystems.pomodorotimer.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilities to handle data conversions.
 */

public class DataUtil {
    private static DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    private DataUtil() {}

    public static Date dateFromDb(String dbVal) throws ParseException {
        if (dbVal == null)
            return null;
        return DF.parse(dbVal);
    }

    public static String dbFromDate(Date dt) {
        if (dt == null)
            return null;
        return DF.format(dt);
    }
}
