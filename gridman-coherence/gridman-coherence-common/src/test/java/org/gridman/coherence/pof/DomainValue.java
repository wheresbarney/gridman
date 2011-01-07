package org.gridman.coherence.pof;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public class DomainValue implements PortableObject {

    private String fieldOne;

    public DomainValue() {
    }

    public DomainValue(String fieldOne) {
        this.fieldOne = fieldOne;
    }

    public String getFieldOne() {
        return fieldOne;
    }

    public void setFieldOne(String fieldOne) {
        this.fieldOne = fieldOne;
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        fieldOne = pofReader.readString(100);
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeString(100, fieldOne);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainValue that = (DomainValue) o;

        if (fieldOne != null ? !fieldOne.equals(that.fieldOne) : that.fieldOne != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return fieldOne != null ? fieldOne.hashCode() : 0;
    }
}
