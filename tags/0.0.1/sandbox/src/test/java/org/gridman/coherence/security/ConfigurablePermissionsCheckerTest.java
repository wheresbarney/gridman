package org.gridman.coherence.security;

import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import org.gridman.security.kerberos.KrbTicket;
import org.gridman.security.permissions.ADGroupSidQualifier;
import org.gridman.security.permissions.PermissionQualifier;
import org.gridman.security.permissions.PrincipalNameQualifier;
import org.junit.Test;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.gridman.testing.Utils.asSet;
import static org.gridman.testing.XmlUtils.removeElement;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Jonathan Knight
 */
public class ConfigurablePermissionsCheckerTest {
    public static final String XML_ONE_PERMISSION_ADGROUP_QUALIFIER =
            "<config>" +
            "  <permissions>" +
            "    <permission>" +
            "      <class-name>org.gridman.coherence.security.CachePermission</class-name>" +
            "      <resource-name>test-name</resource-name>" +
            "      <action>ENSURE</action>" +
            "      <qualifiers>" +
            "        <ad-group>S-1-5-1234</ad-group>" +
            "      </qualifiers>" +
            "    </permission>" +
            "  </permissions>" +
            "</config>";

    public static final String XML_ONE_PERMISSION_PRINCIPAL_QUALIFIER =
            "<config>" +
            "  <permissions>" +
            "    <permission>" +
            "      <class-name>org.gridman.coherence.security.CachePermission</class-name>" +
            "      <resource-name>test-name</resource-name>" +
            "      <action>ENSURE</action>" +
            "      <qualifiers>" +
            "        <principal>" +
            "          <class-name>org.gridman.security.kerberos.KrbTicket</class-name>" +
            "          <principal-name>knightj</principal-name>" +
            "        </principal>" +
            "      </qualifiers>" +
            "    </permission>" +
            "  </permissions>" +
            "</config>";

    public static final String XML_ONE_PERMISSION_TWO_QUALIFIERS =
            "<config>" +
            "  <permissions>" +
            "    <permission>" +
            "      <class-name>org.gridman.coherence.security.CachePermission</class-name>" +
            "      <resource-name>test-name</resource-name>" +
            "      <action>ENSURE</action>" +
            "      <qualifiers>" +
            "        <ad-group>S-1-5-1234</ad-group>" +
            "        <ad-group>S-1-5-5678</ad-group>" +
            "      </qualifiers>" +
            "    </permission>" +
            "  </permissions>" +
            "</config>";

    public static final String XML_TWO_PERMISSIONS =
            "<config>" +
            "  <permissions>" +
            "    <permission>" +
            "      <class-name>org.gridman.coherence.security.CachePermission</class-name>" +
            "      <resource-name>test-name-1</resource-name>" +
            "      <action>ENSURE</action>" +
            "      <qualifiers>" +
            "        <ad-group>S-1-5-1234</ad-group>" +
            "        <ad-group>S-1-5-5678</ad-group>" +
            "      </qualifiers>" +
            "    </permission>" +
            "    <permission>" +
            "      <class-name>org.gridman.coherence.security.CachePermission</class-name>" +
            "      <resource-name>test-name-2</resource-name>" +
            "      <action>ENSURE</action>" +
            "      <qualifiers>" +
            "        <ad-group>S-1-5-9876</ad-group>" +
            "        <ad-group>S-1-5-5432</ad-group>" +
            "      </qualifiers>" +
            "    </permission>" +
            "  </permissions>" +
            "</config>";

