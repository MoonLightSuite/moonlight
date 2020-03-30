/**
 * 
 */
package eu.quanticol.moonlight.signal;

import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class AssignmentSpatialTemporalSignal extends SpatialTemporalSignal<Record> {

	public AssignmentSpatialTemporalSignal(int size, double[] t, Record[][] m) {
		super(size, t, m);
	}

	public AssignmentSpatialTemporalSignal(int size, double[] t, Function<Double, Record[]> f) {
		super(size, t, f);
	}

	public AssignmentSpatialTemporalSignal(int size, Function<Integer, Signal<Record>> f) {
		super(size, f);
	}

	public AssignmentSpatialTemporalSignal(int size) {
		super(size);
	}

}
