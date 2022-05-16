package eu.quanticol.moonlight.offline.signal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static eu.quanticol.moonlight.offline.TestSignalUtils.basicCursorsWithOffset;
import static org.junit.jupiter.api.Assertions.*;

class ParallelSignalCursorTest {

    @Test
    void basicCursorsWithOffsetAreNotSynchronizedOnInit() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new ParallelSignalCursor<>(2, cursors::get);

        assertFalse(parallelCursor.areSynchronized());
    }

    @Test
    void basicCursorsWithOffsetCanSynchronize() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new ParallelSignalCursor<>(2, cursors::get);
        assertFalse(parallelCursor.areSynchronized());

        var startingTime = parallelCursor.syncCursors();

        assertTrue(parallelCursor.areSynchronized());
        assertEquals(2.0, startingTime);
    }

    @Test
    void requestingCursorAtInvalidLocationThrows() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new ParallelSignalCursor<>(2, cursors::get);
        var expectedError = IndexOutOfBoundsException.class;

        Executable failingAction = () -> parallelCursor.getCursorAtLocation(2);

        assertThrows(expectedError, failingAction);
    }

    @Test
    void requestingCursorAtValidLocation() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new ParallelSignalCursor<>(2, cursors::get);
        var cursor = parallelCursor.getCursorAtLocation(1);

        assertEquals(cursors.get(1), cursor);
    }


    @Test
    void getCurrentTime() {
    }

    @Test
    void move() {
    }

    @Test
    void forward() {
    }

    @Test
    void forwardTime() {
    }

    @Test
    void nextTime() {
    }

    @Test
    void backward() {
    }

    @Test
    void previousTime() {
    }

    @Test
    void revert() {
    }

    @Test
    void hasNext() {
    }

    @Test
    void hasPrevious() {
    }

    @Test
    void getCurrentValue() {
    }

    @Test
    void getCursorAtLocation() {
    }

    @Test
    void isCompleted() {
    }
}
