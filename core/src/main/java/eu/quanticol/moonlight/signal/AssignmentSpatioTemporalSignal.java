/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class AssignmentSpatioTemporalSignal extends SpatioTemporalSignal<Assignment> {

	public AssignmentSpatioTemporalSignal(int size, double[] t, Assignment[][] m) {
		super(size, t, m);
	}

	public AssignmentSpatioTemporalSignal(int size, double[] t, Function<Double, Assignment[]> f) {
		super(size, t, f);
	}

	public AssignmentSpatioTemporalSignal(int size, Function<Integer, Signal<Assignment>> f) {
		super(size, f);
	}

	public AssignmentSpatioTemporalSignal(int size) {
		super(size);
	}

}
