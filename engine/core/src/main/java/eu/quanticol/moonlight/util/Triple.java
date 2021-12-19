/**
 * 
 */
package eu.quanticol.moonlight.util;

import java.util.Objects;

/**
 * @author loreti
 *
 */
public class Triple<R, S, T> {

	private final R first;
	private final S second;
	private final T third;

	/**
	 * @param first
	 * @param second
	 * @param third
	 */
	public Triple(R first, S second, T third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * @return the first
	 */
	public R getFirst() {
		return first;
	}

	/**
	 * @return the second
	 */
	public S getSecond() {
		return second;
	}

	/**
	 * @return the third
	 */
	public T getThird() {
		return third;
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second, third);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Triple other = (Triple) obj;
		return Objects.equals(first, other.first) && Objects.equals(second, other.second)
				&& Objects.equals(third, other.third);
	}

	@Override
	public String toString() {
		return "<" + first + ", " + second + ", " + third + ">";
	}
	
	
	
}
