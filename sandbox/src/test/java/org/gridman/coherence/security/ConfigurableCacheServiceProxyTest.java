package org.gridman.coherence.security;

import com.tangosol.net.CacheService;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Jonathan Knight
 */
public class ConfigurableCacheServiceProxyTest {

    private String xmlConfig =
                    "<config>" +
                    "  <fine-grained-permissions>true</fine-grained-permissions>" +
                    "  <permissions-checker>" +
                    "    <instance>" +
                    "      <class-name>org.gridman.coherence.security.PermissionCheckerStub</class-name>" +
                    "    </instance>" +
                    "  </permissions-checker>" +
                    "</config>";

    @Test
    public void shouldSetFineGrainedPermissionsToTrue() throws Exception {
        XmlElement xml = XmlHelper.loadXml(xmlConfig);
        xml.getElement(ConfigurableCacheServiceProxy.XML_FINEGRAINEDPERMISSIONS).setBoolean(true);

        CacheService service = mock(CacheService.class);

        ConfigurableCacheServiceProxy proxy = new ConfigurableCacheServiceProxy(service, xml);
        assertTrue(proxy.isFineGrainedPermissions());
    }

    @Test
    public void shouldSetFineGrainedPermissionsToFalse() throws Exception {
        XmlElement xml = XmlHelper.loadXml(xmlConfig);
        xml.getElement(ConfigurableCacheServiceProxy.XML_FINEGRAINEDPERMISSIONS).setBoolean(false);

        CacheService service = mock(CacheService.class);

        ConfigurableCacheServiceProxy proxy = new ConfigurableCacheServiceProxy(service, xml);
        assertFalse(proxy.isFineGrainedPermissions());
    }

    @Test
    public void shouldInstantiateBasePermissionsChecker() throws Exception {
        XmlElement xml = XmlHelper.loadXml(xmlConfig);

        CacheService service = mock(CacheService.class);

        ConfigurableCacheServiceProxy proxy = new ConfigurableCacheServiceProxy(service, xml);
        assertTrue(proxy.getPermissionChecker() instanceof PermissionCheckerStub);        
    }

}
