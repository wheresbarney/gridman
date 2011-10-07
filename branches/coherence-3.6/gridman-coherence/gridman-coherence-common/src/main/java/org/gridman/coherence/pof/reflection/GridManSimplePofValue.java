package org.gridman.coherence.pof.reflection;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.SimplePofValue;

/**
 * @author Jonathan Knight
 */
public class GridManSimplePofValue extends SimplePofValue implements GridManPofValue {

    private ReadBuffer updatedBinary;

    public GridManSimplePofValue(PofValue valueParent, ReadBuffer bufValue, PofContext ctx, int of, int nType) {
        super(valueParent, bufValue, ctx, of, nType);
    }

    @Override
    public void setOriginalBuffer(ReadBuffer bufValue) {
        super.setOriginalBuffer(bufValue);
    }

    @Override
    public void setDecorations(byte nDecoMask, ReadBuffer bufDeco) {
        super.setDecorations(nDecoMask, bufDeco);
    }

    @Override
    public void setBinaryValue(ReadBuffer binaryValue) {
        if (binaryValue == null) {
            throw new IllegalArgumentException("Updated binary value cannot be set to null");
        }
        this.m_oValue = null;
        this.updatedBinary = binaryValue;
        setDirty();
    }

    @Override
    public ReadBuffer getSerializedValue() {
        if (isDirty() && updatedBinary != null) {
            return updatedBinary;
        }
        return super.getSerializedValue();
    }

}
