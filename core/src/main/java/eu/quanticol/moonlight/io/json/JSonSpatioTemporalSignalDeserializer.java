/**
 * 
 */
package eu.quanticol.moonlight.io.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.quanticol.moonlight.monitoring.SpatioTemporalMonitorinInput;
import eu.quanticol.moonlight.signal.GraphModel;
import eu.quanticol.moonlight.signal.LocationService;
import eu.quanticol.moonlight.signal.LocationServiceList;
import eu.quanticol.moonlight.signal.Record;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialModel;
import eu.quanticol.moonlight.signal.SpatioTemporalSignal;

/**
 * @author loreti
 *
 */
public class JSonSpatioTemporalSignalDeserializer {
	
	private final RecordHandler signalRecordHandler;
	private final RecordHandler edgeRecordHandler;

	public JSonSpatioTemporalSignalDeserializer( RecordHandler signalRecordHandler , RecordHandler edgeRecordHandler) {
		this.signalRecordHandler = signalRecordHandler;
		this.edgeRecordHandler = edgeRecordHandler;
	}
	
	
	public SpatioTemporalMonitorinInput<Record, Record> load( String str ) throws IllegalFileFormat {
		JsonParser parser = new JsonParser();
		JsonObject root = getRoot( parser.parse(str) );
		checkTypes( root );
		Map<String,Integer> locationIndex = getLocationIndex( root );
		SpatioTemporalSignal<Record> signal = loadSignal( locationIndex, root );
		LocationService<Record> locationService = loadLocationService( locationIndex, root );
		return new SpatioTemporalMonitorinInput<>(signal, locationService,null);
	}


