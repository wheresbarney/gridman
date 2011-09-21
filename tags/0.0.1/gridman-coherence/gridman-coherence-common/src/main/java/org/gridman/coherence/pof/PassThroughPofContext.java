package org.gridman.coherence.pof;

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.Binary;

import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public class PassThroughPofContext extends ConfigurablePofContext {

    public PassThroughPofContext() {
    }

    public PassThroughPofContext(String sLocator) {
        super(sLocator);
    }

    public PassThroughPofContext(XmlElement xml) {
        super(xml);
    }

    @Override
    public PofSerializer getPofSerializer(int nTypeId) {
        PofSerializer serializer;
        try {
            serializer = super.getPofSerializer(nTypeId);
        } catch (IllegalArgumentException e) {
            serializer = PassThroughBinarySerializer.INSTANCE;
        }
        return serializer;
     }

    private static class PassThroughBinarySerializer implements PofSerializer {
        static final PassThroughBinarySerializer INSTANCE = new PassThroughBinarySerializer();

        @Override
        public void serialize(PofWriter pofWriter, Object o) throws IOException {
            pofWriter.writeRemainder((Binary)o);
        }

        @Override
        public Object deserialize(PofReader pofReader) throws IOException {
            return pofReader.readRemainder();
        }
    }
}
