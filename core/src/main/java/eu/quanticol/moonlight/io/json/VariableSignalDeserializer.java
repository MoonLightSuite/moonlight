package eu.quanticol.moonlight.io.json;

import com.google.gson.*;
import eu.quanticol.moonlight.signal.AssignmentFactory;
import eu.quanticol.moonlight.signal.VariableArraySignal;

import java.lang.reflect.Type;

class VariableSignalDeserializer implements JsonDeserializer<VariableArraySignal> {
	
	private AssignmentFactory factory;
	
	public VariableSignalDeserializer( AssignmentFactory factory ) {
		this.factory = factory;
	}

    @Override
    public VariableArraySignal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject jso = json.getAsJsonObject();
        JsonArray timeArray = jso.get("t").getAsJsonArray();
        Double[] times = JSONUtils.toArray(timeArray, Double.class, new Double[timeArray.size()], context);
        JsonArray signals = jso.get("signals").getAsJsonArray();
        JSONUtils.checkVariables(factory, signals);
        String[] variables = JSONUtils.getVariables(signals);        
        VariableArraySignal result = new VariableArraySignal(factory);
        for (int i = 0; i < times.length; i++) {
            result.addFromString(times[i], JSONUtils.getAssignmentMap(variables, i, signals));
        }
        return result;
    }

}