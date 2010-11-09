package org.gridman.coherence.pof.reflection;

import com.tangosol.io.pof.*;
import com.tangosol.util.Binary;
import com.tangosol.util.LongArray;
import com.tangosol.util.SimpleLongArray;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

import static org.gridman.testtools.Utils.asDate;
import static org.gridman.testtools.Utils.asTimestamp;

/**
 * @author Jonathan Knight
 */
public class PofClass implements PortableObject {
    public static final int POF_BIGDECIMALFIELD = 100;
    public static final int POF_BIGINTEGERFIELD = 101;
    public static final int POF_BINARYFIELD = 102;
    public static final int POF_BOOLEANFIELD = 103;
    public static final int POF_BOOLEANARRAYFIELD = 104;
    public static final int POF_BYTEFIELD = 105;
    public static final int POF_BYTEARRAYFIELD = 106;
    public static final int POF_CHARFIELD = 107;
    public static final int POF_CHARARRAYFIELD = 108;
    public static final int POF_COLLECTIONFIELD = 109;
    public static final int POF_COLLECTIONOFTYPEFIELD = 110;
    public static final int POF_DATEFIELD = 111;
    public static final int POF_DATETIMEFIELD = 112;
    public static final int POF_TIMESTAMPFIELD = 113;
    public static final int POF_DATETIMEWITHZONEFIELD = 114;
    public static final int POF_TIMESTAMPWITHZONEFIELD = 115;
    public static final int POF_DOUBLEFIELD = 116;
    public static final int POF_DOUBLEARRAYFIELD = 117;
    public static final int POF_FLOATFIELD = 118;
    public static final int POF_FLOATARRAYFIELD = 119;
    public static final int POF_INTFIELD = 120;
    public static final int POF_INTARRAYFIELD = 121;
    public static final int POF_LONGFIELD = 122;
    public static final int POF_LONGARRAYFIELD = 123;
    public static final int POF_LONGARRAYOBJECTFIELD = 124;
    public static final int POF_LONGARRAYOFTYPEFIELD = 125;
    public static final int POF_MAPFIELD = 126;
    public static final int POF_MAPOFKEYTYPEFIELD = 127;
    public static final int POF_MAPOFKEYANDVALUETYPEFIELD = 128;
    public static final int POF_OBJECTFIELD = 129;
    public static final int POF_OBJECTARRAYFIELD = 130;
    public static final int POF_OBJECTARRAYOFTYPEFIELD = 131;
    public static final int POF_RAWDATEFIELD = 132;
    public static final int POF_RAWDATETIMEFIELD = 133;
    public static final int POF_RAWDAYTIMEINTERVALFIELD = 134;
    public static final int POF_RAWQUADFIELD = 135;
    public static final int POF_RAWTIMEFIELD = 136;
    public static final int POF_RAWTIMEINTERVALFIELD = 137;
    public static final int POF_RAWYEARMONTHFIELD = 138;
    public static final int POF_SHORTFIELD = 139;
    public static final int POF_SHORTARRAYFIELD = 140;
    public static final int POF_STRINGFIELD = 141;
    public static final int POF_TIMEFIELD = 142;
    public static final int POF_TIMESTAMPTIMEFIELD = 143;
    public static final int POF_TIMEWITHZONEFIELD = 144;
    public static final int POF_TIMESTAMPTIMEWITHZONEFIELD = 145;

