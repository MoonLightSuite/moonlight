package eu.quanticol.moonlight.io.json;

import com.google.gson.*;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

import java.lang.reflect.Type;

public class SpatioTemporalSignalDeserializer implements JsonDeserializer<SpatioTemporalSignal> {
    @Override
    public SpatioTemporalSignal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jso = json.getAsJsonObject();
        JsonObject signalType = jso.get("signal_type").getAsJsonObject();
        JsonObject edgesType = jso.get("edge_type").getAsJsonObject();
        JsonArray nodes = jso.get("nodes").getAsJsonArray();
        JsonArray trajectory = jso.get("trajectory").getAsJsonArray();

        JsonObject jsonElement = trajectory.get(0).getAsJsonObject();
//        JsonArray timeArray = jsonElement.get("t").getAsJsonArray();
//        Double[] times = JSONUtils.toArray(timeArray, Double.class, new Double[timeArray.size()], context);
//        JsonArray signals = jso.get("signals").getAsJsonArray();
//        String[] variables = JSONUtils.getVariables(signals);

//        Class<?>[] varTypes = getSignalsType(signals);
//        VariableArraySignal result = new VariableArraySignal(variables, new AssignmentFactory(varTypes));
//        Object[][] values = JSONUtils.getValues(times, variables, varTypes, signals, context);
//        for (int i = 0; i < times.length; i++) {
//            result.add(times[i], values[i]);
//        }
        return new SpatioTemporalSignal(nodes.size());
    }
}
