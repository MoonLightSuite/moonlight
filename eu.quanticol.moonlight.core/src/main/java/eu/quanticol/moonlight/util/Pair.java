/**
 * 
 */
package eu.quanticol.moonlight.util;

import java.util.Objects;

/**
 * @author loreti
 *
 */
public class Pair<T,R> {
	
	private final T first;
	
	private final R second;
	
	public Pair(T first, R second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * @return the second
	 */
	public R getSecond() {
		return second;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?,?> other = (Pair<?,?>) obj;
		return Objects.equals(first, other.first) && Objects.equals(second, other.second);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<" + first + " , " + second + ">";
	}
	
	

}