    public BigDecimal bigDecimalField = new BigDecimal("1234.56");
    public BigInteger bigIntegerField = new BigInteger("1000");
    public Binary binaryField = new Binary(new byte[]{0,0,0,0,0,0,0,0});
    public boolean booleanField = true;
    public boolean[] booleanArrayField = {true, false, true, false};
    public byte byteField = 0xF;
    public byte[] byteArrayField = {0x0, 0x1, 0x2, 0x3, 0x4};
    public char charField = 'J';
    public char[] charArrayField = {'G', 'r', 'i', 'd', 'M', 'a', 'n'};
    public Collection collectionField = Arrays.asList("Grid", "Man");
    public Collection collectionOfTypeField = Arrays.asList("JK", "AW");
    public Class collectionType = String.class;
    public Date dateField = asDate(2010, 10, 10);
    public Date dateTimeField = asDate(2010, 10, 9, 12, 0, 5);
    public Timestamp timestampField = asTimestamp(2010, 6, 20, 23, 30, 0);
    public Date dateTimeWithZoneField = asDate(2010, 10, 9, 12, 0, 5, TimeZone.getDefault());
    public Timestamp timestampWithZoneField = asTimestamp(2010, 10, 9, 12, 0, 5, TimeZone.getDefault());
    public double doubleField = 9876.12d;
    public double[] doubleArrayField = {1.2, 3.4, 5.6};
    public float floatField = 1.34f;
    public float[] floatArrayField = {9.8f, 7.6f, 5.4f};
    public int intField = 4321;
    public int[] intArrayField = {1,2,3,4,5};
    public long longField = 987654L;
    public long[] longArrayField = {9,8,7,6,5,4};
    public LongArray longArrayObjectField = new SimpleLongArray();
    public LongArray longArrayOfTypeField = new SimpleLongArray();
    public Class longArrayOfType = String.class;
    public Map mapField = new HashMap();
    public Map mapOfKeyTypeField = new HashMap();
    public Class mapOfKeyType = String.class;
    public Map mapOfKeyAndValueTypeField = new HashMap();
    public Class mapOfKeyAndValueKeyType = String.class;
    public Class mapOfKeyAndValueType = String.class;
    public Object objectField;
    public Object[] objectArrayField = new Object[0];
    public Object[] objectArrayOfTypeField = new Object[0];
    public Class objectArrayType = String.class;
    public RawDate rawDateField = new RawDate(2010, 7, 5);
    public RawDateTime rawDateTimeField = new RawDateTime(new RawDate(2010, 2, 20), new RawTime(19, 30, 0, 500, true));
    public RawDayTimeInterval rawDayTimeIntervalField = new RawDayTimeInterval(5, 12, 45, 30, 1000);
    public RawQuad rawQuadField = new RawQuad(0.0d);
    public RawTime rawTimeField = new RawTime(20, 50, 30, 3987, true);
    public RawTimeInterval rawTimeIntervalField = new RawTimeInterval(3, 52, 45, 5000);
    public RawYearMonthInterval rawYearMonthField = new RawYearMonthInterval(200, 6);
    public short shortField = 9;
    public short[] shortArrayField = {5,4,3,2};
    public String stringField = "The GridMan";
    public Date timeField = asDate(2010, 4, 25, 12, 30, 45);
    public Timestamp timestampTimeField = asTimestamp(2010, 7, 3, 15, 45, 23);
    public Date timeWithZoneField = asDate(2010, 8, 18, 20, 54, 9);
    public Timestamp timestampTimeWithZoneField = asTimestamp(2010, 3, 10, 10, 30, 45);


