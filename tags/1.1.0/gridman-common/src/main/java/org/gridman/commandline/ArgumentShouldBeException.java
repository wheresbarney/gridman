package org.gridman.commandline;

/**
 * @author Jonathan Knight
 */
public class ArgumentShouldBeException extends RuntimeException {

    public ArgumentShouldBeException(String argName, String values) {
        super("Argument " + argName + " should be one of: " + values);
    }

}
