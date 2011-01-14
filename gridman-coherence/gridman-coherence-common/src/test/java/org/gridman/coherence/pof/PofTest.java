package org.gridman.coherence.pof;

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;

public class PofTest {

    @Test
    public void shouldWorkWithObjectArray() throws Exception {
        Object[] values = new Object[] {"one", "two", "three"};

        ConfigurablePofContext serializer = new ConfigurablePofContext("coherence/common/common-pof-config.xml");
        Binary binary = ExternalizableHelper.toBinary(values, serializer);
        Object[] result = (Object[]) ExternalizableHelper.fromBinary(binary, serializer);

        assertThat(result, is(arrayContaining((Object)"one", "two", "three")));
    }

    @Test
    public void shouldWorkWithTwoDimensionObjectArray() throws Exception {
        Object[][] values = new Object[][] {{"one", "two", "three"},{"one", "two", "three"}};

        ConfigurablePofContext serializer = new ConfigurablePofContext("coherence/common/common-pof-config.xml");
        Binary binary = ExternalizableHelper.toBinary(values, serializer);
        Object[] result = (Object[]) ExternalizableHelper.fromBinary(binary, serializer);

        Object[][] copy = null;

        if (result.length > 0 && result[0] instanceof Object[]) {
            copy = new Object[result.length][];
            for (int i=0; i<result.length; i++) {
                copy[i] = (Object[])result[i];
            }
        }

        System.err.println(copy);

        System.err.println("Done");
        //assertThat(result, is(arrayContaining((Object)"one", "two", "three")));
    }

}
