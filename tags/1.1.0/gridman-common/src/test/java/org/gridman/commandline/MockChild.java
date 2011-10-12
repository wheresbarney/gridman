package org.gridman.commandline;

/**
 * @author jonathanknight
 */
public class MockChild extends MockParent {
    @CommandLineArg(name = "-c")
    public String fieldC;

    @CommandLineArg(name = "-d")
    public String fieldD;

}
