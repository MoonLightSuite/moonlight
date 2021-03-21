package eu.quanticol.moonlight.examples.subway.data;


import eu.quanticol.moonlight.signal.SpatialTemporalSignal;

import java.util.ArrayList;
import java.util.List;

/**
 * Spatial-temporal signal that represents a generic signal of the kind
 * (s,t) -> (x1, ..., xn).
 * Note that x1, ..., xn must at least implement Comparable
 *
 * The goal of this class is to minimize the raw usage of the Comparable class,
 * so that user usage can be as type-safe as possible.
 *
 * Moreover, it performs some input checks to make sure the generated signal
 * is correct in terms of the spatial-temporal domain.
 *
 * Based on HashBiMap for the internal structure.
 *
 * @see Comparable
 * @see HashBiMap
 * @see SpatialTemporalSignal
 */
public class MultiValuedSignal
       extends SpatialTemporalSignal<List<Comparable<?>>>
{
    private final int length;
    private int dimensions = 0;
    private final List<Comparable<?>[][]> data;

    /**
     * Fixes the dimensions of the signal.
     *
     * @param size the number of locations of the Spatial model.
     * @param length the time span of the temporal data.
     */
    public MultiValuedSignal(int size, int length) {
        super(size);

        this.length = length;
        data = new ArrayList<>();
    }

    /**
     * Takes the data stored internally so far and performs the actual
     * conversion to Signals.
     * This is a required step before performing any kind of monitoring
     * or analysis over the signal.
     */
    public void initialize() {
        dimensions = data.size();

        if (data.isEmpty())
            throw new IllegalArgumentException("Empty signal passed");

        //System.out.println("Starting Signal Initialization");
        for(int t = 0; t < length; t ++) {
            int time = t;
            add(t, i -> setSignal(i, time));
        }
        //System.out.println("Completed Signal Initialization");

        data.clear();
    }

    /**
     * Given some Comparable data, it performs some checks and prepares it
     * to be later added as the index dimension of the n-dimensional signal.
     *
     * @see HashBiMap to learn more about the kind of data it processes.
     * @see Comparable to learn more about the minimum data requirements
     *
     * @param dimData data to be set as the provided dimension
     * @param index ith dimension of the n-dimensional signal.
     * @return the MultiValuedSignal itself, so that the method can be chained.
     */
    public MultiValuedSignal setDimension(
            Comparable<?>[][] dimData,
            int index) {

        if(!data.isEmpty() && dimData.length != size())
            throw new IllegalArgumentException("Mismatching space size ");

        if(!data.isEmpty() && dimData[0].length != length)
            throw new IllegalArgumentException("Mismatching time length");

        /*
        // We check if all required values exist
        for(int l = 0; l < size(); l++) {
            for(int t = 0; t < length; t++) {
                // When we find a missing value, we throw an exception!
                if(null == dimData[l][t])
                    throw new IllegalArgumentException(
                                                    "Missing time/space data");
            }
        }*/

        data.add(index, dimData);

        return this;
    }


    /**
     * It returns an n-dimensional list, for the given space-time element,
     * based on the dimensions data stored, s.t.
     * (t1, l1) |-> (x1, ..., xn)
     *
     * @param l spatial location of interest
     * @param t time instant of interest
     * @return (x1, ..., xn) n-dimensional data list
     */
    private List<Comparable<?>> setSignal(int l, int t) {
        List<Comparable<?>> signal = new ArrayList<>();

        for (Comparable<?>[][] datum : data) {
            signal.add(datum[l][t]);
        }

        return signal;
    }

    /**
     * @return the number of dimensions of the signal
     */
    public int dimensions() {
        return dimensions;
    }

}