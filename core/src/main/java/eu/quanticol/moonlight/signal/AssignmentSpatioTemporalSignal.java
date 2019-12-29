/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class AssignmentSpatioTemporalSignal extends SpatioTemporalSignal<Record> {

	public AssignmentSpatioTemporalSignal(int size, double[] t, Record[][] m) {
		super(size, t, m);
	}

	public AssignmentSpatioTemporalSignal(int size, double[] t, Function<Double, Record[]> f) {
		super(size, t, f);
	}

	public AssignmentSpatioTemporalSignal(int size, Function<Integer, Signal<Record>> f) {
		super(size, f);
	}

	public AssignmentSpatioTemporalSignal(int size) {
		super(size);
	}

}
