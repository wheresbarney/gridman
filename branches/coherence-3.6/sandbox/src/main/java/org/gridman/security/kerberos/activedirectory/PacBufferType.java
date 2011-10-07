package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRSerializable;

/**
 * @author Jonathan Knight
 */
public enum PacBufferType {
    PAC_LOGON_INFO(0x01, PacLogonInfo.class),
    PAC_CREDENTIALS_INFO(0x02, null),
    PAC_SERVER_CHECKSUM(0x06, null),
    PAC_KDC_CHECKSUM(0x07, null),
    PAC_CLIENT_NAME(0x0A, null),
    PAC_DELEGATION_INFO(0x0B, null),
    PAC_UPN_AND_DNS_INFO(0x0C, null);

    private int id;

    private Class<? extends NDRSerializable> implementation;

    PacBufferType(int id, Class<? extends NDRSerializable> implementation) {
        this.id = id;
        this.implementation = implementation;
    }

    public int getTypeId() {
        return id;
    }

    public Class<? extends NDRSerializable> getImplementation() {
        return implementation;
    }

    public static PacBufferType fromId(int id) {
        for (PacBufferType type : PacBufferType.values()) {
            if (type.getTypeId() == id) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid Buffer Type: " + id);
    }
}
