package org.gridman.testtools.kerberos.apacheds;

import org.gridman.testtools.kerberos.KdcServerContext;
import org.gridman.testtools.kerberos.KdcServerState;


/**
 * @author Jonathan Knight
 */
public class ApacheDsServerContext extends KdcServerContext {

    /**
     * The NonExistant state instance
     */
    private KdcServerState nonExistentState = new NonExistentState(this);

    /**
     * The StartedPristine state instance
     */
    private KdcServerState startedPristineState = new StartedPristineState(this);

    /**
     * The StartedNormal state instance
     */
    private KdcServerState startedNormalState = new StartedNormalState(this);


    public ApacheDsServerContext() {

    }

    @Override
    protected void initialise() {
        super.initialise();
        this.nonExistentState = new NonExistentState(this);
        this.startedNormalState = new StartedNormalState(this);
        this.startedPristineState = new StartedPristineState(this);
    }

    @Override
    public KdcServerState getInitialState() {
        return nonExistentState;
    }


    KdcServerState getNonExistentState() {
        return nonExistentState;
    }


    KdcServerState getStartedPristineState() {
        return startedPristineState;
    }


    KdcServerState getStartedNormalState() {
        return startedNormalState;
    }

}
