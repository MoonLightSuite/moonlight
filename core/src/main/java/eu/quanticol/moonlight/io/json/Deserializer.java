package eu.quanticol.moonlight.io.json;

import eu.quanticol.moonlight.signal.SpatioTemporalSignal;
import eu.quanticol.moonlight.signal.VariableArraySignal;

public class Deserializer {

    public static final DeserializerFunction<VariableArraySignal> VARIABLE_ARRAY_SIGNAL = getVariableArraySignalDeserializer();
    public static final DeserializerFunction<SpatioTemporalSignal> SPATIO_TEMPORAL_SIGNAL = getSpatioTemporalSignalDeserializer();


    private Deserializer() {
        //Utlity class
    }

    private static DeserializerFunction<VariableArraySignal> getVariableArraySignalDeserializer() {
        JSONReader<VariableArraySignal> variableArraySignalJSONReader = new JSONReader<>(new VariableSignalDeserializer(), VariableArraySignal.class);
        return variableArraySignalJSONReader.getDeserializer();
    }

    private static DeserializerFunction<SpatioTemporalSignal> getSpatioTemporalSignalDeserializer() {
        JSONReader<SpatioTemporalSignal> variableArraySignalJSONReader = new JSONReader<>(new SpatioTemporalSignalDeserializer(), SpatioTemporalSignal.class);
        return variableArraySignalJSONReader.getDeserializer();
    }
}