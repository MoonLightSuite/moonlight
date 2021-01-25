package eu.quanticol.moonlight.signal;

import java.util.*;
import java.util.function.Consumer;

/**
 * A segment chain is essentially a linked list of segments providing
 * some extra features specific for its usage.
 *
 * An invariant of the segments list is the fact that all segments
 * start at strictly monotonic increasing times.
 *
 * @param <T>
 */
public class SegmentChain<T>
        extends LinkedList<SegmentInterface<T>>
{

    @Override
    public DiffIterator<SegmentInterface<T>> listIterator() {
        return listIterator(0);
    }

    @Override
    public DiffIterator<SegmentInterface<T>> listIterator(int index) {
        // To implicitly call checkPositionIndex(index) we call parent's method
        SegmentChain.super.listIterator(index);
        return new DiffListItr(index);
    }

    private class DiffListItr implements DiffIterator<SegmentInterface<T>> {
        private final ArrayList<SegmentInterface<T>>  changes;
        private final ListIterator<SegmentInterface<T>> itr;

        public DiffListItr(int index) {
            changes = new ArrayList<>();
            itr = SegmentChain.super.listIterator(index);
        }

        @Override
        public List<SegmentInterface<T>> getChanges() {
            return changes;
        }

        // ---------------------------- MUTATORS ---------------------------- //
        @Override
        public void remove() {
            itr.remove();
            //TODO: should we track removals?
        }

        @Override
        public void set(SegmentInterface<T> e) {
            changes.add(e);
            itr.set(e);
        }

        @Override
        public void add(SegmentInterface<T> e) {
            changes.add(e);
            itr.add(e);
        }

        // ------------------------- END OF MUTATORS ------------------------ //

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return itr.hasPrevious();
        }

        @Override
        public int nextIndex() {
            return itr.nextIndex();
        }

        @Override
        public SegmentInterface<T> next() {
            return itr.next();
        }

        @Override
        public SegmentInterface<T> previous() {
            return itr.previous();
        }

        @Override
        public int previousIndex() {
            return itr.previousIndex();
        }
    }

}
