package org.gridman.security.permissions;

import org.gridman.security.PrincipalStub;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.Subject;
import java.io.FilePermission;
import java.util.Collections;
import java.util.Map;

import static org.gridman.security.JaasHelper.asSubject;
import static org.gridman.utils.CollectionUtils.asSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * @author Jonathan Knight
 */
public class DefaultPermissionCheckerTest {
    @Test
    public void shouldAddSingleQualifiersForPermission() throws Exception {
        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);

        Map expected = Collections.singletonMap(readPermission, asSet(readQualifier));
        assertThat(checker.getPermissionQualifiers(), equalTo(expected));
    }

    @Test
    public void shouldAddMultipleQualifiersForPermission() throws Exception {
        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);
        checker.addPermissionQualifier(readPermission, allQualifier);

        Map expected = Collections.singletonMap(readPermission, asSet(readQualifier, allQualifier));
        assertThat(checker.getPermissionQualifiers(), equalTo(expected));
    }

    @Test
    public void shouldReturnCorrectImplyingQualifiers() throws Exception {

        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);
        checker.addPermissionQualifier(allPermission, allQualifier);
        checker.addPermissionQualifier(writePermission, writeQualifier);

        assertThat(checker.getImpylingPermissionQualifiers(readPermission), equalTo(asSet(readQualifier, allQualifier)));
    }

    @Test
    public void shouldReturnNoImplyingQualifiersIfNoneImplyRequestedPermission() throws Exception {
        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);
        checker.addPermissionQualifier(allPermission, allQualifier);
        checker.addPermissionQualifier(writePermission, writeQualifier);

        assertThat(checker.getImpylingPermissionQualifiers(executePermission).isEmpty(), is(true));
    }

    @Test
    public void shouldAuthoriseCorrectlyQuaulifiedSubject() throws Exception {
        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);
        checker.addPermissionQualifier(allPermission, allQualifier);
        checker.addPermissionQualifier(writePermission, writeQualifier);

        try {
            checker.checkPermission(readPermission, readQualifiedSubject);
        } catch (SecurityException se) {
            fail("A Security Exception should not have been thrown");
        }
    }

    @Test
    public void shouldNotAuthoriseIncorrectlyQuaulifiedSubject() throws Exception {
        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);
        checker.addPermissionQualifier(allPermission, allQualifier);
        checker.addPermissionQualifier(writePermission, writeQualifier);

        try {
            checker.checkPermission(readPermission, writeQualifiedSubject);
            fail("A Security Exception should have been thrown");
        } catch (SecurityException se) {
            //passed
        }
    }

    @Test
    public void shouldNotAuthoriseSubjectIfNoQualifersMatchPermissionAndNoQualifiersQualifyAnyIsFalse() {
        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);
        checker.addPermissionQualifier(allPermission, allQualifier);
        checker.addPermissionQualifier(writePermission, writeQualifier);

        checker.setNoQualifiersQualifiesAny(false);
        
        try {
            checker.checkPermission(executePermission, executeQualifiedSubject);
            fail("A Security Exception should have been thrown");
        } catch (SecurityException se) {
            //passed
        }
    }

    @Test
    public void shouldAuthoriseSubjectIfNoQualifersMatchPermissionAndNoQualifiersQualifyAnyIsTrue() {
        DefaultPermissionChecker checker = new DefaultPermissionChecker();
        checker.addPermissionQualifier(readPermission, readQualifier);
        checker.addPermissionQualifier(allPermission, allQualifier);
        checker.addPermissionQualifier(writePermission, writeQualifier);

        checker.setNoQualifiersQualifiesAny(true);

        try {
            checker.checkPermission(executePermission, executeQualifiedSubject);
        } catch (SecurityException se) {
            fail("A Security Exception should not have been thrown");
        }
    }

    @Before
    public void setup() {
        readPermission = new FilePermission("tmp", "READ");
        readQualifier = new PrincipalNameQualifier(PrincipalStub.class, "reader");
        readQualifiedSubject = asSubject(new PrincipalStub("reader"));

        allPermission = new FilePermission("tmp", "READ,WRITE");
        allQualifier = new PrincipalNameQualifier(PrincipalStub.class, "all");
        allQualifiedSubject = asSubject(new PrincipalStub("all"));

        writePermission = new FilePermission("tmp", "WRITE");
        writeQualifier = new PrincipalNameQualifier(PrincipalStub.class, "writer");
        writeQualifiedSubject = asSubject(new PrincipalStub("writer"));

        executePermission = new FilePermission("tmp", "EXECUTE");
        executeQualifiedSubject = asSubject(new PrincipalStub("execute"));
    }

    FilePermission readPermission;
    PermissionQualifier readQualifier;
    Subject readQualifiedSubject;

    FilePermission allPermission;
    PermissionQualifier allQualifier;
    Subject allQualifiedSubject;

    FilePermission writePermission;
    PermissionQualifier writeQualifier;
    Subject writeQualifiedSubject;

    FilePermission executePermission;
    Subject executeQualifiedSubject;
}
