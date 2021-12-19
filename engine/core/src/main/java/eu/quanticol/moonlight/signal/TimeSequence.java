/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author loreti
 *
 */
public class TimeSequence {

	protected final LinkedList<Double> steps;
	
	public TimeSequence() {
		this( new LinkedList<>() );
	}
	
	private TimeSequence(LinkedList<Double> steps) {
		this.steps = steps;
	}



	public void add( double t ) {
		if (!steps.isEmpty()&&(steps.peekLast()>= t)) {
			throw new IllegalArgumentException("A value greater than "+steps.peekLast()+" is expected! While "+t+" is used.");
		}
		this.steps.add(t);
	}
	
	public void addBefore( double t ) {
		if (!steps.isEmpty()&&(steps.peekFirst()<= t)) {
			throw new IllegalArgumentException("A value less than "+steps.peekLast()+" is expected! While "+t+" is used.");
		}
		this.steps.add(t);
	}
	
	public static TimeSequence merge(TimeSequence steps1, TimeSequence steps2) {
		return new TimeSequenceMerger(steps1 , steps2 ).merge();
	}

	


	public boolean isEmpty() {
		return steps.isEmpty();
	}
	
	
	private static class TimeSequenceMerger {

		private final Iterator<Double> iterator1;
		private final Iterator<Double> iterator2;
		private final TimeSequence result;
		private double time1 = Double.NaN;
		private double time2 = Double.NaN;

		public TimeSequenceMerger(TimeSequence sequence1, TimeSequence sequence2) {
			this.iterator1 = sequence1.steps.iterator();
			this.iterator2 = sequence2.steps.iterator();
			this.result = new TimeSequence();
			stepIterator1();
			stepIterator2();
		}

		private void stepIterator1() {
			this.time1 = (iterator1.hasNext()?iterator1.next():Double.NaN);
		}

		private void stepIterator2() {
			this.time2 = (iterator2.hasNext()?iterator2.next():Double.NaN);
		}

		public TimeSequence merge() {
			while( !Double.isNaN(time1)&&!Double.isNaN(time2)) {
				int code = mergeStep();
				if ((code == 0)||(code == 1)) {
					stepIterator1();
				} 
				if ((code == 0)||(code == 2)) {
					stepIterator2();
				}
			}
			if (!Double.isNaN(time1)) {
				addAll( time1, iterator1 );
			}
			if (!Double.isNaN(time2)) {
				addAll( time2, iterator2 );
			}
			return result;
		}

		private void addAll(double time, Iterator<Double> iterator) {
			result.add(time);
			while (iterator.hasNext()) {
				result.add(iterator.next());
			}
		}

		private int mergeStep() {
			if (time1==time2) {
				result.add(time1);
				return 0;
			} 
			if (time1<time2) {
				result.add(time1);
				return 1;
			} else {
				result.add(time2);
				return 2;
			}
		}
		
		
		
		
	}
	
}
