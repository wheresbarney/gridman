package org.gridman.security.kerberos.junit.activedirectory;

import org.gridman.security.kerberos.junit.KdcServerContext;
import org.gridman.security.kerberos.junit.KdcServerState;


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