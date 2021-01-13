package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.util.MultiValuedTrace;
import eu.quanticol.moonlight.examples.subway.grid.GridDirection;
import eu.quanticol.moonlight.examples.subway.parsing.SignalProcessor;
import eu.quanticol.moonlight.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ErlangSignal implements SignalProcessor<Float> {
    /**
     * Input Signals
     */
    public static final int DEV_CONNECTED = 0;
    public static final int DEV_DIRECTION = 1;

    /**
     * State Signals
     */
    public static final int LOC_ROUTER = 2;
    public static final int LOC_CROWDEDNESS = 3;

    /**
     * Output Signals
     */
    public static final int OUT_ROUTER = 4;


    private int size;
    private int length;

    private Pair<List<Integer>, List<GridDirection>> sampleDevice;

    @Override
    public void initializeSpaceTime(int space, int time) {
        size = space;
        length = time;

        sampleDevice = sampleDevice();
    }

    /**
     * Gathers the values to generate a multi-valued signal of the dimensions
     * of interest from the input.
     *
     * @param input the data to be loaded
     * @return a list of signals representing the 5-tuple:
     * (devConnected, devDirection, locRouter, locCrowdedness, outRouter)
     */
    @Override
    public MultiValuedTrace generateSignal(Float[][] input) {
        MultiValuedTrace trace = new MultiValuedTrace(size, length);

        Boolean[][] devConn = new Boolean[size][length];
        GridDirection[][] devDir = new GridDirection[size][length];
        Integer[][] routerLoc = new Integer[size][length];
        Integer[][] outRouter = new Integer[size][length];

        for(int l = 0; l < size; l++) {
            for(int t = 0; t < length; t++) {
                devConn[l][t] = isDevicePresent(l, t);
                devDir[l][t] = getDeviceDirection(l, t);
                routerLoc[l][t] = getRouterLocation(l);
                outRouter[l][t] = getOutputRouter(l, t);
            }
        }

        trace
                .setDimension(devConn, DEV_CONNECTED)   // Device Position
                .setDimension(devDir, DEV_DIRECTION)    // Device Direction
                .setDimension(routerLoc, LOC_ROUTER)    // Location Router ID
                .setDimension(input, LOC_CROWDEDNESS)   // Location Crowdedness
                .setDimension(outRouter, OUT_ROUTER)    // Output Router ID
                .initialize();

        return trace;
    }

    // ------------- SIGNAL EXTRACTORS ------------- ////

    private Integer getRouterLocation(int l) {
        return l;
    }

    private Boolean isDevicePresent(int l, int t) {
        return sampleDevice.getFirst().get(t) == l;
    }

    private GridDirection getDeviceDirection(int l, int t) {
        return sampleDevice.getSecond().get(t);
    }

    /**
     * Method for crafting arbitrary device signals
     * @return a list of (devicePosition, deviceDirection)
     */
    private Pair<List<Integer>, List<GridDirection>> sampleDevice() {
        List<Integer> positions = new ArrayList<>();
        List<GridDirection> directions = new ArrayList<>();

        // at every time instant i, the device is at position i
        // and is directed to South East.
        for (int t = 0; t < length; t++) {
            if(t < size) {
                positions.add(t);
                directions.add(GridDirection.SE);
            } else {
                positions.add(size);
                directions.add(GridDirection.HH);
            }
        }

        return new Pair<>(positions, directions);
    }

    //TODO: dynamize these methods...
    private Integer getOutputRouter(int l, int t) {
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

    public Pair<List<Integer>, List<GridDirection>> getSampleDevice() {
        return sampleDevice;
    }
}
