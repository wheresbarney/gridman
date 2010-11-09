package org.gridman.coherence.pof.reflection;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.PofUserType;
import com.tangosol.io.pof.reflect.PofValue;

/**
 * @author Jonathan Knight
 */
public class GridManPofUserType extends PofUserType implements GridManPofValue, GridManPofUniformValue{

    private ReadBuffer updatedBinary;

    public GridManPofUserType(PofValue valueParent, ReadBuffer bufValue, PofContext ctx, int of, int nType, int ofChildren, int nVersion) {
        super(valueParent, bufValue, ctx, of, nType, ofChildren, nVersion);
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
