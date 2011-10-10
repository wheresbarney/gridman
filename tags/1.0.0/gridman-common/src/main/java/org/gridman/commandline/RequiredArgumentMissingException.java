package org.gridman.commandline;

/**
 * @author Jonathan Knight
 */
public class RequiredArgumentMissingException extends CommandLineException {

    public RequiredArgumentMissingException(String argName) {
        super("Required command line argument [" + argName + "] is missing");
    }

}
