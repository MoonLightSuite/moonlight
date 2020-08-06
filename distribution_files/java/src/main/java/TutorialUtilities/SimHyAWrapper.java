package TutorialUtilities;

import simhya.dataprocessing.DataCollector;
import simhya.dataprocessing.HybridDataCollector;
import simhya.dataprocessing.OdeDataCollector;
import simhya.dataprocessing.StochasticDataCollector;
import simhya.model.flat.FlatModel;
import simhya.model.flat.parser.FlatParser;
import simhya.model.flat.parser.ParseException;
import simhya.simengine.HybridSimulator;
import simhya.simengine.Simulator;
import simhya.simengine.SimulatorFactory;
import simhya.simengine.ode.OdeSimulator;
import simhya.simengine.utils.InactiveProgressMonitor;

/**
 * Wrapper class for loading SimHyA models easily. (the original
 * matlab.SimHyAModel class is unpredictable for some reason)
 */
public class SimHyAWrapper {

    private FlatModel flatModel;
    private Simulator simulator;
    private DataCollector collector;

    public void loadModel(String modelFile) {
        FlatParser parser = new FlatParser();
        try {
            flatModel = parser.parseFromFile(modelFile);
            setSSA();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Model parsing failed.");
        }
    }

    public FlatModel getFlatModel() {
        return flatModel;
    }

    /**
     * @param tfinal
     *            The simulation end time
     * @param timepoints
     *            The number of timepoints returned (excluding the first one)
     * @return A 2-D array whose first row is the times for which the state is
     *         recorded. The rest of the rows correspond to the species
     *         involved.
     *
     */
    public double[][] simulate(double tfinal, int timepoints) {
        simulator.setInitialTime(0);
        simulator.setFinalTime(tfinal);

        collector.clearAll();
        collector.storeWholeTrajectoryData(1);
        collector.setPrintConditionByTime(timepoints, tfinal);
        simulator.initialize();
        // breaks otherwise
        if (!(simulator instanceof OdeSimulator)
                && !(simulator instanceof HybridSimulator))
            collector.newTrajectory();
        simulator.resetModel(true);
        simulator.reinitialize();
        simulator.run();

        return collector.getTrajectory(0).getAllData();
    }

    public void setSSA() {
        collector = new StochasticDataCollector(flatModel);
        collector.saveAllVariables();
        simulator = SimulatorFactory.newSSAsimulator(flatModel, collector);
        simulator.setProgressMonitor(new InactiveProgressMonitor());
    }

    public void setGB() {
        collector = new StochasticDataCollector(flatModel);
        collector.saveAllVariables();
        simulator = SimulatorFactory.newGBsimulator(flatModel, collector);
        simulator.setProgressMonitor(new InactiveProgressMonitor());
    }

    public void setHybrid() {
        collector = new HybridDataCollector(flatModel);
        collector.saveAllVariables();
        simulator = SimulatorFactory.newHybridSimulator(flatModel, collector);
        simulator.setProgressMonitor(new InactiveProgressMonitor());
    }

    public void setODE() {
        collector = new OdeDataCollector(flatModel);
        collector.saveAllVariables();
        simulator = SimulatorFactory.newODEsimulator(flatModel, collector);
        simulator.setProgressMonitor(new InactiveProgressMonitor());
    }

}

