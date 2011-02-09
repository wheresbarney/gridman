package org.gridman.commandline;

/**
 * @author Jonathan Knight
 */
public class UnrecognisedArgumentException extends CommandLineException {

    public UnrecognisedArgumentException(String argName) {
        super("Unrecognised command line argument [" + argName + "]");
    }

}
