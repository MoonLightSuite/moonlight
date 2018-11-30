/**
 * 
 */
package eu.quanticol.moonlight.signal;

/**
 * @author loreti
 *
 */
public class Segment<T> {
	
	private double time;
	private final T value;
	private Segment<T> next;
	private Segment<T> previous;
	
	public Segment( double time , T value ) {
		this.time = time;
		this.value = value;
		this.previous = null;
	}
	
	private Segment(Segment<T> previous, double time, T value) {
		this.previous = previous;
		this.time = time;
		this.value = value;
	}

	public double getTime() {
		return time;
	}
	
	public T getValue() {
		return value;
	}
	
	public Segment<T> getNext() {
		return next;
	}
	
	public Segment<T> getPrevious() {
		return previous;
	}
	
	public void setNext( Segment<T> next ) {
		this.next = next;
	}
	
	public T getValueAt( double t ) {
		Segment<T> selected = jump( t );
		return (selected==null?null:selected.value);
	}
	
	public Segment<T> jump( double t ) {
		if (t<time) {
			return backwardTo( this, t );
		} else {
			return forwardTo( this, t );
		}
	}
	
	public static <T> Segment<T> forwardTo(Segment<T> segment, double t) {
		Segment<T> cursor = segment;
		while (cursor != null) {
			if (cursor.contains(t)) {
				return cursor;
			} 
			cursor = cursor.next;
		}
		return null;	
	}

	public static <T> Segment<T> backwardTo(Segment<T> segment, double t) {
		Segment<T> cursor = segment;
		while (cursor != null) {
			if (cursor.contains(t)) {
				return cursor;
			} 
			cursor = cursor.previous;
		}
		return null;	
	}

	public boolean contains(double t) {
		return (time==t)||((time<t)&&(next!=null)&&(t<next.time));
	}

	public static double getTime(Segment<?> s) {
		return (s==null?Double.NaN:s.getTime());
	}

	public Segment<T> addAfter(double time, T value) {
		if (this.time>=time) {
			throw new IllegalArgumentException(); //TODO: Add error message!
		}
		if (!this.value.equals(value)) {
			this.next = new Segment<T>(this, time, value);
			return this.next;
		} else {
			return this;
		}
	}

	public Segment<T> addBefore(double time, T value) {
		if (this.time<=time) {
			throw new IllegalArgumentException(); //TODO: Add error message!
		}
		if (!this.value.equals(value)) {
			this.next = new Segment<T>(this, time, value);
			return this.next;
		} else {
			this.time = time;
			return this;
		}
	}

	public double getSegmentEnd() {
		if (next == null) {
			return Double.NaN;
		}
		return next.getTime();
	}

	public double getPreviousTime() {
		if (previous == null) {
			return Double.NaN;
		}
		return previous.getTime();
	}
	
	@Override
	public String toString() {
		return (previous!=null?"<":"[")+time+":"+value+(next!=null?">":"]");
	}
	
}
