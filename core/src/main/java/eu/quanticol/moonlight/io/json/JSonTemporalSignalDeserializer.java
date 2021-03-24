/**
 * 
 */
package eu.quanticol.moonlight.io.json;

import java.util.stream.IntStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.quanticol.moonlight.signal.space.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;

/**
 * @author loreti
 *
 */
public class JSonTemporalSignalDeserializer {
	
	private final RecordHandler recordHandler;

	public JSonTemporalSignalDeserializer( RecordHandler recordHandler ) {
		this.recordHandler = recordHandler;
	}
	
	
	public Signal<MoonLightRecord> load(String str ) throws IllegalFileFormat {
		JsonParser parser = new JsonParser();
		JsonElement tree = parser.parse(str);
		return fromJSon( tree );
	}


	private Signal<MoonLightRecord> fromJSon(JsonElement tree) throws IllegalFileFormat {
		if (!tree.isJsonObject()) {
			throw new IllegalFileFormat("A JSON object is expected!");
		}
		JsonObject root = tree.getAsJsonObject();
		checkTraceType( root );
		double[] times = getTime(root);
		checkVariables( root );		
		return createSignal(times,root.get(JSONUtils.SIGNAL_TAG).getAsJsonArray());
	}


	private Signal<MoonLightRecord> createSignal(double[] times, JsonArray signals) {
		String[] variables = recordHandler.getVariables();
		Signal<MoonLightRecord> toReturn = new Signal<MoonLightRecord>();
		for( int i=0 ; i<times.length ; i++ ) {
			toReturn.add(times[i],recordHandler.fromStringArray( JSONUtils.getAssignmentMap(variables, i, signals)) );
		}
		return toReturn;
	}


	private double[] getTime(JsonObject root) throws IllegalFileFormat {
		if (!root.has(JSONUtils.TIME_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.TIME_TAG+" is missing!");						
		}
		if (!root.get(JSONUtils.TIME_TAG).isJsonArray()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.TIME_TAG+" should be an array!");
		}
		JsonArray timeArray = root.get(JSONUtils.TIME_TAG).getAsJsonArray();
		int size = timeArray.size();
		try {
			return IntStream.range(0, size).mapToDouble(i -> timeArray.get(i).getAsDouble()).toArray();			
		} catch (Exception e){
			throw new IllegalFileFormat("Tag "+JSONUtils.TIME_TAG+" should be an array of doubles!");
		}
	}


	private void checkVariables(JsonObject root) throws IllegalFileFormat {		
		if (!root.has(JSONUtils.SIGNAL_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.SIGNAL_TAG+" is missing!");			
		}
		if (!root.get(JSONUtils.SIGNAL_TAG).isJsonArray()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.SIGNAL_TAG+" is not array!");
		}
		JsonArray signal = root.get(JSONUtils.SIGNAL_TAG).getAsJsonArray();
		if (signal.size() != recordHandler.size()) {
			throw new IllegalFileFormat("Wrong number of variables in tag "+JSONUtils.SIGNAL_TAG+"!");
		}
		for( int i=0 ; i<signal.size() ; i++ ) {
			JsonElement e = signal.get(i);
			if (!e.isJsonObject()) {
				throw new IllegalFileFormat("Tag "+JSONUtils.SIGNAL_TAG+" should be an array of objects!");
			}
			JsonObject eo = e.getAsJsonObject();
			if (!eo.has(JSONUtils.NAME_TAG)) {
				throw new IllegalFileFormat("Tag "+JSONUtils.NAME_TAG+" is missing!");
			}
			if (!eo.has(JSONUtils.TYPE_TAG)) {
				throw new IllegalFileFormat("Tag "+JSONUtils.TYPE_TAG+" is missing!");				
			}
			String name = eo.get(JSONUtils.NAME_TAG).getAsString();
			String type = eo.get(JSONUtils.TYPE_TAG).getAsString();
			if (recordHandler.getVariableIndex(name)<0) {
				throw new IllegalFileFormat("Unknown variable "+name+"!");
			}
			if (!recordHandler.checkVariableType(name, type)) {
				throw new IllegalFileFormat("Wrong type for variable "+name+"! Expected "+recordHandler.getTypeCode(name)+" is "+ type+"!");
			}
		}
	}


	private void checkTraceType(JsonObject root) throws IllegalFileFormat {
		if (!root.has(JSONUtils.TRACE_TYPE_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.TRACE_TYPE_TAG+" is missing!");
		}
		String trace_type = root.get(JSONUtils.TRACE_TYPE_TAG).getAsString();
		if (!JSONUtils.TRACE_TYPE_TEMPORAL.equals(trace_type)) {
			throw new IllegalFileFormat("Wrong value for tag "+JSONUtils.TRACE_TYPE_TAG+": expected "+JSONUtils.TRACE_TYPE_TEMPORAL+" is "+trace_type);
		}
	}
	
	
}
