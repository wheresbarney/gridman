package org.gridman.coherence.pof.reflection;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public class GridManPofValueParser extends PofValueParser {

    public static PofValue parse(ReadBuffer buf, PofContext ctx) {
        ReadBuffer.BufferInput in = buf.getBufferInput();
        ReadBuffer bufDeco = null;
        byte nDecoMask = 0;
        int of;
        int cb;
        try {
            int nType = in.readUnsignedByte();
            switch (nType) {
                case 13:
                    readInt(in);
                    in.readUnsignedByte();
                    of = in.getOffset();
                    cb = buf.length() - of;
                    break;
                case 18:
                    nDecoMask = in.readByte();
                    if ((nDecoMask & 0x1) == 0) {
                        throw new EOFException("Decorated binary is missing a value");
                    }

                    cb = readInt(in);
                    of = in.getOffset();

                    int ofDeco = of + cb;
                    bufDeco = buf.getReadBuffer(ofDeco, buf.length() - ofDeco);

                    buf = buf.getReadBuffer(of, cb);
                case 21:
                    of = 1;
                    cb = buf.length() - 1;
                    break;
                default:
                    of = 0;
                    cb = buf.length();
            }
        }
        catch (IOException e) {
            throw ensureRuntimeException(e);
        }

        GridManPofValue valueRoot = (GridManPofValue) parseValue(null, buf.getReadBuffer(of, cb), ctx, of);

        valueRoot.setOriginalBuffer(buf);
        valueRoot.setDecorations(nDecoMask, bufDeco);

        return valueRoot;
    }

    public static PofValue parseUniformValue(PofValue valueParent, int nType, ReadBuffer bufValue, PofContext ctx, int of) {
        ReadBuffer.BufferInput in = bufValue.getBufferInput();
        GridManPofValue value = (GridManPofValue) GridManPofValueParser.instantiatePofValue(valueParent, nType, bufValue, ctx, of, in);
        ((GridManPofUniformValue) value).setUniformEncoded();
        return value;
    }

    public static PofValue parseValue(PofValue valueParent, ReadBuffer bufValue, PofContext ctx, int of) {
        ReadBuffer.BufferInput in = bufValue.getBufferInput();
        try {
            int nType = in.readPackedInt();
            return GridManPofValueParser.instantiatePofValue(valueParent, nType, bufValue, ctx, of, in);
        }
        catch (IOException e) {
            throw ensureRuntimeException(e);
        }
    }

    public static PofValue instantiatePofValue(PofValue valueParent, int nType, ReadBuffer bufValue, PofContext ctx, int of, ReadBuffer.BufferInput in) {
        try {
            int cSize;
            int ofChildren;
            int nElementType;
            switch (nType) {
                case -24:
                    cSize = in.readPackedInt();
                    ofChildren = in.getOffset();
                    return new GridManPofArray(valueParent, bufValue, ctx, of, nType, ofChildren, cSize);
                case -25:
                    nElementType = in.readPackedInt();
                    cSize = in.readPackedInt();
                    ofChildren = in.getOffset();
                    return new GridManPofUniformArray(valueParent, bufValue, ctx, of, nType, ofChildren, cSize, nElementType);
                case -22:
                    cSize = in.readPackedInt();
                    ofChildren = in.getOffset();
                    return new GridManPofCollection(valueParent, bufValue, ctx, of, nType, ofChildren, cSize);
                case -23:
                    nElementType = in.readPackedInt();
                    cSize = in.readPackedInt();
                    ofChildren = in.getOffset();
                    return new GridManPofUniformCollection(valueParent, bufValue, ctx, of, nType, ofChildren, cSize, nElementType);
                case -26:
                    in.readPackedInt();
                    ofChildren = in.getOffset();
                    return new GridManPofSparseArray(valueParent, bufValue, ctx, of, nType, ofChildren);
                case -27:
                    nElementType = in.readPackedInt();
                    in.readPackedInt();
                    ofChildren = in.getOffset();
                    return new GridManPofUniformSparseArray(valueParent, bufValue, ctx, of, nType, ofChildren, nElementType);
            }

            if (nType >= 0) {
                int nVersionId = in.readPackedInt();
                ofChildren = in.getOffset();
                return new GridManPofUserType(valueParent, bufValue, ctx, of, nType, ofChildren, nVersionId);
            }

            return new GridManSimplePofValue(valueParent, bufValue, ctx, of, nType);
        }
        catch (IOException e) {
            throw ensureRuntimeException(e);
        }
    }
}
