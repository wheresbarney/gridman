package org.gridman.kerberos.junit;

/**
 * @author Jonathan Knight
 */
public class ExistingKdcContext extends KdcServerContext {

    private ExistingKdcState state;

    public ExistingKdcContext() {
    }

    @Override
    protected void initialise() {
        super.initialise();
        state = new ExistingKdcState(this);
    }

    @Override
    public KdcServerState getInitialState() {
        return state;
    }

}
