package org.gridman.commandline;

/**
 * @author Jonathan Knight
 */
public class UnhandledClassTypeException extends RuntimeException {

    public UnhandledClassTypeException(String argName, Class type) {
        super("Ungandled command line class type " + type + " for argument " + argName);
    }

}
