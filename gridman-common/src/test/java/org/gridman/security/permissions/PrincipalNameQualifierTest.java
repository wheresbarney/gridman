package org.gridman.security.permissions;

import org.gridman.security.PrincipalStub;
import org.gridman.security.kerberos.KrbTicket;
import org.junit.Test;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Jonathan Knight
 */
public class PrincipalNameQualifierTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfConstructedWithNullPrincipalType() throws Exception {
        new PrincipalNameQualifier(null, "knightj");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfConstructedWithNullPrincipalName() throws Exception {
        new PrincipalNameQualifier(KrbTicket.class, null);
    }

    @Test
    public void shouldSetCorrectPrincipalType() throws Exception {
        PrincipalNameQualifier qualifier = new PrincipalNameQualifier(KrbTicket.class, "knightj");
        assertThat("Principal type should be set correctly", qualifier.getPrincipalType().getName(), equalTo(KrbTicket.class.getName()));
    }

    @Test
    public void shouldSetCorrectPrincipalName() throws Exception {
        PrincipalNameQualifier qualifier = new PrincipalNameQualifier(KrbTicket.class, "knightj");
        assertThat("Principal name should be set correctly", qualifier.getName(), equalTo("knightj"));
    }

    @Test
    public void shouldNotQualifyIfSubjectDoesNotContainRequiredPrincipal() throws Exception {
        Set<? extends Principal> principals = Collections.singleton(new PrincipalStub("knightj"));
        Subject subject = new Subject(false, principals, Collections.emptySet(), Collections.emptySet());

        PrincipalNameQualifier qualifier = new PrincipalNameQualifier(KrbTicket.class, "knightj");
        assertThat("Qualifier should return false", qualifier.qualifies(subject), is(false));
    }

    @Test
    public void shouldNotQualifyIfSubjectContainRequiredPrincipalButWithWrongName() throws Exception {
        Set<? extends Principal> principals = Collections.singleton(new PrincipalStub("wilsona"));
        Subject subject = new Subject(false, principals, Collections.emptySet(), Collections.emptySet());

        PrincipalNameQualifier qualifier = new PrincipalNameQualifier(PrincipalStub.class, "knightj");
        assertThat("Qualifier should return false", qualifier.qualifies(subject), is(false));
    }

    @Test
    public void shouldQualifyIfSubjectContainRequiredPrincipalWithWrongName() throws Exception {
        Set<? extends Principal> principals = Collections.singleton(new PrincipalStub("knightj"));
        Subject subject = new Subject(false, principals, Collections.emptySet(), Collections.emptySet());

        PrincipalNameQualifier qualifier = new PrincipalNameQualifier(PrincipalStub.class, "knightj");
        assertThat("Qualifier should return true", qualifier.qualifies(subject), is(true));
    }
}
