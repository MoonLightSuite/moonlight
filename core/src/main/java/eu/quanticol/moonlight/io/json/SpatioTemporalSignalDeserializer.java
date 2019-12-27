package eu.quanticol.moonlight.io.json;

import com.google.gson.*;
import eu.quanticol.moonlight.signal.Assignment;
import eu.quanticol.moonlight.signal.AssignmentFactory;
import eu.quanticol.moonlight.signal.AssignmentSpatioTemporalSignal;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpatioTemporalSignalDeserializer implements JsonDeserializer<AssignmentSpatioTemporalSignal> {
	
	private AssignmentFactory factory;
	
	public SpatioTemporalSignalDeserializer( AssignmentFactory factory ) {
		this.factory = factory;
	}

	
    @Override
    public AssignmentSpatioTemporalSignal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jso = json.getAsJsonObject();
        JsonObject signalType = jso.get("signal_type").getAsJsonObject();
        Function<Object[], Assignment> toAssignment = factory::fromObject;//getAssignment(getSignalsType(signalType));

        JsonObject edgesType = jso.get("edge_type").getAsJsonObject();
        JsonArray nodes = jso.get("nodes").getAsJsonArray();
        JsonArray trajectory = jso.get("trajectory").getAsJsonArray();
        AssignmentSpatioTemporalSignal spatioTemporalSignal = new AssignmentSpatioTemporalSignal(nodes.size());
        List<String> locations = IntStream.range(0, nodes.size()).mapToObj(nodes::get).map(JsonElement::getAsString).collect(Collectors.toList());
        ArrayList<String> variables = new ArrayList<>(signalType.keySet());
        for (int i = 0; i < trajectory.size(); i++) {
            JsonObject jsonElement = trajectory.get(i).getAsJsonObject();
            double time = jsonElement.get("time").getAsDouble();
            JsonObject signals = jsonElement.get("signals").getAsJsonObject();
            spatioTemporalSignal.add(time, l -> toAssignment.apply(getValues(signals.get(nodes.get(l).getAsString()).getAsJsonObject(), variables)));
        }
        return spatioTemporalSignal;
    }

    private Object[] getValues(JsonObject object, List<String> keys) {
        return keys.stream().map(object::get).toArray();
    }

//    private Function<Object[], Assignment> getAssignment(Class<?>[] varTypes) {
//        return object -> new Assignment(varTypes, object);
//    }

    private Class<?>[] getSignalsType(JsonObject jso) {
        return jso.keySet().stream().map(jso::get).map(s -> JSONUtils.getVariableType(s.getAsString())).toArray(Class<?>[]::new);
    }


}
