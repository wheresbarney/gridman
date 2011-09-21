package org.gridman.commandline;

/**
 * @author Jonathan Knight
 */
public class ArgumentShouldBeBooleanException extends CommandLineException {

    public ArgumentShouldBeBooleanException(String argName) {
        super("Argument " + argName + " should be 'true' or 'false'");
    }

}
