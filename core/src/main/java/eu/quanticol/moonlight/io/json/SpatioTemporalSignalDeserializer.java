package eu.quanticol.moonlight.io.json;

import com.google.gson.*;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpatioTemporalSignalDeserializer implements JsonDeserializer<SpatioTemporalSignal> {
    @Override
    public SpatioTemporalSignal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jso = json.getAsJsonObject();
        JsonObject signalType = jso.get("signal_type").getAsJsonObject();
        JsonObject edgesType = jso.get("edge_type").getAsJsonObject();
        JsonArray nodes = jso.get("nodes").getAsJsonArray();
        JsonArray trajectory = jso.get("trajectory").getAsJsonArray();
        SpatioTemporalSignal<JsonPrimitive[]> spatioTemporalSignal = new SpatioTemporalSignal<>(nodes.size());
        List<String> locations = IntStream.range(0, nodes.size()).mapToObj(nodes::get).map(JsonElement::getAsString).collect(Collectors.toList());
        ArrayList<String> variables = new ArrayList<>(signalType.keySet());
        //trajectory.get(i).getAsJsonObject().get("signals").getAsJsonObject().get("l1").getAsJsonObject()
        for (int i = 0; i < trajectory.size(); i++) {
            JsonObject jsonElement = trajectory.get(i).getAsJsonObject();
            double time = jsonElement.get("time").getAsDouble();
            JsonObject signals = jsonElement.get("signals").getAsJsonObject();
            spatioTemporalSignal.add(time, l -> getValues(signals.get(nodes.get(l).getAsString()).getAsJsonObject(),variables));
            //spatioTemporalSignal.add(time, (i -> signals);
        }
        return spatioTemporalSignal;

//        JsonArray timeArray = jsonElement.get("t").getAsJsonArray();
//        Double[] times = JSONUtils.toArray(timeArray, Double.class, new Double[timeArray.size()], context);
//        JsonArray signals = jso.get("signals").getAsJsonArray();
//        String[] variables = JSONUtils.getVariables(signals);
//        for (int i = 0; i < ; i++) {
//
//        }
//        double time = start;
//        while (time < end) {
//            double current = time;
//            s.add(time, (i -> f.apply(current, i)));
//            time += dt;
//        }
//        s.add(end, (i -> f.apply(end, i)));


//        Class<?>[] varTypes = getSignalsType(signals);
//        VariableArraySignal result = new VariableArraySignal(variables, new AssignmentFactory(varTypes));
//        Object[][] values = JSONUtils.getValues(times, variables, varTypes, signals, context);
//        for (int i = 0; i < times.length; i++) {
//            result.add(times[i], values[i]);
//        }

    }

    private JsonPrimitive[] getValues(JsonObject object, List<String> keys) {
        return keys.stream().map(object::get).map(JsonElement::getAsJsonPrimitive).toArray(JsonPrimitive[]::new);
    }
}
