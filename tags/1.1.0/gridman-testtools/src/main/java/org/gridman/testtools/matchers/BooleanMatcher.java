package org.gridman.testtools.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Jonathan Knight
 */
abstract class BooleanMatcher extends BaseMatcher<Boolean> {
    private boolean match;

    public BooleanMatcher(boolean match) {
        this.match = match;
    }

    public boolean matches(Object arg) {
        return match == (Boolean)arg;
    }

    public void describeTo(Description description) {
        description.appendText(String.valueOf(match));
    }

}
