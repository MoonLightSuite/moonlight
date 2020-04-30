package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.examples.subway.data.HashBiMap;
import eu.quanticol.moonlight.examples.subway.grid.Grid;
import eu.quanticol.moonlight.examples.subway.grid.GridDirection;
import eu.quanticol.moonlight.examples.subway.io.DataReader;
import eu.quanticol.moonlight.examples.subway.io.FileType;
import eu.quanticol.moonlight.examples.subway.parsing.*;
import eu.quanticol.moonlight.formula.BooleanDomain;
import eu.quanticol.moonlight.formula.DoubleDistance;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
import static eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.*;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.signal.GraphModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * General idea of this scenario:
 *
 * Consider we are in a city and there is a grid-like network of phone cells.
 * We want to make sure that the Assisted Routing feature of these cells
 * satisfies the specification.
 * More precisely:
 *  - at some points in time a device makes a request at a specific cell,
 *      indicating the direction towards which is moving.
 *  - the cells provide one (or more?) identifier of the next cell where the
 *      device should connect.
 *  - the next cell should not be too crowded and should be located in a
 *      direction consistent with the one of the device.
 *
 * We want to enforce:
 *  - a safety condition that says that a suitable cell is suggested when
 *      receiving a request.
 *  - a liveness condition that says that eventually a request will happen and
 *      that, if this is the case, within a time boundary a reply is received.
 *
 *
 *  TODO: write the properties in symbols
 */
public class Erlang {

    /**
     * Source files location
     */
    private static final String TRAJECTORY_SOURCE = Erlang.class.getResource("trajectory.csv").getPath();
    private static final String NETWORK_SOURCE = Erlang.class.getResource("adj_matrix.txt").getPath();

    /**
     * Numeric constants of the problem
     */
    private static final int LH = 1;       // location horizon (neighbourhood)
    private static final double C = 5;     // crowdedness threshold
    private static final double TH = 10;    // properties time horizon
    private static final double T2 = 7;    // properties time horizon
    private static final double T3 = 1;    // output signal reaction time

    /**
     * We initialize the domains and the spatial network
     * @see Grid for a description of the spatial model.
     */
    private static final DoubleDomain ROBUSTNESS = new DoubleDomain();
    private static final BooleanDomain SATISFACTION = new BooleanDomain();
    private static final GraphModel<Double> network = new Grid().getModel(NETWORK_SOURCE);

    /**
     * Signal Dimensions (i.e. signal domain)
     */
    private static final TrajectoryExtractor singleTraj = new TrajectoryExtractor(network.size());
    private static final MultiTrajectoryExtractor multiTraj = new MultiTrajectoryExtractor(network.size());
    private static final Collection<HashBiMap<Integer, Integer, Double>> data =
            new DataReader<>(TRAJECTORY_SOURCE, FileType.CSV, multiTraj).read();
    private static final int timeSamples = multiTraj.getTimePoints();

    /**
     * Input Signals
     */
    private static final int DEV_CONNECTED = 0;
    private static final int DEV_DIRECTION = 1;

    /**
     * State Signals
     */
    private static final int LOC_ROUTER = 2;
    private static final int LOC_CROWDEDNESS = 3;

    /**
     * Output Signals
     */
    private static final int OUT_ROUTER = 4;


    public static void main(String[] argv) {
        List<? extends SpatialTemporalSignal> signalSet = toSignals(data);
        //SpatialTemporalSignal<List<Comparable>> signal = createSTSignal(network.size(), timeSamples, Erlang::getMultiValuedSignal);
        SpatialTemporalSignal<List<Comparable>> signal = signalSet.get(0);

        //// We are considering a dynamic Location Service ///
        LocationService<Double> locService = createOrientedLocSvc(sampleDevice().getFirst(), sampleDevice().getSecond());

        // Now we can monitor the system for the satisfaction of our properties
        SpatialTemporalMonitor<Double, List<Comparable>, Boolean> safety = neighbourSafety();
        SpatialTemporalSignal<Boolean> output = safety.monitor(locService, signal);
        /*StatisticalModelChecker<Double, List<Comparable>, Boolean> smc =
                new StatisticalModelChecker<Double, List<Comparable>, Boolean>(safety, data, locService);
        smc.run();*/
        List<Signal<Boolean>> signals = output.getSignals();

        System.out.println("The safety monitoring result is: " +
                    signals.get(0).valueAt(0));

        SpatialTemporalMonitor<Double, List<Comparable>, Boolean> liveness = communicationLiveness();
        output = liveness.monitor(locService, signal);
        signals = output.getSignals();


        System.out.println("The liveness monitoring result is: " +
                    signals.get(0).valueAt(0));
    }

