package org.gridman.coherence.pof.reflection;

import com.tangosol.io.ReadBuffer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.reflect.AbstractPofValue;
import com.tangosol.io.pof.reflect.PofValue;
import com.tangosol.io.pof.reflect.PofValueParser;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

/**
 * @author Jonathan Knight
 */
public class GridManPofTest {

    ConfigurablePofContext serializer;

    @Before
    public void setup() {
         serializer = new ConfigurablePofContext("coherence/pof/reflection/test-pof-config.xml");
    }

    @Test
    public void shouldSerializeAndDeserializetestClass() throws Exception {
        PofClass original = new PofClass();
        Binary binary = ExternalizableHelper.toBinary(original, serializer);
        PofClass deserialized = (PofClass) ExternalizableHelper.fromBinary(binary, serializer);
        assertThat(deserialized, is(original));
    }

    @Test
    public void shouldGetGridManPofValue() throws Exception {
        PofClass original = new PofClass();
        Binary binary = ExternalizableHelper.toBinary(original, serializer);
        PofValue pofValue = GridManPofValueParser.parse(binary, serializer);
        assertThat(pofValue, instanceOf(GridManPofValue.class));
    }

    @Test
    public void shouldGetOriginalValueFromGridManPofValue() throws Exception {
        PofClass original = new PofClass();
        Binary binary = ExternalizableHelper.toBinary(original, serializer);
        PofValue pofValue = GridManPofValueParser.parse(binary, serializer);
        assertThat((PofClass)pofValue.getValue(), is(original));
    }

    @Test
    public void shouldSetBinaryValueForBigDecimalField() throws Exception {
        shouldSetBinaryValueForField(PofClass.POF_BIGDECIMALFIELD, "bigDecimalField", new BigDecimal("100.5"));
    }

    @Test
    public void shouldSetBinaryValueForBigIntegerField() throws Exception {
        shouldSetBinaryValueForField(PofClass.POF_BIGINTEGERFIELD, "bigIntegerField", new BigInteger("100"));
    }

    @Test
    public void shouldSetBinaryValueForStringField() throws Exception {
        shouldSetBinaryValueForField(PofClass.POF_STRINGFIELD, "stringField", "Hello World");
    }

    @Test
    public void timer() throws Exception {
        PofClass pofClassOne = new PofClass();
        PofClass pofClassTwo = new PofClass();

        pofClassTwo.stringField = "Hello World";
        int POF_ID = PofClass.POF_STRINGFIELD;

        Binary binaryOne = ExternalizableHelper.toBinary(pofClassOne, serializer);
        Binary binaryTwo = ExternalizableHelper.toBinary(pofClassTwo, serializer);

        PofValue pofValueTwo = GridManPofValueParser.parse(binaryTwo, serializer);

        AbstractPofValue fieldToCopy = (AbstractPofValue) pofValueTwo.getChild(POF_ID);
        ReadBuffer binaryValue = fieldToCopy.getSerializedValue();

        long start = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            PofValue pofValueOne = GridManPofValueParser.parse(binaryOne, serializer);
            GridManPofValue fieldToUpdate = (GridManPofValue) pofValueOne.getChild(POF_ID);
            //fieldToUpdate.setBinaryValue(binaryValue);
            //Binary updatedBinary = pofValueOne.applyChanges();
        }
        long binaryTime = System.currentTimeMillis() - start;


        start = System.currentTimeMillis();
        for (int i=0; i<10000; i++) {
            PofValue pofValueOne = PofValueParser.parse(binaryOne, serializer);
            PofValue fieldToUpdate = pofValueOne.getChild(POF_ID);
            //fieldToUpdate.setValue(pofClassTwo.bigDecimalField);
            //Binary updatedBinary = pofValueOne.applyChanges();
        }
        long originalTime = System.currentTimeMillis() - start;

        System.err.println("Orignal: " + originalTime);
        System.err.println("Binary: " + binaryTime);
    }

    public void shouldSetBinaryValueForField(int POF_ID, String fieldName, Object value) throws Exception {
        PofClass pofClassOne = new PofClass();
        PofClass pofClassTwo = new PofClass();

        Field field = PofClass.class.getDeclaredField(fieldName);
        field.set(pofClassTwo, value);

        Binary binaryOne = ExternalizableHelper.toBinary(pofClassOne, serializer);
        Binary binaryTwo = ExternalizableHelper.toBinary(pofClassTwo, serializer);

        PofValue pofValueOne = GridManPofValueParser.parse(binaryOne, serializer);
        PofValue pofValueTwo = GridManPofValueParser.parse(binaryTwo, serializer);

        GridManPofValue fieldToUpdate = (GridManPofValue) pofValueOne.getChild(POF_ID);
        AbstractPofValue fieldToCopy = (AbstractPofValue) pofValueTwo.getChild(POF_ID);
        assertThat(fieldToCopy.getValue(), is(value));
        
        fieldToUpdate.setBinaryValue(fieldToCopy.getSerializedValue());
        Binary updatedBinary = pofValueOne.applyChanges();
        PofClass updated = (PofClass) ExternalizableHelper.fromBinary(updatedBinary, serializer);
        assertThat(updated.bigDecimalField, is(pofClassTwo.bigDecimalField));
    }

}
