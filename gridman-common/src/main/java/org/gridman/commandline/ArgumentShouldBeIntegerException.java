package org.gridman.commandline;

/**
 * @author Jonathan Knight
 */
public class ArgumentShouldBeIntegerException extends CommandLineException {

    public ArgumentShouldBeIntegerException(String argName) {
        super("Argument " + argName + " should be an integer");
    }

}