	private Map<String, Integer> getLocationIndex(JsonObject root) throws IllegalFileFormat {
		if (!root.has(JSONUtils.NODES_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.NODES_TAG+" is missing!");			
		}
		if (!root.get(JSONUtils.NODES_TAG).isJsonArray()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.NODES_TAG+" should be an array!");			
		}
		JsonArray nodes = root.get(JSONUtils.NODES_TAG).getAsJsonArray();
		Map<String,Integer> toReturn = new HashMap<>();
		IntStream.range(0, nodes.size()).sequential().forEach(i -> toReturn.put(nodes.get(i).getAsString(), i));
		return toReturn;
	}


	private JsonObject getRoot(JsonElement tree) throws IllegalFileFormat {
		if (!tree.isJsonObject()) {
			throw new IllegalFileFormat("A JSON object is expected!");
		}
		JsonObject root = tree.getAsJsonObject();
		if (!root.has(JSONUtils.TRACE_TYPE_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.TRACE_TYPE_TAG+" is missing!");
		}
		String trace_type = root.get(JSONUtils.TRACE_TYPE_TAG).getAsString();
		if (!JSONUtils.TRACE_TYPE_SPATIO_TEMPORAL.equals(trace_type)) {
			throw new IllegalFileFormat("Wrong value for tag "+JSONUtils.TRACE_TYPE_TAG+": expected "+JSONUtils.TRACE_TYPE_SPATIO_TEMPORAL+" is "+trace_type);
		}		
		return root;
	}


	private void checkTypes(JsonObject root) throws IllegalFileFormat {
		Map<String,String> declaredSignalVariables = getMapOf(root,JSONUtils.SIGNAL_TYPE_TAG);
		checkType(declaredSignalVariables,signalRecordHandler,"signal");
		Map<String,String> declaredEdgeVariables = getMapOf(root,JSONUtils.EDGE_TYPE_TAG);		
		checkType(declaredEdgeVariables,signalRecordHandler,"edge");
	}

	private void checkType(Map<String, String> vMap, RecordHandler handler,
			String context) throws IllegalFileFormat {
		for (Entry<String, String> e : vMap.entrySet()) {
			if (handler.getVariableIndex(e.getKey())<0) {
				throw new IllegalFileFormat("Unknown "+context+" variable "+e.getKey()+"!");
			}
			if (!handler.checkVariableType(e.getKey(), e.getValue())) {
				throw new IllegalFileFormat("Wrong type for "+context+" variable "+e.getKey()+"! Expected "+handler.getTypeCode(e.getKey())+" is "+ e.getValue()+"!");				
			}
		}
	}


	private Map<String, String> getMapOf(JsonObject root, String tagName) throws IllegalFileFormat {
		if (!root.has(tagName)) {
			throw new IllegalFileFormat("Tag "+tagName+" is missing!");
		}
		if (!root.get(tagName).isJsonObject()) {
			throw new IllegalFileFormat("Tag "+tagName+" should be a JSON Object!");
		}
		JsonObject o = root.get(tagName).getAsJsonObject();
		Map<String,String> toReturn = new HashMap<>();
		o.entrySet().forEach(e -> toReturn.put(e.getKey(), e.getValue().getAsString()));
		return toReturn;
	}

	private LocationService<Record> loadLocationService(Map<String, Integer> locationIndex, JsonObject root) throws IllegalFileFormat {
		if (!root.has(JSONUtils.SPACE_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.SPACE_TAG+" is missing!");						
		}
		if (!root.get(JSONUtils.SPACE_TAG).isJsonArray()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.SPACE_TAG+" should be an array!");						
		}
		JsonArray spatialModels = root.get(JSONUtils.SPACE_TAG).getAsJsonArray();
		LocationServiceList<Record> toReturn = new LocationServiceList<>();
		for( int i=0 ; i< spatialModels.size() ; i++ ) {
			if (!spatialModels.get(i).isJsonObject()) {
				throw new IllegalFileFormat("An object is expected as element "+i+" of tag"+JSONUtils.SPACE_TAG+"!");						
			}
			JsonObject timeModel = spatialModels.get(i).getAsJsonObject();
			if (!timeModel.has(JSONUtils.TIME_TAG)) {
				throw new IllegalFileFormat("Tag "+JSONUtils.TIME_TAG+" at element "+i+" of array "+JSONUtils.SPACE_TAG+"!");						
			}
			toReturn.add(timeModel.get(JSONUtils.TIME_TAG).getAsDouble(), getModel(locationIndex,timeModel.get(JSONUtils.EDGES_TAG).getAsJsonArray()));
		}
		return toReturn;
	}


	private SpatialModel<Record> getModel(Map<String, Integer> locationIndex, JsonArray edges) throws IllegalFileFormat {
		GraphModel<Record> model = new GraphModel<>(locationIndex.size());
		for( int i=0 ; i<edges.size(); i++ ) {
			JsonObject edge = edges.get(i).getAsJsonObject();
			if ((!edge.has(JSONUtils.SRC_TAG))||(!edge.has(JSONUtils.TRG_TAG))||(!edge.has(JSONUtils.VALUES_TAG))) {
				throw new IllegalFileFormat("Missing mandatory tag  "+JSONUtils.TIME_TAG+" at element "+i+" of array "+JSONUtils.SPACE_TAG+"!");						
			}
			Integer src = locationIndex.get( edge.get(JSONUtils.SRC_TAG).getAsString() );
			Integer trg = locationIndex.get( edge.get(JSONUtils.TRG_TAG).getAsString() );
			model.add(src, getEdgeValues(edge.get(JSONUtils.VALUES_TAG)), trg);
		}
		return model;
	}


	private Record getEdgeValues(JsonElement jsonElement) throws IllegalFileFormat {
		if (!jsonElement.isJsonObject()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.VALUES_TAG+" in edge definitions should be an object!");
		}
		JsonObject edgeValues = jsonElement.getAsJsonObject();
		Map<String,String> valuesMap = new HashMap<>();
		for( String v: edgeRecordHandler.getVariables() ) {
			if (!edgeValues.has(v)) {
				throw new IllegalFileFormat("Definition of variable "+v+" is missing in edge definition!");			
			}
			String value = edgeValues.get(v).getAsString();
			if (edgeRecordHandler.checkValueFromString(v, value)) {
				throw new IllegalFileFormat("Wrong value "+value+"for "+v+"!");			
			}
			valuesMap.put(v, value);
		}
		return edgeRecordHandler.fromString(valuesMap);
	}


	private SpatioTemporalSignal<Record> loadSignal(Map<String, Integer> locationIndex, JsonObject root) throws IllegalFileFormat {				
		if (!root.has(JSONUtils.SIGNAL_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.SIGNAL_TAG+" is missing!");			
		}
		if (!root.get(JSONUtils.SIGNAL_TAG).isJsonObject()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.SIGNAL_TAG+" is not an object!");
		}
		JsonObject signals = root.get(JSONUtils.SIGNAL_TAG).getAsJsonObject();
		double[] time = getTime(signals);
		JsonObject data = getSignalData(locationIndex,signals);
		String[] locations = new String[locationIndex.size()];
		locationIndex.forEach((v,i) -> locations[i]=v);
		return new SpatioTemporalSignal<>(locations.length, i -> getSignal(time,locations[i],data.get(locations[i]).getAsJsonObject()));
	}

	private Signal<Record> getSignal(double[] time, String string, JsonObject locationData) {
		Signal<Record> record = new Signal<Record>();
		IntStream.range(0, time.length).sequential().forEach(i -> record.add(time[i], signalRecordHandler.fromString(getRecordStringMap(i,locationData,signalRecordHandler))));
		return record;
	}


	private Map<String,String> getRecordStringMap(int i, JsonObject data, RecordHandler handler) {
		Map<String,String> toReturn = new HashMap<>();
		for (String v : handler.getVariables()) {
			JsonElement e = data.get(v);
			if (e.isJsonArray()) {
				toReturn.put(v, e.getAsJsonArray().get(i).getAsString());
			} else {
				toReturn.put(v, e.getAsString());
			}
		}
		return toReturn;
	}


	private JsonObject getSignalData(Map<String, Integer> locationIndex, JsonObject signals) throws IllegalFileFormat {
		if (!signals.has(JSONUtils.VALUES_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.VALUES_TAG+" is missing!");									
		}
		if (!signals.get(JSONUtils.VALUES_TAG).isJsonObject()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.VALUES_TAG+" should be an object!");									
		}
		JsonObject data = signals.get(JSONUtils.VALUES_TAG).getAsJsonObject();
		for (String location : locationIndex.keySet()) {
			if (!data.has(location)) {
				throw new IllegalFileFormat("No signal data for location "+location+"!");									
			}
			if (!data.get(location).isJsonObject()) {
				throw new IllegalFileFormat("Wrong format for signal data of location "+location+"!");
			}
			JsonObject locationSignal = data.get(location).getAsJsonObject();
			for (String v : signalRecordHandler.getVariables()) {
				if (!locationSignal.has(v)) {
					throw new IllegalFileFormat("Missing data for variable "+v+" of location "+location+"!");
				}
				if (locationSignal.get(v).isJsonArray()) {
					JsonArray dataArray = locationSignal.get(v).getAsJsonArray();
					if (!IntStream.range(0, dataArray.size()).allMatch(i -> signalRecordHandler.checkValueFromString(v,dataArray.get(i).getAsString()))) {
						throw new IllegalFileFormat("Wrong value for variable "+v+" of location "+location+"!");
					}							
				} else {
					if (!signalRecordHandler.checkValueFromString(v, locationSignal.get(v).getAsString())) {
						throw new IllegalFileFormat("Wrong value for variable "+v+" of location "+location+"!");
					}
				}
			}
		}		
		return data;
	}


	private double[] getTime(JsonObject signals) throws IllegalFileFormat {
		if (!signals.has(JSONUtils.TIME_TAG)) {
			throw new IllegalFileFormat("Tag "+JSONUtils.TIME_TAG+" is missing!");						
		}
		if (!signals.get(JSONUtils.TIME_TAG).isJsonArray()) {
			throw new IllegalFileFormat("Tag "+JSONUtils.TIME_TAG+" should be an array!");
		}
		JsonArray timeArray = signals.get(JSONUtils.TIME_TAG).getAsJsonArray();
		int size = timeArray.size();
		try {
			return IntStream.range(0, size).mapToDouble(i -> timeArray.get(i).getAsDouble()).toArray();			
		} catch (Exception e){
			throw new IllegalFileFormat("Tag "+JSONUtils.TIME_TAG+" should be an array of doubles!");
		}
	}
	
}
