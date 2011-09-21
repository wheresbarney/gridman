package org.gridman.coherence.security;

import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.Base;
import org.gridman.security.permissions.ADGroupSidQualifier;
import org.gridman.security.permissions.DefaultPermissionChecker;
import org.gridman.security.permissions.PermissionQualifier;
import org.gridman.security.permissions.PrincipalNameQualifier;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.Permission;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;

/**
 * An extension of {@link org.gridman.security.permissions.DefaultPermissionChecker} that allows
 * the set of {@link java.security.Permission}s and {@link org.gridman.security.permissions.PermissionQualifier}s to be loaded from an XML configuration.
 * <p/>
 * The XML configuration should follow this format:
 * 
 *  <permissions>
 *   <permission>
 *     <class-name></class-name>
 *     <resource-name></resource-name>
 *     <action></action>
 *     <qualifiers>
 *       <principal>
 *         <class-name></class-name>
 *         <principal-name></principal-name>
 *       </principal>
 *       <ad-group>S-1-5</ad-group>
 *     </qualifiers>
 *   </permission>
 * </permissions>
 *
 * @author Jonathan Knight
 */
public class ConfigurablePermissionsChecker extends DefaultPermissionChecker {
    public static final String XML_PERMISSIONS = "permissions";
    public static final String XML_PERMISSION = "permission";
    public static final String XML_PERMISSIONNAME = "resource-name";
    public static final String XML_PERMISSIONACTION = "action";
    public static final String XML_CLASSNAME = "class-name";
    public static final String XML_QUALIFIERS = "qualifiers";
    public static final String XML_PRINCIPALNAMEQUALIFIER = "principal";
    public static final String XML_PRINCIPALNAME = "principal-name";
    public static final String XML_ADGROUPQUALIFIER = "ad-group";

    public ConfigurablePermissionsChecker() {
    }

    public ConfigurablePermissionsChecker(String configFile) {
        URL url = getClass().getResource(configFile);
        init(XmlHelper.loadXml(url));
    }

    public ConfigurablePermissionsChecker(XmlElement xmlConfig) {
        if (xmlConfig != null) {
            init(xmlConfig);
        }
    }

    @SuppressWarnings({"unchecked"})
    void init(XmlElement xmlConfig) {
        XmlElement permissionsXML;
        if (XML_PERMISSIONS.equals(xmlConfig.getName())) {
            permissionsXML = xmlConfig;
        } else {
            permissionsXML = xmlConfig.getSafeElement(XML_PERMISSIONS);
        }
        
        Iterator<XmlElement> it = permissionsXML.getElements(XML_PERMISSION);

        while (it.hasNext()) {
            XmlElement permissionElement = it.next();
            Permission permission = permissionFromXml(permissionElement);

            XmlElement qualifiersElement = permissionElement.getElement(XML_QUALIFIERS);
            if (qualifiersElement != null) {
                for (XmlElement qualifierElement : (List<XmlElement>) qualifiersElement.getElementList()) {
                    PermissionQualifier qualifier = null;
                    String name = qualifierElement.getName();
                    if (XML_PRINCIPALNAMEQUALIFIER.equals(name)) {
                        qualifier = createPrincipalQualifier(qualifierElement);
                    } else if (XML_ADGROUPQUALIFIER.equals(name)) {
                        qualifier = createADGroupQualifier(qualifierElement);
                    }

                    if (qualifier != null) {
                        super.addPermissionQualifier(permission, qualifier);
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public PermissionQualifier createPrincipalQualifier(XmlElement qualifierElement) {
        String principalClassName = getRequiredXmlElementString(qualifierElement, XML_CLASSNAME);
        String principalName = getRequiredXmlElementString(qualifierElement, XML_PRINCIPALNAME);

        Class<Principal> principalType;
        try {
            principalType = (Class<Principal>) Class.forName(principalClassName);
        } catch (ClassNotFoundException e) {
            throw Base.ensureRuntimeException(e, "Error creating PrincipalNameQualifier instance from xml. " +
                    "Cannot find Principal class " + principalClassName + " specified in XML\n" + qualifierElement);
        }

        return new PrincipalNameQualifier(principalType, principalName);
    }

    public PermissionQualifier createADGroupQualifier(XmlElement qualifierElement) {
        String groupString = qualifierElement.getString();
        if (groupString == null || groupString.trim().length() == 0) {
            throw new RuntimeException(qualifierElement.getName() + " element cannot be blank\n"
                    + qualifierElement.getParent());
        }
        return new ADGroupSidQualifier(groupString.split(","));
    }

    @SuppressWarnings({"unchecked"})
    public Permission permissionFromXml(XmlElement permissionElement) {
        String permissionClassName = getRequiredXmlElementString(permissionElement, XML_CLASSNAME);
        String resourceName = getRequiredXmlElementString(permissionElement, XML_PERMISSIONNAME);
        String action = getRequiredXmlElementString(permissionElement, XML_PERMISSIONACTION);
        try {
            Class<Permission> clazz = (Class<Permission>) Class.forName(permissionClassName);
            Constructor<Permission>permissionConstructor = clazz.getConstructor(String.class, String.class);
            return permissionConstructor.newInstance(resourceName, action);
        } catch (ClassNotFoundException e) {
            throw Base.ensureRuntimeException(e, "Error creating Permission instance from xml. " +
                    "Cannot find class " + permissionClassName + " specified in XML\n" + permissionElement);
        } catch (NoSuchMethodException e) {
            throw Base.ensureRuntimeException(e, "Error creating Permission instance from xml. " +
                    "Cannot find constructor " + permissionClassName + "(String, String)\n" + permissionElement);
        } catch (Exception e) {
            throw Base.ensureRuntimeException(e, "Error creating Permission instance from xml.\n" + permissionElement);
        }
    }

    public String getRequiredXmlElementString(XmlElement parent, String name) {
        XmlElement child = parent.getElement(name);
        if (child == null) {
            throw new RuntimeException("Missing required XML element - " + name + "\n" + parent);
        }
        String value = child.getString();
        if (value == null || value.length() == 0) {
            throw new RuntimeException("XML tag " + name + " cannot be empty\n" + parent);
        }
        return value;
    }
    
}
