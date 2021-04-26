package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.util.MultiValuedTrace;
import eu.quanticol.moonlight.io.parsing.SignalProcessor;

public class ErlangSignal implements SignalProcessor<Float> {
   /**
     * State Signals
     */
    public static final int LOC_CROWDEDNESS = 0;

    /**
     * Predictors Signals
     */
    public static final int CROWDEDNESS_1_STEP = 1;
    public static final int CROWDEDNESS_2_STEP = 2;
    public static final int CROWDEDNESS_3_STEP = 3;

    /**
     * POI Signals
     */
    public static final int IS_HOSPITAL = 4;


    private int size;
    private int length;
    private final int dimensions;
    private int currDim = 0;
    private MultiValuedTrace trace;

    Float[][][] data;

    public ErlangSignal(int inputs) {
        dimensions = inputs;
        data = new Float[4][][];
    }

    @Override
    public void initializeSpaceTime(int space, int time) {
        size = space;
        length = time;
        trace = new MultiValuedTrace(space, time);
    }

    /**
     * Factory method that generates signals.
     *
     * @param input the data used to generate the signal
     * @return a signal generated from the input data
     */
    @Override
    public MultiValuedTrace generateSignal(Float[][] input) {
        if(currDim < dimensions) {
            data[currDim] = input;
            currDim++;
        } else
            throw new UnsupportedOperationException("Exceeding max inputs");
        return trace;
    }

    /**
     * Gathers the values to generate a multi-valued signal of the dimensions
     * of interest from the input.
     *
     * @return a list of signals representing the 5-tuple:
     * (devConnected, devDirection, locRouter, locCrowdedness, outRouter)
     */
    public MultiValuedTrace generateSignal()
    {
        Boolean[][] isHospital = new Boolean[size][length];

        for(int l = 0; l < size; l++) {
            for(int t = 0; t < length; t++) {
                isHospital[l][t] = isHospital(l, t);
            }
        }

        for(int i = 0; i < data.length; i++){
            trace.setDimension(data[i], i);
        }

        trace.setDimension(isHospital, IS_HOSPITAL);   // POI ID boolean

        /*
        trace.setDimension(crowd, LOC_CROWDEDNESS)    // Location Crowdedness
             .setDimension(pred1, CROWDEDNESS_1_STEP) // 1 Step Predictor
             .setDimension(pred2, CROWDEDNESS_2_STEP) // 2 Step Predictor
             .setDimension(pred3, CROWDEDNESS_3_STEP) // 3 Step Predictor
             .setDimension(isHospital, IS_HOSPITAL)   // POI ID boolean
             .initialize();
        */
        trace.initialize();
        return trace;
    }

    // ------------- SIGNAL EXTRACTORS ------------- ////

    private Boolean isHospital(int l, int t) {
        // Starting from (0,0)
        // Hospitals: (4,10) | (12,8) | (10,17)
        return l == 4 + 10 * 21 || l == 12 + 8 * 21 || l == 10 + 17 * 21;
    }
}
