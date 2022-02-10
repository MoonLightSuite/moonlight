package eu.quanticol.moonlight.io.parsing;

import eu.quanticol.moonlight.util.MultiValuedTrace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiRawTrajectoryExtractorTest {

    private final String[] inputHeader = {"V0", "V1", "V2"};
    private SignalProcessor<Float> processor;


    @BeforeEach
    void init() {
        processor = new EmptySignalProcessor();
    }

    @Test
    void trivialInput() {
        MultiRawTrajectoryExtractor t = new MultiRawTrajectoryExtractor(1, processor);

        t.initialize(inputHeader);

        // We expect the time span to be equal to the array size passed
        assertEquals(3, t.getTimePoints());

        // We expect a result list of 0 elements
        assertNotNull(t.result());
        assertEquals(0, t.result().size());
    }

    @Disabled
    @Test
    void simpleGoodFileProcessing() {
        String[][] data_good = {{"1", "2", "3"}, {"4", "5", "6"}};

        MultiRawTrajectoryExtractor t = new MultiRawTrajectoryExtractor(2, processor);

        t.initialize(inputHeader);

        t.process(data_good[0]);
        t.process(data_good[1]);

        // We expect a result list of 1 element (the space has size 2)
        assertEquals(1,t.result().size());

        MultiValuedTrace data = t.result().iterator().next();

        // We expect a space size of 2
        assertEquals(2, data.size());

        // We expect a time span of 3
        assertEquals(3, data.getSignals().get(0).end());

        // We expect the element (0,0) to have value 1
        assertEquals((float) 1, data.getSignals().get(0).valueAt(0).get(0));
    }

    @Test
    void simpleBadFileProcessing_1() {
        String[][] data_bad_1 = {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"}
        };

        MultiRawTrajectoryExtractor t = new MultiRawTrajectoryExtractor(2, processor);

        t.initialize(inputHeader);

        t.process(data_bad_1[0]);
        t.process(data_bad_1[1]);
        t.process(data_bad_1[2]);

        // We are trying to get the results of incomplete input data
        assertThrows(IllegalArgumentException.class, t::result);
    }

    @Test
    void simpleBadFileProcessing_2() {
        String[][] data_bad_2 = {
                {"1", "2", "3"},
                {"4", "5"}
        };

        MultiRawTrajectoryExtractor t = new MultiRawTrajectoryExtractor(1, processor);

        t.initialize(inputHeader);

        t.process(data_bad_2[0]);

        // We are trying to get the results of incomplete input data
        assertThrows(ArrayIndexOutOfBoundsException.class,
                        () -> t.process(data_bad_2[1])
                    );
    }


    static class EmptySignalProcessor implements SignalProcessor<Float> {
        private int space;
        private int time;

        @Override
        public void initializeSpaceTime(int space, int time) {
            this.space = space;
            this.time = time;
        }

        @Override
        public MultiValuedTrace generateSignal(Float[][] data) {
            MultiValuedTrace signal  = new MultiValuedTrace(space, time);
            signal.setDimension(data, 0).initialize();

            return signal;
        }
    }
}