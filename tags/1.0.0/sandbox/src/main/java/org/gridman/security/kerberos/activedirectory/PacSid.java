package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRSerializable;
import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;

import java.io.IOException;
import java.util.Arrays;

/**
 * A representation of a Microsoft Security Identifier (SID) value.
 *
 * @author Jonathan Knight
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc230324(v=PROT.10).aspx">
 *      [MS-PAC]: Privilege Attribute Certificate Data Structure
 *      </a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc230364(v=PROT.10)">MS RPC_SID</a>
 */
public class PacSid implements NDRSerializable {
    public static final int ATTRIB_SE_GROUP_MANDATORY = 0x01;
    public static final int ATTRIB_SE_GROUP_ENABLED_BY_DEFAULT = 0x02;
    public static final int ATTRIB_SE_GROUP_ENABLED = 0x04;
    public static final int ATTRIB_SE_GROUP_OWNER = 0x08;
    public static final int ATTRIB_SE_GROUP_USE_FOR_DENY_ONLY = 0x10;
    public static final int ATTRIB_SE_GROUP_INTEGRITY = 0x20;
    public static final int ATTRIB_SE_GROUP_INTEGRITY_ENABLED = 0x40;
    public static final int ATTRIB_SE_GROUP_RESOURCE = 0x20000000;
    public static final int ATTRIB_SE_GROUP_LOGIN_ID = 0xC0000000;

    private static final char[] HEX_CHAR_TABLE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f'};

    private byte revision = 1;
    private byte[] authority = {0x00, 0x00, 0x00, 0x00, 0x00, 0x05};
    private long[] subAuthorities;
    private int attributes;
    private boolean readOnly = false;

    public PacSid() {
    }

