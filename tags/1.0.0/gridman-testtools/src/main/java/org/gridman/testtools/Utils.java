package org.gridman.testtools;

import javax.security.auth.Subject;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Jonathan Knight
 */
public class Utils {

    public static <T> Set<T> asSet(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }

    public static Subject asSubject(Principal... principals) {
        return new Subject(false, asSet(principals), Collections.emptySet(), Collections.emptySet());
    }

    public static Date asDate(int yyyy, int mm, int dd) {
        return asDate(yyyy, mm, dd, 0, 0, 0, TimeZone.getTimeZone("GMT"));
    }

    public static Date asDate(int yyyy, int mm, int dd, int hh, int mi, int ss) {
        return asDate(yyyy, mm, dd, hh, mi, ss, TimeZone.getTimeZone("GMT"));
    }

    public static Date asDate(int yyyy, int mm, int dd, int hh, int mi, int ss, TimeZone zone) {
        Calendar cal = Calendar.getInstance(zone);
        cal.set(yyyy, mm, dd, hh, mi, ss);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Timestamp asTimestamp(int yyyy, int mm, int dd, int hh, int mi, int ss) {
        return asTimestamp(yyyy, mm, dd, hh, mi, ss, TimeZone.getTimeZone("GMT"));
    }

    public static Timestamp asTimestamp(int yyyy, int mm, int dd, int hh, int mi, int ss, TimeZone zone) {
        Calendar cal = Calendar.getInstance(zone);
        cal.set(yyyy, mm, dd, hh, mi, ss);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }
}