    // --------- FORMULAE --------- //

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> neighbourSafety() {
        return globallyMonitor(   // Globally in TH...
                    impliesMonitor(request(), SATISFACTION, someCell())
                , new Interval(0, TH), SATISFACTION);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> communicationLiveness() {
        return eventuallyMonitor(   // Eventually in T2...
                impliesMonitor(request(), SATISFACTION,
                        eventuallyMonitor(response(), new Interval(0, T3), SATISFACTION))
                , new Interval(0, T2), SATISFACTION);
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> request() {
        return andMonitor(devConnected(), SATISFACTION, devMoving());
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> response() {
        return outputRouter(); //some quality metrics may be added here...
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> someCell() {
        return somewhereMonitor(
                    andMonitor(locationCrowdedness(), SATISFACTION, locationRouter())
                , distance(0, LH), SATISFACTION);
    }


    // --------- ATOMIC PREDICATES --------- //

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> devConnected() {
        return atomicMonitor((s -> (Boolean) s.get(DEV_CONNECTED)));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> devMoving() {
        return atomicMonitor((s -> s.get(DEV_DIRECTION) != GridDirection.HH));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> locationRouter() {
        return atomicMonitor((s -> (Integer) s.get(LOC_ROUTER) >= 0));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> locationCrowdedness() {
        return atomicMonitor((s -> (Double) s.get(LOC_CROWDEDNESS) < C));
    }

    private static SpatialTemporalMonitor<Double, List<Comparable>, Boolean> outputRouter() {
        return atomicMonitor((s -> (Integer) s.get(OUT_ROUTER) >= 0));
    }


    // ------------- HELPERS ------------- //


    private static List<MultiValuedSignal> toSignals(Collection<HashBiMap<Integer, Integer, Double>> input) {
        List<MultiValuedSignal> signals = new ArrayList<>();
        for(HashBiMap<Integer, Integer, Double> s : input) {
            MultiValuedSignal signal = new MultiValuedSignal(network.size(), timeSamples);



            HashBiMap<Integer, Integer, Boolean> devConn = new HashBiMap<>();
            HashBiMap<Integer, Integer, GridDirection> devDir = new HashBiMap<>();
            HashBiMap<Integer, Integer, Integer> routerLoc = new HashBiMap<>();
            HashBiMap<Integer, Integer, Integer> outRouter = new HashBiMap<>();
            for(int l = 0; l < network.size(); l++) {
                for(int t = 0; t < timeSamples; t++) {
                    devConn.put(l, t, isDevicePresent(l, t));
                    devDir.put(l, t, getDeviceDirection(l, t));
                    routerLoc.put(l, t, getRouterLocation(l));
                    outRouter.put(l, t, getOutputRouter(l, t));
                }
            }

            signal
            .setDimension(devConn, DEV_CONNECTED)   // Device Position
            .setDimension(devDir, DEV_DIRECTION)    // Device Direction
            .setDimension(routerLoc, LOC_ROUTER)    // Location Router ID
            .setDimension(s, LOC_CROWDEDNESS)       // Location Crowdedness
            .setDimension(outRouter, OUT_ROUTER)    // Output Router ID
            .initialize();

            signals.add(signal);
        }
        return signals;
    }

    /**
     * Given a list of (devPos, devDir), one for every time instant,
     * it updates the graph by addind orientation-specific edges.
     *
     * @param devPos list of positions of the device, at each time instant
     * @param devDir list of directions of the device, at each time instant
     * @return a dynamic location service for the grid spatial model
     */
    private static LocationService<Double>
    createOrientedLocSvc(List<Integer> devPos, List<GridDirection> devDir) {
        LocationServiceList<Double> locService = new LocationServiceList<>();

        // initial configuration
        List<Pair<Double, Integer>> edges = getEdges(devPos.get(0), devDir.get(0));
        for(Pair<Double, Integer> e: edges)
            network.add(devPos.get(0), e.getFirst(), e.getSecond());

        locService.add(devPos.get(0), network);

        //for each position at every time instant...
        for (int i = 1; i < devPos.size(); i++) {
            // remove previous edges
            for(Pair<Double, Integer> e: edges)
                network.remove(devPos.get(i - 1), e.getSecond());

            // fetch new neighbours
            edges = getEdges(devPos.get(i), devDir.get(i));

            // add new edges
            for(Pair<Double, Integer> e: edges)
                network.add(devPos.get(i), e.getFirst(), e.getSecond());

            locService.add(devPos.get(i), network);
        }

        return locService;
    }

    /**
     * Given a node, returns a set of edges in the given direction
     * @param node node identifier
     * @param dir direction of interest
     * @return a list of (weight, destination) relevant to the given direction
     */
    private static List<Pair<Double, Integer>> getEdges(Integer node, GridDirection dir) {
        List<Integer> ns = Grid.getNeighboursByDirection(node, dir, network.size());

        List<Pair<Double, Integer>> edges = new ArrayList<>();
        for(Integer n: ns) {
            edges.add(new Pair<>(1.0, n));
        }

        return edges;
    }

    /**
     * It calculates the proper distance, given a spatial model
     *
     * @param from double representing the starting position
     * @param to double representing the ending position
     * @return a DoubleDistance object, meaningful in the given Spatial Model
     */
    private static Function<SpatialModel<Double>, DistanceStructure<Double, ?>> distance(double from, double to) {
        return g -> new DistanceStructure<>(x -> x, new DoubleDistance(), from, to, g);
    }

    /*private <T extends Comparable<T>> T[][] toSTVector(BiFunction<Integer, Integer, T> f) {
        Class<T> type = new T();
        Array.newInstance(type, 10);
        List<List<T>> data = new ArrayList<>();
        for(int l = 0; l < network.size(); l++) {
            List<T> timeSeries = new ArrayList<>();
            for(int t = 0; t < timeSamples; t++) {
                timeSeries.add(f.apply(t, l));
            }
            data.add(timeSeries);
        }

        List<T>[] test = (List<T>[]) data.toArray();
        return o;
    }*/

    /**
     * Gathers the values to generate a multi-valued signal of arbitrary dimensions
     * @param t the time instant considered
     * @param l the location considered
     * @return a list representing the 5-tuple (devConnected, devDirection, locRouter, locCrowdedness, outRouter)
     */
    private static List<Comparable> getMultiValuedSignal(int t, int l) {
        List<Comparable> s = new ArrayList<>();
        s.add(DEV_CONNECTED, isDevicePresent(t, l));              //devConnected
        s.add(DEV_DIRECTION, getDeviceDirection(t, l));           //devDirection
        s.add(LOC_ROUTER, getRouterLocation(l));               //locRouter
        s.add(LOC_CROWDEDNESS, getTrajectoryValue(t, l));         //locCrowdedness
        s.add(OUT_ROUTER, getOutputRouter(t, l));                 //outRouter

        return s;
    }

    // ------------- SIGNAL EXTRACTORS ------------- ////

    private static Double getTrajectoryValue(int l, int t) {
        return ((double[][]) data.toArray()[0])[l][t];
    }

    private static Integer getRouterLocation(int l) {
        return l;
    }

    private static Boolean isDevicePresent(int l, int t) {
        return sampleDevice().getFirst().get(t) == l;
    }

    private static GridDirection getDeviceDirection(int l, int t) {
        return sampleDevice().getSecond().get(t);
    }

    /**
     * Method for crafting arbitrary device signals
     * @return a list of (devicePosition, deviceDirection)
     */
    private static Pair<List<Integer>, List<GridDirection>> sampleDevice() {
        List<Integer> positions = new ArrayList<>();
        List<GridDirection> directions = new ArrayList<>();

        // at every time instant i, the device is at position i
        // and is directed to South East.
        for(int t = 0; t < timeSamples; t++) {
            positions.add(t);
            directions.add(GridDirection.SE);
        }

        return new Pair<>(positions, directions);
    }

    //TODO: dynamize these methods...
    private static Integer getOutputRouter(int t, int l) {
        return 1;
    }

    // ------------- OTHERS ------------- ////
    // These two methods try to correlate the OUT and LOC signals,
    // might be useful in future...
    /*
    private static Boolean acceptableLocation(List<Comparable> s) {
        int l = (Integer) s.get(LOC_ROUTER);
        int n = (Integer) s.get(OUT_ROUTER);
        GridDirection d = (GridDirection) s.get(DEV_DIRECTION);

        return Grid.checkDirection(n,l,d,network.size());
    }

    private static Boolean sameRouter(List<Comparable> s) {
        Comparable s1 = s.get(OUT_ROUTER);
        Comparable s2 = s.get(LOC_ROUTER);

        return s1.equals(s2);
    }
     */
}