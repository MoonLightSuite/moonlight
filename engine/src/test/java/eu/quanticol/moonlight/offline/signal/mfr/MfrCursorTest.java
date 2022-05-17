package eu.quanticol.moonlight.offline.signal.mfr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static eu.quanticol.moonlight.offline.TestSignalUtils.basicCursorsWithOffset;
import static org.junit.jupiter.api.Assertions.*;

class MfrCursorTest {
    private final int[] cursorLocations = new int[]{1, 2};

    @Test
    void basicCursorsWithOffsetAreNotSynchronizedOnInit() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new MfrCursor<>(cursorLocations, cursors::get);

        assertEquals(2, parallelCursor.getCursors().size());
        assertFalse(parallelCursor.areSynchronized());
    }

    @Test
    void basicCursorsWithOffsetCanSynchronize() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new MfrCursor<>(cursorLocations, cursors::get);
        assertFalse(parallelCursor.areSynchronized());

        parallelCursor.syncCursors();

        assertTrue(parallelCursor.areSynchronized());
    }

    @Test
    void requestingCursorAtInvalidLocationThrows() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new MfrCursor<>(cursorLocations, cursors::get);
        var expectedError = IllegalArgumentException.class;

        Executable failingAction = () -> parallelCursor.getCursorAtLocation(0);

        assertThrows(expectedError, failingAction);
    }

    @Test
    void requestingCursorAtValidLocationRetrievesTheRightOne() {
        var cursors = basicCursorsWithOffset();
        var parallelCursor = new MfrCursor<>(cursorLocations, cursors::get);
        var cursor = parallelCursor.getCursorAtLocation(1);

        assertEquals(cursors.get(1), cursor);
    }

}
