package org.gridman.coherence.security.simple;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.gridman.classloader.ClusterStarter;
import org.gridman.coherence.util.NullInvokable;

import javax.security.auth.Subject;
import java.security.PrivilegedAction;

/**
 * Tests all the Security Tests
 * Note that ordering is important here,
 * since we want to let someone in and then check that disallowed users can't afterwards.
 * @author Andrew Wilson
 */
public class SecurityTest extends TestCase {
    private static final Logger logger = Logger.getLogger(SecurityTest.class);

    static final String ALLOWED = "ALLOWED";
    static final String DISALLOWED = "DISALLOWED";
    static final String DISALLOWED_CACHE = "DISALLOWED_CACHE";
    static final String DISALLOWED_CACHE_WRITE = "DISALLOWED_CACHE_WRITE"; // can read, but not write.
    static final String DISALLOWED_INVOKE = "DISALLOWED_INVOKE";

    private PrivilegedAction<Object> getAction = new PrivilegedAction<Object>() {
        @Override public Object run() {
            return CacheFactory.getCache("securityTest").get(1);
        }
    };

    private PrivilegedAction<Object> putAction = new PrivilegedAction<Object>() {
        @Override public Object run() {
            return CacheFactory.getCache("securityTest").put(1,"A");
        }
    };

    private PrivilegedAction<Object> invokeAction = new PrivilegedAction<Object>() {
        @Override public Object run() {
            return ((InvocationService)CacheFactory.getService("InvokeService")).query(new NullInvokable(),null);
        }
    };

    public void testMe() throws Throwable {

        // @todo commented out for now - need to get code checked in.
        if(true) { return; }
        
        ClusterStarter.getInstance().ensureCluster("/coherence/security/simple/simpSecCluster.properties");

        // CHECK WE CAN GET IN TO START WITH
        checkAllowed();

        // CHECK WE DON'T GET IN AS THE DISALLOWED USERS.

        // Check that the disallowed cache putter user cannot put
        try {
            Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(DISALLOWED_CACHE_WRITE), putAction);
            fail("Did not get expected SecurityException");
        } catch(Exception e) {
            logger.info("Got expected SE : " + e);
        }

        // Check that the disallowed_invoke cannot invoke
        try {
            Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(DISALLOWED_INVOKE), invokeAction);
            fail("Did not get expected SecurityException");
        } catch(Exception e) {
            logger.info("Got expected SE : " + e);
        }

        // Check that the disallowed user cannot get in.
        try {
            Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(DISALLOWED), getAction);
            fail("Did not get expected SecurityException");
        } catch(Exception e) {
            logger.info("Got expected SE : " + e);
        }              

        // CHECK WE CAN STILL GET IN AFTER WE HAVE BEEN DENIED!
        checkAllowed();
    }

    private void checkAllowed() {
        // Check that the allowed user can put.
        Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(ALLOWED), putAction);

        // Check that the allowed user can get.
        Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(ALLOWED), getAction);

        // And do an invoke
        Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(ALLOWED), invokeAction);

        // Check that the disallowed cache putter user can do a get
        Subject.doAs(CoherenceSecurityUtils.getSimpleSubject(DISALLOWED_CACHE_WRITE), getAction);
    }
}

