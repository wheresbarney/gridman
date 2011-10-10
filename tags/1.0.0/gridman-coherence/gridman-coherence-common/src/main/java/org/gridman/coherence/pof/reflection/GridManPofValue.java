package org.gridman.coherence.pof.reflection;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.reflect.PofValue;

/**
 * @author Jonathan Knight
 */
public interface GridManPofValue extends PofValue {

    void setBinaryValue(ReadBuffer binaryValue);

    void setOriginalBuffer(ReadBuffer bufValue);

    void setDecorations(byte nDecoMask, ReadBuffer bufDeco);
}

