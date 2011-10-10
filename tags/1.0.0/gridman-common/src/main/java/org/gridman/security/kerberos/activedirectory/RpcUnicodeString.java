package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRSerializable;
import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;

import java.io.IOException;

/**
 * The RPC_UNICODE_STRING structure specifies a Unicode string. This structure is defined in IDL as follows:
 *
 * @author Jonathan Knight
 */
public class RpcUnicodeString implements NDRSerializable {

    /**
     * Length: The length, in bytes, of the string pointed to by the Buffer member, not including the terminating
     * null character if any. The length MUST be a multiple of 2. The length SHOULD equal the entire size of the
     * Buffer, in which case there is no terminating null character. Any method that accesses this structure MUST
     * use the Length specified instead of relying on the presence or absence of a null character.
     */
    private short length;

    /**
     * MaximumLength: The maximum size, in bytes, of the string pointed to by Buffer. The size MUST be a multiple of 2.
     * If not, the size MUST be decremented by 1 prior to use. This value MUST not be less than Length.
     */
    private short maximumLength;

    /**
     * Buffer: A pointer to a string buffer. If MaximumLength is greater than zero, the buffer MUST contain a non-null value.
     */
    private int pointer;

    /** The actual value of the String */
    private String stringValue;

    public RpcUnicodeString() {
    }

    public RpcUnicodeString(String stringValue, int pointer) {
        this.stringValue = stringValue;
        this.length = (short)(stringValue.length() * 2);
        this.maximumLength = this.length;
        this.pointer = pointer;
    }

    public RpcUnicodeString(short length, short maximumLength, int pointer) {
        this.length = length;
        this.maximumLength = maximumLength;
        if (this.maximumLength % 2 != 0) {
            this.maximumLength -= 1;
        }

        if (this.length > this.maximumLength) {
            throw new IllegalArgumentException("The length parameter cannot be greater than maximumLength");
        }
        this.pointer = pointer;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public short getMaximumLength() {
        return maximumLength;
    }

    public void setMaximumLength(short maximumLength) {
        this.maximumLength = maximumLength;
    }

    public int getPointer() {
        return pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void deserialize(NDRStream stream) throws IOException {
        length = stream.readShort();
        maximumLength = stream.readShort();
        pointer = stream.readInt();
    }

    public void serialize(NDRWriter writer) throws IOException {
        writer.writeShort(length);
        writer.writeShort(maximumLength);
        writer.writeInt(pointer);
    }

    public void deserializeString(NDRStream stream) throws IOException {
        String value = stream.readConformantVaryingNonTerminatedUnicodeString();

        if(maximumLength > 0 && value == null) {
            throw new IOException("Malformed UnicodeStringPointer UnicodeString has non-zero maximumLength but is not null String");
        }

        int expected = length / 2;
        int actualLength = (value != null) ? value.length() : 0;
        if(actualLength != expected) {
            throw new IOException("Malformed UnicodeStringPointer Invalid UnicodeString size: expected " + expected + " but was " + actualLength);
        }

        stringValue = value;
    }

    public void serializeString(NDRWriter writer) throws IOException {
        writer.writeConformantVaryingNonTerminatedUnicodeString(stringValue);
    }

    @Override
    public String toString() {
        return stringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RpcUnicodeString that = (RpcUnicodeString) o;

        if (length != that.length) {
            return false;
        }
        if (maximumLength != that.maximumLength) {
            return false;
        }
        if (pointer != that.pointer) {
            return false;
        }
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) length;
        result = 31 * result + (int) maximumLength;
        result = 31 * result + pointer;
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        return result;
    }
}
