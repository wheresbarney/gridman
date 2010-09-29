package org.gridman.kerberos.junit.activedirectory;

import org.gridman.kerberos.junit.KdcServerContext;
import org.gridman.kerberos.junit.KdcServerState;


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