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
	private double end = Double.NaN;
	
	public Segment( double time , T value ) {
		this.time = time;
		this.value = value;
		this.previous = null;
	}
	
	private Segment(Segment<T> previous, double time, T value) {
		this.previous = previous;
		this.time = time;
		this.value = value;
		this.end = time;
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
		return (time==t)||((time<=t)&&((Double.isFinite(end)&&(t<=end))||(next!=null)&&(t<next.time)));
	}

	public static double getTime(Segment<?> s) {
		return (s==null?Double.NaN:s.getTime());
	}

	public Segment<T> addAfter(double time, T value) {
		if (this.time>=time) {
			throw new IllegalArgumentException("Time: "+time+" Expexted: >="+this.time); //TODO: Add error message!
		}
		if (!this.value.equals(value)) {
			this.next = new Segment<T>(this, time, value);
			this.end = Double.NaN;
			return this.next;
		} else {
			this.end = time;
			this.next = null;
			return this;
		}
	}

	public Segment<T> addBefore(double time, T value) {
		if (this.time<=time) {
			throw new IllegalArgumentException(); //TODO: Add error message!
		}
		if (!this.value.equals(value)) {
			this.previous = new Segment<T>(this, time, value);
			this.previous.next = this;
			return this.previous;
		} else {
			if (isAPoint()) {
				this.end = this.time;
			}
			this.time = time;
			return this;
		}
	}

	public double getSegmentEnd() {
		if (next == null) {
			if (Double.isNaN(this.end)) {
				return this.time;
			}
			return this.end;
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

	public void endAt(double end) {
		if (end<this.time) {
			throw new IllegalArgumentException(); //TODO: Add message!
		}
		this.end = end;
	}

	public Segment<T> splitAt(double time) {
		if (this.time>=time) {
			throw new IllegalArgumentException();
		}
		this.time = time;
		return this;
	}

	public boolean isTheEnd(double time) {
		return (this.end == time);
	}

	public boolean doEndAt(double t) {
		return (this.end==t);
	}

	public boolean isRightClosed() {
		return !Double.isNaN(this.end);
	}

	public double nextTimeAfter(double time) {
		if (this.next!=null) {
			return next.getTime();
		} else {
			if (time<end) {
				return end;
			} else {
				return Double.NaN;
			}
		}
	}

	public void isFirst() {
		this.previous = null;
	}

	public boolean isAPoint() {
		return (this.next==null)&&(Double.isNaN(this.end)||(this.time==this.end));
	}
	
}
