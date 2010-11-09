package org.gridman.testtools.kerberos.activedirectory;

import org.gridman.testtools.kerberos.KdcServerContext;
import org.gridman.testtools.kerberos.KdcServerState;


/**
 * @author Jonathan Knight
 */
public class ActiveDirectoryServerContext extends KdcServerContext {

    private ActiveDirectoryState state;

    public ActiveDirectoryServerContext() {
    }

    @Override
    protected void initialise() {
        super.initialise();
        state = new ActiveDirectoryState(this);
    }

    @Override
    public KdcServerState getInitialState() {
        return state;
    }

}