import eu.quanticol.moonlight.util.FormulaGenerator;
import tutorial_utilities.SStats;
import tutorial_utilities.SpatialTemporalSimHyA;
import eu.quanticol.jsstl.core.formula.*;
import eu.quanticol.jsstl.core.monitor.*;
import eu.quanticol.jsstl.core.space.GraphModel;
import eu.quanticol.jsstl.core.signal.BooleanSignal;

public class JSSTLBikes {
    /**
     * The files containing the data to run the experiment
     */
    private static final String SIMHYA_MODEL_FILE = "simulation/733bike.txt";
    private static final String GRAPH_FILE = "simulation/733stationsGraph.tra";

    /**
     * Experiment General Constants
     */
    private static final int SIMULATION_TIME = 50; // Timespan of the simulation
    private static final int SIMULATION_STEPS = 100;
    private static final int SPACE_LOCATIONS = 733;

    /**
     * Formula constants
     */
    private static final double T_end = 40; // Time horizon of the property
    private static final double d = 0.3;    // Max distance we want to consider

    public static void main(String[] args) throws Exception {
        String graphPath = JSSTLBikes.class.getResource(GRAPH_FILE).getPath();
        SpatialTemporalSimHyA model =
                new SpatialTemporalSimHyA(graphPath, SPACE_LOCATIONS);
        String modelPath = JSSTLBikes.class.getResource(SIMHYA_MODEL_FILE).getPath();
        model.loadModel(modelPath);

        GraphModel g = model.getGraphModel();

        Formula phi1 = loadProperty();

        Signal s = model.simulate(SIMULATION_TIME, SIMULATION_STEPS);

        // *************************** MONITORING *************************** //

        SStats<SpatialBooleanSignal> stats = new SStats<>();
        SpatialBooleanSignal b = stats.record(() ->
                phi1.booleanCheck(null, g, s));

        BooleanSignal bt = b.spatialBoleanSignal.get(g.getLocation(0));

        System.out.println("Boolean signal:" + bt);
        System.out.println("Satisfied: " + bt.getValueAt(0));
        System.out.println("Execution stats:" + stats.analyze());
    }

    public static Formula loadProperty() {

        // /// %%%%%% PROPERTY %%%%%%% /////////////////////////

        // /// VAR and SIGNAL
        final int VAR_B = 0;
        final int VAR_S = 1;

        ParametricExpression expression1 = parameters ->
                (SignalExpression) variables -> variables[VAR_B];

        ParametricExpression expression2 = parameters ->
                (SignalExpression) variables -> variables[VAR_S];

        // ///// ATOMIC PROP
        AtomicFormula atomB = new AtomicFormula(expression1, true);
        AtomicFormula atomS = new AtomicFormula(expression2, true);

        // /// PARAMETRIC INTERVAL for the somewhere
        ParametricInterval spaceInt1 = new ParametricInterval();
        spaceInt1.setLower(0);
        spaceInt1.setUpper(d);

        // ///// SOMEWHERE
        SomewhereFormula somewhereB = new SomewhereFormula(spaceInt1, atomB);
        SomewhereFormula somewhereS = new SomewhereFormula(spaceInt1, atomS);

        // /// AND
        AndFormula andBS = new AndFormula(somewhereB, somewhereS);

        // /// PARAMETRIC INTERVAL for the globally
        ParametricInterval metricInterval1 = new ParametricInterval();
        metricInterval1.setLower(0);
        metricInterval1.setUpper(T_end);

        // ///// GLOBALLY
        return new GloballyFormula(metricInterval1, andBS);
    }


}
