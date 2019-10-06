package eu.quanticol.moonlight.io.json;

import com.google.gson.*;
import eu.quanticol.moonlight.signal.AssignmentFactory;
import eu.quanticol.moonlight.signal.VariableArraySignal;

import java.lang.reflect.Type;

class VariableSignalDeserializer implements JsonDeserializer<VariableArraySignal> {

    @Override
    public VariableArraySignal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jso = json.getAsJsonObject();
        JsonArray timeArray = jso.get("t").getAsJsonArray();
        Double[] times = JSONUtils.toArray(timeArray, Double.class, new Double[timeArray.size()], context);
        JsonArray signals = jso.get("signals").getAsJsonArray();
        String[] variables = JSONUtils.getVariables(signals);
        Class<?>[] varTypes = getSignalsType(signals);
        VariableArraySignal result = new VariableArraySignal(variables, new AssignmentFactory(varTypes));
        Object[][] values = JSONUtils.getValues(times, variables, varTypes, signals, context);
        for (int i = 0; i < times.length; i++) {
            result.add(times[i], values[i]);
        }
        return result;
    }

    private Class<?>[] getSignalsType(JsonArray jsa) {
        Class<?>[] types = new Class<?>[jsa.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = JSONUtils.getVariableType(jsa.get(i).getAsJsonObject().get("type").getAsString());
        }
        return types;
    }
}