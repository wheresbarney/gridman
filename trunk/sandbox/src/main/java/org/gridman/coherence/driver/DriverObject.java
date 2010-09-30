package org.gridman.coherence.driver;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

/**
 * This is a simple object, used in the driver test.
 */
public class DriverObject implements PortableObject {

    public static final int POF_OFFSET_SIMPLE_VALUE = 1;
    
    private int indexValue;
    private int simpleValue;
    private byte[] data;

    public DriverObject() {} // required by POF

    public DriverObject(int offset, byte[] data) {
        indexValue = offset;
        simpleValue = offset;
        this.data = data;
    }

    public int getIndexValue() { return indexValue; }

    public int getSimpleValue() { return simpleValue; }

    @Override public void readExternal(PofReader pofReader) throws IOException {
        indexValue = pofReader.readInt(0);
        simpleValue = pofReader.readInt(1);
        data = pofReader.readByteArray(2);
    }

    @Override public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeInt(0,indexValue);
        pofWriter.writeInt(1,simpleValue);
        pofWriter.writeByteArray(2,data);
    }
}
