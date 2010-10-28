package org.gridman.coherence.security;

import com.tangosol.net.InvocationService;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Jonathan Knight
 */
public class ConfigurableInvocationServiceProxyTest {

    private String xmlConfig =
                    "<config>" +
                    "  <permissions-checker>" +
                    "    <instance>" +
                    "      <class-name>org.gridman.coherence.security.PermissionCheckerStub</class-name>" +
                    "    </instance>" +
                    "  </permissions-checker>" +
                    "</config>";

    @Test
    public void shouldInstantiateBasePermissionsChecker() throws Exception {
        XmlElement xml = XmlHelper.loadXml(xmlConfig);

        InvocationService service = mock(InvocationService.class);

        ConfigurableInvokeServiceProxy proxy = new ConfigurableInvokeServiceProxy(service, xml);
        assertTrue(proxy.getPermissionChecker() instanceof PermissionCheckerStub);
    }

}