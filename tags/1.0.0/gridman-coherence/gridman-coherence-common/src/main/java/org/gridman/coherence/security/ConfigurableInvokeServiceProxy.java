package org.gridman.coherence.security;

import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.WrapperInvocationService;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import org.gridman.coherence.security.simple.CoherenceSecurityUtils;
import org.gridman.security.permissions.PermissionChecker;

import java.util.Map;
import java.util.Set;

/**
 * @author Jonathan Knight
 */
public class ConfigurableInvokeServiceProxy extends WrapperInvocationService implements XmlHelper.ParameterResolver {

    public static final String XML_PERMISSIONSCHECKER = "permissions-checker";

    private PermissionChecker permissionChecker;

    public ConfigurableInvokeServiceProxy(InvocationService service, XmlElement xmlConfig) {
        super(service);
        init(xmlConfig);
    }

    protected void init(XmlElement xmlConfig) {
        permissionChecker = ensureInstance(xmlConfig, XML_PERMISSIONSCHECKER, PermissionChecker.class);
    }

    /**
     * Create an instance of the class configured using the
     * specified child tag of the given XmlElement.
     *
     * @param xmlConfig - the XmlElement containing the child tag containing
     *                    the configuration to use to build an instance of the
     *                    required class.
     * @param tag       - the child XML tag containing the configuration to use
     * @param type      - the expected type of the instance created
     * @param <T>       - the type of the class to return
     * @return a constructed instance of the required class configured with the specified XML.
     */
    @SuppressWarnings({"unchecked"})
    <T> T ensureInstance(XmlElement xmlConfig, String tag, Class<T> type) {
        XmlElement xml = xmlConfig.getElement(tag);
        if (xml == null) {
            throw new RuntimeException("missing " + tag + " element\n" + xmlConfig);
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (T) XmlHelper.createInstance(xml, classLoader, this, type);
    }

    public PermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    @Override
    public void execute(Invocable task, Set setMembers, InvocationObserver observer) {
        InvocablePermission permission = new InvocablePermission(task.getClass(), InvocableActions.EXECUTE.mask);
        permissionChecker.checkPermission(permission, CoherenceSecurityUtils.getCurrentSubject());
        super.execute(task, setMembers, observer);
    }

    @Override
    public Map query(Invocable task, Set setMembers) {
        InvocablePermission permission = new InvocablePermission(task.getClass(), InvocableActions.QUERY.mask);
        permissionChecker.checkPermission(permission, CoherenceSecurityUtils.getCurrentSubject());
        return super.query(task, setMembers);
    }

    @Override
    public Object resolveParameter(String s, String s1) {
        return XmlHelper.ParameterResolver.UNRESOLVED;
    }
}
