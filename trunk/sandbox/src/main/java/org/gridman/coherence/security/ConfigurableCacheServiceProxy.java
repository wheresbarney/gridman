package org.gridman.coherence.security;

import com.tangosol.net.CacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.WrapperCacheService;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import org.gridman.coherence.security.simple.CoherenceSecurityUtils;
import org.gridman.security.permissions.PermissionChecker;

/**
 * @author Jonathan Knight
 */
public class ConfigurableCacheServiceProxy extends WrapperCacheService implements XmlHelper.ParameterResolver {
    public static final String XML_FINEGRAINEDPERMISSIONS = "fine-grained-permissions";
    public static final String XML_PERMISSIONSCHECKER = "permissions-checker";

    private PermissionChecker permissionChecker;

    private boolean fineGrainedPermissions = false;

    public ConfigurableCacheServiceProxy(CacheService service, XmlElement xmlConfig) {
        super(service);
        init(xmlConfig);
    }

    protected void init(XmlElement xmlConfig) {
        fineGrainedPermissions = xmlConfig.getSafeElement(XML_FINEGRAINEDPERMISSIONS).getBoolean(false);

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

    public boolean isFineGrainedPermissions() {
        return fineGrainedPermissions;
    }

    public PermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    @Override
    public NamedCache ensureCache(String cacheName, ClassLoader classLoader) {
        CachePermission permission = new CachePermission(cacheName, CacheActions.ENSURE.mask);
        permissionChecker.checkPermission(permission, CoherenceSecurityUtils.getCurrentSubject());
        NamedCache cache = super.ensureCache(cacheName, classLoader);
        if (fineGrainedPermissions) {
            //cache = new BaseSecurityReadWriteWrapperCache(cache, permissionChecker);
        }
        return cache;
    }

    @Override
    public Object resolveParameter(String s, String s1) {
        return XmlHelper.ParameterResolver.UNRESOLVED;
    }
}