    @Test
    public void shouldThrowExceptionIfPermissionTagHasNoClassNameTag() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_ADGROUP_QUALIFIER);
        removeElement(xmlConfig, "permissions", "permission", "class-name");

        // And checker configured with xml...
        try {
            new ConfigurablePermissionsChecker(xmlConfig);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            // then...
            assertThat(e.getMessage().startsWith("Missing required XML element - "+ ConfigurablePermissionsChecker.XML_CLASSNAME), is(true));

        }
    }

    @Test
    public void shouldThrowExceptionIfPermissionTagHasNoNameTag() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_ADGROUP_QUALIFIER);
        removeElement(xmlConfig, "permissions", "permission", "resource-name");

        // And checker configured with xml...
        try {
            new ConfigurablePermissionsChecker(xmlConfig);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            // then...
            assertThat(e.getMessage().startsWith("Missing required XML element - " + ConfigurablePermissionsChecker.XML_PERMISSIONNAME), is(true));

        }
    }

    @Test
    public void shouldThrowExceptionIfPermissionTagHasNoActionTag() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_ADGROUP_QUALIFIER);
        removeElement(xmlConfig, "permissions", "permission", "action");

        // And checker configured with xml...
        try {
            new ConfigurablePermissionsChecker(xmlConfig);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            // then...
            assertThat(e.getMessage().startsWith("Missing required XML element - " + ConfigurablePermissionsChecker.XML_PERMISSIONACTION), is(true));

        }
    }

    @Test
    public void shouldThrowExceptionIfPrincipalTagHasNoClassNameTag() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_PRINCIPAL_QUALIFIER);
        removeElement(xmlConfig, "permissions", "permission", "qualifiers", "principal", "class-name");

        // And checker configured with xml...
        try {
            new ConfigurablePermissionsChecker(xmlConfig);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            // then...
            assertThat(e.getMessage().startsWith("Missing required XML element - " + ConfigurablePermissionsChecker.XML_CLASSNAME), is(true));

        }
    }

    @Test
    public void shouldThrowExceptionIfPrincipalTagHasBlankClassNameTag() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_PRINCIPAL_QUALIFIER);
        xmlConfig.findElement("permissions/permission/qualifiers/principal/class-name").setString("");

        // And checker configured with xml...
        try {
            new ConfigurablePermissionsChecker(xmlConfig);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            // then...
            assertThat(e.getMessage().startsWith("XML tag " + ConfigurablePermissionsChecker.XML_CLASSNAME + " cannot be empty"), is(true));

        }
    }

    @Test
    public void shouldThrowExceptionIfPrincipalTagHasNoPrincipalNameTag() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_PRINCIPAL_QUALIFIER);
        removeElement(xmlConfig, "permissions", "permission", "qualifiers", "principal", "principal-name");

        // And checker configured with xml...
        try {
            new ConfigurablePermissionsChecker(xmlConfig);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            // then...
            assertThat(e.getMessage().startsWith("Missing required XML element - " + ConfigurablePermissionsChecker.XML_PRINCIPALNAME), is(true));

        }
    }

    @Test
    public void shouldThrowExceptionIfPrincipalTagHasBlankPrincipalNameTag() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_PRINCIPAL_QUALIFIER);
        xmlConfig.findElement("permissions/permission/qualifiers/principal/principal-name").setString("");

        // And checker configured with xml...
        try {
            new ConfigurablePermissionsChecker(xmlConfig);
            fail("An exception should have been thrown");
        } catch (Exception e) {
            // then...
            assertThat(e.getMessage().startsWith("XML tag " + ConfigurablePermissionsChecker.XML_PRINCIPALNAME + " cannot be empty"), is(true));

        }
    }

    @Test
    public void shouldHaveNoPermissionQualifiersIfPermissionsTagHasNoQualifiers() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_ADGROUP_QUALIFIER);
        removeElement(xmlConfig, "permissions", "permission", "qualifiers");

        // And checker configured with xml...
        ConfigurablePermissionsChecker checker = new ConfigurablePermissionsChecker(xmlConfig);

        // then...
        assertThat(checker.getPermissionQualifiers().size(), is(0));
    }
    
    @Test
    public void shouldHaveNoPermissionQualifiersIfPermissionsQualifiersTagIsEmpty() throws Exception {
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_ADGROUP_QUALIFIER);
        removeElement(xmlConfig, "permissions", "permission", "qualifiers", "*");

        // And checker configured with xml...
        ConfigurablePermissionsChecker checker = new ConfigurablePermissionsChecker(xmlConfig);

        // then...
        assertThat(checker.getPermissionQualifiers().size(), is(0));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldContainSinglePermissionAndSingleADGroupQualifier() throws Exception {
        // when XML is...
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_ADGROUP_QUALIFIER);

        // And checker configured with xml...
        ConfigurablePermissionsChecker checker = new ConfigurablePermissionsChecker(xmlConfig);

        // then...
        Map<Permission,Set<PermissionQualifier>> expected = new HashMap<Permission,Set<PermissionQualifier>>();
        expected.put(new CachePermission("test-name", "ENSURE"), asSet((PermissionQualifier)new ADGroupSidQualifier("S-1-5-1234")));
        
        assertThat(checker.getPermissionQualifiers(), is(expected));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldContainSinglePermissionAndSinglePrincipalNameQualifier() throws Exception {
        // when XML is...
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_PRINCIPAL_QUALIFIER);

        // And checker configured with xml...
        ConfigurablePermissionsChecker checker = new ConfigurablePermissionsChecker(xmlConfig);

        // then...
        Map<Permission,Set<PermissionQualifier>> expected = new HashMap<Permission,Set<PermissionQualifier>>();
        expected.put(new CachePermission("test-name", "ENSURE")
                , asSet((PermissionQualifier)new PrincipalNameQualifier(KrbTicket.class, "knightj")));

        assertThat(checker.getPermissionQualifiers(), is(expected));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldContainSinglePermissionAndTwoADGroupQualifiers() throws Exception {
        // when XML is...
        XmlElement xmlConfig = XmlHelper.loadXml(XML_ONE_PERMISSION_TWO_QUALIFIERS);

        // And checker configured with xml...
        ConfigurablePermissionsChecker checker = new ConfigurablePermissionsChecker(xmlConfig);

        // then...
        Map<Permission,Set<PermissionQualifier>> expected = new HashMap<Permission,Set<PermissionQualifier>>();
        expected.put(new CachePermission("test-name", "ENSURE"),
                asSet((PermissionQualifier)new ADGroupSidQualifier("S-1-5-1234"), (PermissionQualifier)new ADGroupSidQualifier("S-1-5-5678")));

        assertThat(checker.getPermissionQualifiers(), is(expected));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldContainTwoPermissions() throws Exception {
        // when XML is...
        XmlElement xmlConfig = XmlHelper.loadXml(XML_TWO_PERMISSIONS);

        // And checker configured with xml...
        ConfigurablePermissionsChecker checker = new ConfigurablePermissionsChecker(xmlConfig);

        // then...
        Map<Permission,Set<PermissionQualifier>> expected = new HashMap<Permission,Set<PermissionQualifier>>();
        expected.put(new CachePermission("test-name-1", "ENSURE"),
                asSet((PermissionQualifier)new ADGroupSidQualifier("S-1-5-1234"), (PermissionQualifier)new ADGroupSidQualifier("S-1-5-5678")));
        expected.put(new CachePermission("test-name-2", "ENSURE"),
                asSet((PermissionQualifier)new ADGroupSidQualifier("S-1-5-9876"), (PermissionQualifier)new ADGroupSidQualifier("S-1-5-5432")));

        assertThat(checker.getPermissionQualifiers(), is(expected));
    }
}
