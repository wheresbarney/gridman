package org.gridman.encoding.ndr;

import java.io.IOException;

/**
 * @author Jonathan Knight
 */
public interface NDRSerializable {

    void serialize(NDRWriter writer) throws IOException;

    void deserialize(NDRStream stream) throws IOException;
}
