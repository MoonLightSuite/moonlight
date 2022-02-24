package eu.quanticol.moonlight.core.space;

import java.util.function.Function;

/**
 *
 * @param <E> Type of edge labels of the spatial model.
 * @param <M> Type of the distance metric
 */
public interface DistanceStructure<E, M> {

    /**
     * @return the spatial model on which the distance structure is defined.
     */
    SpatialModel<E> getModel();

    /**
     * @return the distance function used to compute the distance.
     */
    Function<E, M> getDistanceFunction();

    /**
     * @return the distance domain used to compute the distance.
     */
    DistanceDomain<M> getDistanceDomain();

    /**
     * Method to retrieve the distance between the two locations.
     * The operation can be commutative or not, depending on the implementation.
     *
     * @param from source form which the computation starts
     * @param to destination to which to look for computing the distance
     * @return the aggregated distance to reach <code>to</code>
     *         starting at <code>from</code>.
     */
    M getDistance(int from, int to);

    /**
     * Helper method, might be preferable to the combination of
     * <code>isWithinBounds(getDistance(from, to))</code>,
     * if it makes sense for the current distance structure.
     *
     * @param from source form which the computation starts
     * @param to destination to which to look for the analysis
     * @return <code>true</code> when <code>to</code> can be reached.
     *         <code>false</code> otherwise
     */
    boolean canReach(int from, int to);

    /**
     * Method to assess whether one location is within the bounds of the
     * spatial structure
     * @param d distance
     * @return <code>true</code> when <code>d</code> is within bounds.
     *         <code>false</code> otherwise
     */
    boolean isWithinBounds(M d);
}
