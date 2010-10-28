package org.gridman.testing;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
}