    public PofClass() {
        longArrayObjectField.add("One");
        longArrayOfTypeField.add("One");
        longArrayOfTypeField.add("Two");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PofClass pofClass = (PofClass) o;

        if (booleanField != pofClass.booleanField) {
            return false;
        }
        if (byteField != pofClass.byteField) {
            return false;
        }
        if (charField != pofClass.charField) {
            return false;
        }
        if (Double.compare(pofClass.doubleField, doubleField) != 0) {
            return false;
        }
        if (Float.compare(pofClass.floatField, floatField) != 0) {
            return false;
        }
        if (intField != pofClass.intField) {
            return false;
        }
        if (longField != pofClass.longField) {
            return false;
        }
        if (shortField != pofClass.shortField) {
            return false;
        }
        if (bigDecimalField != null ? !bigDecimalField.equals(pofClass.bigDecimalField) : pofClass.bigDecimalField != null) {
            return false;
        }
        if (bigIntegerField != null ? !bigIntegerField.equals(pofClass.bigIntegerField) : pofClass.bigIntegerField != null) {
            return false;
        }
        if (binaryField != null ? !binaryField.equals(pofClass.binaryField) : pofClass.binaryField != null) {
            return false;
        }
        if (!Arrays.equals(booleanArrayField, pofClass.booleanArrayField)) {
            return false;
        }
        if (!Arrays.equals(byteArrayField, pofClass.byteArrayField)) {
            return false;
        }
        if (!Arrays.equals(charArrayField, pofClass.charArrayField)) {
            return false;
        }
        if (collectionField != null ? !collectionField.equals(pofClass.collectionField) : pofClass.collectionField != null) {
            return false;
        }
        if (collectionOfTypeField != null ? !collectionOfTypeField.equals(pofClass.collectionOfTypeField) : pofClass.collectionOfTypeField != null) {
            return false;
        }
//        if (dateField != null ? !dateField.equals(pofClass.dateField) : pofClass.dateField != null) {
//            return false;
//        }
//        if (dateTimeField != null ? !dateTimeField.equals(pofClass.dateTimeField) : pofClass.dateTimeField != null) {
//            return false;
//        }
//        if (dateTimeWithZoneField != null ? !dateTimeWithZoneField.equals(pofClass.dateTimeWithZoneField) : pofClass.dateTimeWithZoneField != null) {
//            return false;
//        }
        if (!Arrays.equals(doubleArrayField, pofClass.doubleArrayField)) {
            return false;
        }
        if (!Arrays.equals(floatArrayField, pofClass.floatArrayField)) {
            return false;
        }
        if (!Arrays.equals(intArrayField, pofClass.intArrayField)) {
            return false;
        }
        if (!Arrays.equals(longArrayField, pofClass.longArrayField)) {
            return false;
        }
        if (longArrayObjectField != null ? !longArrayObjectField.equals(pofClass.longArrayObjectField) : pofClass.longArrayObjectField != null) {
            return false;
        }
        if (longArrayOfTypeField != null ? !longArrayOfTypeField.equals(pofClass.longArrayOfTypeField) : pofClass.longArrayOfTypeField != null) {
            return false;
        }
        if (mapField != null ? !mapField.equals(pofClass.mapField) : pofClass.mapField != null) {
            return false;
        }
        if (mapOfKeyAndValueTypeField != null ? !mapOfKeyAndValueTypeField.equals(pofClass.mapOfKeyAndValueTypeField) : pofClass.mapOfKeyAndValueTypeField != null) {
            return false;
        }
        if (mapOfKeyTypeField != null ? !mapOfKeyTypeField.equals(pofClass.mapOfKeyTypeField) : pofClass.mapOfKeyTypeField != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(objectArrayField, pofClass.objectArrayField)) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(objectArrayOfTypeField, pofClass.objectArrayOfTypeField)) {
            return false;
        }
        if (objectField != null ? !objectField.equals(pofClass.objectField) : pofClass.objectField != null) {
            return false;
        }
        if (rawDateField != null ? !rawDateField.equals(pofClass.rawDateField) : pofClass.rawDateField != null) {
            return false;
        }
        if (rawDateTimeField != null ? !rawDateTimeField.equals(pofClass.rawDateTimeField) : pofClass.rawDateTimeField != null) {
            return false;
        }
        if (rawDayTimeIntervalField != null ? !rawDayTimeIntervalField.equals(pofClass.rawDayTimeIntervalField) : pofClass.rawDayTimeIntervalField != null) {
            return false;
        }
        if (rawQuadField != null ? !rawQuadField.equals(pofClass.rawQuadField) : pofClass.rawQuadField != null) {
            return false;
        }
        if (rawTimeField != null ? !rawTimeField.equals(pofClass.rawTimeField) : pofClass.rawTimeField != null) {
            return false;
        }
        if (rawTimeIntervalField != null ? !rawTimeIntervalField.equals(pofClass.rawTimeIntervalField) : pofClass.rawTimeIntervalField != null) {
            return false;
        }
        if (rawYearMonthField != null ? !rawYearMonthField.equals(pofClass.rawYearMonthField) : pofClass.rawYearMonthField != null) {
            return false;
        }
        if (!Arrays.equals(shortArrayField, pofClass.shortArrayField)) {
            return false;
        }
        if (stringField != null ? !stringField.equals(pofClass.stringField) : pofClass.stringField != null) {
            return false;
        }
//        if (timePartEqual(this.timeField, pofClass.timeField)) {
//            return false;
//        }
//        if (timePartEqual(this.timeWithZoneField, pofClass.timeWithZoneField)) {
//            return false;
//        }
//        if (timePartEqual(this.timestampField, pofClass.timestampField)) {
//            return false;
//        }
//        if (timePartEqual(this.timestampTimeField, pofClass.timestampTimeField)) {
//            return false;
//        }
//        if (timePartEqual(this.timestampTimeWithZoneField, pofClass.timestampTimeWithZoneField)) {
//            return false;
//        }
//        if (timePartEqual(this.timestampWithZoneField, pofClass.timestampWithZoneField)) {
//            return false;
//        }

