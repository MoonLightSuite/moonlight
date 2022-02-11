package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.io.MoonLightRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataHandlerTest {

    @Test
    public void testDoubleFromObjectDouble() throws IllegalValueException {
        Double d = 2.0;
        assertEquals(2.0,DataHandler.REAL.fromObject(d));
    }

    @Test
    public void testDoubleFromObjectInteger() throws IllegalValueException {
        Integer d = 2;
        assertEquals(2.0,DataHandler.REAL.fromObject(d));
    }

    @Test
    public void testDoubleFromObjectString() throws IllegalValueException {
        String v = "fail!";
        assertThrows(IllegalValueException.class, () -> {DataHandler.REAL.fromObject(v);});
    }

    @Test
    public void testDoubleFromStringCorrect() throws IllegalValueException {
        Double d = 2.0;
        assertEquals(d, DataHandler.REAL.fromString(d.toString()));
    }

    @Test
    public void testDoubleFromStringFail() throws IllegalValueException {
        String v = "fail!";
        assertThrows(IllegalValueException.class, () -> {DataHandler.REAL.fromString(v);});
    }

    @Test
    public void testIntegerFromObjectInteger() throws IllegalValueException {
        Integer i = 3;
        assertEquals(i,DataHandler.INTEGER.fromObject(i));
    }

    @Test
    public void testIntegerFromObjectNumber() throws IllegalValueException {
        Number d = 2.0;
        assertEquals(2,DataHandler.INTEGER.fromObject(d));
    }

    @Test
    public void testIntegerFromObjectString() throws IllegalValueException {
        String v = "fail!";
        assertThrows(IllegalValueException.class, () -> {DataHandler.INTEGER.fromObject(v);});
    }

    @Test
    public void testIntegerFromStringCorrect() throws IllegalValueException {
        Integer i = 2;
        assertEquals(i, DataHandler.INTEGER.fromString(i.toString()));
    }

    @Test
    public void testIntegerFromStringFail() throws IllegalValueException {
        String v = "fail!";
        assertThrows(IllegalValueException.class, () -> {DataHandler.INTEGER.fromString(v);});
    }

    @Test
    public void testBooleanFromObject() {
        assertTrue(DataHandler.BOOLEAN.fromObject(true));
        assertFalse(DataHandler.BOOLEAN.fromObject(false));
    }

    @Test
    public void testBooleanFromObjectFail() {
        assertThrows(IllegalValueException.class,() -> {DataHandler.BOOLEAN.fromObject("fail!");});
    }

    @Test
    public void testBooleanFromStringTrue() {
        assertTrue(DataHandler.BOOLEAN.fromString("true"));
    }

    @Test
    public void testBooleanFromStringFalse() {
        assertFalse(DataHandler.BOOLEAN.fromString("false"));
    }

    @Test
    public void testBooleanFromDoubleTrue() {
        assertTrue(DataHandler.BOOLEAN.fromDouble(2.0));
    }

    @Test
    public void testBooleanFromDoubleFalse() {
        assertFalse(DataHandler.BOOLEAN.fromDouble(-2.0));
    }

    @Test
    public void testRecordEquals() {
        RecordHandler rh = new RecordHandler(DataHandler.REAL,DataHandler.INTEGER,DataHandler.BOOLEAN);
        MoonLightRecord r1 = rh.fromObjectArray(4.440193097192868,4,false);
        MoonLightRecord r2 = rh.fromObjectArray(4.440193097192868,4,false);
        assertEquals(r1,r2);
    }
}