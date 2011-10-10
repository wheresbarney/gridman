package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRSerializable;
import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

/**
 * A FileTime structure is a 64-bit value that represents the number of 100-nanosecond intervals
 * that have elapsed since January 1, 1601, Coordinated Universal Time (UTC).
 *
 * @author Jonathan Knight
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc230324(v=PROT.10).aspx">
 *        [MS-PAC]: Privilege Attribute Certificate Data Structure
 *      </a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc237917(v=PROT.10).aspx">FileTime</a>
 */
public class FileTime extends Date implements NDRSerializable {
    private static final long FILETIME_BASE = -11644473600000L;

    /**
     * Set the date value of this FileTime be deserializing the value from
     * the specified NDR stream.
     *
     * @param stream - the NDR encoded stream to deserialize this filetime from
     * @throws IOException if an IO error occurs
     */
    public void deserialize(NDRStream stream) throws IOException {

        long bigEnd = stream.readUnsignedInt();
        long littleEnd = stream.readUnsignedInt();
        if(littleEnd != 0x7fffffffL && bigEnd != 0xffffffffL) {
            BigInteger bigEndInt = BigInteger.valueOf(bigEnd);
            BigInteger littleEndInt = BigInteger.valueOf(littleEnd);
            BigInteger dateValue = bigEndInt.add(littleEndInt.shiftLeft(32));
            dateValue = dateValue.divide(BigInteger.valueOf(10000L));
            dateValue = dateValue.add(BigInteger.valueOf(FILETIME_BASE));
            this.setTime(dateValue.longValue());
        }
    }

    /**
     * Serialize this FileTime value to the specified NDR stream.
     *
     * @param writer the writer to serialize to
     * @throws IOException if an IO error occurs
     */
    public void serialize(NDRWriter writer) throws IOException {
        BigInteger dateValue = BigInteger.valueOf(getTime());
        dateValue = dateValue.subtract(BigInteger.valueOf(FILETIME_BASE));
        dateValue = dateValue.multiply(BigInteger.valueOf(10000L));

        long bigEnd = dateValue.shiftRight(32).longValue();
        long littleEnd = (long)dateValue.intValue() & 0xffffffffL;
        writer.writeUnsignedInt(littleEnd);
        writer.writeUnsignedInt(bigEnd);
    }

}
