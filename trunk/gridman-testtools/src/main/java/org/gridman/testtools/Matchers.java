package org.gridman.testtools;

import org.gridman.testtools.matchers.False;
import org.gridman.testtools.matchers.True;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * @author Jonathan Knight
 */
public class Matchers {

    /**
     */
    @Factory
    public static Matcher<Boolean> isTrue() {
        return new True();
    }
    /**
     */
    @Factory
    public static Matcher<Boolean> isFalse() {
        return new False();
    }
}
