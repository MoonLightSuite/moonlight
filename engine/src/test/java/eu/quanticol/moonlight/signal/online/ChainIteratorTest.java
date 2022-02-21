package eu.quanticol.moonlight.signal.online;

import eu.quanticol.moonlight.online.signal.ChainIterator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ChainIteratorTest {

    @Test
    void peekNextBasicTest1() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList());
        assertEquals(0, itr.peekNext());
        assertTrue(itr.noEffects());
    }

    @Test
    void peekNextBasicTest2() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 3);
        assertThrows(NoSuchElementException.class, itr::peekNext);
        assertTrue(itr.noEffects());
    }

    @Test
    void peekPrevBasicTest1() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList());
        assertThrows(NoSuchElementException.class, itr::peekPrevious);
        assertTrue(itr.noEffects());
    }

    @Test
    void peekPrevBasicTest2() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 2);
        assertEquals(2, itr.peekNext());
        assertTrue(itr.noEffects());
    }

    @Test
    void peekPrevBoundaryTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList());
        assertEquals(-1, itr.tryPeekPrevious(-1));
        assertTrue(itr.noEffects());
    }

    @Test
    void peekNextBoundaryTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 3);
        assertEquals(-1, itr.tryPeekNext(-1));
        assertTrue(itr.noEffects());
    }

    @Test
    void prevCoherenceTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 1);

        Integer peekedPrev = itr.peekPrevious();
        Integer prev = itr.previous();

        assertEquals(prev, peekedPrev);
        assertTrue(itr.noEffects());
    }

    @Test
    void nextCoherenceTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 1);

        Integer peekedNext = itr.peekNext();
        Integer next = itr.next();

        assertEquals(next, peekedNext);
        assertTrue(itr.noEffects());
    }

    @Test
    void prevNotAlteringNextTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 1);

        itr.peekPrevious();
        Integer next = itr.next();

        assertEquals(1, next);
        assertTrue(itr.noEffects());
    }

    @Test
    void nextNotAlteringPrevTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 1);

        itr.peekNext();
        Integer prev = itr.previous();

        assertEquals(0, prev);
        assertTrue(itr.noEffects());
    }

    @Test
    void changesTest() {
        List<Integer> data = threeElemList();
        ChainIterator<Integer> itr = new ChainIterator<>(data);

        itr.next();
        itr.set(7);

        assertEquals(7, data.get(0).intValue());
        assertFalse(itr.noEffects());
    }

    @Test
    void prevIndexTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 1);
        int prev = itr.previousIndex();
        assertEquals(0, prev);
    }

    @Test
    void nextIndexTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(threeElemList(), 1);
        int next = itr.nextIndex();
        assertEquals(1, next);
    }

    @Test
    void hasNeighboursTest() {
        ChainIterator<Integer> itr = new ChainIterator<>(twoElemList());

        assertFalse(itr.hasPrevious());
        assertTrue(itr.hasNext());

        itr.next();
        itr.next();

        assertTrue(itr.hasPrevious());
        assertFalse(itr.hasNext());
    }

    private List<Integer> twoElemList() {
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(1);
        return list;
    }

    private List<Integer> threeElemList() {
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(1);
        list.add(2);
        return list;
    }

}