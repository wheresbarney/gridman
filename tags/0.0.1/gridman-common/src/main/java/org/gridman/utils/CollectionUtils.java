package org.gridman.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class CollectionUtils {
    
    public static <T> Set<T> asSet(T... elements) {
        return new HashSet<T>(Arrays.asList(elements));
    }

}
