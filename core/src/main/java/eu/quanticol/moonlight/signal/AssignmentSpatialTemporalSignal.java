/**
 * 
 */
package eu.quanticol.moonlight.signal;

import eu.quanticol.moonlight.space.MoonLightRecord;

import java.util.function.Function;

/**
 * @author loreti
 *
 */
public class AssignmentSpatialTemporalSignal extends SpatialTemporalSignal<MoonLightRecord> {

	public AssignmentSpatialTemporalSignal(int size, double[] t, MoonLightRecord[][] m) {
		super(size, t, m);
	}

	public AssignmentSpatialTemporalSignal(int size, double[] t, Function<Double, MoonLightRecord[]> f) {
		super(size, t, f);
	}

	public AssignmentSpatialTemporalSignal(int size, Function<Integer, Signal<MoonLightRecord>> f) {
		super(size, f);
	}

	public AssignmentSpatialTemporalSignal(int size) {
		super(size);
	}

}
