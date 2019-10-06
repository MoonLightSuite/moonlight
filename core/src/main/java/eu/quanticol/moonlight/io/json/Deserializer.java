package eu.quanticol.moonlight.io.json;

import eu.quanticol.moonlight.signal.VariableArraySignal;

public class Deserializer {

    public static final DeserializerFunction<VariableArraySignal> VARIABLE_ARRAY_SIGNAL = getVariableArraySignalDeserializer();


    private Deserializer() {
        //Utlity class
    }

    private  static DeserializerFunction<VariableArraySignal> getVariableArraySignalDeserializer() {
        JSONReader<VariableArraySignal> variableArraySignalJSONReader = new JSONReader<>(new VariableSignalDeserializer(), VariableArraySignal.class);
        return variableArraySignalJSONReader.getDeserializer();
    }
}