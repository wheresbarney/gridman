package org.gridman.coherence.pof.reflection;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.PofUniformSparseArray;
import com.tangosol.io.pof.reflect.PofValue;

/**
 * @author Jonathan Knight
 */
public class GridManPofUniformSparseArray extends PofUniformSparseArray implements GridManPofValue, GridManPofUniformValue{

    private ReadBuffer updatedBinary;

    public GridManPofUniformSparseArray(PofValue valueParent, ReadBuffer bufValue, PofContext ctx, int of, int nType, int ofChildren, int nElementType) {
        super(valueParent, bufValue, ctx, of, nType, ofChildren, nElementType);
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

    @Override
    public void setUniformEncoded() {
        super.setUniformEncoded();
    }

    @Override
    protected PofValue extractChild(ReadBuffer buf, int of, int cb)
    {
      return (isUniformCollection())
              ? GridManPofValueParser.parseUniformValue(this, this.getUniformElementType(), buf.getReadBuffer(of, cb), getPofContext(), getOffset() + of)
              : GridManPofValueParser.parseValue(this, buf.getReadBuffer(of, cb), getPofContext(), getOffset() + of);
    }

}