        return true;
    }

    private boolean timePartEqual(Date thisDate, Date otherDate) {
        if (thisDate == null && otherDate == null) {
            return true;
        }

        if (thisDate == null && otherDate != null ) {
            return false;
        }

        if (thisDate != null && otherDate == null ) {
            return false;
        }
        Calendar thisCalendar = Calendar.getInstance();
        thisCalendar.setTime(thisDate);
        thisCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTime(otherDate);
        otherCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));

        return thisCalendar.get(Calendar.HOUR) == otherCalendar.get(Calendar.HOUR)
            && thisCalendar.get(Calendar.MINUTE) == otherCalendar.get(Calendar.MINUTE)
            && thisCalendar.get(Calendar.SECOND) == otherCalendar.get(Calendar.SECOND)
            && thisCalendar.get(Calendar.MILLISECOND) == otherCalendar.get(Calendar.MILLISECOND);
    }

    private boolean datePartEqual(Date thisDate, Date otherDate) {
        if (thisDate == null && otherDate == null) {
            return true;
        }

        if (thisDate == null && otherDate != null ) {
            return false;
        }

        if (thisDate != null && otherDate == null ) {
            return false;
        }

        Calendar thisCalendar = Calendar.getInstance();
        thisCalendar.setTime(thisDate);
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTime(otherDate);

        return thisCalendar.get(Calendar.HOUR) == otherCalendar.get(Calendar.HOUR)
            && thisCalendar.get(Calendar.MINUTE) == otherCalendar.get(Calendar.MINUTE)
            && thisCalendar.get(Calendar.SECOND) == otherCalendar.get(Calendar.SECOND)
            && thisCalendar.get(Calendar.MILLISECOND) == otherCalendar.get(Calendar.MILLISECOND);
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        bigDecimalField = pofReader.readBigDecimal(POF_BIGDECIMALFIELD);
        bigIntegerField = pofReader.readBigInteger(POF_BIGINTEGERFIELD);
        binaryField = pofReader.readBinary(POF_BINARYFIELD);
        booleanField = pofReader.readBoolean(POF_BOOLEANFIELD);
        booleanArrayField = pofReader.readBooleanArray(POF_BOOLEANARRAYFIELD);
        byteField = pofReader.readByte(POF_BYTEFIELD);
        byteArrayField = pofReader.readByteArray(POF_BYTEARRAYFIELD);
        charField = pofReader.readChar(POF_CHARFIELD);
        charArrayField = pofReader.readCharArray(POF_CHARARRAYFIELD);
        collectionField = pofReader.readCollection(POF_COLLECTIONFIELD, new ArrayList());
        collectionOfTypeField = pofReader.readCollection(POF_COLLECTIONOFTYPEFIELD, new ArrayList());
        collectionType = collectionOfTypeField == null || collectionOfTypeField.isEmpty() ? null : collectionOfTypeField.iterator().next().getClass();
        dateField = pofReader.readDate(POF_DATEFIELD);
        dateTimeField = pofReader.readDate(POF_DATETIMEFIELD);
        Date d = pofReader.readDate(POF_TIMESTAMPFIELD);
        timestampField = (d != null) ? new Timestamp(d.getTime()) : null;
        dateTimeWithZoneField = pofReader.readDate(POF_DATETIMEWITHZONEFIELD);
        d = pofReader.readDate(POF_TIMESTAMPWITHZONEFIELD);
        timestampWithZoneField = (d != null) ? new Timestamp(d.getTime()) : null;
        doubleField = pofReader.readDouble(POF_DOUBLEFIELD);
        doubleArrayField = pofReader.readDoubleArray(POF_DOUBLEARRAYFIELD);
        floatField = pofReader.readFloat(POF_FLOATFIELD);
        floatArrayField = pofReader.readFloatArray(POF_FLOATARRAYFIELD);
        intField = pofReader.readInt(POF_INTFIELD);
        intArrayField = pofReader.readIntArray(POF_INTARRAYFIELD);
        longField = pofReader.readLong(POF_LONGFIELD);
        longArrayField = pofReader.readLongArray(POF_LONGARRAYFIELD);
//        longArrayObjectField = pofReader.readLongArray(POF_LONGARRAYOBJECTFIELD, new SimpleLongArray());
//        longArrayOfTypeField = pofReader.readLongArray(POF_LONGARRAYOFTYPEFIELD, new SimpleLongArray());
        mapField = pofReader.readMap(POF_MAPFIELD, new HashMap());
        mapOfKeyTypeField = pofReader.readMap(POF_MAPOFKEYTYPEFIELD, new HashMap());
        mapOfKeyAndValueTypeField = pofReader.readMap(POF_MAPOFKEYANDVALUETYPEFIELD, new HashMap());
        objectField = pofReader.readObject(POF_OBJECTFIELD);
        objectArrayField = pofReader.readObjectArray(POF_OBJECTARRAYFIELD, new Object[0]);
        objectArrayOfTypeField = pofReader.readObjectArray(POF_OBJECTARRAYOFTYPEFIELD,  new Object[0]);
        rawDateField = pofReader.readRawDate(POF_RAWDATEFIELD);
        rawDateTimeField = pofReader.readRawDateTime(POF_RAWDATETIMEFIELD);
        rawDayTimeIntervalField = pofReader.readRawDayTimeInterval(POF_RAWDAYTIMEINTERVALFIELD);
        rawQuadField = pofReader.readRawQuad(POF_RAWQUADFIELD);
        rawTimeField = pofReader.readRawTime(POF_RAWTIMEFIELD);
        rawTimeIntervalField = pofReader.readRawTimeInterval(POF_RAWTIMEINTERVALFIELD);
        rawYearMonthField = pofReader.readRawYearMonthInterval(POF_RAWYEARMONTHFIELD);
        shortField = pofReader.readShort(POF_SHORTFIELD);
        shortArrayField = pofReader.readShortArray(POF_SHORTARRAYFIELD);
        stringField = pofReader.readString(POF_STRINGFIELD);
        timeField = pofReader.readDate(POF_TIMEFIELD);
        d = pofReader.readDate(POF_TIMESTAMPTIMEFIELD);
        timestampTimeField = (d != null) ? new Timestamp(d.getTime()) : null;
        timeWithZoneField = pofReader.readDate(POF_TIMEWITHZONEFIELD);
        d = pofReader.readDate(POF_TIMESTAMPTIMEWITHZONEFIELD);
        timestampTimeWithZoneField = (d != null) ? new Timestamp(d.getTime()) : null;
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeBigDecimal(POF_BIGDECIMALFIELD, bigDecimalField);
        pofWriter.writeBigInteger(POF_BIGINTEGERFIELD, bigIntegerField);
        pofWriter.writeBinary(POF_BINARYFIELD, binaryField);
        pofWriter.writeBoolean(POF_BOOLEANFIELD, booleanField);
        pofWriter.writeBooleanArray(POF_BOOLEANARRAYFIELD, booleanArrayField);
        pofWriter.writeByte(POF_BYTEFIELD, byteField);
        pofWriter.writeByteArray(POF_BYTEARRAYFIELD, byteArrayField);
        pofWriter.writeChar(POF_CHARFIELD, charField);
        pofWriter.writeCharArray(POF_CHARARRAYFIELD, charArrayField);
        pofWriter.writeCollection(POF_COLLECTIONFIELD, collectionField);
        pofWriter.writeCollection(POF_COLLECTIONOFTYPEFIELD, collectionOfTypeField, collectionType);
        pofWriter.writeDate(POF_DATEFIELD, dateField);
        pofWriter.writeDateTime(POF_DATETIMEFIELD, dateTimeField);
        pofWriter.writeDateTime(POF_TIMESTAMPFIELD, timestampField);
        pofWriter.writeDateTimeWithZone(POF_DATETIMEWITHZONEFIELD, dateTimeWithZoneField);
        pofWriter.writeDateTimeWithZone(POF_TIMESTAMPWITHZONEFIELD, timestampWithZoneField);
        pofWriter.writeDouble(POF_DOUBLEFIELD, doubleField);
        pofWriter.writeDoubleArray(POF_DOUBLEARRAYFIELD, doubleArrayField);
        pofWriter.writeFloat(POF_FLOATFIELD, floatField);
        pofWriter.writeFloatArray(POF_FLOATARRAYFIELD, floatArrayField);
        pofWriter.writeInt(POF_INTFIELD, intField);
        pofWriter.writeIntArray(POF_INTARRAYFIELD, intArrayField);
        pofWriter.writeLong(POF_LONGFIELD, longField);
        pofWriter.writeLongArray(POF_LONGARRAYFIELD, longArrayField);
//        pofWriter.writeLongArray(POF_LONGARRAYOBJECTFIELD, longArrayObjectField);
//        pofWriter.writeLongArray(POF_LONGARRAYOFTYPEFIELD, longArrayOfTypeField, longArrayOfType);
        pofWriter.writeMap(POF_MAPFIELD, mapField);
        pofWriter.writeMap(POF_MAPOFKEYTYPEFIELD, mapOfKeyTypeField, mapOfKeyType);
        pofWriter.writeMap(POF_MAPOFKEYANDVALUETYPEFIELD, mapOfKeyAndValueTypeField, mapOfKeyAndValueKeyType, mapOfKeyAndValueType);
        pofWriter.writeObject(POF_OBJECTFIELD, objectField);
        pofWriter.writeObjectArray(POF_OBJECTARRAYFIELD, objectArrayField);
        pofWriter.writeObjectArray(POF_OBJECTARRAYOFTYPEFIELD, objectArrayOfTypeField, objectArrayType);
        pofWriter.writeRawDate(POF_RAWDATEFIELD, rawDateField);
        pofWriter.writeRawDateTime(POF_RAWDATETIMEFIELD, rawDateTimeField);
        pofWriter.writeRawDayTimeInterval(POF_RAWDAYTIMEINTERVALFIELD, rawDayTimeIntervalField);
        pofWriter.writeRawQuad(POF_RAWQUADFIELD, rawQuadField);
        pofWriter.writeRawTime(POF_RAWTIMEFIELD, rawTimeField);
        pofWriter.writeRawTimeInterval(POF_RAWTIMEINTERVALFIELD, rawTimeIntervalField);
        pofWriter.writeRawYearMonthInterval(POF_RAWYEARMONTHFIELD, rawYearMonthField);
        pofWriter.writeShort(POF_SHORTFIELD, shortField);
        pofWriter.writeShortArray(POF_SHORTARRAYFIELD, shortArrayField);
        pofWriter.writeString(POF_STRINGFIELD, stringField);
        pofWriter.writeTime(POF_TIMEFIELD, timeField);
        pofWriter.writeTime(POF_TIMESTAMPTIMEFIELD, timestampTimeField);
        pofWriter.writeTimeWithZone(POF_TIMEWITHZONEFIELD, timeWithZoneField);
        pofWriter.writeTimeWithZone(POF_TIMESTAMPTIMEWITHZONEFIELD, timestampTimeWithZoneField);
    }


}