    public PacSid(long... subAuthorities) {
        this((byte) 1, new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x05}, subAuthorities);
    }

    public PacSid(byte revision, byte[] authority, long... subAuthorities) {
        this.revision = revision;
        this.authority = authority;
        this.subAuthorities = subAuthorities;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public byte getRevision() {
        return revision;
    }

    public int getSubCount() {
        return subAuthorities.length;
    }

    public boolean hasAllZeroSubAuthorities() {
        for (long subAuthority : subAuthorities) {
            if (subAuthority != 0) {
                return false;
            }
        }
        return false;
    }

    public int getAttributes() {
        return attributes;
    }

    public void setAttributes(int attributes) {
        if (readOnly) {
            throw new IllegalStateException("This PacSid is read only");
        }
        this.attributes = attributes;
    }

    public long getAuthority() {
        long authorityAsLong = ((int) authority[0] & 0xff);
        for (int i=1; i<authority.length; i++) {
            authorityAsLong <<= 8;
            authorityAsLong += ((int) authority[i] & 0xff);
        }
        return authorityAsLong;
    }

    public byte[] getAuthorityBytes() {
        return authority;
    }
    
    public long[] getSubAuthorities() {
        return subAuthorities;
    }

    /**
     * Append the specified Sub-Authorities to this SID's Sub-Authorities
     *
     * @param extraSubAuthorities - the Sub-Authorities to add to this SID.
     */
    public void addSubAuthorities(long... extraSubAuthorities) {
        if (readOnly) {
            throw new IllegalStateException("This PacSid is read only");
        }

        if (extraSubAuthorities.length > 0) {
            long[] newSubs = new long[this.subAuthorities.length + extraSubAuthorities.length];
            if (this.subAuthorities.length > 0) {
                System.arraycopy(this.subAuthorities, 0, newSubs, 0, this.subAuthorities.length);
            }
            System.arraycopy(extraSubAuthorities, 0, newSubs, this.subAuthorities.length, extraSubAuthorities.length);
            this.subAuthorities = newSubs;
        }
    }

    public PacSid mergeWithAttributes(PacSid other) {
        PacSid sid = merge(other);
        sid.setAttributes(other.getAttributes());
        return sid;
    }

    public PacSid merge(PacSid other) {
        PacSid sid = new PacSid(revision, authority);
        sid.subAuthorities = new long[this.subAuthorities.length + other.subAuthorities.length];
        System.arraycopy(this.subAuthorities, 0, sid.subAuthorities, 0, this.subAuthorities.length);
        System.arraycopy(other.subAuthorities, 0, sid.subAuthorities, this.subAuthorities.length, other.subAuthorities.length);
        return sid;
    }

    public void deserializeRID(NDRStream stream) throws IOException {
        if (readOnly) {
            throw new IllegalStateException("This PacSid is read only");
        }
        subAuthorities = new long[1];
        subAuthorities[0] = stream.readUnsignedInt();
    }

    public void serializeRID(NDRWriter writer) throws IOException {
        writer.writeUnsignedInt(subAuthorities[0]);
    }

    public void deserializeRIDAndAttributes(NDRStream stream) throws IOException {
        if (readOnly) {
            throw new IllegalStateException("This PacSid is read only");
        }
        deserializeRID(stream);
        this.attributes = stream.readInt();
    }

    public void serializeRIDAndAttributes(NDRWriter writer) throws IOException {
        serializeRID(writer);
        writer.writeInt(this.attributes);
    }

    /**
     * Deserialize the attributes of this PacSid from the specified NDRStream.
     * An NDR encoded SID contains the following format
     * <ul>
     * <li>int subSize - the number of sub authorities in this SID</li>
     * <li>byte revision</li>
     * <li>byte subCount - the number of sub authorities in this SID</li>
     * <li>uint[subCont] the toByteArray of sub-authorities</li>
     * </ul>
     *
     * @param stream the stream containing the NDR encoded attributes of this PacSid.
     * @throws IOException if an IOError occurs
     */
    public void deserialize(NDRStream stream) throws IOException {
        if (readOnly) {
            throw new IllegalStateException("This PacSid is read only");
        }
        
        int subSize = stream.readInt();
        this.revision = stream.readByte();
        byte subCount = stream.readByte();
        this.authority = new byte[6];
        stream.readFully(this.authority);
        this.subAuthorities = new long[subCount];
        for (int i = 0; i < subCount; i++) {
            this.subAuthorities[i] = stream.readUnsignedInt();
        }
    }

    public void serialize(NDRWriter writer) throws IOException {
        int subCount = getSubCount();
        writer.writeInt(subCount);
        writer.writeByte(this.revision);
        writer.writeByte((byte) subCount);
        writer.write(this.authority);
        for (long subAuth : this.subAuthorities) {
            writer.writeUnsignedInt(subAuth);
        }
    }

    /**
     * The SID string format syntax, a format commonly used for a string representation of the SID type (as specified in section 2.4.2), is described by the following ABNF syntax, as specified in [RFC4234].
     * <p/>
     * SID= "S-1-" IdentifierAuthority 1*SubAuthority
     * IdentifierAuthority:
     * <ul>
     * <li>If the identifier authority is < 2^32, the identifier authority is represented as a decimal number</li>
     * <li>If the identifier authority is >= 2^32, the identifier authority is represented in hexadecimal</li>
     * </ul>
     * SubAuthority= "-" 1*10DIGIT
     * <ul>
     * <li>Sub-Authority is always represented as a decimal number</li>
     * <li>No leading "0" characters are allowed when IdentifierAuthority or SubAuthority is represented as
     * a decimal number</li>
     * <li>All hexadecimal digits must be output in string format, pre-pended by "0x"</li>
     * </ul>
     *
     * @return string representation of this SID
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("S-");
        string.append(revision)
                .append('-');

        if (authority[0] == 0x00 && authority[1] == 0x00) {
            long authorityValue = getAuthority();
            string.append(authorityValue);
        } else {
            string.append("0x");
            for (byte b : authority) {
                string.append(HEX_CHAR_TABLE[b >> 4]);
                string.append(HEX_CHAR_TABLE[b & 0x0f]);
            }
        }

        if (subAuthorities != null) {
            for (long subAuthority : subAuthorities) {
                string.append('-').append(subAuthority);
            }
        }
        return string.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PacSid pacSid = (PacSid) o;

        return Arrays.equals(subAuthorities, pacSid.subAuthorities)
                && Arrays.equals(authority, pacSid.authority)
                && revision == pacSid.revision;

    }

    @Override
    public int hashCode() {
        int result = (int) revision;
        result = 31 * result + (authority != null ? Arrays.hashCode(authority) : 0);
        result = 31 * result + (subAuthorities != null ? Arrays.hashCode(subAuthorities) : 0);
        return result;
    }

    public static PacSid parse(String sidString) {
        String[] parts = sidString.split("-");
        byte revision = Byte.parseByte(parts[1]);
        byte[] authority;

        if (parts[2].startsWith("0x")) {
            authority = hexStringToByteArray(parts[2]);
        } else {
            long authLong = Long.parseLong(parts[2]);
            authority = new byte[6];
            for(int i=5; i>2; i--) {
                authority[i] = (byte)authLong;
                authLong >>= 8;
            }
        }

        long[] subAuthorities = new long[parts.length - 3];
        for (int i=3; i<parts.length; i++) {
            subAuthorities[i-3] = Long.parseLong(parts[i]);
        }

        return new PacSid(revision, authority, subAuthorities);
    }

    private static byte[] hexStringToByteArray(String hex) {
        hex = hex.substring(2);
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
