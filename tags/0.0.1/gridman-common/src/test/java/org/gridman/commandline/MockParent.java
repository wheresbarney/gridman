package org.gridman.commandline;

/**
 * @author jonathanknight
 */
public class MockParent {
    @CommandLineArg(name = "-a")
    public String fieldA;

    @CommandLineArg(name = "-b")
    public String fieldB;
}
