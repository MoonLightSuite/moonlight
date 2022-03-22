package eu.quanticol.moonlight.offline.signal;

public class OfflineSignalCursor<T> implements SignalCursor<Double, T> {

    private final boolean forward;
    private Segment<T> first;
    private Segment<T> last;
    private Segment<T> current ;
    private double time;
    private Segment<T> previous = null;

    public OfflineSignalCursor(boolean forward, Segment<T> first,
                               Segment<T> last)
    {
        this.forward = forward;
        this.first = first;
        this.last = last;
        current = setCurrent();
        time = setTime();
    }

    private double setTime() {
        if(current != null) {
            if(forward)
                return current.getStart();
            else
                return current.getSegmentEnd();
        }
        return Double.NaN;
    }

    private Segment<T> setCurrent() {
        return forward ? first : last;
    }

    @Override
    public Double getCurrentTime() {
        return time;
    }

    @Override
    public T getCurrentValue() {
        return (current != null ? current.getValue() : null);
    }

    @Override
    public void forward() {
        if (current != null) {
            previous = current;
            if ((!current.isRightClosed()) || (current.doEndAt(time))) {
                current = current.getNext();
                time = (current != null ? current.getStart() : Double.NaN);
            } else {
                time = current.getSegmentEnd();
            }
        }
    }

    @Override
    public void backward() {
        if (current != null) {
            previous = current;
            if (time>current.getStart()) {
                time = current.getStart();
            } else {
                current = current.getPrevious();
                time = (current != null ? current.getStart() : Double.NaN);
            }
        }
    }

    //TODO: this method is really a workaround, it's not needed to
    //      always store the previous segment
    @Override
    public void revert() {
        if(previous != null) {
            current = previous;
        } else
            throw new UnsupportedOperationException("Nothing to revert");
    }

    @Override
    public void move(Double t) {
        if (current != null) {
            current = current.jump(t);
            time = t;
        }
    }

    @Override
    public Double nextTime() {
        if (current != null) {
            return current.nextTimeAfter(time);
        }
        return Double.NaN;
    }

    @Override
    public Double previousTime() {
        if (current != null) {
            if (current.getStart() < time) {
                return current.getStart();
            } else {
                return current.getPreviousTime();
            }
        }
        return Double.NaN;
    }

    @Override
    public boolean hasNext() {
        return (current != null) && (current.getNext() != null);
    }

    @Override
    public boolean hasPrevious() {
        return (current != null) && (current.getPrevious() != null);
    }

    @Override
    public boolean isCompleted() {
        return (current == null);//||(current.isTheEnd(time)));
        //return ((current == null));//||(current.isTheEnd(time)));
    }

//    @Override
//    public String toString() {
//        return Signal.this.toString() + (current == null ? "!" : ("@(" + current.getStart() + ")"));
//    }

}
