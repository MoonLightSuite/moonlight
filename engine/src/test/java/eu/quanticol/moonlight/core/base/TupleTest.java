package eu.quanticol.moonlight.core.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TupleTest {
    @Test
    void simpleTupleTest() {
        TupleType tupleType = TupleType.of(Integer.class, String.class);
        Tuple tuple = Tuple.of(tupleType, 1, "test");

        assertEquals(2, tupleType.size());
        assertEquals(2, tuple.size());
        assertEquals(Integer.class, tuple.getType().getIthType(0));
        assertEquals(String.class, tuple.getType().getIthType(1));
        assertEquals(1, (Integer) tuple.getIthValue(0));
        assertEquals("test", tuple.getIthValue(1));
    }

    @Test
    void emptyTupleTest() {
        final Tuple tempty = Tuple.of(TupleType.of());
        assertEquals(0, tempty.size());
    }

    @Test
    void wrongArityTest() {
        TupleType emptyTupleType = TupleType.of();
        assertThrows(IllegalArgumentException.class,
                        () -> Tuple.of(emptyTupleType, 1));
    }

    @Test
    void wrongTypesCreationTest() {
        TupleType tupleType = TupleType.of(Integer.class, Character.class);
        assertThrows(IllegalArgumentException.class,
                        () -> Tuple.of(tupleType, 'a', 1));
    }

    @Test
    void wrongTypesRetrievalTest() {
        TupleType tupleType = TupleType.of(Integer.class, Character.class);
        Tuple tuple = Tuple.of(tupleType, 1, 'a');
        assertThrows(ClassCastException.class, () -> {
            @SuppressWarnings("UnusedDeclaration")  // We are checking the
            String value = tuple.getIthValue(1); // static type here, so we
                                                    // needed a declaration
        });
    }

    @Test
    void tupleEquality() {
        TupleType tupleType = TupleType.of(Integer.class, Character.class);
        Tuple tuple1 = Tuple.of(tupleType, 1, 'a');
        Tuple tuple2 = Tuple.of(tupleType, 1, 'a');
        Tuple tuple3 = Tuple.of(tupleType, 2, 'a');
        assertEquals(tuple1, tuple2);
        assertNotEquals(tuple1, tuple3);
    }

    @Test
    void comparingTuples1() {
        TupleType tupleType = TupleType.of(Integer.class, Character.class);
        Tuple tuple1 = Tuple.of(tupleType, 1, 'a');
        Tuple tuple2 = Tuple.of(tupleType, 2, 'b');
        assertTrue(tuple1.compareTo(tuple2) < 0);
        assertTrue(tuple2.compareTo(tuple1) > 0);
    }

    @Test
    void comparingTuples2() {
        TupleType tupleType = TupleType.of(Integer.class, Character.class);
        Tuple tuple1 = Tuple.of(tupleType, 1, 'a');
        Tuple tuple2 = Tuple.of(tupleType, 2, 'a');
        assertFalse(tuple1.compareTo(tuple2) < 0);
        assertFalse(tuple2.compareTo(tuple1) > 0);
    }

}