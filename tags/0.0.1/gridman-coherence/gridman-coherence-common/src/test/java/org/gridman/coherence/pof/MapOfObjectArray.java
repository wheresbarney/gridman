package org.gridman.coherence.pof;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.util.Map;

public class MapOfObjectArray implements PortableObject {

    private Map<Object,Object> map;

    public MapOfObjectArray() {
    }

    public Map<Object, Object> getMap() {
        return map;
    }

    public void setMap(Map<Object, Object> map) {
        this.map = map;
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        for (Map.Entry<Object,Object> entry : map.entrySet()) {
            
        }
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {

    }
}
