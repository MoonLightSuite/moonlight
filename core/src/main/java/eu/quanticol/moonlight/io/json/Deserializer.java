package eu.quanticol.moonlight.io.json;

import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.AssignmentFactory;
import eu.quanticol.moonlight.signal.AssignmentSpatioTemporalSignal;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.signal.VariableArraySignal;

public class Deserializer {

//    public static final DeserializerFunction<VariableArraySignal> VARIABLE_ARRAY_SIGNAL = getVariableArraySignalDeserializer();
//    public static final DeserializerFunction<SpatioTemporalSignal> SPATIO_TEMPORAL_SIGNAL = getSpatioTemporalSignalDeserializer();


    private Deserializer() {
        //Utlity class
    }

    public static DeserializerFunction<VariableArraySignal> getVariableArraySignalDeserializer(AssignmentFactory factory) {
        JSONReader<VariableArraySignal> variableArraySignalJSONReader = new JSONReader<>(new VariableSignalDeserializer(factory), VariableArraySignal.class);
        return variableArraySignalJSONReader.getDeserializer();
    }

    public static DeserializerFunction<AssignmentSpatioTemporalSignal> getSpatioTemporalSignalDeserializer(AssignmentFactory factory) {
        JSONReader<AssignmentSpatioTemporalSignal> variableArraySignalJSONReader = new JSONReader<AssignmentSpatioTemporalSignal>(new SpatioTemporalSignalDeserializer(factory), AssignmentSpatioTemporalSignal.class);
        return variableArraySignalJSONReader.getDeserializer();
    }
}